package com.proyectofinal.fintech.application.port.in.ai;

import com.proyectofinal.fintech.application.exception.ai.AiUnavailableException;
import com.proyectofinal.fintech.application.exception.ai.ForbiddenActorException;
import com.proyectofinal.fintech.application.result.ai.AiFraudExplanation;
import com.proyectofinal.fintech.domain.exception.NotFoundException;

/**
 * Input port for explaining a fraud event using AI.
 * Zero Spring imports.
 */
public interface ExplainFraudUseCase {

    /**
     * Command for requesting a fraud event explanation.
     */
    record ExplainFraudCommand(
            String actorUserId,
            String actorRole,
            String fraudEventId
    ) {
        public ExplainFraudCommand {
            java.util.Objects.requireNonNull(actorUserId, "actorUserId must not be null");
            java.util.Objects.requireNonNull(actorRole, "actorRole must not be null");
            java.util.Objects.requireNonNull(fraudEventId, "fraudEventId must not be null");
        }
    }

    /**
     * Explains a fraud event in natural language.
     *
     * @param command the validated command
     * @return structured fraud explanation
     * @throws NotFoundException      if the fraud event does not exist
     * @throws ForbiddenActorException if the actor is not authorized for this event
     * @throws AiUnavailableException  if the AI provider is unavailable
     */
    AiFraudExplanation explain(ExplainFraudCommand command);
}
