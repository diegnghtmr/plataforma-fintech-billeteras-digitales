package com.proyectofinal.fintech.domain.port;

/**
 * Output port for generating unique notification identifiers.
 * ZERO Spring/Jakarta imports — pure domain interface.
 */
public interface NotificationIdGenerator {

    /**
     * Returns the next unique notification ID.
     * Format: NTF-{n} where n is a zero-padded 6-digit integer (e.g. NTF-000001).
     *
     * @return a unique, never-null notification ID
     */
    String next();
}
