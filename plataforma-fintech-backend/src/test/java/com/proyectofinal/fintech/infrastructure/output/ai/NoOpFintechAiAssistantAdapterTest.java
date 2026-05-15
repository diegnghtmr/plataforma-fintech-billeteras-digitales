package com.proyectofinal.fintech.infrastructure.output.ai;

import com.proyectofinal.fintech.application.exception.ai.AiUnavailableException;
import com.proyectofinal.fintech.application.result.ai.*;
import com.proyectofinal.fintech.domain.structures.MiLista;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * T05 — NoOpFintechAiAssistantAdapter tests.
 * All three port methods must throw AiUnavailableException with reason PROVIDER_DISABLED.
 */
class NoOpFintechAiAssistantAdapterTest {

    private final NoOpFintechAiAssistantAdapter adapter = new NoOpFintechAiAssistantAdapter();

    @Test
    void answer_throwsAiUnavailableException_withProviderDisabledReason() {
        FintechAiContext ctx = buildContext();
        AiUnavailableException ex = assertThrows(AiUnavailableException.class,
                () -> adapter.answer(ctx, "test message"));
        assertEquals(AiUnavailableException.Reason.PROVIDER_DISABLED, ex.getReason());
    }

    @Test
    void explainFraud_throwsAiUnavailableException_withProviderDisabledReason() {
        FraudInsightContext ctx = new FraudInsightContext(
                "fe-1", "u1", "LARGE_TRANSACTION", "HIGH", "desc",
                java.time.Instant.now());
        AiUnavailableException ex = assertThrows(AiUnavailableException.class,
                () -> adapter.explainFraud(ctx));
        assertEquals(AiUnavailableException.Reason.PROVIDER_DISABLED, ex.getReason());
    }

    @Test
    void draftAction_throwsAiUnavailableException_withProviderDisabledReason() {
        FintechAiContext ctx = buildContext();
        AiUnavailableException ex = assertThrows(AiUnavailableException.class,
                () -> adapter.draftAction(ctx, "transfer money"));
        assertEquals(AiUnavailableException.Reason.PROVIDER_DISABLED, ex.getReason());
    }

    private FintechAiContext buildContext() {
        MiLista<String> empty = new MiLista<>();
        return new FintechAiContext(
                "USER",
                AiScope.USER,
                "last 7 days",
                new AnalyticsSnapshot(empty, empty, empty, 0),
                null,
                new FraudSnapshot(0, empty),
                new ScheduledOperationsSnapshot(0, empty),
                new NotificationSnapshot(0, empty),
                new DataQualityNotes("ok"),
                "hash123"
        );
    }
}
