package com.proyectofinal.fintech.application.usecase.ai;

import com.proyectofinal.fintech.application.exception.ai.ForbiddenActorException;
import com.proyectofinal.fintech.application.port.in.ai.AskFintechAiUseCase.AskFintechAiCommand;
import com.proyectofinal.fintech.application.port.out.ai.AiAuditPort;
import com.proyectofinal.fintech.application.port.out.ai.FintechAiAssistantPort;
import com.proyectofinal.fintech.application.result.ai.*;
import com.proyectofinal.fintech.domain.structures.MiLista;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * T30/T31 — AskFintechAiService full pipeline tests.
 * T30: happy path pipeline with mocked port returning valid answer.
 * T31: cache hit — port is called only once even with two identical requests.
 * BuildFintechAiContextService is mocked to return a stub context.
 */

@ExtendWith(MockitoExtension.class)
class AskFintechAiServiceTest {

    @Mock
    private FintechAiAssistantPort port;

    @Mock
    private AiAuditPort auditPort;

    @Mock
    private BuildFintechAiContextService contextBuilder;

    private AskFintechAiService service;
    private AiResponseCache cache;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2026-01-01T12:00:00Z"), ZoneId.of("UTC"));
        cache = new AiResponseCache(Duration.ofMinutes(10), clock);

        // Stub context builder to return a consistent stub context
        // Using lenient() because some tests throw before reaching contextBuilder
        MiLista<String> empty = new MiLista<>();
        FintechAiContext stubCtx = new FintechAiContext(
                "USER", AiScope.USER, "last 7 days",
                new AnalyticsSnapshot(empty, empty, empty, 0),
                null,
                new FraudSnapshot(0, empty),
                new ScheduledOperationsSnapshot(0, empty),
                new NotificationSnapshot(0, empty),
                new DataQualityNotes("test"),
                "stub-hash");
        lenient().when(contextBuilder.build(any())).thenReturn(stubCtx);

        service = new AskFintechAiService(
                new AuthorizationService(),
                new IntentClassifier(),
                cache,
                port,
                new AiIntentValidator(),
                new AiObservability(),
                auditPort,
                contextBuilder,
                1000
        );
    }

    // ── T30: happy path pipeline ──────────────────────────────────────────────

    @Test
    void ask_validCommand_returnsMockedPortAnswer() {
        AiChatAnswer stubAnswer = buildAnswer(AiIntent.EXPLAIN_BALANCE_CHANGE, "conv1");
        when(port.answer(any(), any())).thenReturn(stubAnswer);

        AskFintechAiCommand cmd = new AskFintechAiCommand(
                "u1", "USER", AiScope.USER, "conv1",
                "mi saldo cambió ayer", "last 7 days");

        AiChatAnswer result = service.ask(cmd);

        assertNotNull(result);
        assertEquals("Your balance changed.", result.answer());
        assertEquals(AiIntent.EXPLAIN_BALANCE_CHANGE, result.intent());
        verify(port, times(1)).answer(any(), eq("mi saldo cambió ayer"));
    }

    @Test
    void ask_adminWithAnalyticsMessage_returnsPortAnswer() {
        AiChatAnswer stubAnswer = buildAnswer(AiIntent.EXPLAIN_ANALYTICS, "conv2");
        when(port.answer(any(), any())).thenReturn(stubAnswer);

        AskFintechAiCommand cmd = new AskFintechAiCommand(
                "admin1", "ADMIN", AiScope.ADMIN, "conv2",
                "analytics de las metricas", "last 30 days");

        AiChatAnswer result = service.ask(cmd);

        assertNotNull(result);
        assertEquals(AiIntent.EXPLAIN_ANALYTICS, result.intent());
        verify(port, times(1)).answer(any(), any());
    }

    @Test
    void ask_userWithAdminScope_throwsForbiddenActorException() {
        AskFintechAiCommand cmd = new AskFintechAiCommand(
                "u1", "USER", AiScope.ADMIN, "conv3",
                "analytics platform", "last 7 days");

        assertThrows(ForbiddenActorException.class, () -> service.ask(cmd));
        verify(port, never()).answer(any(), any());
    }

    // ── T31: cache hit — port called only once ─────────────────────────────────

    @Test
    void ask_cachHit_portCalledOnlyOnce() {
        AiChatAnswer stubAnswer = buildAnswer(AiIntent.EXPLAIN_BALANCE_CHANGE, "conv1");
        when(port.answer(any(), any())).thenReturn(stubAnswer);

        AskFintechAiCommand cmd = new AskFintechAiCommand(
                "u1", "USER", AiScope.USER, "conv1",
                "saldo cambió ayer", "last 7 days");

        // First call — hits port
        AiChatAnswer first = service.ask(cmd);
        // Second identical call — must be served from cache
        AiChatAnswer second = service.ask(cmd);

        assertNotNull(first);
        assertNotNull(second);
        assertEquals(first.answer(), second.answer());
        // Port MUST have been called exactly once (cache hit on second)
        verify(port, times(1)).answer(any(), any());
    }

    @Test
    void ask_differentConversationIds_portCalledTwice() {
        AiChatAnswer stubAnswer = buildAnswer(AiIntent.EXPLAIN_BALANCE_CHANGE, "conv1");
        when(port.answer(any(), any())).thenReturn(stubAnswer);

        AskFintechAiCommand cmd1 = new AskFintechAiCommand(
                "u1", "USER", AiScope.USER, "conv-A",
                "saldo cambió", "last 7 days");
        AskFintechAiCommand cmd2 = new AskFintechAiCommand(
                "u1", "USER", AiScope.USER, "conv-B",
                "saldo cambió", "last 7 days");

        service.ask(cmd1);
        service.ask(cmd2);

        // Different conversationId → different cache key → port called twice
        verify(port, times(2)).answer(any(), any());
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private AiChatAnswer buildAnswer(AiIntent intent, String conversationId) {
        MiLista<String> metrics = new MiLista<>();
        metrics.add("WALLET_SNAPSHOT");
        MiLista<AiSuggestedAction> actions = new MiLista<>();
        return new AiChatAnswer(
                "Your balance changed.", intent, 0.9,
                metrics, actions, false, conversationId, "USER_CONTEXT");
    }
}
