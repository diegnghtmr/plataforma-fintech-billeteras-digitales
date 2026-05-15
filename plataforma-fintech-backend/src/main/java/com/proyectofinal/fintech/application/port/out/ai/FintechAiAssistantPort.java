package com.proyectofinal.fintech.application.port.out.ai;

import com.proyectofinal.fintech.application.exception.ai.AiUnavailableException;
import com.proyectofinal.fintech.application.result.ai.AiActionDraft;
import com.proyectofinal.fintech.application.result.ai.AiChatAnswer;
import com.proyectofinal.fintech.application.result.ai.AiFraudExplanation;
import com.proyectofinal.fintech.application.result.ai.FintechAiContext;
import com.proyectofinal.fintech.application.result.ai.FraudInsightContext;

/**
 * Output port for the AI assistant.
 * Implementations: NoOpFintechAiAssistantAdapter (slice 1), RestClientFintechAssistantAdapter (slice 2).
 * Zero Spring imports.
 */
public interface FintechAiAssistantPort {

    /**
     * Answers a user's fintech question using the provided context.
     *
     * @param context     aggregated fintech context for the actor
     * @param userMessage the user's natural-language message
     * @return structured AI answer
     * @throws AiUnavailableException when the provider is unavailable
     */
    AiChatAnswer answer(FintechAiContext context, String userMessage);

    /**
     * Explains a fraud event in natural language.
     *
     * @param context fraud event insight context
     * @return structured fraud explanation
     * @throws AiUnavailableException when the provider is unavailable
     */
    AiFraudExplanation explainFraud(FraudInsightContext context);

    /**
     * Drafts a structured action based on the user's message.
     *
     * @param context     aggregated fintech context
     * @param userMessage the user's natural-language message
     * @return structured action draft (always requiresConfirmation=true)
     * @throws AiUnavailableException when the provider is unavailable
     */
    AiActionDraft draftAction(FintechAiContext context, String userMessage);
}
