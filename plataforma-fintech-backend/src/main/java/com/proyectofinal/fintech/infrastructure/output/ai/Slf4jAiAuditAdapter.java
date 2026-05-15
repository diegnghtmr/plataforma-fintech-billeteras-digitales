package com.proyectofinal.fintech.infrastructure.output.ai;

import com.proyectofinal.fintech.application.port.out.ai.AiAuditPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SLF4J-backed implementation of AiAuditPort.
 * Records AI interaction audit events to the application log at INFO level.
 * Never logs conversation content — only metadata (userId hash, intent, latencyMs, model).
 * <p>
 * This is an alternative to NoOpAiAuditAdapter for environments where audit trails are required.
 * Wire this bean in AiConfig instead of NoOpAiAuditAdapter when audit logging is needed.
 */
public class Slf4jAiAuditAdapter implements AiAuditPort {

    private static final Logger log = LoggerFactory.getLogger(Slf4jAiAuditAdapter.class);

    @Override
    public void record(AiAuditEvent event) {
        if (event == null) return;
        // Never log conversation content (userId, model, intent, latency, success only)
        log.info("[AI_AUDIT] userId={} role={} intent={} model={} latencyMs={} success={} errorCode={}",
                hashId(event.userId()),
                event.role(),
                event.intent(),
                event.model(),
                event.latencyMs(),
                event.success(),
                event.errorCode() != null ? event.errorCode() : "-");
    }

    private String hashId(String id) {
        if (id == null) return "null";
        // Simple 8-char prefix of hash to avoid PII leakage
        int hash = Math.abs(id.hashCode());
        return "u" + Integer.toHexString(hash).substring(0, Math.min(8, Integer.toHexString(hash).length()));
    }
}
