package com.proyectofinal.fintech.infrastructure.output.memory;

import com.proyectofinal.fintech.domain.port.BeneficioIdGenerator;

import java.util.concurrent.atomic.AtomicLong;

/**
 * In-memory adapter for BeneficioIdGenerator.
 * Produces IDs in format "BEN-000001", "BEN-000002", etc.
 */
public class SequentialBeneficioIdGenerator implements BeneficioIdGenerator {

    private final AtomicLong counter = new AtomicLong(0);

    @Override
    public String next() {
        long id = counter.incrementAndGet();
        return String.format("BEN-%06d", id);
    }
}
