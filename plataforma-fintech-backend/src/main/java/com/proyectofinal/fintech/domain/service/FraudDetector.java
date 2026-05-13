package com.proyectofinal.fintech.domain.service;

import com.proyectofinal.fintech.domain.model.FraudEvent;
import com.proyectofinal.fintech.domain.model.FraudSeverity;
import com.proyectofinal.fintech.domain.model.Transaccion;
import com.proyectofinal.fintech.domain.model.TransactionStatus;
import com.proyectofinal.fintech.domain.port.FraudEventIdGenerator;
import com.proyectofinal.fintech.domain.port.TransactionRepository;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

/**
 * Domain service for detecting fraud in transactions.
 * ADR-11.4: Refactored from static utility to injectable instance class.
 * ADR-11.5: FraudEventIdGenerator moved to constructor so callers only pass the transaction.
 * ZERO Spring/Jakarta imports — constructor injection only.
 *
 * Rules (evaluated in order — first match wins):
 *   Rule A: amount > 10000 → HIGH LARGE_TRANSACTION
 *   Rule B (velocity): ≥3 SUCCESSFUL tx for same sourceUserId within 60s before tx.timestamp,
 *                       excluding tx itself (by id) → HIGH HIGH_VELOCITY
 */
public class FraudDetector {

    private static final double LARGE_TRANSACTION_THRESHOLD = 10_000.0;
    private static final long VELOCITY_WINDOW_SECONDS = 60L;
    private static final int VELOCITY_THRESHOLD = 3;

    private final TransactionRepository txRepo;
    private final FraudEventIdGenerator idGen;
    private final Clock clock;

    public FraudDetector(TransactionRepository txRepo, FraudEventIdGenerator idGen, Clock clock) {
        this.txRepo = txRepo;
        this.idGen = idGen;
        this.clock = clock;
    }

    /**
     * Evaluates a transaction for potential fraud.
     * Must be called AFTER txRepo.save(tx) so that velocity count is correct.
     *
     * @param tx the transaction to check (already persisted)
     * @return Optional containing a FraudEvent if fraud detected, empty otherwise
     */
    public Optional<FraudEvent> detect(Transaccion tx) {
        // Rule A: large transaction
        if (tx.getAmount() > LARGE_TRANSACTION_THRESHOLD) {
            return Optional.of(new FraudEvent(
                    idGen.next(),
                    tx.getSourceUserId(),
                    tx.getId(),
                    "LARGE_TRANSACTION",
                    FraudSeverity.HIGH,
                    "Transacción de monto inusual: " + tx.getAmount(),
                    Instant.now(clock)
            ));
        }

        // Rule B: velocity — count SUCCESSFUL for same user within 60s, excluding this tx
        Instant windowStart = tx.getTimestamp().minusSeconds(VELOCITY_WINDOW_SECONDS);
        int recentCount = 0;
        for (Transaccion t : txRepo.findByUserId(tx.getSourceUserId())) {
            if (t.getId().equals(tx.getId())) continue;                      // exclude own tx
            if (t.getStatus() != TransactionStatus.SUCCESSFUL) continue;      // only successful
            if (t.getTimestamp().isBefore(windowStart)) continue;             // outside window
            if (t.getTimestamp().isAfter(tx.getTimestamp())) continue;        // after current tx
            recentCount++;
        }

        if (recentCount >= VELOCITY_THRESHOLD) {
            return Optional.of(new FraudEvent(
                    idGen.next(),
                    tx.getSourceUserId(),
                    tx.getId(),
                    "HIGH_VELOCITY",
                    FraudSeverity.HIGH,
                    "Alta velocidad de transacciones: " + recentCount + " en 60s",
                    Instant.now(clock)
            ));
        }

        return Optional.empty();
    }
}
