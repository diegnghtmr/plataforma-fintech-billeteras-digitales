package com.proyectofinal.fintech.application.result.ai;

import com.proyectofinal.fintech.domain.structures.MiLista;

/**
 * The structured answer returned by the AI port for a chat request.
 * Collections use MiLista to comply with ADR-9.1 (no java.util collections in application layer).
 * Immutable record. Zero Spring imports.
 */
public record AiChatAnswer(
        String answer,
        AiIntent intent,
        double confidence,
        MiLista<String> usedMetrics,
        MiLista<AiSuggestedAction> suggestedActions,
        boolean requiresConfirmation,
        String conversationId,
        String usedContext
) {

    /**
     * Returns a new AiChatAnswer with requiresConfirmation forced to true.
     */
    public AiChatAnswer withRequiresConfirmation(boolean value) {
        return new AiChatAnswer(answer, intent, confidence, usedMetrics,
                suggestedActions, value, conversationId, usedContext);
    }
}
