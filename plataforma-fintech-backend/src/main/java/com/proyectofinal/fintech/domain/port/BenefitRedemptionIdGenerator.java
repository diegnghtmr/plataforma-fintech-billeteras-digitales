package com.proyectofinal.fintech.domain.port;

/**
 * Output port for generating unique BenefitRedemption IDs.
 * Format: RED-{n} where n is a zero-padded 6-digit integer.
 * ZERO Spring/Jakarta imports.
 */
public interface BenefitRedemptionIdGenerator {
    String next();
}
