package com.proyectofinal.fintech.infrastructure.output.memory;

import com.proyectofinal.fintech.domain.port.FraudEventIdGenerator;

import java.util.concurrent.atomic.AtomicLong;

/**
 * In-memory implementation of FraudEventIdGenerator.
 * Produces IDs in the format FRD-000001 using an AtomicLong counter.
 * Thread-safe.
 */
public class SequentialFraudEventIdGenerator implements FraudEventIdGenerator {

    private final AtomicLong counter = new AtomicLong(0);

    @Override
    public String next() {
        return String.format("FRD-%06d", counter.incrementAndGet());
    }
}
