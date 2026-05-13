package com.proyectofinal.fintech.domain.port;

/**
 * Output port: generates unique IDs for fraud events.
 * ZERO Spring/Jakarta imports — pure domain interface.
 */
public interface FraudEventIdGenerator {
    /** Returns the next unique fraud event ID. */
    String next();
}
