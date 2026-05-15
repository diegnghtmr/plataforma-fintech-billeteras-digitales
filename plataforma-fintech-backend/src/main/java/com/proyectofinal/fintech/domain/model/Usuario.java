package com.proyectofinal.fintech.domain.model;

import com.proyectofinal.fintech.domain.service.LoyaltyLevelCalculator;

import java.time.Instant;

/**
 * Domain entity representing a platform user.
 * Plain POJO — ZERO Spring/Jakarta imports.
 * Points and loyaltyLevel are mutable so SDD 7 can update them.
 */
public class Usuario {

    private final String id;
    private final String name;
    private final String email;
    private final Instant registeredAt;
    private double points;
    private LoyaltyLevel loyaltyLevel;

    public Usuario(String id, String name, String email, Instant registeredAt,
                   double points, LoyaltyLevel loyaltyLevel) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.registeredAt = registeredAt;
        this.points = points;
        this.loyaltyLevel = loyaltyLevel;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public Instant getRegisteredAt() { return registeredAt; }
    public double getPoints() { return points; }
    public LoyaltyLevel getLoyaltyLevel() { return loyaltyLevel; }

    public void setPoints(double points) { this.points = points; }
    public void setLoyaltyLevel(LoyaltyLevel loyaltyLevel) { this.loyaltyLevel = loyaltyLevel; }

    /**
     * ADR-7.3 / ADR-11.6: Sole entry point for mutating points and loyalty level atomically.
     * Points are clamped at 0; level is recomputed via LoyaltyLevelCalculator.
     *
     * @param delta points to add (positive) or subtract (negative)
     * @return true if the loyaltyLevel ordinal INCREASED (level-up event); false otherwise.
     *         Demotions (ordinal decreases) always return false — no level-up notification on reversal.
     */
    public boolean addPoints(double delta) {
        LoyaltyLevel previousLevel = this.loyaltyLevel;
        this.points = Math.max(0.0, this.points + delta);
        this.loyaltyLevel = LoyaltyLevelCalculator.from(this.points);
        return this.loyaltyLevel.ordinal() > previousLevel.ordinal();
    }

    /**
     * Deducts points for a benefit redemption.
     * REQ-F1.3: validates amount > 0 and sufficient balance.
     *
     * @param amount points to deduct (must be > 0)
     * @throws IllegalArgumentException if amount <= 0 ("invalid_points_amount")
     * @throws IllegalArgumentException if insufficient points ("insufficient_points")
     */
    public void spendPoints(int amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("invalid_points_amount");
        }
        if (this.points < amount) {
            throw new IllegalArgumentException("insufficient_points");
        }
        addPoints(-amount);
    }
}
