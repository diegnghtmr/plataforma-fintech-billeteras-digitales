package com.proyectofinal.fintech.domain.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * T05-B03 / T11-B01 (RED) — Transaccion domain model tests.
 * Covers S-DOM-01 (construction defaults), S-DOM-02 (status mutation only),
 * and S17 (riskLevel default LOW + markRiskLevel).
 */
class TransaccionTest {

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

    // S-DOM-01: Construction defaults — reversible=true, status=SUCCESSFUL, nullables accepted
    @Test
    void construction_reversibleTrueAndStatusSuccessful() {
        Transaccion tx = new Transaccion(
                "TX-000001", NOW, TransactionType.RECHARGE, 1000.0,
                "W001", null, "USR001", null,
                TransactionStatus.SUCCESSFUL, 10.0, null, true
        );

        assertThat(tx.getId()).isEqualTo("TX-000001");
        assertThat(tx.getTimestamp()).isEqualTo(NOW);
        assertThat(tx.getType()).isEqualTo(TransactionType.RECHARGE);
        assertThat(tx.getAmount()).isEqualTo(1000.0);
        assertThat(tx.getSourceWalletId()).isEqualTo("W001");
        assertThat(tx.getTargetWalletId()).isNull();
        assertThat(tx.getSourceUserId()).isEqualTo("USR001");
        assertThat(tx.getTargetUserId()).isNull();
        assertThat(tx.getStatus()).isEqualTo(TransactionStatus.SUCCESSFUL);
        assertThat(tx.getPointsGenerated()).isEqualTo(10.0);
        assertThat(tx.getDescription()).isNull();
        assertThat(tx.isReversible()).isTrue();
    }

    // S-DOM-01: nullable fields can be null
    @Test
    void construction_nullableFieldsAccepted() {
        Transaccion tx = new Transaccion(
                "TX-000002", NOW, TransactionType.WITHDRAWAL, 200.0,
                "W001", null, "USR001", null,
                TransactionStatus.SUCCESSFUL, 0.0, null, true
        );
        assertThat(tx.getTargetWalletId()).isNull();
        assertThat(tx.getTargetUserId()).isNull();
        assertThat(tx.getDescription()).isNull();
    }

    // S-DOM-02: setStatus mutates ONLY status; all other fields unchanged
    @Test
    void setStatus_mutatesOnlyStatus() {
        Transaccion tx = new Transaccion(
                "TX-000003", NOW, TransactionType.RECHARGE, 500.0,
                "W001", null, "USR001", null,
                TransactionStatus.SUCCESSFUL, 5.0, "test desc", true
        );

        tx.setStatus(TransactionStatus.REVERSED);

        assertThat(tx.getStatus()).isEqualTo(TransactionStatus.REVERSED);
        assertThat(tx.getId()).isEqualTo("TX-000003");
        assertThat(tx.getAmount()).isEqualTo(500.0);
        assertThat(tx.isReversible()).isTrue();
    }

    // S17: default riskLevel is LOW
    @Test
    void construction_defaultRiskLevelIsLow() {
        Transaccion tx = new Transaccion(
                "TX-000004", NOW, TransactionType.RECHARGE, 100.0,
                "W001", null, "USR001", null,
                TransactionStatus.SUCCESSFUL, 1.0, null, true
        );
        assertThat(tx.getRiskLevel()).isEqualTo(FraudSeverity.LOW);
    }

    // S17: markRiskLevel updates field
    @Test
    void markRiskLevel_high_updatesField() {
        Transaccion tx = new Transaccion(
                "TX-000005", NOW, TransactionType.RECHARGE, 100.0,
                "W001", null, "USR001", null,
                TransactionStatus.SUCCESSFUL, 1.0, null, true
        );
        tx.markRiskLevel(FraudSeverity.HIGH);
        assertThat(tx.getRiskLevel()).isEqualTo(FraudSeverity.HIGH);
    }

    // Last call wins
    @Test
    void markRiskLevel_calledMultipleTimes_lastValueWins() {
        Transaccion tx = new Transaccion(
                "TX-000006", NOW, TransactionType.RECHARGE, 100.0,
                "W001", null, "USR001", null,
                TransactionStatus.SUCCESSFUL, 1.0, null, true
        );
        tx.markRiskLevel(FraudSeverity.HIGH);
        tx.markRiskLevel(FraudSeverity.LOW);
        assertThat(tx.getRiskLevel()).isEqualTo(FraudSeverity.LOW);
    }
}
