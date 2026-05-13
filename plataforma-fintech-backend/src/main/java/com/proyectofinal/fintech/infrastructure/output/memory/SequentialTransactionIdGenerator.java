package com.proyectofinal.fintech.infrastructure.output.memory;

import com.proyectofinal.fintech.domain.port.TransactionIdGenerator;

import java.util.concurrent.atomic.AtomicLong;

/**
 * In-memory adapter for TransactionIdGenerator.
 * Produces IDs in format "TX-000001", "TX-000002", etc., using AtomicLong for thread safety.
 * ADR-5.4: zero-padded format for sort stability.
 */
public class SequentialTransactionIdGenerator implements TransactionIdGenerator {

    private final AtomicLong counter = new AtomicLong(0);

    @Override
    public String next() {
        long id = counter.incrementAndGet();
        return String.format("TX-%06d", id);
    }
}
