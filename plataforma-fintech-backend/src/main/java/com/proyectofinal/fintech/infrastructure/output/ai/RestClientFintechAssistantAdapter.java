package com.proyectofinal.fintech.infrastructure.output.ai;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyectofinal.fintech.application.exception.ai.AiUnavailableException;
import com.proyectofinal.fintech.application.port.out.ai.FintechAiAssistantPort;
import com.proyectofinal.fintech.application.result.ai.*;
import com.proyectofinal.fintech.domain.structures.MiLista;
import com.proyectofinal.fintech.domain.structures.TablaHash;
import com.proyectofinal.fintech.infrastructure.output.ai.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Real OpenRouter HTTP adapter implementing FintechAiAssistantPort.
 * Uses Spring's RestClient (no RestTemplate) and Jackson ObjectMapper via AiChatAnswerWire
 * to keep the application result types (AiChatAnswer etc.) Jackson-free.
 * <p>
 * Error mapping:
 * - HTTP 401/403 → UPSTREAM_AUTH
 * - HTTP 429     → UPSTREAM_RATE_LIMIT
 * - HTTP 5xx     → UPSTREAM_5XX
 * - Socket/timeout → UPSTREAM_TIMEOUT
 * - JSON parse error → UPSTREAM_PARSE
 */
public class RestClientFintechAssistantAdapter implements FintechAiAssistantPort {

    private static final Logger log = LoggerFactory.getLogger(RestClientFintechAssistantAdapter.class);
    private static final String CHAT_COMPLETIONS_PATH = "/chat/completions";
    private static final double TEMPERATURE = 0.2;

    private final RestClient restClient;
    private final String model;
    private final ObjectMapper objectMapper;
    private final PromptResources prompts;

    public RestClientFintechAssistantAdapter(
            RestClient restClient,
            String model,
            ObjectMapper objectMapper,
            PromptResources prompts) {
        this.restClient = Objects.requireNonNull(restClient);
        this.model = Objects.requireNonNull(model);
        this.objectMapper = Objects.requireNonNull(objectMapper);
        this.prompts = Objects.requireNonNull(prompts);
    }

    @Override
    public AiChatAnswer answer(FintechAiContext context, String userMessage) {
        String systemMsg = prompts.system("fintech-chat");
        String userMsg = prompts.userTemplate("fintech-chat")
                .replace("{{actor_role}}", safe(context.actorRole()))
                .replace("{{scope}}", safe(context.scope() != null ? context.scope().name() : null))
                .replace("{{range}}", safe(context.range()))
                .replace("{{context_json}}", serializeContext(context))
                .replace("{{user_message}}", safe(userMessage));

        String content = sendRequest(systemMsg, userMsg);
        try {
            AiChatAnswerWire wire = objectMapper.readValue(content, AiChatAnswerWire.class);
            return wireToAnswer(wire);
        } catch (JsonProcessingException e) {
            log.warn("[AI] Failed to parse chat answer JSON: {}", e.getMessage());
            throw new AiUnavailableException(AiUnavailableException.Reason.UPSTREAM_PARSE,
                    "LLM returned unparseable JSON for chat answer", e);
        }
    }

    @Override
    public AiFraudExplanation explainFraud(FraudInsightContext context) {
        String systemMsg = prompts.system("fintech-fraud");
        String userMsg = prompts.userTemplate("fintech-fraud")
                .replace("{{fraud_event_id}}", safe(context.fraudEventId()))
                .replace("{{user_id}}", safe(context.userId()))
                .replace("{{type}}", safe(context.type()))
                .replace("{{severity}}", safe(context.severity()))
                .replace("{{description}}", safe(context.description()))
                .replace("{{created_at}}", context.createdAt() != null ? context.createdAt().toString() : "unknown");

        String content = sendRequest(systemMsg, userMsg);
        try {
            AiFraudExplanationWire wire = objectMapper.readValue(content, AiFraudExplanationWire.class);
            return wireToFraudExplanation(wire, context.fraudEventId());
        } catch (JsonProcessingException e) {
            log.warn("[AI] Failed to parse fraud explanation JSON: {}", e.getMessage());
            throw new AiUnavailableException(AiUnavailableException.Reason.UPSTREAM_PARSE,
                    "LLM returned unparseable JSON for fraud explanation", e);
        }
    }

    @Override
    public AiActionDraft draftAction(FintechAiContext context, String userMessage) {
        String systemMsg = prompts.system("fintech-action");
        String userMsg = prompts.userTemplate("fintech-action")
                .replace("{{actor_role}}", safe(context.actorRole()))
                .replace("{{scope}}", safe(context.scope() != null ? context.scope().name() : null))
                .replace("{{context_json}}", serializeContext(context))
                .replace("{{user_message}}", safe(userMessage));

        String content = sendRequest(systemMsg, userMsg);
        try {
            AiActionDraftWire wire = objectMapper.readValue(content, AiActionDraftWire.class);
            return wireToActionDraft(wire);
        } catch (JsonProcessingException e) {
            log.warn("[AI] Failed to parse action draft JSON: {}", e.getMessage());
            throw new AiUnavailableException(AiUnavailableException.Reason.UPSTREAM_PARSE,
                    "LLM returned unparseable JSON for action draft", e);
        }
    }

    // ── HTTP call ─────────────────────────────────────────────────────────────

    private String sendRequest(String systemMessage, String userMessage) {
        OpenRouterChatRequest request = new OpenRouterChatRequest(
                model,
                List.of(
                        new OpenRouterChatRequest.Message("system", systemMessage),
                        new OpenRouterChatRequest.Message("user", userMessage)
                ),
                TEMPERATURE,
                Map.of("type", "json_object"));

        try {
            OpenRouterChatResponse response = restClient.post()
                    .uri(CHAT_COMPLETIONS_PATH)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(request)
                    .retrieve()
                    .onStatus(status -> status.value() == 401 || status.value() == 403,
                            (req, res) -> {
                                throw new AiUnavailableException(AiUnavailableException.Reason.UPSTREAM_AUTH,
                                        "OpenRouter authentication failed: HTTP " + res.getStatusCode().value());
                            })
                    .onStatus(status -> status.value() == 429,
                            (req, res) -> {
                                throw new AiUnavailableException(AiUnavailableException.Reason.UPSTREAM_RATE_LIMIT,
                                        "OpenRouter rate limit exceeded: HTTP 429");
                            })
                    .onStatus(HttpStatusCode::is5xxServerError,
                            (req, res) -> {
                                throw new AiUnavailableException(AiUnavailableException.Reason.UPSTREAM_5XX,
                                        "OpenRouter server error: HTTP " + res.getStatusCode().value());
                            })
                    .body(OpenRouterChatResponse.class);

            if (response == null || response.choices() == null || response.choices().isEmpty()) {
                throw new AiUnavailableException(AiUnavailableException.Reason.UPSTREAM_PARSE,
                        "OpenRouter returned empty choices list");
            }

            OpenRouterChatResponse.Message msg = response.choices().get(0).message();
            if (msg == null || msg.content() == null) {
                throw new AiUnavailableException(AiUnavailableException.Reason.UPSTREAM_PARSE,
                        "OpenRouter response choice has null message content");
            }

            return msg.content();

        } catch (AiUnavailableException e) {
            throw e; // pass through — already mapped
        } catch (ResourceAccessException e) {
            log.warn("[AI] Connection/timeout error: {}", e.getMessage());
            throw new AiUnavailableException(AiUnavailableException.Reason.UPSTREAM_TIMEOUT,
                    "OpenRouter request timed out or connection failed", e);
        } catch (RestClientResponseException e) {
            log.warn("[AI] Unexpected HTTP error: {} {}", e.getStatusCode(), e.getMessage());
            throw new AiUnavailableException(AiUnavailableException.Reason.UPSTREAM_5XX,
                    "OpenRouter unexpected HTTP error: " + e.getStatusCode(), e);
        }
    }

    // ── Wire → Application type mappers ──────────────────────────────────────

    private AiChatAnswer wireToAnswer(AiChatAnswerWire wire) {
        AiIntent intent = parseIntent(wire.intent());

        MiLista<String> metrics = new MiLista<>();
        if (wire.usedMetrics() != null) {
            for (String m : wire.usedMetrics()) metrics.add(m);
        }

        MiLista<AiSuggestedAction> actions = new MiLista<>();
        if (wire.suggestedActions() != null) {
            for (AiChatAnswerWire.SuggestedActionWire a : wire.suggestedActions()) {
                AiIntent actionType = parseIntent(a.type());
                actions.add(new AiSuggestedAction(actionType, a.label()));
            }
        }

        boolean requiresConfirmation = wire.requiresConfirmation() != null && wire.requiresConfirmation();
        double confidence = wire.confidence() != null ? wire.confidence() : 0.5;

        return new AiChatAnswer(
                wire.answer(),
                intent,
                confidence,
                metrics,
                actions,
                requiresConfirmation,
                wire.conversationId(),
                wire.usedContext()
        );
    }

    private AiFraudExplanation wireToFraudExplanation(AiFraudExplanationWire wire, String fallbackId) {
        MiLista<String> evidence = new MiLista<>();
        if (wire.evidence() != null) {
            for (String e : wire.evidence()) evidence.add(e);
        }

        String fraudEventId = wire.fraudEventId() != null ? wire.fraudEventId() : fallbackId;
        return new AiFraudExplanation(
                fraudEventId,
                wire.summary(),
                wire.severityExplanation(),
                evidence,
                wire.recommendation()
        );
    }

    private AiActionDraft wireToActionDraft(AiActionDraftWire wire) {
        AiIntent intent = parseIntent(wire.intent());

        TablaHash<String, String> draft = new TablaHash<>();
        if (wire.draft() != null) {
            for (Map.Entry<String, String> entry : wire.draft().entrySet()) {
                draft.put(entry.getKey(), entry.getValue());
            }
        }

        MiLista<String> missingFields = new MiLista<>();
        if (wire.missingFields() != null) {
            for (String f : wire.missingFields()) missingFields.add(f);
        }

        boolean requiresConfirmation = wire.requiresConfirmation() == null || wire.requiresConfirmation();

        return new AiActionDraft(intent, requiresConfirmation, draft, missingFields, wire.explanation());
    }

    private AiIntent parseIntent(String intentStr) {
        if (intentStr == null || intentStr.isBlank()) {
            return AiIntent.UNKNOWN;
        }
        try {
            return AiIntent.valueOf(intentStr.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return AiIntent.UNKNOWN;
        }
    }

    private String safe(String value) {
        return value != null ? value : "";
    }

    private String serializeContext(FintechAiContext context) {
        // Build a minimal JSON representation of the context for the prompt
        // Jackson is allowed here (infrastructure layer)
        try {
            return objectMapper.writeValueAsString(buildContextMap(context));
        } catch (JsonProcessingException e) {
            return "{}";
        }
    }

    private Map<String, Object> buildContextMap(FintechAiContext context) {
        java.util.LinkedHashMap<String, Object> map = new java.util.LinkedHashMap<>();
        map.put("actorRole", context.actorRole());
        map.put("scope", context.scope() != null ? context.scope().name() : null);
        map.put("range", context.range());
        if (context.analytics() != null) {
            map.put("totalTransactionCount", context.analytics().totalTransactionCount());
        }
        if (context.userFinancials() != null) {
            java.util.LinkedHashMap<String, Object> uf = new java.util.LinkedHashMap<>();
            uf.put("userId", context.userFinancials().userId());
            uf.put("totalBalance", context.userFinancials().totalBalance());
            uf.put("transactionCount", context.userFinancials().transactionCount());
            uf.put("loyaltyPoints", context.userFinancials().loyaltyPoints());
            uf.put("loyaltyLevel", context.userFinancials().loyaltyLevel());
            map.put("userFinancials", uf);
        }
        if (context.fraud() != null) {
            map.put("totalFraudEvents", context.fraud().totalFraudEvents());
        }
        if (context.scheduled() != null) {
            map.put("pendingScheduledOps", context.scheduled().pendingCount());
        }
        if (context.notifications() != null) {
            map.put("unreadNotifications", context.notifications().unreadCount());
        }
        return map;
    }
}
