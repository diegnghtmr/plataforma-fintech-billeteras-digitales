package com.proyectofinal.fintech.infrastructure.input.rest.dto.ai;

import java.util.List;

/**
 * Response DTO for GET /ai/fraud-events/{id}/explain.
 */
public record AiFraudExplanationResponseDto(
        String fraudEventId,
        String summary,
        String severityExplanation,
        List<String> evidence,
        String recommendation,
        AiUsedContextDto usedContext
) {
}
