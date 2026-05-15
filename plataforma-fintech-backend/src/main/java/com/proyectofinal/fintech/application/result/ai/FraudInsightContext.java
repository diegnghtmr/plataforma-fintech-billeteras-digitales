package com.proyectofinal.fintech.application.result.ai;

import java.time.Instant;

/**
 * Immutable context passed to the AI port for fraud event explanation.
 * Contains only metadata — no raw transaction records (RNF-AI-02).
 * Zero Spring imports.
 */
public record FraudInsightContext(
        String fraudEventId,
        String userId,
        String type,
        String severity,
        String description,
        Instant createdAt
) {
}
