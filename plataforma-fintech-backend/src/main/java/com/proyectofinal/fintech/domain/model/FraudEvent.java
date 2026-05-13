package com.proyectofinal.fintech.domain.model;

import java.time.Instant;

/**
 * Domain entity representing a detected fraud event.
 * Plain POJO — ZERO Spring/Jakarta imports.
 * All 7 fields are set at construction and are immutable.
 */
public class FraudEvent {

    private final String id;
    private final String userId;
    private final String transactionId;   // nullable
    private final String type;
    private final FraudSeverity severity;
    private final String description;
    private final Instant createdAt;

    public FraudEvent(String id, String userId, String transactionId,
                      String type, FraudSeverity severity,
                      String description, Instant createdAt) {
        this.id = id;
        this.userId = userId;
        this.transactionId = transactionId;
        this.type = type;
        this.severity = severity;
        this.description = description;
        this.createdAt = createdAt;
    }

    public String getId() { return id; }
    public String getUserId() { return userId; }
    public String getTransactionId() { return transactionId; }
    public String getType() { return type; }
    public FraudSeverity getSeverity() { return severity; }
    public String getDescription() { return description; }
    public Instant getCreatedAt() { return createdAt; }
}
