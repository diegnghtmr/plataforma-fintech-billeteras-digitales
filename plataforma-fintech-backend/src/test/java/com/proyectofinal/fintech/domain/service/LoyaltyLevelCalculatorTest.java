package com.proyectofinal.fintech.domain.service;

import com.proyectofinal.fintech.domain.model.LoyaltyLevel;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * T11-A03 (RED) — LoyaltyLevelCalculator: PDF-compliant thresholds.
 * Thresholds: ≤500 BRONZE, 501-1000 SILVER, 1001-5000 GOLD, >5000 PLATINUM
 */
class LoyaltyLevelCalculatorTest {

    @Test
    void from_zero_returnsBronze() {
        assertThat(LoyaltyLevelCalculator.from(0)).isEqualTo(LoyaltyLevel.BRONZE);
    }

    @Test
    void from_500_returnsBronze() {
        assertThat(LoyaltyLevelCalculator.from(500)).isEqualTo(LoyaltyLevel.BRONZE);
    }

    @Test
    void from_501_returnsSilver() {
        assertThat(LoyaltyLevelCalculator.from(501)).isEqualTo(LoyaltyLevel.SILVER);
    }

    @Test
    void from_1000_returnsSilver() {
        assertThat(LoyaltyLevelCalculator.from(1000)).isEqualTo(LoyaltyLevel.SILVER);
    }

    @Test
    void from_1001_returnsGold() {
        assertThat(LoyaltyLevelCalculator.from(1001)).isEqualTo(LoyaltyLevel.GOLD);
    }

    @Test
    void from_5000_returnsGold() {
        assertThat(LoyaltyLevelCalculator.from(5000)).isEqualTo(LoyaltyLevel.GOLD);
    }

    @Test
    void from_5001_returnsPlatinum() {
        assertThat(LoyaltyLevelCalculator.from(5001)).isEqualTo(LoyaltyLevel.PLATINUM);
    }

    @Test
    void from_maxInt_returnsPlatinum() {
        assertThat(LoyaltyLevelCalculator.from(Integer.MAX_VALUE)).isEqualTo(LoyaltyLevel.PLATINUM);
    }

    @Test
    void from_negative_returnsBronze() {
        assertThat(LoyaltyLevelCalculator.from(-1)).isEqualTo(LoyaltyLevel.BRONZE);
    }
}
