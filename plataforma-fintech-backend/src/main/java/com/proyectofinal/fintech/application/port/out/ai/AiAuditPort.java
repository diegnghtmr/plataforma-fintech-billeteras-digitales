package com.proyectofinal.fintech.application.port.out.ai;

/**
 * Output port SPI for AI call audit events.
 * Implementations: NoOpAiAuditAdapter (slice 1), Slf4jAiAuditAdapter (slice 2).
 * Zero Spring imports.
 */
public interface AiAuditPort {

    /**
     * Records an AI audit event.
     *
     * @param event the audit event to record
     */
    void record(AiAuditEvent event);

    /**
     * Immutable audit event record.
     * Never includes the prompt body or API key.
     */
    record AiAuditEvent(
            String conversationId,
            String userId,
            String role,
            String intent,
            String model,
            long latencyMs,
            boolean success,
            String errorCode
    ) {
    }
}
