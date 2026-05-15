package com.proyectofinal.fintech.infrastructure.output.ai;

import com.proyectofinal.fintech.application.port.out.ai.AiAuditPort;

/**
 * NoOp adapter for AiAuditPort.
 * Active in slice 1. Replaced by Slf4jAiAuditAdapter in slice 2.
 * The record method intentionally does nothing.
 */
public class NoOpAiAuditAdapter implements AiAuditPort {

    @Override
    public void record(AiAuditEvent event) {
        // No-op in slice 1 — Slf4jAiAuditAdapter added in slice 2
    }
}
