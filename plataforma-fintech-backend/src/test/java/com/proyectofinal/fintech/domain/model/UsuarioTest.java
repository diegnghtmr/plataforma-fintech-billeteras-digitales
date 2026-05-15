package com.proyectofinal.fintech.domain.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

/**
 * T07-B03 / T11-D06 — UsuarioTest.
 * Covers addPoints returning boolean (levelChanged) per ADR-11.6.
 * Thresholds: ≤500 BRONZE, 501-1000 SILVER, 1001-5000 GOLD, >5000 PLATINUM
 */
class UsuarioTest {

    @Test
    void defaultValues_points_zero_loyaltyLevel_bronze() {
        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        Usuario usuario = new Usuario("USR001", "Juan Pérez", "juan@example.com", now, 0.0, LoyaltyLevel.BRONZE);

        assertEquals(0.0, usuario.getPoints());
        assertEquals(LoyaltyLevel.BRONZE, usuario.getLoyaltyLevel());
    }

    @Test
    void constructor_setsAllFields() {
        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        Usuario usuario = new Usuario("USR001", "Juan Pérez", "juan@example.com", now, 100.0, LoyaltyLevel.SILVER);

        assertEquals("USR001", usuario.getId());
        assertEquals("Juan Pérez", usuario.getName());
        assertEquals("juan@example.com", usuario.getEmail());
        assertEquals(now, usuario.getRegisteredAt());
        assertEquals(100.0, usuario.getPoints());
        assertEquals(LoyaltyLevel.SILVER, usuario.getLoyaltyLevel());
    }

    @Test
    void setPoints_updatesPoints() {
        Instant now = Instant.now();
        Usuario usuario = new Usuario("USR001", "Test", "test@example.com", now, 0.0, LoyaltyLevel.BRONZE);
        usuario.setPoints(500.0);
        assertEquals(500.0, usuario.getPoints());
    }

    @Test
    void setLoyaltyLevel_updatesLevel() {
        Instant now = Instant.now();
        Usuario usuario = new Usuario("USR001", "Test", "test@example.com", now, 0.0, LoyaltyLevel.BRONZE);
        usuario.setLoyaltyLevel(LoyaltyLevel.GOLD);
        assertEquals(LoyaltyLevel.GOLD, usuario.getLoyaltyLevel());
    }

    // T07-B03: addPoints grows points and updates level
    @Test
    void addPoints_positiveGrowsPoints() {
        Instant now = Instant.now();
        Usuario usuario = new Usuario("USR001", "Test", "test@example.com", now, 0.0, LoyaltyLevel.BRONZE);
        usuario.addPoints(1000.0);
        assertThat(usuario.getPoints()).isEqualTo(1000.0);
        // PDF threshold: 501-1000 → SILVER
        assertThat(usuario.getLoyaltyLevel()).isEqualTo(LoyaltyLevel.SILVER);
    }

    @Test
    void addPoints_negativeClampsToZero() {
        Instant now = Instant.now();
        Usuario usuario = new Usuario("USR001", "Test", "test@example.com", now, 500.0, LoyaltyLevel.BRONZE);
        usuario.addPoints(-1000.0);
        assertThat(usuario.getPoints()).isEqualTo(0.0);
        assertThat(usuario.getLoyaltyLevel()).isEqualTo(LoyaltyLevel.BRONZE);
    }

    @Test
    void addPoints_levelDemotesOnClamp() {
        Instant now = Instant.now();
        // Start at SILVER (1200 pts per new thresholds: 1001-5000 is actually GOLD, 501-1000 SILVER)
        Usuario usuario = new Usuario("USR001", "Test", "test@example.com", now, 700.0, LoyaltyLevel.SILVER);
        usuario.addPoints(-700.0);
        assertThat(usuario.getPoints()).isEqualTo(0.0);
        assertThat(usuario.getLoyaltyLevel()).isEqualTo(LoyaltyLevel.BRONZE);
    }

    // T11-D06: addPoints returns true when loyaltyLevel ordinal increases
    @Test
    void addPoints_levelUpReturnsTrueWhenLevelIncreases() {
        Instant now = Instant.now();
        // 480 points → BRONZE; add 30 → 510 → SILVER
        Usuario usuario = new Usuario("USR001", "Test", "test@example.com", now, 480.0, LoyaltyLevel.BRONZE);
        boolean levelChanged = usuario.addPoints(30.0);
        assertThat(levelChanged).isTrue();
        assertThat(usuario.getLoyaltyLevel()).isEqualTo(LoyaltyLevel.SILVER);
    }

    @Test
    void addPoints_noLevelChangeReturnsFalse() {
        Instant now = Instant.now();
        // 200 points → BRONZE; add 50 → 250 → still BRONZE
        Usuario usuario = new Usuario("USR001", "Test", "test@example.com", now, 200.0, LoyaltyLevel.BRONZE);
        boolean levelChanged = usuario.addPoints(50.0);
        assertThat(levelChanged).isFalse();
        assertThat(usuario.getLoyaltyLevel()).isEqualTo(LoyaltyLevel.BRONZE);
    }

    @Test
    void addPoints_negativeDeltaReturnsFalse_demotion() {
        Instant now = Instant.now();
        // Start SILVER, subtract → BRONZE; demotion should return false (ADR-11.6: no emitLevelUp on demotion)
        Usuario usuario = new Usuario("USR001", "Test", "test@example.com", now, 600.0, LoyaltyLevel.SILVER);
        boolean levelChanged = usuario.addPoints(-600.0);
        assertThat(levelChanged).isFalse();
    }

    // F-33 (RED) — REQ-F1.3, REQ-F1.4: spendPoints

    @Test
    void spendPoints_sufficientPoints_deductsAndRecomputesLevel() {
        Instant now = Instant.now();
        // 500 points BRONZE; spend 200 → 300 points still BRONZE
        Usuario user = new Usuario("USR001", "Test", "test@example.com", now, 500.0, LoyaltyLevel.BRONZE);
        user.spendPoints(200);
        assertThat(user.getPoints()).isEqualTo(300.0);
        assertThat(user.getLoyaltyLevel()).isEqualTo(LoyaltyLevel.BRONZE);
    }

    @Test
    void spendPoints_insufficientPoints_throws() {
        Instant now = Instant.now();
        Usuario user = new Usuario("USR001", "Test", "test@example.com", now, 100.0, LoyaltyLevel.BRONZE);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> user.spendPoints(200));
        assertEquals("insufficient_points", ex.getMessage());
        assertThat(user.getPoints()).isEqualTo(100.0); // unchanged
    }

    @Test
    void spendPoints_zeroAmount_throws() {
        Instant now = Instant.now();
        Usuario user = new Usuario("USR001", "Test", "test@example.com", now, 500.0, LoyaltyLevel.BRONZE);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> user.spendPoints(0));
        assertEquals("invalid_points_amount", ex.getMessage());
    }

    @Test
    void spendPoints_negativeAmount_throws() {
        Instant now = Instant.now();
        Usuario user = new Usuario("USR001", "Test", "test@example.com", now, 500.0, LoyaltyLevel.BRONZE);
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> user.spendPoints(-50));
        assertEquals("invalid_points_amount", ex.getMessage());
    }
}
