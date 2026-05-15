package com.proyectofinal.fintech.infrastructure.input.rest.dto;

/**
 * Response DTO for a benefit redemption record.
 * REQ-F1.2.
 */
public record BenefitRedemptionResponseDto(
        String id,
        String userId,
        String benefitId,
        int pointsSpent,
        String redeemedAt
) {}
