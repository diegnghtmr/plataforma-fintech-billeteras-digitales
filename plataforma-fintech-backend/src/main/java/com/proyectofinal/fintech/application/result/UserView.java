package com.proyectofinal.fintech.application.result;

import com.proyectofinal.fintech.domain.model.LoyaltyLevel;

import java.time.Instant;

/**
 * Immutable result record produced by GetUserUseCase.
 * Contains all user fields plus computed wallet aggregates.
 * ZERO Spring/Jakarta imports.
 */
public record UserView(
        String id,
        String name,
        String email,
        Instant registeredAt,
        double points,
        LoyaltyLevel loyaltyLevel,
        int walletCount,
        double totalBalance
) {}
