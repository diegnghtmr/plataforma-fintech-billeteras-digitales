package com.proyectofinal.fintech.application.usecase.ai;

import com.proyectofinal.fintech.application.exception.ai.AiMessageTooLongException;
import com.proyectofinal.fintech.application.port.in.ai.DraftAiActionUseCase;
import com.proyectofinal.fintech.application.port.out.ai.AiAuditPort;
import com.proyectofinal.fintech.application.port.out.ai.FintechAiAssistantPort;
import com.proyectofinal.fintech.application.result.ai.AiActionDraft;
import com.proyectofinal.fintech.application.result.ai.AiIntent;
import com.proyectofinal.fintech.application.result.ai.FintechAiContext;

import java.util.Objects;

/**
 * Application service implementing DraftAiActionUseCase.
 * Full pipeline: validates message length, authorizes role,
 * builds context via BuildFintechAiContextService, delegates to AI port.
 * Always forces requiresConfirmation=true on the returned draft.
 * Constructor-injected, no Spring annotations — wired in AiConfig.
 * Zero Spring imports.
 */
public class DraftAiActionService implements DraftAiActionUseCase {

    private final AuthorizationService authorizationService;
    private final IntentClassifier intentClassifier;
    private final FintechAiAssistantPort port;
    private final AiObservability observability;
    private final AiAuditPort auditPort;
    private final BuildFintechAiContextService contextBuilder;
    private final int maxMessageLength;

    public DraftAiActionService(
            AuthorizationService authorizationService,
            IntentClassifier intentClassifier,
            FintechAiAssistantPort port,
            AiObservability observability,
            AiAuditPort auditPort,
            BuildFintechAiContextService contextBuilder,
            int maxMessageLength) {
        this.authorizationService = Objects.requireNonNull(authorizationService);
        this.intentClassifier = Objects.requireNonNull(intentClassifier);
        this.port = Objects.requireNonNull(port);
        this.observability = Objects.requireNonNull(observability);
        this.auditPort = Objects.requireNonNull(auditPort);
        this.contextBuilder = Objects.requireNonNull(contextBuilder);
        this.maxMessageLength = maxMessageLength;
    }

    @Override
    public AiActionDraft draft(DraftAiActionCommand command) {
        Objects.requireNonNull(command, "command must not be null");

        // Validate message length
        if (command.message().length() > maxMessageLength) {
            throw new AiMessageTooLongException(maxMessageLength, command.message().length());
        }

        // Classify and authorize
        AiIntent candidateIntent = intentClassifier.classify(command.scope(), command.message());
        authorizationService.authorize(command.actorRole(), command.scope(), candidateIntent);

        // Build context via real context builder
        // DraftAiActionCommand wraps to AskFintechAiCommand for context building
        com.proyectofinal.fintech.application.port.in.ai.AskFintechAiUseCase.AskFintechAiCommand contextCmd =
                new com.proyectofinal.fintech.application.port.in.ai.AskFintechAiUseCase.AskFintechAiCommand(
                        command.actorUserId(), command.actorRole(), command.scope(),
                        command.conversationId(), command.message(), command.range());
        FintechAiContext ctx = contextBuilder.build(contextCmd);

        // Delegate to port
        long start = System.currentTimeMillis();
        AiActionDraft draft = port.draftAction(ctx, command.message());
        long latencyMs = System.currentTimeMillis() - start;

        // Force requiresConfirmation=true regardless of LLM output
        if (!draft.requiresConfirmation()) {
            draft = new AiActionDraft(draft.intent(), true, draft.draft(),
                    draft.missingFields(), draft.explanation());
        }

        auditPort.record(new AiAuditPort.AiAuditEvent(
                command.conversationId(), command.actorUserId(), command.actorRole(),
                candidateIntent.name(), "unknown", latencyMs, true, null));

        return draft;
    }
}
