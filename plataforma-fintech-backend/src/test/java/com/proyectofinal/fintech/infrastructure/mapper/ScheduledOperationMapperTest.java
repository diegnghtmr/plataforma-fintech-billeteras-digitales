package com.proyectofinal.fintech.infrastructure.mapper;

import com.proyectofinal.fintech.domain.model.*;
import com.proyectofinal.fintech.infrastructure.input.rest.dto.ScheduledOperationResponseDto;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

/**
 * T08-B24 (RED) — ScheduledOperationMapper tests.
 */
class ScheduledOperationMapperTest {

    private final ScheduledOperationMapper mapper = new ScheduledOperationMapper();
    private static final Instant SCHEDULED = Instant.parse("2026-06-01T00:00:00Z");

    @Test
    void toDto_allFieldsMapped() {
        OperacionProgramada op = new OperacionProgramada(
                "SOP-000001", ScheduledOperationType.RECHARGE, ScheduledOperationStatus.PENDING,
                "USR001", "W001", "USR002", "W002",
                100.0, SCHEDULED, "desc");

        ScheduledOperationResponseDto dto = mapper.toDto(op);

        assertEquals("SOP-000001", dto.id());
        assertEquals("RECHARGE", dto.type());
        assertEquals("PENDING", dto.status());
        assertEquals("USR001", dto.sourceUserId());
        assertEquals("W001", dto.sourceWalletId());
        assertEquals("USR002", dto.targetUserId());
        assertEquals("W002", dto.targetWalletId());
        assertEquals(100.0, dto.amount());
        assertEquals(SCHEDULED.toString(), dto.scheduledAt());
        assertEquals("desc", dto.description());
    }

    @Test
    void toDto_nullableFieldsPreserved() {
        OperacionProgramada op = new OperacionProgramada(
                "SOP-000001", ScheduledOperationType.RECHARGE, ScheduledOperationStatus.PENDING,
                "USR001", "W001", null, null,
                50.0, SCHEDULED, null);

        ScheduledOperationResponseDto dto = mapper.toDto(op);

        assertNull(dto.targetUserId());
        assertNull(dto.targetWalletId());
        assertNull(dto.description());
    }
}
