package com.proyectofinal.fintech.infrastructure.input.rest.dto.ai;

import com.proyectofinal.fintech.application.result.ai.AiActionDraft;
import com.proyectofinal.fintech.application.result.ai.AiChatAnswer;
import com.proyectofinal.fintech.application.result.ai.AiFraudExplanation;
import com.proyectofinal.fintech.application.result.ai.AiSuggestedAction;
import com.proyectofinal.fintech.domain.structures.MiLista;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Mapper between application result types (MiLista-based) and REST DTOs (JDK List-based).
 * This is the single allowed conversion point from MiLista to java.util.List at the REST boundary.
 */
public class AiDtoMapper {

    public AiChatResponseDto toDto(AiChatAnswer answer, long latencyMs) {
        AiSuggestedActionDto suggestedAction = null;
        if (answer.suggestedActions() != null && answer.suggestedActions().size() > 0) {
            AiSuggestedAction first = answer.suggestedActions().get(0);
            suggestedAction = new AiSuggestedActionDto(
                    first.type() != null ? first.type().name() : null,
                    first.label());
        }

        List<String> sources = new ArrayList<>();
        if (answer.usedContext() != null) {
            sources.add(answer.usedContext());
        }

        AiUsedContextDto usedContext = new AiUsedContextDto(
                sources,
                answer.intent() != null ? answer.intent().name() : "UNKNOWN",
                null);

        return new AiChatResponseDto(
                answer.conversationId(),
                answer.intent() != null ? answer.intent().name() : "UNKNOWN",
                answer.answer(),
                suggestedAction,
                usedContext,
                "unknown",
                latencyMs);
    }

    public AiFraudExplanationResponseDto toDto(AiFraudExplanation explanation) {
        List<String> evidenceList = miListaToList(explanation.evidence());

        AiUsedContextDto usedContext = new AiUsedContextDto(
                List.of("FRAUD_EVENTS"), "USER", null);

        return new AiFraudExplanationResponseDto(
                explanation.fraudEventId(),
                explanation.summary(),
                explanation.severityExplanation(),
                evidenceList,
                explanation.recommendation(),
                usedContext);
    }

    public AiActionDraftResponseDto toDto(AiActionDraft draft) {
        // Convert TablaHash to Map
        Map<String, String> draftMap = new LinkedHashMap<>();
        if (draft.draft() != null) {
            for (String k : draft.draft().keys()) {
                draft.draft().get(k).ifPresent(v -> draftMap.put(k, v));
            }
        }

        List<String> missingFields = miListaToList(draft.missingFields());

        AiUsedContextDto usedContext = new AiUsedContextDto(
                List.of("WALLET_SNAPSHOT"), "USER", null);

        return new AiActionDraftResponseDto(
                draft.intent() != null ? draft.intent().name() : "UNKNOWN",
                draftMap,
                missingFields,
                draft.requiresConfirmation(),
                usedContext);
    }

    private <T> List<T> miListaToList(MiLista<T> miLista) {
        if (miLista == null) return List.of();
        return miLista.toList();
    }
}
