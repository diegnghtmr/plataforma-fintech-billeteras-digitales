package com.proyectofinal.fintech.application.port.in.ai;

import com.proyectofinal.fintech.application.exception.ai.AiInvalidIntentException;
import com.proyectofinal.fintech.application.exception.ai.AiMessageTooLongException;
import com.proyectofinal.fintech.application.exception.ai.AiUnavailableException;
import com.proyectofinal.fintech.application.exception.ai.ForbiddenActorException;
import com.proyectofinal.fintech.application.result.ai.AiActionDraft;
import com.proyectofinal.fintech.application.result.ai.AiScope;

/**
 * Input port for drafting an AI-assisted action.
 * Action drafts always have requiresConfirmation=true — the AI never executes operations.
 * Zero Spring imports.
 */
public interface DraftAiActionUseCase {

    /**
     * Command for requesting an action draft.
     */
    record DraftAiActionCommand(
            String actorUserId,
            String actorRole,
            AiScope scope,
            String message,
            String range,
            String conversationId
    ) {
        public DraftAiActionCommand {
            java.util.Objects.requireNonNull(actorUserId, "actorUserId must not be null");
            java.util.Objects.requireNonNull(actorRole, "actorRole must not be null");
            java.util.Objects.requireNonNull(scope, "scope must not be null");
            java.util.Objects.requireNonNull(message, "message must not be null");
        }
    }

    /**
     * Drafts a structured action based on the user's message.
     *
     * @param command the validated command
     * @return structured action draft with requiresConfirmation always true
     * @throws AiMessageTooLongException if message exceeds max length
     * @throws ForbiddenActorException   if actor role/scope is not permitted
     * @throws AiUnavailableException    if the AI provider is unavailable
     * @throws AiInvalidIntentException  if the LLM returns an invalid intent
     */
    AiActionDraft draft(DraftAiActionCommand command);
}
