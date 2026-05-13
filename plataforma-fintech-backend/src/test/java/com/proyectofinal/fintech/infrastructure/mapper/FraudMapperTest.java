package com.proyectofinal.fintech.infrastructure.mapper;

import com.proyectofinal.fintech.domain.model.FraudEvent;
import com.proyectofinal.fintech.domain.model.FraudSeverity;
import com.proyectofinal.fintech.infrastructure.input.rest.dto.FraudEventResponseDto;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * T09-B23 (RED) — FraudMapper tests.
 */
class FraudMapperTest {

    private final FraudMapper mapper = new FraudMapper();
    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

    @Test
    void toDto_fraudEvent_mapsAllFields() {
        FraudEvent event = new FraudEvent(
                "FRD-000001", "USR001", "TX-000001",
                "LARGE_TRANSACTION", FraudSeverity.HIGH,
                "Transacción de monto inusual: 15000.0", NOW
        );

        FraudEventResponseDto dto = mapper.toDto(event);

        assertThat(dto.id()).isEqualTo("FRD-000001");
        assertThat(dto.userId()).isEqualTo("USR001");
        assertThat(dto.transactionId()).isEqualTo("TX-000001");
        assertThat(dto.type()).isEqualTo("LARGE_TRANSACTION");
        assertThat(dto.severity()).isEqualTo("HIGH");
        assertThat(dto.description()).isEqualTo("Transacción de monto inusual: 15000.0");
        assertThat(dto.createdAt()).isEqualTo(NOW);
    }

    @Test
    void toDto_nullTransactionId_mappedAsNull() {
        FraudEvent event = new FraudEvent(
                "FRD-000001", "USR001", null,
                "LARGE_TRANSACTION", FraudSeverity.MEDIUM,
                "desc", NOW
        );

        FraudEventResponseDto dto = mapper.toDto(event);
        assertThat(dto.transactionId()).isNull();
    }
}
