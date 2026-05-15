package com.proyectofinal.fintech.domain.port;

/**
 * Output port for generating unique Beneficio IDs.
 * Format: BEN-{n} where n is a zero-padded 6-digit integer.
 * ZERO Spring/Jakarta imports.
 */
public interface BeneficioIdGenerator {
    String next();
}
