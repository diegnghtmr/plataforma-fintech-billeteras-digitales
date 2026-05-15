package com.proyectofinal.fintech.application.usecase.ai;

import com.proyectofinal.fintech.application.exception.ai.AiMessageTooLongException;
import com.proyectofinal.fintech.application.exception.ai.ForbiddenActorException;
import com.proyectofinal.fintech.application.port.in.ai.AskFintechAiUseCase;
import com.proyectofinal.fintech.application.port.out.ai.AiAuditPort;
import com.proyectofinal.fintech.application.port.out.ai.FintechAiAssistantPort;
import com.proyectofinal.fintech.application.result.ai.AiChatAnswer;
import com.proyectofinal.fintech.application.result.ai.AiIntent;
import com.proyectofinal.fintech.application.result.ai.FintechAiContext;

import java.util.Objects;

/**
 * Application service implementing AskFintechAiUseCase.
 * Full pipeline: validates message length, classifies intent, authorizes,
 * builds real context via BuildFintechAiContextService, checks cache,
 * delegates to AI port, validates response, caches result.
 * Constructor-injected, no Spring annotations — wired in AiConfig.
 * Zero Spring imports.
 */
public class AskFintechAiService implements AskFintechAiUseCase {

    private final AuthorizationService authorizationService;
    private final IntentClassifier intentClassifier;
    private final AiResponseCache responseCache;
    private final FintechAiAssistantPort port;
    private final AiIntentValidator intentValidator;
    private final AiObservability observability;
    private final AiAuditPort auditPort;
    private final BuildFintechAiContextService contextBuilder;
    private final int maxMessageLength;

    public AskFintechAiService(
            AuthorizationService authorizationService,
            IntentClassifier intentClassifier,
            AiResponseCache responseCache,
            FintechAiAssistantPort port,
            AiIntentValidator intentValidator,
            AiObservability observability,
            AiAuditPort auditPort,
            BuildFintechAiContextService contextBuilder,
            int maxMessageLength) {
        this.authorizationService = Objects.requireNonNull(authorizationService);
        this.intentClassifier = Objects.requireNonNull(intentClassifier);
        this.responseCache = Objects.requireNonNull(responseCache);
        this.port = Objects.requireNonNull(port);
        this.intentValidator = Objects.requireNonNull(intentValidator);
        this.observability = Objects.requireNonNull(observability);
        this.auditPort = Objects.requireNonNull(auditPort);
        this.contextBuilder = Objects.requireNonNull(contextBuilder);
        this.maxMessageLength = maxMessageLength;
    }

    @Override
    public AiChatAnswer ask(AskFintechAiCommand command) {
        Objects.requireNonNull(command, "command must not be null");

        // Step 2: validate message length
        if (command.message().length() > maxMessageLength) {
            throw new AiMessageTooLongException(maxMessageLength, command.message().length());
        }

        // Step 3: classify intent
        AiIntent candidateIntent = intentClassifier.classify(command.scope(), command.message());

        // Step 4: authorize role/scope/intent
        authorizationService.authorize(command.actorRole(), command.scope(), candidateIntent);

        // Step 5: build context via real context builder
        FintechAiContext ctx = contextBuilder.build(command);

        // Step 6-7: check cache
        String cacheKey = cacheKey(command.actorUserId(), command.conversationId(),
                candidateIntent, ctx.contextHash());
        java.util.Optional<AiChatAnswer> cached = responseCache.get(cacheKey);
        if (cached.isPresent()) {
            observability.recordCacheHit(command.actorUserId(), candidateIntent);
            return cached.get();
        }

        // Step 8: delegate to port (throws AiUnavailableException in slice 1)
        long start = System.currentTimeMillis();
        AiChatAnswer raw = port.answer(ctx, command.message());
        long latencyMs = System.currentTimeMillis() - start;

        // Step 9: validate response intent
        AiChatAnswer validated = intentValidator.validate(raw, candidateIntent);

        // Step 10: cache
        responseCache.put(cacheKey, validated);

        // Step 11: emit audit
        observability.recordAnswered(command.actorUserId(), candidateIntent, validated.confidence());
        auditPort.record(new AiAuditPort.AiAuditEvent(
                command.conversationId(), command.actorUserId(), command.actorRole(),
                candidateIntent.name(), "unknown", latencyMs, true, null));

        return validated;
    }

    private String cacheKey(String userId, String conversationId, AiIntent intent, String contextHash) {
        return userId + ":" + conversationId + ":" + intent.name() + ":" + contextHash;
    }
}
