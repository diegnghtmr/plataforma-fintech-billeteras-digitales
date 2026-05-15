package com.proyectofinal.fintech.infrastructure.output.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyectofinal.fintech.application.exception.ai.AiUnavailableException;
import com.proyectofinal.fintech.application.result.ai.*;
import com.proyectofinal.fintech.domain.structures.MiLista;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

/**
 * T27/T28/T29 — RestClientFintechAssistantAdapter tests.
 * T27: adapter is constructable, server stubs answer.
 * T28: happy path — parses LLM JSON response into AiChatAnswer.
 * T29: HTTP 429 → UPSTREAM_RATE_LIMIT; malformed JSON → UPSTREAM_PARSE.
 */
class RestClientFintechAssistantAdapterTest {

    private MockRestServiceServer server;
    private RestClientFintechAssistantAdapter adapter;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper()
                .findAndRegisterModules(); // enables JavaTimeModule
        RestClient.Builder builder = RestClient.builder()
                .baseUrl("https://openrouter.ai/api/v1");
        server = MockRestServiceServer.bindTo(builder).build();
        RestClient restClient = builder.build();
        PromptResources prompts = new PromptResources();
        adapter = new RestClientFintechAssistantAdapter(restClient, "test-model", objectMapper, prompts);
    }

    // ── T27: adapter is wired up ──────────────────────────────────────────────

    @Test
    void constructor_doesNotThrow() {
        assertNotNull(adapter);
    }

    // ── T28: happy path — parses JSON response ─────────────────────────────────

    @Test
    void answer_happyPath_returnsAiChatAnswer() throws Exception {
        String wireJson = """
                {
                  "answer": "Your balance decreased by 500 due to transfer.",
                  "intent": "EXPLAIN_BALANCE_CHANGE",
                  "confidence": 0.95,
                  "usedMetrics": ["WALLET_SNAPSHOT"],
                  "suggestedActions": [
                    {"type": "EXPLAIN_BALANCE_CHANGE", "label": "Show details"}
                  ],
                  "requiresConfirmation": false,
                  "conversationId": "conv1",
                  "usedContext": "USER_CONTEXT"
                }
                """;

        String openRouterResponse = buildOpenRouterResponse(wireJson);

        server.expect(requestTo("https://openrouter.ai/api/v1/chat/completions"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(openRouterResponse, MediaType.APPLICATION_JSON));

        FintechAiContext ctx = buildContext("u1", AiScope.USER);
        AiChatAnswer answer = adapter.answer(ctx, "what happened to my balance?");

        assertNotNull(answer);
        assertEquals("Your balance decreased by 500 due to transfer.", answer.answer());
        assertEquals(AiIntent.EXPLAIN_BALANCE_CHANGE, answer.intent());
        assertEquals(0.95, answer.confidence(), 0.001);
        assertFalse(answer.usedMetrics().isEmpty());
        assertFalse(answer.suggestedActions().isEmpty());
    }

    @Test
    void explainFraud_happyPath_returnsAiFraudExplanation() throws Exception {
        String wireJson = """
                {
                  "fraudEventId": "fe-1",
                  "summary": "A large transaction was flagged.",
                  "severityExplanation": "HIGH severity because amount exceeded threshold.",
                  "evidence": ["Amount 10000", "Unusual time"],
                  "recommendation": "Contact your bank immediately."
                }
                """;

        String openRouterResponse = buildOpenRouterResponse(wireJson);

        server.expect(requestTo("https://openrouter.ai/api/v1/chat/completions"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(openRouterResponse, MediaType.APPLICATION_JSON));

        FraudInsightContext ctx = new FraudInsightContext(
                "fe-1", "u1", "LARGE_TRANSACTION", "HIGH", "suspicious tx", Instant.now());

        AiFraudExplanation explanation = adapter.explainFraud(ctx);

        assertNotNull(explanation);
        assertEquals("fe-1", explanation.fraudEventId());
        assertEquals("A large transaction was flagged.", explanation.summary());
        assertFalse(explanation.evidence().isEmpty());
    }

    @Test
    void draftAction_happyPath_returnsAiActionDraft() throws Exception {
        String wireJson = """
                {
                  "intent": "DRAFT_TRANSFER",
                  "requiresConfirmation": true,
                  "draft": {"targetUserId": "u2", "amount": "100"},
                  "missingFields": ["sourceWalletId"],
                  "explanation": "I'll help you set up a transfer."
                }
                """;

        String openRouterResponse = buildOpenRouterResponse(wireJson);

        server.expect(requestTo("https://openrouter.ai/api/v1/chat/completions"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(openRouterResponse, MediaType.APPLICATION_JSON));

        FintechAiContext ctx = buildContext("u1", AiScope.USER);
        AiActionDraft draft = adapter.draftAction(ctx, "transfer 100 to u2");

        assertNotNull(draft);
        assertEquals(AiIntent.DRAFT_TRANSFER, draft.intent());
        assertTrue(draft.requiresConfirmation());
        assertFalse(draft.draft().isEmpty());
    }

    // ── T29a: HTTP 429 → UPSTREAM_RATE_LIMIT ─────────────────────────────────

    @Test
    void answer_http429_throwsUpstreamRateLimit() {
        server.expect(requestTo("https://openrouter.ai/api/v1/chat/completions"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.TOO_MANY_REQUESTS)
                        .body("{\"error\":\"rate limited\"}")
                        .contentType(MediaType.APPLICATION_JSON));

        FintechAiContext ctx = buildContext("u1", AiScope.USER);
        AiUnavailableException ex = assertThrows(AiUnavailableException.class,
                () -> adapter.answer(ctx, "test"));
        assertEquals(AiUnavailableException.Reason.UPSTREAM_RATE_LIMIT, ex.getReason());
    }

    // ── T29b: malformed JSON → UPSTREAM_PARSE ────────────────────────────────

    @Test
    void answer_malformedJson_throwsUpstreamParse() {
        String openRouterResponse = buildOpenRouterResponse("NOT_VALID_JSON{{{{");

        server.expect(requestTo("https://openrouter.ai/api/v1/chat/completions"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withSuccess(openRouterResponse, MediaType.APPLICATION_JSON));

        FintechAiContext ctx = buildContext("u1", AiScope.USER);
        AiUnavailableException ex = assertThrows(AiUnavailableException.class,
                () -> adapter.answer(ctx, "test"));
        assertEquals(AiUnavailableException.Reason.UPSTREAM_PARSE, ex.getReason());
    }

    // ── T29c: HTTP 401 → UPSTREAM_AUTH ───────────────────────────────────────

    @Test
    void answer_http401_throwsUpstreamAuth() {
        server.expect(requestTo("https://openrouter.ai/api/v1/chat/completions"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withStatus(HttpStatus.UNAUTHORIZED)
                        .body("{\"error\":\"unauthorized\"}")
                        .contentType(MediaType.APPLICATION_JSON));

        FintechAiContext ctx = buildContext("u1", AiScope.USER);
        AiUnavailableException ex = assertThrows(AiUnavailableException.class,
                () -> adapter.answer(ctx, "test"));
        assertEquals(AiUnavailableException.Reason.UPSTREAM_AUTH, ex.getReason());
    }

    // ── T29d: HTTP 500 → UPSTREAM_5XX ────────────────────────────────────────

    @Test
    void answer_http500_throwsUpstream5xx() {
        server.expect(requestTo("https://openrouter.ai/api/v1/chat/completions"))
                .andExpect(method(HttpMethod.POST))
                .andRespond(withServerError()
                        .body("{\"error\":\"internal server error\"}")
                        .contentType(MediaType.APPLICATION_JSON));

        FintechAiContext ctx = buildContext("u1", AiScope.USER);
        AiUnavailableException ex = assertThrows(AiUnavailableException.class,
                () -> adapter.answer(ctx, "test"));
        assertEquals(AiUnavailableException.Reason.UPSTREAM_5XX, ex.getReason());
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private FintechAiContext buildContext(String userId, AiScope scope) {
        MiLista<String> empty = new MiLista<>();
        return new FintechAiContext(
                "USER", scope, "last 7 days",
                new AnalyticsSnapshot(empty, empty, empty, 0),
                null,
                new FraudSnapshot(0, empty),
                new ScheduledOperationsSnapshot(0, empty),
                new NotificationSnapshot(0, empty),
                new DataQualityNotes("ok"),
                "testhash");
    }

    private String buildOpenRouterResponse(String contentJson) {
        // Escape the content for embedding in outer JSON string
        String escaped = contentJson
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
        return """
                {
                  "id": "gen-1",
                  "model": "test-model",
                  "choices": [
                    {
                      "message": {
                        "role": "assistant",
                        "content": "%s"
                      },
                      "finish_reason": "stop"
                    }
                  ],
                  "usage": {"prompt_tokens": 100, "completion_tokens": 50}
                }
                """.formatted(escaped);
    }
}
