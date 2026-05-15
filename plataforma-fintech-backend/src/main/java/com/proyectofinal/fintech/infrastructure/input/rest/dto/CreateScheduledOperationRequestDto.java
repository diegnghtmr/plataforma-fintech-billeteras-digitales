package com.proyectofinal.fintech.infrastructure.input.rest.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for creating a scheduled operation.
 * Matches OpenAPI CreateScheduledOperationRequest schema literally.
 */
public record CreateScheduledOperationRequestDto(
        @NotBlank String type,
        @NotBlank String sourceUserId,
        @NotBlank String sourceWalletId,
        String targetUserId,
        String targetWalletId,
        @NotNull @DecimalMin("0.01") Double amount,
        @NotBlank String scheduledAt,
        String description,
        String recurrence
) {}
