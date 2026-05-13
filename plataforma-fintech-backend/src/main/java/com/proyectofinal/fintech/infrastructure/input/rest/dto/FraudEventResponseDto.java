package com.proyectofinal.fintech.infrastructure.input.rest.dto;

import java.time.Instant;

/**
 * DTO for fraud event response.
 */
public record FraudEventResponseDto(
        String id,
        String userId,
        String transactionId,
        String type,
        String severity,
        String description,
        Instant createdAt
) {}
