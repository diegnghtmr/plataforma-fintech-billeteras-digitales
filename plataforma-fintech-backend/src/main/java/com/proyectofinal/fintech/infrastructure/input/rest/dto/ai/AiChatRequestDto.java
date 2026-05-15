package com.proyectofinal.fintech.infrastructure.input.rest.dto.ai;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for POST /ai/chat.
 * Bean Validation enforced. Lives in infrastructure — JDK types allowed here.
 */
public record AiChatRequestDto(
        @Size(max = 64) String conversationId,
        @NotBlank @Size(min = 1, max = 1000) String message,
        @NotNull String scope,
        @NotNull String actorRole,
        @NotBlank String actorUserId,
        String from,
        String to
) {
}
