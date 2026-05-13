package com.proyectofinal.fintech.infrastructure.output.memory;

import com.proyectofinal.fintech.domain.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * T08-B10 (RED) — InMemoryScheduledOperationRepository tests.
 */
class InMemoryScheduledOperationRepositoryTest {

    private InMemoryScheduledOperationRepository repo;

    @BeforeEach
    void setUp() {
        repo = new InMemoryScheduledOperationRepository();
    }

    private OperacionProgramada makeOp(String id, ScheduledOperationStatus status, Instant scheduledAt) {
        return new OperacionProgramada(id, ScheduledOperationType.RECHARGE, status,
                "USR001", "W001", null, null, 100.0, scheduledAt, null);
    }

    @Test
    void save_andFindById_returnsOp() {
        OperacionProgramada op = makeOp("SOP-000001", ScheduledOperationStatus.PENDING,
                Instant.parse("2026-06-01T00:00:00Z"));
        repo.save(op);
        Optional<OperacionProgramada> found = repo.findById("SOP-000001");
        assertTrue(found.isPresent());
        assertEquals("SOP-000001", found.get().getId());
    }

    @Test
    void findById_notFound_returnsEmpty() {
        assertTrue(repo.findById("GHOST").isEmpty());
    }

    @Test
    void findAll_returnsAllOps() {
        repo.save(makeOp("SOP-000001", ScheduledOperationStatus.PENDING, Instant.parse("2026-06-01T00:00:00Z")));
        repo.save(makeOp("SOP-000002", ScheduledOperationStatus.CANCELLED, Instant.parse("2026-06-02T00:00:00Z")));

        List<OperacionProgramada> all = toList(repo.findAll());
        assertEquals(2, all.size());
    }

    @Test
    void findPendingInPriorityOrder_onlyPending_orderedByScheduledAtAsc() {
        Instant t1 = Instant.parse("2026-06-03T00:00:00Z");
        Instant t2 = Instant.parse("2026-06-01T00:00:00Z");
        Instant t3 = Instant.parse("2026-06-02T00:00:00Z");

        repo.save(makeOp("SOP-000001", ScheduledOperationStatus.PENDING, t1));
        repo.save(makeOp("SOP-000002", ScheduledOperationStatus.CANCELLED, t2));
        repo.save(makeOp("SOP-000003", ScheduledOperationStatus.PENDING, t3));

        List<OperacionProgramada> pending = toList(repo.findPendingInPriorityOrder());
        assertEquals(2, pending.size());
        // t3 < t1 => SOP-000003 first
        assertEquals("SOP-000003", pending.get(0).getId());
        assertEquals("SOP-000001", pending.get(1).getId());
    }

    @Test
    void findPendingInPriorityOrder_isNonDestructive() {
        repo.save(makeOp("SOP-000001", ScheduledOperationStatus.PENDING, Instant.parse("2026-06-01T00:00:00Z")));

        List<OperacionProgramada> first = toList(repo.findPendingInPriorityOrder());
        List<OperacionProgramada> second = toList(repo.findPendingInPriorityOrder());

        assertEquals(1, first.size());
        assertEquals(1, second.size());
        assertEquals(first.get(0).getId(), second.get(0).getId());
    }

    @Test
    void save_upsert_updatesExisting() {
        OperacionProgramada op = makeOp("SOP-000001", ScheduledOperationStatus.PENDING,
                Instant.parse("2026-06-01T00:00:00Z"));
        repo.save(op);
        op.markCancelled();
        repo.save(op);

        Optional<OperacionProgramada> found = repo.findById("SOP-000001");
        assertTrue(found.isPresent());
        assertEquals(ScheduledOperationStatus.CANCELLED, found.get().getStatus());

        // Cancelled op should NOT appear in pending list
        List<OperacionProgramada> pending = toList(repo.findPendingInPriorityOrder());
        assertEquals(0, pending.size());
    }

    private <T> List<T> toList(Iterable<T> iterable) {
        List<T> list = new ArrayList<>();
        for (T item : iterable) {
            list.add(item);
        }
        return list;
    }
}
