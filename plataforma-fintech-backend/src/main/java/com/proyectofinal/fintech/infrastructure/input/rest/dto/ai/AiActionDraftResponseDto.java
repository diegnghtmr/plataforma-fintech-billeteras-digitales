package com.proyectofinal.fintech.infrastructure.input.rest.dto.ai;

import java.util.List;
import java.util.Map;

/**
 * Response DTO for POST /ai/action-draft.
 */
public record AiActionDraftResponseDto(
        String intent,
        Map<String, String> draft,
        List<String> missingFields,
        boolean requiresConfirmation,
        AiUsedContextDto usedContext
) {
}
