package com.proyectofinal.fintech.application.port.in.ai;

import com.proyectofinal.fintech.application.exception.ai.AiInvalidIntentException;
import com.proyectofinal.fintech.application.exception.ai.AiMessageTooLongException;
import com.proyectofinal.fintech.application.exception.ai.AiUnavailableException;
import com.proyectofinal.fintech.application.exception.ai.ForbiddenActorException;
import com.proyectofinal.fintech.application.result.ai.AiChatAnswer;
import com.proyectofinal.fintech.application.result.ai.AiScope;

/**
 * Input port for asking the AI assistant a fintech question.
 * Zero Spring imports.
 */
public interface AskFintechAiUseCase {

    /**
     * Command for asking the AI assistant.
     */
    record AskFintechAiCommand(
            String actorUserId,
            String actorRole,
            AiScope scope,
            String conversationId,
            String message,
            String range
    ) {
        public AskFintechAiCommand {
            java.util.Objects.requireNonNull(actorUserId, "actorUserId must not be null");
            java.util.Objects.requireNonNull(actorRole, "actorRole must not be null");
            java.util.Objects.requireNonNull(scope, "scope must not be null");
            java.util.Objects.requireNonNull(message, "message must not be null");
        }
    }

    /**
     * Processes the AI chat command and returns a structured answer.
     *
     * @param command the validated command
     * @return structured AI chat answer
     * @throws AiMessageTooLongException if message exceeds max length
     * @throws ForbiddenActorException   if actor role/scope is not permitted
     * @throws AiUnavailableException    if the AI provider is unavailable
     * @throws AiInvalidIntentException  if the LLM returns an invalid intent
     */
    AiChatAnswer ask(AskFintechAiCommand command);
}
