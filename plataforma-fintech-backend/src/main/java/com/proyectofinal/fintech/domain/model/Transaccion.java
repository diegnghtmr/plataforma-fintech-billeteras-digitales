package com.proyectofinal.fintech.domain.model;

import java.time.Instant;

/**
 * Domain entity representing a financial transaction.
 * Plain POJO — ZERO Spring/Jakarta imports.
 * Mutable fields: {@code status} (SDD 6 reversal) and {@code riskLevel} (SDD 11 fraud).
 * All other fields are set at construction and MUST NOT change after creation.
 * ADR-11.3: riskLevel defaults to LOW; NO constructor change to avoid cascading impact.
 */
public class Transaccion {

    private final String id;
    private final Instant timestamp;
    private final TransactionType type;
    private final double amount;
    private final String sourceWalletId;
    private final String targetWalletId;     // nullable
    private final String sourceUserId;
    private final String targetUserId;       // nullable
    private TransactionStatus status;        // mutable — SDD 6 may flip to REVERSED
    private final double pointsGenerated;
    private final String description;        // nullable
    private final boolean reversible;
    private FraudSeverity riskLevel = FraudSeverity.LOW;  // mutable — SDD 11 fraud detection

    public Transaccion(String id, Instant timestamp, TransactionType type, double amount,
                       String sourceWalletId, String targetWalletId,
                       String sourceUserId, String targetUserId,
                       TransactionStatus status, double pointsGenerated,
                       String description, boolean reversible) {
        this.id = id;
        this.timestamp = timestamp;
        this.type = type;
        this.amount = amount;
        this.sourceWalletId = sourceWalletId;
        this.targetWalletId = targetWalletId;
        this.sourceUserId = sourceUserId;
        this.targetUserId = targetUserId;
        this.status = status;
        this.pointsGenerated = pointsGenerated;
        this.description = description;
        this.reversible = reversible;
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public String getId() { return id; }
    public Instant getTimestamp() { return timestamp; }
    public TransactionType getType() { return type; }
    public double getAmount() { return amount; }
    public String getSourceWalletId() { return sourceWalletId; }
    public String getTargetWalletId() { return targetWalletId; }
    public String getSourceUserId() { return sourceUserId; }
    public String getTargetUserId() { return targetUserId; }
    public TransactionStatus getStatus() { return status; }
    public double getPointsGenerated() { return pointsGenerated; }
    public String getDescription() { return description; }
    public boolean isReversible() { return reversible; }
    public FraudSeverity getRiskLevel() { return riskLevel; }

    // ── Mutation ──────────────────────────────────────────────────────────────

    /**
     * Mutates only the {@code status} field.
     * Used by SDD 6 (reversal) to flip status to REVERSED.
     *
     * @param status the new status
     */
    public void setStatus(TransactionStatus status) {
        this.status = status;
    }

    /**
     * Mutates the {@code riskLevel} field.
     * Called by SDD 11 fraud detection after persist + detect cycle.
     * ADR-11.3: separate mutator instead of constructor parameter to avoid cascading.
     *
     * @param severity the fraud severity level
     */
    public void markRiskLevel(FraudSeverity severity) {
        this.riskLevel = severity;
    }
}
