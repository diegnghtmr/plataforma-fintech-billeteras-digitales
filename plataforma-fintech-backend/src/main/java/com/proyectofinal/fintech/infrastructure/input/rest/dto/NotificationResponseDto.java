package com.proyectofinal.fintech.infrastructure.input.rest.dto;

/**
 * Response DTO for a notification.
 * Matches OpenAPI NotificationResponse schema literally.
 */
public record NotificationResponseDto(
        String id,
        String userId,
        String type,
        String severity,
        String title,
        String message,
        boolean read,
        String createdAt
) {}
