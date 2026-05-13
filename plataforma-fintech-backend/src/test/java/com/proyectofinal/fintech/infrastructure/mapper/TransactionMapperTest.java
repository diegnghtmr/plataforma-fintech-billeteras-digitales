package com.proyectofinal.fintech.infrastructure.mapper;

import com.proyectofinal.fintech.application.result.ExternalTransferResult;
import com.proyectofinal.fintech.domain.model.FraudSeverity;
import com.proyectofinal.fintech.domain.model.Transaccion;
import com.proyectofinal.fintech.domain.model.TransactionStatus;
import com.proyectofinal.fintech.domain.model.TransactionType;
import com.proyectofinal.fintech.infrastructure.input.rest.dto.ExternalTransferResponseDto;
import com.proyectofinal.fintech.infrastructure.input.rest.dto.TransactionResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * T05-B17 / T11-B04 (RED) — TransactionMapper tests.
 * Verifies Transaccion → TransactionResponseDto mapping for all 13 fields (incl riskLevel).
 * Nulls must be preserved, enum names must be preserved.
 */
class TransactionMapperTest {

    private TransactionMapper mapper;
    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

    @BeforeEach
    void setUp() {
        mapper = new TransactionMapper();
    }

    @Test
    void toDto_mapsAll12Fields() {
        Transaccion tx = new Transaccion(
                "TX-000001", NOW, TransactionType.RECHARGE, 1000.0,
                "W001", "W002", "USR001", "USR002",
                TransactionStatus.SUCCESSFUL, 10.0, "test description", true
        );

        TransactionResponseDto dto = mapper.toDto(tx);

        assertThat(dto.id()).isEqualTo("TX-000001");
        assertThat(dto.timestamp()).isEqualTo(NOW.toString());
        assertThat(dto.type()).isEqualTo("RECHARGE");
        assertThat(dto.amount()).isEqualTo(1000.0);
        assertThat(dto.sourceWalletId()).isEqualTo("W001");
        assertThat(dto.targetWalletId()).isEqualTo("W002");
        assertThat(dto.sourceUserId()).isEqualTo("USR001");
        assertThat(dto.targetUserId()).isEqualTo("USR002");
        assertThat(dto.status()).isEqualTo("SUCCESSFUL");
        assertThat(dto.pointsGenerated()).isEqualTo(10.0);
        assertThat(dto.description()).isEqualTo("test description");
        assertThat(dto.reversible()).isTrue();
        assertThat(dto.riskLevel()).isEqualTo("LOW");
    }

    // T11-B04: riskLevel HIGH is mapped correctly
    @Test
    void toDto_mapsRiskLevelHigh() {
        Transaccion tx = new Transaccion(
                "TX-999", NOW, TransactionType.RECHARGE, 15000.0,
                "W001", null, "USR001", null,
                TransactionStatus.SUCCESSFUL, 150.0, null, true
        );
        tx.markRiskLevel(FraudSeverity.HIGH);

        TransactionResponseDto dto = mapper.toDto(tx);

        assertThat(dto.riskLevel()).isEqualTo("HIGH");
    }

    @Test
    void toDto_nullableFieldsPreserved() {
        Transaccion tx = new Transaccion(
                "TX-000002", NOW, TransactionType.WITHDRAWAL, 500.0,
                "W001", null, "USR001", null,
                TransactionStatus.SUCCESSFUL, 0.0, null, true
        );

        TransactionResponseDto dto = mapper.toDto(tx);

        assertThat(dto.targetWalletId()).isNull();
        assertThat(dto.targetUserId()).isNull();
        assertThat(dto.description()).isNull();
    }

    @Test
    void toDto_enumNamesPreservedAsStrings() {
        Transaccion tx = new Transaccion(
                "TX-000003", NOW, TransactionType.EXTERNAL_TRANSFER_SENT, 200.0,
                "W001", "W002", "USR001", "USR002",
                TransactionStatus.REVERSED, 2.0, null, false
        );

        TransactionResponseDto dto = mapper.toDto(tx);

        assertThat(dto.type()).isEqualTo("EXTERNAL_TRANSFER_SENT");
        assertThat(dto.status()).isEqualTo("REVERSED");
        assertThat(dto.reversible()).isFalse();
    }

    @Test
    void toDtoFromExternalResult_mapsOutgoingAndIncoming() {
        Transaccion outgoing = new Transaccion(
                "TX-000001", NOW, TransactionType.EXTERNAL_TRANSFER_SENT, 500.0,
                "W_A", "W_B", "USR_A", "USR_B",
                TransactionStatus.SUCCESSFUL, 5.0, null, true
        );
        Transaccion incoming = new Transaccion(
                "TX-000002", NOW, TransactionType.EXTERNAL_TRANSFER_RECEIVED, 500.0,
                "W_A", "W_B", "USR_A", "USR_B",
                TransactionStatus.SUCCESSFUL, 0.0, null, true
        );

        ExternalTransferResult result = new ExternalTransferResult(outgoing, incoming);
        ExternalTransferResponseDto dto = mapper.toDto(result);

        assertThat(dto.outgoingTransaction().id()).isEqualTo("TX-000001");
        assertThat(dto.incomingTransaction().id()).isEqualTo("TX-000002");
        assertThat(dto.outgoingTransaction().type()).isEqualTo("EXTERNAL_TRANSFER_SENT");
        assertThat(dto.incomingTransaction().type()).isEqualTo("EXTERNAL_TRANSFER_RECEIVED");
    }
}
