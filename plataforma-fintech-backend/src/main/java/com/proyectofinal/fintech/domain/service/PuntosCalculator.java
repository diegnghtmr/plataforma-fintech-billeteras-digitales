package com.proyectofinal.fintech.domain.service;

import com.proyectofinal.fintech.domain.model.TransactionType;

/**
 * Domain service for points calculation.
 * Pure utility — static, final, no Spring annotations.
 * ADR-11.1: PDF-compliant formula: floor(amount / 100) * rate
 * Rates: RECHARGE→1, WITHDRAWAL→2, INTERNAL_TRANSFER→3, EXTERNAL_TRANSFER_SENT→3, RECEIVED→0
 * Guard: amount < 100 or negative → 0.
 */
public final class PuntosCalculator {

    /** Bonus points awarded when an ExecuteDueScheduledOperationsUseCase completes a run. ADR-11.10 */
    public static final double SCHEDULED_EXECUTION_BONUS = 5.0;

    private PuntosCalculator() {
        // utility class — no instantiation
    }

    /**
     * Computes loyalty points earned for a given transaction type and amount.
     * Formula: {@code floor(amount / 100) * rate}
     *
     * @param type   the type of the transaction
     * @param amount the monetary amount of the transaction
     * @return points earned (never negative)
     */
    public static double compute(TransactionType type, double amount) {
        if (amount < 100.0) {
            return 0.0;
        }
        long floorUnits = (long) (amount / 100.0);
        return switch (type) {
            case RECHARGE                   -> floorUnits * 1.0;
            case WITHDRAWAL                 -> floorUnits * 2.0;
            case INTERNAL_TRANSFER          -> floorUnits * 3.0;
            case EXTERNAL_TRANSFER_SENT     -> floorUnits * 3.0;
            case EXTERNAL_TRANSFER_RECEIVED -> 0.0;
        };
    }
}
