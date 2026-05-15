package com.proyectofinal.fintech.infrastructure.input.rest.dto.ai;

import java.util.List;

/**
 * Response DTO for POST /ai/chat.
 */
public record AiChatResponseDto(
        String conversationId,
        String intent,
        String answer,
        AiSuggestedActionDto suggestedAction,
        AiUsedContextDto usedContext,
        String modelUsed,
        long latencyMs
) {
}
