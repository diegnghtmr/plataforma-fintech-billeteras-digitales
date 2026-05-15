package com.proyectofinal.fintech.infrastructure.output.ai;

import com.proyectofinal.fintech.application.exception.ai.AiUnavailableException;
import com.proyectofinal.fintech.application.port.out.ai.FintechAiAssistantPort;
import com.proyectofinal.fintech.application.result.ai.AiActionDraft;
import com.proyectofinal.fintech.application.result.ai.AiChatAnswer;
import com.proyectofinal.fintech.application.result.ai.AiFraudExplanation;
import com.proyectofinal.fintech.application.result.ai.FintechAiContext;
import com.proyectofinal.fintech.application.result.ai.FraudInsightContext;

/**
 * NoOp adapter for FintechAiAssistantPort.
 * Active when APP_AI_PROVIDER is "none" or unset.
 * All methods throw AiUnavailableException(PROVIDER_DISABLED) — ensures uniform 503 handling.
 * Wired by AiConfig when provider != "openrouter".
 */
public class NoOpFintechAiAssistantAdapter implements FintechAiAssistantPort {

    private static final String MESSAGE =
            "AI is disabled (APP_AI_PROVIDER=none). Configure OpenRouter to enable.";

    @Override
    public AiChatAnswer answer(FintechAiContext context, String userMessage) {
        throw new AiUnavailableException(AiUnavailableException.Reason.PROVIDER_DISABLED, MESSAGE);
    }

    @Override
    public AiFraudExplanation explainFraud(FraudInsightContext context) {
        throw new AiUnavailableException(AiUnavailableException.Reason.PROVIDER_DISABLED, MESSAGE);
    }

    @Override
    public AiActionDraft draftAction(FintechAiContext context, String userMessage) {
        throw new AiUnavailableException(AiUnavailableException.Reason.PROVIDER_DISABLED, MESSAGE);
    }
}
