package com.proyectofinal.fintech.domain.port;

/**
 * Output port for generating unique transaction identifiers.
 * ZERO Spring/Jakarta imports — pure domain interface.
 */
public interface TransactionIdGenerator {

    /**
     * Returns the next unique transaction ID.
     * Format: TX-{n} where n is a zero-padded 6-digit integer (e.g. TX-000001).
     *
     * @return a unique, never-null transaction ID
     */
    String next();
}
