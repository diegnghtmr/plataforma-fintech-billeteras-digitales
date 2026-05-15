package com.proyectofinal.fintech.application.usecase.ai;

import com.proyectofinal.fintech.application.exception.ai.ForbiddenActorException;
import com.proyectofinal.fintech.application.port.in.ai.ExplainFraudUseCase;
import com.proyectofinal.fintech.application.port.out.ai.AiAuditPort;
import com.proyectofinal.fintech.application.port.out.ai.FintechAiAssistantPort;
import com.proyectofinal.fintech.application.result.ai.AiFraudExplanation;
import com.proyectofinal.fintech.application.result.ai.FraudInsightContext;
import com.proyectofinal.fintech.domain.exception.ErrorCode;
import com.proyectofinal.fintech.domain.exception.NotFoundException;
import com.proyectofinal.fintech.domain.model.FraudEvent;
import com.proyectofinal.fintech.domain.port.FraudEventRepository;

import java.util.Objects;

/**
 * Application service implementing ExplainFraudUseCase.
 * Slice 1 skeleton: loads fraud event, checks actor ownership, delegates to port.
 * Constructor-injected, no Spring annotations — wired in AiConfig.
 * Zero Spring imports.
 */
public class ExplainFraudService implements ExplainFraudUseCase {

    private final FraudEventRepository fraudEventRepository;
    private final FintechAiAssistantPort port;
    private final AiResponseCache responseCache;
    private final AiObservability observability;
    private final AiAuditPort auditPort;

    public ExplainFraudService(
            FraudEventRepository fraudEventRepository,
            FintechAiAssistantPort port,
            AiResponseCache responseCache,
            AiObservability observability,
            AiAuditPort auditPort) {
        this.fraudEventRepository = Objects.requireNonNull(fraudEventRepository);
        this.port = Objects.requireNonNull(port);
        this.responseCache = Objects.requireNonNull(responseCache);
        this.observability = Objects.requireNonNull(observability);
        this.auditPort = Objects.requireNonNull(auditPort);
    }

    @Override
    public AiFraudExplanation explain(ExplainFraudCommand command) {
        Objects.requireNonNull(command, "command must not be null");

        // Load fraud event — throws NotFoundException if absent
        FraudEvent fraudEvent = fraudEventRepository.findById(command.fraudEventId())
                .orElseThrow(() -> new NotFoundException(ErrorCode.FRAUD_EVENT_NOT_FOUND,
                        "Fraud event " + command.fraudEventId() + " not found"));

        // Authorize: USER can only see their own events
        if ("USER".equals(command.actorRole())
                && !fraudEvent.getUserId().equals(command.actorUserId())) {
            throw new ForbiddenActorException(
                    "AI_FORBIDDEN: USER " + command.actorUserId()
                    + " may not access fraud event " + command.fraudEventId());
        }

        // Build fraud insight context (no raw transaction data)
        FraudInsightContext ctx = new FraudInsightContext(
                fraudEvent.getId(),
                fraudEvent.getUserId(),
                fraudEvent.getType(),
                fraudEvent.getSeverity().name(),
                fraudEvent.getDescription(),
                fraudEvent.getCreatedAt()
        );

        // For slice 1, fraud explain cache is not implemented (same AiChatAnswer type required)
        // Full cache support added in slice 2.

        // Delegate to port
        long start = System.currentTimeMillis();
        AiFraudExplanation explanation = port.explainFraud(ctx);
        long latencyMs = System.currentTimeMillis() - start;

        // Emit audit
        auditPort.record(new AiAuditPort.AiAuditEvent(
                null, command.actorUserId(), command.actorRole(),
                "EXPLAIN_FRAUD_EVENT", "unknown", latencyMs, true, null));

        return explanation;
    }
}
