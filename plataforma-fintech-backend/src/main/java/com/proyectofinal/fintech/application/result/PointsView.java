package com.proyectofinal.fintech.application.result;

import com.proyectofinal.fintech.domain.model.LoyaltyLevel;

/**
 * Read model returned by GetUserPointsUseCase.
 * ZERO Spring/Jakarta imports.
 */
public record PointsView(String userId, double points, LoyaltyLevel loyaltyLevel) {
}
