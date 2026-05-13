package com.proyectofinal.fintech.domain.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * T09-B01 (RED) — FraudEvent constructor and immutability.
 */
class FraudEventTest {

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

    @Test
    void constructor_storesAllSevenFields() {
        FraudEvent event = new FraudEvent(
                "FRD-000001",
                "USR001",
                "TX-000001",
                "LARGE_TRANSACTION",
                FraudSeverity.HIGH,
                "Transacción de monto inusual: 15000.0",
                NOW
        );

        assertThat(event.getId()).isEqualTo("FRD-000001");
        assertThat(event.getUserId()).isEqualTo("USR001");
        assertThat(event.getTransactionId()).isEqualTo("TX-000001");
        assertThat(event.getType()).isEqualTo("LARGE_TRANSACTION");
        assertThat(event.getSeverity()).isEqualTo(FraudSeverity.HIGH);
        assertThat(event.getDescription()).isEqualTo("Transacción de monto inusual: 15000.0");
        assertThat(event.getCreatedAt()).isEqualTo(NOW);
    }

    @Test
    void constructor_nullableTransactionId_isAccepted() {
        FraudEvent event = new FraudEvent(
                "FRD-000001", "USR001", null,
                "LARGE_TRANSACTION", FraudSeverity.HIGH,
                "desc", NOW
        );
        assertThat(event.getTransactionId()).isNull();
    }

    @Test
    void createdAt_isImmutable() {
        FraudEvent event = new FraudEvent(
                "FRD-000001", "USR001", "TX-1",
                "LARGE_TRANSACTION", FraudSeverity.HIGH,
                "desc", NOW
        );
        // No setter for createdAt — just verify the returned Instant is the same reference
        assertThat(event.getCreatedAt()).isSameAs(NOW);
    }
}
