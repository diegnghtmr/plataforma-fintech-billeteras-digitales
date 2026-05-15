package com.proyectofinal.fintech.application.usecase.ai;

import com.proyectofinal.fintech.application.exception.ai.AiInvalidIntentException;
import com.proyectofinal.fintech.application.result.ai.AiChatAnswer;
import com.proyectofinal.fintech.application.result.ai.AiIntent;
import com.proyectofinal.fintech.domain.structures.MiLista;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * T03 — AiIntentValidator tests.
 * Validates that null intent throws AiInvalidIntentException,
 * draft intents force requiresConfirmation=true, and valid intents pass through.
 */
class AiIntentValidatorTest {

    private final AiIntentValidator validator = new AiIntentValidator();

    private AiChatAnswer makeAnswer(AiIntent intent, boolean requiresConfirmation) {
        return new AiChatAnswer(
                "Some answer",
                intent,
                0.8,
                new MiLista<>(),
                new MiLista<>(),
                requiresConfirmation,
                "conv1",
                "WALLET_SNAPSHOT"
        );
    }

    @Test
    void validate_validIntent_passesThrough() {
        AiChatAnswer raw = makeAnswer(AiIntent.EXPLAIN_BALANCE_CHANGE, false);
        AiChatAnswer result = validator.validate(raw, AiIntent.EXPLAIN_BALANCE_CHANGE);
        assertEquals(AiIntent.EXPLAIN_BALANCE_CHANGE, result.intent());
    }

    @Test
    void validate_nullIntent_throwsAiInvalidIntentException() {
        AiChatAnswer raw = makeAnswer(null, false);
        assertThrows(AiInvalidIntentException.class, () ->
                validator.validate(raw, AiIntent.EXPLAIN_BALANCE_CHANGE));
    }

    @Test
    void validate_draftScheduledOperation_forcesRequiresConfirmationTrue() {
        AiChatAnswer raw = makeAnswer(AiIntent.DRAFT_SCHEDULED_OPERATION, false);
        AiChatAnswer result = validator.validate(raw, AiIntent.DRAFT_SCHEDULED_OPERATION);
        assertTrue(result.requiresConfirmation(),
                "DRAFT_SCHEDULED_OPERATION must always set requiresConfirmation=true");
    }

    @Test
    void validate_draftTransfer_forcesRequiresConfirmationTrue() {
        AiChatAnswer raw = makeAnswer(AiIntent.DRAFT_TRANSFER, false);
        AiChatAnswer result = validator.validate(raw, AiIntent.DRAFT_TRANSFER);
        assertTrue(result.requiresConfirmation(),
                "DRAFT_TRANSFER must always set requiresConfirmation=true");
    }

    @Test
    void validate_nonDraftIntent_doesNotForceRequiresConfirmation() {
        AiChatAnswer raw = makeAnswer(AiIntent.SUMMARIZE_ACTIVITY, false);
        AiChatAnswer result = validator.validate(raw, AiIntent.SUMMARIZE_ACTIVITY);
        assertFalse(result.requiresConfirmation());
    }

    @Test
    void validate_unknownIntentFromLlm_passesThrough() {
        // UNKNOWN is a valid enum value — passes through without throwing
        AiChatAnswer raw = makeAnswer(AiIntent.UNKNOWN, false);
        assertDoesNotThrow(() -> validator.validate(raw, AiIntent.SUMMARIZE_ACTIVITY));
    }
}
