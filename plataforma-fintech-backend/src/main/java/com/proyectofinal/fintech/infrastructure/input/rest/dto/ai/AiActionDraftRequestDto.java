package com.proyectofinal.fintech.infrastructure.input.rest.dto.ai;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for POST /ai/action-draft.
 */
public record AiActionDraftRequestDto(
        @NotBlank @Size(min = 1, max = 1000) String message,
        @NotNull String actorRole,
        @NotBlank String actorUserId,
        @NotNull String scope,
        String conversationId
) {
}
