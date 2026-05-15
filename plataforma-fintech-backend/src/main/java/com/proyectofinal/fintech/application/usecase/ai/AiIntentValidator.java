package com.proyectofinal.fintech.application.usecase.ai;

import com.proyectofinal.fintech.application.exception.ai.AiInvalidIntentException;
import com.proyectofinal.fintech.application.result.ai.AiChatAnswer;
import com.proyectofinal.fintech.application.result.ai.AiIntent;

/**
 * Validates AI responses: enforces intent allowlist and forces requiresConfirmation=true
 * for draft intents (DRAFT_SCHEDULED_OPERATION, DRAFT_TRANSFER).
 * Zero Spring imports.
 */
public class AiIntentValidator {

    /**
     * Validates an AI response.
     * - If intent is null → throws AiInvalidIntentException.
     * - If intent is DRAFT_SCHEDULED_OPERATION or DRAFT_TRANSFER → forces requiresConfirmation=true.
     * - If intent is UNKNOWN → passes through (graceful degradation).
     *
     * @param raw             the raw answer from the AI adapter
     * @param candidateIntent the intent classified by IntentClassifier
     * @return validated (possibly mutated) AiChatAnswer
     * @throws AiInvalidIntentException if intent is null or structurally invalid
     */
    public AiChatAnswer validate(AiChatAnswer raw, AiIntent candidateIntent) {
        if (raw.intent() == null) {
            throw new AiInvalidIntentException(
                    "AI returned null intent — expected one of AiIntent enum values");
        }

        // Force requiresConfirmation=true for draft intents
        if (raw.intent() == AiIntent.DRAFT_SCHEDULED_OPERATION
                || raw.intent() == AiIntent.DRAFT_TRANSFER) {
            if (!raw.requiresConfirmation()) {
                return raw.withRequiresConfirmation(true);
            }
        }

        return raw;
    }
}
