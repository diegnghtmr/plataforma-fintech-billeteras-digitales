package com.proyectofinal.fintech.domain.model;

/**
 * Compile-time constants for fraud event types.
 * ADR-7.2: String constants (not enum) to avoid breaking FraudEvent.type wire format.
 * ZERO Spring/Jakarta imports.
 */
public final class FraudType {

    private FraudType() {}

    public static final String LARGE_TRANSACTION    = "LARGE_TRANSACTION";
    public static final String HIGH_VELOCITY        = "HIGH_VELOCITY";
    public static final String CYCLE_DETECTED       = "CYCLE_DETECTED";
    public static final String REPEATED_DESTINATION = "REPEATED_DESTINATION";
    public static final String WALLET_FRAGMENTATION = "WALLET_FRAGMENTATION";
    public static final String FREQUENCY_BURST      = "FREQUENCY_BURST";
    public static final String OFF_HOURS            = "OFF_HOURS";
}
