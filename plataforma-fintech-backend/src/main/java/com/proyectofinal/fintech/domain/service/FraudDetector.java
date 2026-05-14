package com.proyectofinal.fintech.domain.service;

import com.proyectofinal.fintech.domain.model.FraudEvent;
import com.proyectofinal.fintech.domain.model.FraudSeverity;
import com.proyectofinal.fintech.domain.model.FraudType;
import com.proyectofinal.fintech.domain.model.Transaccion;
import com.proyectofinal.fintech.domain.model.TransactionStatus;
import com.proyectofinal.fintech.domain.port.FraudEventIdGenerator;
import com.proyectofinal.fintech.domain.port.TransactionRepository;
import com.proyectofinal.fintech.domain.structures.MiLista;
import com.proyectofinal.fintech.domain.structures.TablaHash;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.HashSet;
import java.util.Optional;

/**
 * Domain service for detecting fraud in transactions.
 * ADR-11.4: Refactored from static utility to injectable instance class.
 * ADR-11.5: FraudEventIdGenerator moved to constructor so callers only pass the transaction.
 * ZERO Spring/Jakarta imports — constructor injection only.
 *
 * Rules (evaluated in order — first match wins, ADR-7.1):
 *   Rule A: amount > 10000 → HIGH LARGE_TRANSACTION
 *   Rule B (velocity): ≥3 SUCCESSFUL tx for same sourceUserId within 60s → HIGH HIGH_VELOCITY
 *   Rule C: ≥3 transfers to same target (user+wallet) within 5min → HIGH REPEATED_DESTINATION
 *   Rule D: ≥3 distinct source wallets within 2min AND totalAmount > 5000 → HIGH WALLET_FRAGMENTATION
 *   Rule E: history ≥10 AND last-1h count ≥ 5× avg hourly rate → MEDIUM FREQUENCY_BURST
 *   Rule F: history ≥20 AND current tx hour not in seen hours → LOW OFF_HOURS
 */
public class FraudDetector {

    private static final double LARGE_TRANSACTION_THRESHOLD = 10_000.0;
    private static final long VELOCITY_WINDOW_SECONDS = 60L;
    private static final int VELOCITY_THRESHOLD = 3;
    private static final long REPEATED_DEST_WINDOW_SECONDS = 300L;   // 5 min
    private static final int REPEATED_DEST_THRESHOLD = 3;
    private static final long FRAGMENTATION_WINDOW_SECONDS = 120L;   // 2 min
    private static final int FRAGMENTATION_WALLET_THRESHOLD = 3;
    private static final double FRAGMENTATION_AMOUNT_THRESHOLD = 5_000.0;
    private static final int FREQ_SPIKE_MIN_HISTORY = 10;
    private static final int FREQ_SPIKE_MULTIPLIER = 5;
    private static final int OFF_HOURS_MIN_HISTORY = 20;

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
     * History is computed once and passed to all helpers (avoids 5× repo scan).
     *
     * @param tx the transaction to check (already persisted)
     * @return Optional containing a FraudEvent if fraud detected, empty otherwise
     */
    public Optional<FraudEvent> detect(Transaccion tx) {
        // Rule A: large transaction (no history needed)
        Optional<FraudEvent> ruleA = checkLarge(tx);
        if (ruleA.isPresent()) return ruleA;

        // Compute history once — passed to all remaining helpers
        MiLista<Transaccion> hist = buildHistory(tx);

        // Rule B: velocity
        Optional<FraudEvent> ruleB = checkVelocity(tx, hist);
        if (ruleB.isPresent()) return ruleB;

        // Rule C: repeated destination
        Optional<FraudEvent> ruleC = checkRepeatedDestination(tx, hist);
        if (ruleC.isPresent()) return ruleC;

        // Rule D: amount fragmentation
        Optional<FraudEvent> ruleD = checkAmountFragmentation(tx, hist);
        if (ruleD.isPresent()) return ruleD;

        // Rule E: frequency spike
        Optional<FraudEvent> ruleE = checkFrequencySpike(tx, hist);
        if (ruleE.isPresent()) return ruleE;

        // Rule F: unusual hours
        Optional<FraudEvent> ruleF = checkUnusualHours(tx, hist);
        if (ruleF.isPresent()) return ruleF;

        return Optional.empty();
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private MiLista<Transaccion> buildHistory(Transaccion tx) {
        MiLista<Transaccion> hist = new MiLista<>();
        for (Transaccion t : txRepo.findByUserId(tx.getSourceUserId())) {
            if (!t.getId().equals(tx.getId())) {
                hist.add(t);
            }
        }
        return hist;
    }

    private Optional<FraudEvent> checkLarge(Transaccion tx) {
        if (tx.getAmount() > LARGE_TRANSACTION_THRESHOLD) {
            return Optional.of(new FraudEvent(
                    idGen.next(),
                    tx.getSourceUserId(),
                    tx.getId(),
                    FraudType.LARGE_TRANSACTION,
                    FraudSeverity.HIGH,
                    "Transacción de monto inusual: " + tx.getAmount(),
                    Instant.now(clock)
            ));
        }
        return Optional.empty();
    }

    private Optional<FraudEvent> checkVelocity(Transaccion tx, MiLista<Transaccion> hist) {
        Instant windowStart = tx.getTimestamp().minusSeconds(VELOCITY_WINDOW_SECONDS);
        int recentCount = 0;
        for (Transaccion t : hist) {
            if (t.getStatus() != TransactionStatus.SUCCESSFUL) continue;
            if (t.getTimestamp().isBefore(windowStart)) continue;
            if (t.getTimestamp().isAfter(tx.getTimestamp())) continue;
            recentCount++;
        }
        if (recentCount >= VELOCITY_THRESHOLD) {
            return Optional.of(new FraudEvent(
                    idGen.next(),
                    tx.getSourceUserId(),
                    tx.getId(),
                    FraudType.HIGH_VELOCITY,
                    FraudSeverity.HIGH,
                    "Alta velocidad de transacciones: " + recentCount + " en 60s",
                    Instant.now(clock)
            ));
        }
        return Optional.empty();
    }

    /**
     * Rule C — REPEATED_DESTINATION.
     * Counts history transfers to same (targetUserId + targetWalletId) within [now-5min, now).
     * If count (from history) + 1 (current tx itself) >= 3, emits HIGH.
     * REQ-1.2: HIGH severity per spec.
     */
    private Optional<FraudEvent> checkRepeatedDestination(Transaccion tx, MiLista<Transaccion> hist) {
        if (tx.getTargetUserId() == null || tx.getTargetWalletId() == null) {
            return Optional.empty();
        }
        String destKey = tx.getTargetUserId() + "#" + tx.getTargetWalletId();
        Instant windowStart = tx.getTimestamp().minusSeconds(REPEATED_DEST_WINDOW_SECONDS);

        TablaHash<String, Integer> destCounts = new TablaHash<>();
        for (Transaccion t : hist) {
            if (t.getStatus() != TransactionStatus.SUCCESSFUL) continue;
            if (t.getTimestamp().isBefore(windowStart)) continue;
            if (t.getTimestamp().isAfter(tx.getTimestamp())) continue;
            if (t.getTargetUserId() == null || t.getTargetWalletId() == null) continue;
            String key = t.getTargetUserId() + "#" + t.getTargetWalletId();
            int count = destCounts.get(key).orElse(0);
            destCounts.put(key, count + 1);
        }

        int histCount = destCounts.get(destKey).orElse(0);
        int totalIncludingCurrent = histCount + 1; // +1 for the current tx
        if (totalIncludingCurrent >= REPEATED_DEST_THRESHOLD) {
            // REQ-1.2: HIGH severity per spec
            return Optional.of(new FraudEvent(
                    idGen.next(),
                    tx.getSourceUserId(),
                    tx.getId(),
                    FraudType.REPEATED_DESTINATION,
                    FraudSeverity.HIGH, // REQ-1.2: HIGH severity per spec
                    "Transferencias repetidas al mismo destino en 5 minutos: " + totalIncludingCurrent,
                    Instant.now(clock)
            ));
        }
        return Optional.empty();
    }

    /**
     * Rule D — WALLET_FRAGMENTATION.
     * Tracks distinct source wallets + combined amount within last 2min.
     * Emits HIGH if distinctWallets >= 3 AND totalAmount > 5000.
     */
    private Optional<FraudEvent> checkAmountFragmentation(Transaccion tx, MiLista<Transaccion> hist) {
        Instant windowStart = tx.getTimestamp().minusSeconds(FRAGMENTATION_WINDOW_SECONDS);
        TablaHash<String, BigDecimal> walletAmounts = new TablaHash<>();

        for (Transaccion t : hist) {
            if (t.getStatus() != TransactionStatus.SUCCESSFUL) continue;
            if (t.getTimestamp().isBefore(windowStart)) continue;
            if (t.getTimestamp().isAfter(tx.getTimestamp())) continue;
            String walletKey = t.getSourceWalletId();
            BigDecimal prev = walletAmounts.get(walletKey).orElse(BigDecimal.ZERO);
            walletAmounts.put(walletKey, prev.add(BigDecimal.valueOf(t.getAmount())));
        }

        // Include current tx wallet
        String txWallet = tx.getSourceWalletId();
        BigDecimal prev = walletAmounts.get(txWallet).orElse(BigDecimal.ZERO);
        walletAmounts.put(txWallet, prev.add(BigDecimal.valueOf(tx.getAmount())));

        // Count distinct wallets and total amount
        int distinctWallets = 0;
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (BigDecimal amt : walletAmounts.values()) {
            distinctWallets++;
            totalAmount = totalAmount.add(amt);
        }

        if (distinctWallets >= FRAGMENTATION_WALLET_THRESHOLD
                && totalAmount.compareTo(BigDecimal.valueOf(FRAGMENTATION_AMOUNT_THRESHOLD)) > 0) {
            return Optional.of(new FraudEvent(
                    idGen.next(),
                    tx.getSourceUserId(),
                    tx.getId(),
                    FraudType.WALLET_FRAGMENTATION,
                    FraudSeverity.HIGH,
                    "Fragmentación de monto desde " + distinctWallets + " billeteras, total: " + totalAmount,
                    Instant.now(clock)
            ));
        }
        return Optional.empty();
    }

    /**
     * Rule E — FREQUENCY_BURST.
     * Guard: history < 10 → skip.
     * Computes avg hourly rate from full history, counts last-1h transactions.
     * Emits MEDIUM if last1h >= 5 × avgPerHour.
     */
    private Optional<FraudEvent> checkFrequencySpike(Transaccion tx, MiLista<Transaccion> hist) {
        int histSize = hist.size();
        if (histSize < FREQ_SPIKE_MIN_HISTORY) return Optional.empty();

        // Find first tx timestamp in history
        Instant earliest = tx.getTimestamp();
        for (Transaccion t : hist) {
            if (t.getTimestamp().isBefore(earliest)) {
                earliest = t.getTimestamp();
            }
        }

        long totalSeconds = tx.getTimestamp().getEpochSecond() - earliest.getEpochSecond();
        double hoursTotal = Math.max(1.0, totalSeconds / 3600.0);
        double avgPerHour = histSize / hoursTotal;

        // Count txs in last 1 hour
        Instant oneHourAgo = tx.getTimestamp().minusSeconds(3600L);
        int last1h = 0;
        for (Transaccion t : hist) {
            if (!t.getTimestamp().isBefore(oneHourAgo) && !t.getTimestamp().isAfter(tx.getTimestamp())) {
                last1h++;
            }
        }

        if (last1h >= FREQ_SPIKE_MULTIPLIER * avgPerHour) {
            return Optional.of(new FraudEvent(
                    idGen.next(),
                    tx.getSourceUserId(),
                    tx.getId(),
                    FraudType.FREQUENCY_BURST,
                    FraudSeverity.MEDIUM,
                    "Pico de frecuencia: " + last1h + " tx en última hora vs promedio " + String.format("%.2f", avgPerHour) + "/h",
                    Instant.now(clock)
            ));
        }
        return Optional.empty();
    }

    /**
     * Rule F — OFF_HOURS.
     * Guard: history < 20 → skip.
     * Builds set of seen hours from history. If current tx hour not in set → emits LOW.
     * HashSet<Integer> is a tolerated JDK boundary use — no own Conjunto exists.
     */
    private Optional<FraudEvent> checkUnusualHours(Transaccion tx, MiLista<Transaccion> hist) {
        if (hist.size() < OFF_HOURS_MIN_HISTORY) return Optional.empty();

        // HashSet<Integer> is a tolerated JDK boundary use — no own Conjunto exists
        HashSet<Integer> hoursSeen = new HashSet<>();
        for (Transaccion t : hist) {
            hoursSeen.add(t.getTimestamp().atZone(java.time.ZoneOffset.UTC).getHour());
        }

        int currentHour = tx.getTimestamp().atZone(java.time.ZoneOffset.UTC).getHour();
        if (!hoursSeen.contains(currentHour)) {
            return Optional.of(new FraudEvent(
                    idGen.next(),
                    tx.getSourceUserId(),
                    tx.getId(),
                    FraudType.OFF_HOURS,
                    FraudSeverity.LOW,
                    "Transacción en horario inusual " + String.format("%02d:00", currentHour),
                    Instant.now(clock)
            ));
        }
        return Optional.empty();
    }
}
