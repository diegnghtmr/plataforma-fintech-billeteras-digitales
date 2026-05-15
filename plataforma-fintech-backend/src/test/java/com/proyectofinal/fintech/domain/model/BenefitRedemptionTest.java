package com.proyectofinal.fintech.domain.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

/**
 * F-31 (RED) — BenefitRedemption record tests.
 * REQ-F1.2, REQ-F1.4: validation of fields.
 */
class BenefitRedemptionTest {

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

    @Test
    void constructor_validArgs_creates() {
        BenefitRedemption r = new BenefitRedemption("RED-001", "USR001", "BEN-001", 100, NOW);
        assertEquals("RED-001", r.id());
        assertEquals("USR001", r.userId());
        assertEquals("BEN-001", r.benefitId());
        assertEquals(100, r.pointsSpent());
        assertEquals(NOW, r.redeemedAt());
    }

    @Test
    void constructor_nullId_throws() {
        assertThrows(IllegalArgumentException.class, () ->
                new BenefitRedemption(null, "USR001", "BEN-001", 100, NOW));
    }

    @Test
    void constructor_nullUserId_throws() {
        assertThrows(IllegalArgumentException.class, () ->
                new BenefitRedemption("RED-001", null, "BEN-001", 100, NOW));
    }

    @Test
    void constructor_nullBenefitId_throws() {
        assertThrows(IllegalArgumentException.class, () ->
                new BenefitRedemption("RED-001", "USR001", null, 100, NOW));
    }

    @Test
    void constructor_zeroPointsSpent_throws() {
        assertThrows(IllegalArgumentException.class, () ->
                new BenefitRedemption("RED-001", "USR001", "BEN-001", 0, NOW));
    }

    @Test
    void constructor_negativePointsSpent_throws() {
        assertThrows(IllegalArgumentException.class, () ->
                new BenefitRedemption("RED-001", "USR001", "BEN-001", -5, NOW));
    }
}
