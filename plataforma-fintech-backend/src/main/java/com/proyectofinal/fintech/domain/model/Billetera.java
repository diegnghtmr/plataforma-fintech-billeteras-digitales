package com.proyectofinal.fintech.domain.model;

import java.time.Instant;

/**
 * Domain entity representing a digital wallet.
 * Plain POJO — ZERO Spring/Jakarta imports.
 * Balance and transactionCount are mutable for SDD 5+.
 */
public class Billetera {

    private final String code;
    private final String name;
    private final String type;
    private final String ownerId;
    private double balance;
    private boolean active;
    private final Instant createdAt;
    private int transactionCount;

    public Billetera(String code, String name, String type, String ownerId,
                     double balance, boolean active, Instant createdAt, int transactionCount) {
        this.code = code;
        this.name = name;
        this.type = type;
        this.ownerId = ownerId;
        this.balance = balance;
        this.active = active;
        this.createdAt = createdAt;
        this.transactionCount = transactionCount;
    }

    public String getCode() { return code; }
    public String getName() { return name; }
    public String getType() { return type; }
    public String getOwnerId() { return ownerId; }
    public double getBalance() { return balance; }
    public boolean isActive() { return active; }
    public Instant getCreatedAt() { return createdAt; }
    public int getTransactionCount() { return transactionCount; }

    public void setBalance(double balance) { this.balance = balance; }
    public void setActive(boolean active) { this.active = active; }
    public void setTransactionCount(int transactionCount) { this.transactionCount = transactionCount; }
}
