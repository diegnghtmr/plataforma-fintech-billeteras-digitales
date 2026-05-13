package com.proyectofinal.fintech.infrastructure.input.rest.dto;

/**
 * Response DTO for user data.
 * loyaltyLevel serialized as String (enum name).
 * registeredAt as ISO-8601 String.
 */
public record UserResponseDto(
        String id,
        String name,
        String email,
        String registeredAt,
        double points,
        String loyaltyLevel,
        int walletCount,
        double totalBalance
) {}
