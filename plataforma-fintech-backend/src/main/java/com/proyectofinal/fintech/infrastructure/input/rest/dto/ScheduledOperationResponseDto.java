package com.proyectofinal.fintech.infrastructure.input.rest.dto;

/**
 * Response DTO for a scheduled operation.
 * Matches OpenAPI ScheduledOperationResponse schema literally.
 */
public record ScheduledOperationResponseDto(
        String id,
        String type,
        String status,
        String sourceUserId,
        String sourceWalletId,
        String targetUserId,
        String targetWalletId,
        double amount,
        String scheduledAt,
        String description,
        String recurrence
) {}
