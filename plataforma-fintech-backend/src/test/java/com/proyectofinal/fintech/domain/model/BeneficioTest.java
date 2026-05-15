package com.proyectofinal.fintech.domain.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * F-29 (RED) — Beneficio domain class tests.
 * REQ-F1.3: pointsCost >= 1; REQ-F1.1: name must be non-blank.
 */
class BeneficioTest {

    @Test
    void constructor_validArgs_creates() {
        Beneficio b = new Beneficio("BEN-001", "Cashback 1%", "1% cashback on all purchases", 100, true);
        assertEquals("BEN-001", b.getId());
        assertEquals("Cashback 1%", b.getName());
        assertEquals("1% cashback on all purchases", b.getDescription());
        assertEquals(100, b.getPointsCost());
        assertTrue(b.isActive());
    }

    @Test
    void constructor_zeroPointsCost_throws() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                new Beneficio("BEN-001", "Cashback", "desc", 0, true));
        assertEquals("invalid_points_cost", ex.getMessage());
    }

    @Test
    void constructor_negativePointsCost_throws() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                new Beneficio("BEN-001", "Cashback", "desc", -1, true));
        assertEquals("invalid_points_cost", ex.getMessage());
    }

    @Test
    void constructor_blankName_throws() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                new Beneficio("BEN-001", "  ", "desc", 100, true));
        assertEquals("invalid_benefit_name", ex.getMessage());
    }

    @Test
    void constructor_nullName_throws() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                new Beneficio("BEN-001", null, "desc", 100, true));
        assertEquals("invalid_benefit_name", ex.getMessage());
    }
}
