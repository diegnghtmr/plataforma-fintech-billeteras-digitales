package com.proyectofinal.fintech.infrastructure.output.memory;

import com.proyectofinal.fintech.domain.port.BenefitRedemptionIdGenerator;

import java.util.concurrent.atomic.AtomicLong;

/**
 * In-memory adapter for BenefitRedemptionIdGenerator.
 * Produces IDs in format "RED-000001", "RED-000002", etc.
 */
public class SequentialBenefitRedemptionIdGenerator implements BenefitRedemptionIdGenerator {

    private final AtomicLong counter = new AtomicLong(0);

    @Override
    public String next() {
        long id = counter.incrementAndGet();
        return String.format("RED-%06d", id);
    }
}
