package com.proyectofinal.fintech.domain.model;

import java.time.Instant;

/**
 * Domain record representing a benefit redemption event.
 * Compact constructor validates all required fields.
 * ZERO Spring/Jakarta imports — REQ-F1.2, REQ-F1.4.
 */
public record BenefitRedemption(String id, String userId, String benefitId, int pointsSpent, Instant redeemedAt) {

    public BenefitRedemption {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("invalid_redemption_id");
        if (userId == null || userId.isBlank()) throw new IllegalArgumentException("invalid_user_id");
        if (benefitId == null || benefitId.isBlank()) throw new IllegalArgumentException("invalid_benefit_id");
        if (pointsSpent <= 0) throw new IllegalArgumentException("invalid_points_spent");
    }
}
