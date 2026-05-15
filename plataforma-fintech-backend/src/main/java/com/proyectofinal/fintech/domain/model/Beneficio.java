package com.proyectofinal.fintech.domain.model;

/**
 * Domain entity representing a redeemable benefit/reward.
 * Immutable — ZERO Spring/Jakarta imports.
 * REQ-F1.1: Fields: id, name, description, pointsCost, active.
 * REQ-F1.3: pointsCost must be >= 1.
 */
public class Beneficio {

    private final String id;
    private final String name;
    private final String description;
    private final int pointsCost;
    private final boolean active;

    public Beneficio(String id, String name, String description, int pointsCost, boolean active) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("invalid_benefit_name");
        }
        if (pointsCost < 1) {
            throw new IllegalArgumentException("invalid_points_cost");
        }
        this.id = id;
        this.name = name;
        this.description = description;
        this.pointsCost = pointsCost;
        this.active = active;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public int getPointsCost() { return pointsCost; }
    public boolean isActive() { return active; }
}
