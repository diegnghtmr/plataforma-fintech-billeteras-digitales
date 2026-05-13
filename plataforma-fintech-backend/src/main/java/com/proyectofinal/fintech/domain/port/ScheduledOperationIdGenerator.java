package com.proyectofinal.fintech.domain.port;

/**
 * Output port for generating unique scheduled operation identifiers.
 * ZERO Spring/Jakarta imports — pure domain interface.
 */
public interface ScheduledOperationIdGenerator {

    /**
     * Returns the next unique scheduled operation ID.
     * Format: SOP-{n} where n is a zero-padded 6-digit integer (e.g. SOP-000001).
     *
     * @return a unique, never-null scheduled operation ID
     */
    String next();
}
