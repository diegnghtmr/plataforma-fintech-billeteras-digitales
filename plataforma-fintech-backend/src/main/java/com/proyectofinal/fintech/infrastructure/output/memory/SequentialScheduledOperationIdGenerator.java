package com.proyectofinal.fintech.infrastructure.output.memory;

import com.proyectofinal.fintech.domain.port.ScheduledOperationIdGenerator;

import java.util.concurrent.atomic.AtomicLong;

/**
 * In-memory adapter for ScheduledOperationIdGenerator.
 * Produces IDs in format "SOP-000001", "SOP-000002", etc.
 */
public class SequentialScheduledOperationIdGenerator implements ScheduledOperationIdGenerator {

    private final AtomicLong counter = new AtomicLong(0);

    @Override
    public String next() {
        long id = counter.incrementAndGet();
        return String.format("SOP-%06d", id);
    }
}
