package com.proyectofinal.fintech.infrastructure.input.rest.dto;

/**
 * Response DTO for wallet data.
 * createdAt as ISO-8601 String.
 */
public record WalletResponseDto(
        String code,
        String name,
        String type,
        String ownerId,
        double balance,
        boolean active,
        String createdAt,
        int transactionCount
) {}
