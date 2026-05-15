package com.proyectofinal.fintech.application.result;

/**
 * Read model for a benefit redemption record.
 * REQ-F1.2: includes id, userId, benefitId, pointsSpent, redeemedAt.
 */
public record BenefitRedemptionView(String id, String userId, String benefitId, int pointsSpent, String redeemedAt) {}
