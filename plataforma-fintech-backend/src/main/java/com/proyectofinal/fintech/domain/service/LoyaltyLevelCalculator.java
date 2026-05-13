package com.proyectofinal.fintech.domain.service;

import com.proyectofinal.fintech.domain.model.LoyaltyLevel;

/**
 * Pure domain service: derives LoyaltyLevel from a points balance.
 * ZERO Spring/Jakarta imports. No I/O, no injected dependencies.
 *
 * ADR-11.2 — PDF-compliant thresholds:
 *   BRONZE  [0,   500]
 *   SILVER  [501, 1000]
 *   GOLD    [1001, 5000]
 *   PLATINUM (5001, ∞)
 *
 * Negative inputs are clamped to 0 before evaluation.
 */
public final class LoyaltyLevelCalculator {

    private LoyaltyLevelCalculator() {
        // Utility class — non-instantiable
    }

    /**
     * Derives the {@link LoyaltyLevel} for the given points balance.
     *
     * @param points the current points balance (negative values treated as 0)
     * @return the corresponding loyalty level
     */
    public static LoyaltyLevel from(double points) {
        double effective = Math.max(0.0, points);

        if (effective <= 500.0) {
            return LoyaltyLevel.BRONZE;
        } else if (effective <= 1000.0) {
            return LoyaltyLevel.SILVER;
        } else if (effective <= 5000.0) {
            return LoyaltyLevel.GOLD;
        } else {
            return LoyaltyLevel.PLATINUM;
        }
    }
}
