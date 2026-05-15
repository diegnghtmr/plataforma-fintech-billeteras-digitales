package com.proyectofinal.fintech.domain.model;

import java.time.Instant;

/**
 * Domain entity representing a scheduled financial operation.
 * Plain POJO — ZERO Spring/Jakarta imports.
 * Implements Comparable for ColaPrioridad ordering: scheduledAt ASC, id ASC tie-break.
 */
public class OperacionProgramada implements Comparable<OperacionProgramada> {

    private final String id;
    private final ScheduledOperationType type;
    private ScheduledOperationStatus status;
    private final String sourceUserId;
    private final String sourceWalletId;
    private final String targetUserId;       // nullable
    private final String targetWalletId;     // nullable
    private final double amount;
    private final Instant scheduledAt;
    private final String description;        // nullable
    private final RecurrenceType recurrence;

    /**
     * Full constructor with recurrence.
     * @throws IllegalArgumentException if recurrence is null
     */
    public OperacionProgramada(String id, ScheduledOperationType type, ScheduledOperationStatus status,
                                String sourceUserId, String sourceWalletId,
                                String targetUserId, String targetWalletId,
                                double amount, Instant scheduledAt, String description,
                                RecurrenceType recurrence) {
        if (recurrence == null) {
            throw new IllegalArgumentException("invalid_recurrence");
        }
        this.id = id;
        this.type = type;
        this.status = status;
        this.sourceUserId = sourceUserId;
        this.sourceWalletId = sourceWalletId;
        this.targetUserId = targetUserId;
        this.targetWalletId = targetWalletId;
        this.amount = amount;
        this.scheduledAt = scheduledAt;
        this.description = description;
        this.recurrence = recurrence;
    }

    /**
     * Backward-compatible constructor — defaults recurrence to NONE.
     */
    public OperacionProgramada(String id, ScheduledOperationType type, ScheduledOperationStatus status,
                                String sourceUserId, String sourceWalletId,
                                String targetUserId, String targetWalletId,
                                double amount, Instant scheduledAt, String description) {
        this(id, type, status, sourceUserId, sourceWalletId, targetUserId, targetWalletId,
                amount, scheduledAt, description, RecurrenceType.NONE);
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public String getId() { return id; }
    public ScheduledOperationType getType() { return type; }
    public ScheduledOperationStatus getStatus() { return status; }
    public String getSourceUserId() { return sourceUserId; }
    public String getSourceWalletId() { return sourceWalletId; }
    public String getTargetUserId() { return targetUserId; }
    public String getTargetWalletId() { return targetWalletId; }
    public double getAmount() { return amount; }
    public Instant getScheduledAt() { return scheduledAt; }
    public String getDescription() { return description; }
    public RecurrenceType getRecurrence() { return recurrence; }

    // ── Status mutations ─────────────────────────────────────────────────────

    /** Transitions status to EXECUTED. */
    public void markExecuted() {
        this.status = ScheduledOperationStatus.EXECUTED;
    }

    /** Transitions status to CANCELLED. */
    public void markCancelled() {
        this.status = ScheduledOperationStatus.CANCELLED;
    }

    /** Transitions status to FAILED. */
    public void markFailed() {
        this.status = ScheduledOperationStatus.FAILED;
    }

    // ── Comparable: scheduledAt ASC, id ASC tie-break ────────────────────────

    @Override
    public int compareTo(OperacionProgramada other) {
        int cmp = this.scheduledAt.compareTo(other.scheduledAt);
        if (cmp != 0) {
            return cmp;
        }
        return this.id.compareTo(other.id);
    }
}
