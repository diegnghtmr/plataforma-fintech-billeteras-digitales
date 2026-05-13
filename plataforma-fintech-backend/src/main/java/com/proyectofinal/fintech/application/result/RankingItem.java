package com.proyectofinal.fintech.application.result;

import com.proyectofinal.fintech.domain.model.LoyaltyLevel;

/**
 * Read model for a single ranking entry returned by GetPointsRankingUseCase.
 * ZERO Spring/Jakarta imports.
 */
public record RankingItem(int position, String userId, String userName, double points, LoyaltyLevel loyaltyLevel) {
}
