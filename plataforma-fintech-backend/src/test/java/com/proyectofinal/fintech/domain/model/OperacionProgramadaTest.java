package com.proyectofinal.fintech.domain.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

/**
 * T08-B02 (RED) — OperacionProgramada domain model tests.
 * Covers S-DOM-1 and S-DOM-2.
 */
class OperacionProgramadaTest {

    private OperacionProgramada make(String id, ScheduledOperationType type, String sourceUserId,
                                      String sourceWalletId, Instant scheduledAt) {
        return new OperacionProgramada(id, type, ScheduledOperationStatus.PENDING,
                sourceUserId, sourceWalletId, null, null,
                100.0, scheduledAt, null);
    }

    @Test
    void defaultStatus_isPending() {
        OperacionProgramada op = make("SOP-000001", ScheduledOperationType.RECHARGE,
                "USR001", "W001", Instant.parse("2026-06-01T00:00:00Z"));
        assertEquals(ScheduledOperationStatus.PENDING, op.getStatus());
    }

    @Test
    void markExecuted_changesStatusToExecuted() {
        OperacionProgramada op = make("SOP-000001", ScheduledOperationType.RECHARGE,
                "USR001", "W001", Instant.parse("2026-06-01T00:00:00Z"));
        op.markExecuted();
        assertEquals(ScheduledOperationStatus.EXECUTED, op.getStatus());
    }

    @Test
    void markCancelled_changesStatusToCancelled() {
        OperacionProgramada op = make("SOP-000001", ScheduledOperationType.RECHARGE,
                "USR001", "W001", Instant.parse("2026-06-01T00:00:00Z"));
        op.markCancelled();
        assertEquals(ScheduledOperationStatus.CANCELLED, op.getStatus());
    }

    @Test
    void markFailed_changesStatusToFailed() {
        OperacionProgramada op = make("SOP-000001", ScheduledOperationType.RECHARGE,
                "USR001", "W001", Instant.parse("2026-06-01T00:00:00Z"));
        op.markFailed();
        assertEquals(ScheduledOperationStatus.FAILED, op.getStatus());
    }

    // S-DOM-1: same scheduledAt — id ASC tie-break
    @Test
    void compareTo_sameScheduledAt_ordersByIdAsc() {
        Instant now = Instant.parse("2026-06-01T00:00:00Z");
        OperacionProgramada opA = make("SOP-000001", ScheduledOperationType.RECHARGE, "USR001", "W001", now);
        OperacionProgramada opB = make("SOP-000002", ScheduledOperationType.RECHARGE, "USR001", "W001", now);

        assertTrue(opA.compareTo(opB) < 0, "SOP-000001 should come before SOP-000002");
        assertTrue(opB.compareTo(opA) > 0, "SOP-000002 should come after SOP-000001");
    }

    // S-DOM-1: different scheduledAt — earlier first
    @Test
    void compareTo_differentScheduledAt_earlierFirst() {
        Instant earlier = Instant.parse("2026-06-01T00:00:00Z");
        Instant later   = Instant.parse("2026-06-02T00:00:00Z");
        OperacionProgramada opEarly = make("SOP-000002", ScheduledOperationType.RECHARGE, "USR001", "W001", earlier);
        OperacionProgramada opLate  = make("SOP-000001", ScheduledOperationType.RECHARGE, "USR001", "W001", later);

        assertTrue(opEarly.compareTo(opLate) < 0, "Earlier scheduledAt should order first regardless of id");
    }

    // S-DOM-2: status transitions verified via markCancelled
    @Test
    void markCancelled_scenario_S_DOM_2() {
        OperacionProgramada op = make("SOP-000001", ScheduledOperationType.RECHARGE,
                "USR001", "W001", Instant.parse("2026-06-01T00:00:00Z"));
        assertEquals(ScheduledOperationStatus.PENDING, op.getStatus());
        op.markCancelled();
        assertEquals(ScheduledOperationStatus.CANCELLED, op.getStatus());
    }
}
