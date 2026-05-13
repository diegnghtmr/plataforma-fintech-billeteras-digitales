package com.proyectofinal.fintech.domain.service;

import com.proyectofinal.fintech.domain.model.TransactionType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * T11-A01 (RED) — PuntosCalculator: validates PDF-compliant floor-based points.
 * Formula: floor(amount / 100) * rate
 * Rates: RECHARGE→1, WITHDRAWAL→2, INTERNAL_TRANSFER→3, EXTERNAL_TRANSFER_SENT→3, RECEIVED→0
 */
class PuntosCalculatorTest {

    // S1 — RECHARGE 550 → floor(550/100)*1 = 5
    @Test
    void compute_recharge550_returns5() {
        assertThat(PuntosCalculator.compute(TransactionType.RECHARGE, 550.0)).isEqualTo(5.0);
    }

    // S2 — WITHDRAWAL 300 → floor(300/100)*2 = 6
    @Test
    void compute_withdrawal300_returns6() {
        assertThat(PuntosCalculator.compute(TransactionType.WITHDRAWAL, 300.0)).isEqualTo(6.0);
    }

    // S3 — INTERNAL_TRANSFER 1000 → floor(1000/100)*3 = 30
    @Test
    void compute_internalTransfer1000_returns30() {
        assertThat(PuntosCalculator.compute(TransactionType.INTERNAL_TRANSFER, 1000.0)).isEqualTo(30.0);
    }

    // S4a — EXTERNAL_TRANSFER_SENT 2500 → floor(2500/100)*3 = 75
    @Test
    void compute_externalTransferSent2500_returns75() {
        assertThat(PuntosCalculator.compute(TransactionType.EXTERNAL_TRANSFER_SENT, 2500.0)).isEqualTo(75.0);
    }

    // S4b — EXTERNAL_TRANSFER_RECEIVED always 0
    @Test
    void compute_externalTransferReceived_returnsZero() {
        assertThat(PuntosCalculator.compute(TransactionType.EXTERNAL_TRANSFER_RECEIVED, 2500.0)).isEqualTo(0.0);
    }

    // S5 — amount < 100 → 0 regardless of type
    @Test
    void compute_amount99Recharge_returnsZero() {
        assertThat(PuntosCalculator.compute(TransactionType.RECHARGE, 99.0)).isEqualTo(0.0);
    }

    @Test
    void compute_amount100Recharge_returns1() {
        assertThat(PuntosCalculator.compute(TransactionType.RECHARGE, 100.0)).isEqualTo(1.0);
    }

    // Negative amount → 0
    @Test
    void compute_negativeAmount_returnsZero() {
        assertThat(PuntosCalculator.compute(TransactionType.RECHARGE, -50.0)).isEqualTo(0.0);
    }

    // Zero amount → 0
    @Test
    void compute_zeroAmount_returnsZero() {
        assertThat(PuntosCalculator.compute(TransactionType.WITHDRAWAL, 0.0)).isEqualTo(0.0);
    }

    // Floor semantics: 150 → floor(150/100) = 1, *2 = 2
    @Test
    void compute_withdrawal150_returns2() {
        assertThat(PuntosCalculator.compute(TransactionType.WITHDRAWAL, 150.0)).isEqualTo(2.0);
    }

    // Exact 100 for INTERNAL_TRANSFER → floor(100/100)*3 = 3
    @Test
    void compute_internalTransfer100_returns3() {
        assertThat(PuntosCalculator.compute(TransactionType.INTERNAL_TRANSFER, 100.0)).isEqualTo(3.0);
    }
}
