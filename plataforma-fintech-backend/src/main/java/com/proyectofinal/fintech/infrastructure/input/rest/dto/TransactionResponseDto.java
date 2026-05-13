package com.proyectofinal.fintech.infrastructure.input.rest.dto;

/**
 * Response DTO for a single transaction.
 * 13 fields: original 12 + riskLevel (ADR-11.3 / S17).
 * riskLevel is non-nullable — defaults to LOW for all existing transactions.
 */
public record TransactionResponseDto(
        String id,
        String timestamp,
        String type,
        double amount,
        String sourceWalletId,
        String targetWalletId,
        String sourceUserId,
        String targetUserId,
        String status,
        double pointsGenerated,
        String description,
        boolean reversible,
        String riskLevel
) {
}
