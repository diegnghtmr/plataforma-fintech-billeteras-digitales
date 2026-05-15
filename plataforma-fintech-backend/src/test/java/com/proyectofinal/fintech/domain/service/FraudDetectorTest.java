package com.proyectofinal.fintech.domain.service;

import com.proyectofinal.fintech.domain.model.*;
import com.proyectofinal.fintech.domain.model.FraudType;
import com.proyectofinal.fintech.domain.port.FraudEventIdGenerator;
import com.proyectofinal.fintech.domain.port.TransactionRepository;
import com.proyectofinal.fintech.infrastructure.output.memory.SequentialFraudEventIdGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * T11-C01 (RED) — FraudDetector instance class tests.
 * Constructor: FraudDetector(TransactionRepository, FraudEventIdGenerator, Clock).
 * Rule A: amount > 10000 → HIGH LARGE_TRANSACTION.
 * Rule B (velocity): ≥3 SUCCESSFUL for same user within 60s (excluding own tx id) → HIGH_VELOCITY.
 * Rule A wins when both match.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class FraudDetectorTest {

    @Mock
    private TransactionRepository txRepo;

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");
    private Clock clock;
    private FraudDetector detector;

    @BeforeEach
    void setUp() {
        clock = Clock.fixed(NOW, ZoneId.of("UTC"));
        FraudEventIdGenerator idGen = new SequentialFraudEventIdGenerator();
        detector = new FraudDetector(txRepo, idGen, clock);
    }

    private Transaccion makeTx(String id, double amount, String userId) {
        return new Transaccion(
                id, NOW, TransactionType.RECHARGE, amount,
                "W001", null, userId, null,
                TransactionStatus.SUCCESSFUL, 0.0, null, true
        );
    }

    private Transaccion makeSuccessfulTx(String id, String userId, Instant ts) {
        return new Transaccion(
                id, ts, TransactionType.RECHARGE, 100.0,
                "W001", null, userId, null,
                TransactionStatus.SUCCESSFUL, 0.0, null, true
        );
    }

    // Rule A: amount > 10000 → HIGH LARGE_TRANSACTION
    @Test
    void detect_amountAboveThreshold_returnsHighLargeTransaction() {
        Transaccion tx = makeTx("TX-001", 10000.01, "USR001");
        when(txRepo.findByUserId("USR001")).thenReturn(List.of());

        Optional<FraudEvent> result = detector.detect(tx);

        assertThat(result).isPresent();
        FraudEvent event = result.get();
        assertThat(event.getSeverity()).isEqualTo(FraudSeverity.HIGH);
        assertThat(event.getType()).isEqualTo(FraudType.LARGE_TRANSACTION);
        assertThat(event.getUserId()).isEqualTo("USR001");
        assertThat(event.getTransactionId()).isEqualTo("TX-001");
    }

    // Rule A: exactly at threshold (10000) → no fraud
    @Test
    void detect_amountExactlyAtThreshold_returnsEmpty() {
        Transaccion tx = makeTx("TX-002", 10000.0, "USR001");
        when(txRepo.findByUserId("USR001")).thenReturn(List.of());

        Optional<FraudEvent> result = detector.detect(tx);
        assertThat(result).isEmpty();
    }

    // Rule B: 3 SUCCESSFUL transactions within 60s (excluding own tx) → HIGH_VELOCITY
    @Test
    void detect_velocityRule_threeRecentSuccessful_returnsHighVelocity() {
        Transaccion tx = makeTx("TX-NEW", 100.0, "USR001");

        // 3 previous SUCCESSFUL transactions within 60s
        Instant t1 = NOW.minusSeconds(10);
        Instant t2 = NOW.minusSeconds(20);
        Instant t3 = NOW.minusSeconds(30);
        List<Transaccion> existing = List.of(
                makeSuccessfulTx("TX-A", "USR001", t1),
                makeSuccessfulTx("TX-B", "USR001", t2),
                makeSuccessfulTx("TX-C", "USR001", t3)
        );
        when(txRepo.findByUserId("USR001")).thenReturn(existing);

        Optional<FraudEvent> result = detector.detect(tx);

        assertThat(result).isPresent();
        assertThat(result.get().getType()).isEqualTo(FraudType.HIGH_VELOCITY);
        assertThat(result.get().getSeverity()).isEqualTo(FraudSeverity.HIGH);
    }

    // Rule B: 2 prior + current = 3rd transaction → HIGH_VELOCITY (threshold=2 prior)
    @Test
    void detect_velocityRule_twoPriorPlusCurrent_returnsHighVelocity() {
        Transaccion tx = makeTx("TX-NEW", 100.0, "USR001");

        List<Transaccion> existing = List.of(
                makeSuccessfulTx("TX-A", "USR001", NOW.minusSeconds(10)),
                makeSuccessfulTx("TX-B", "USR001", NOW.minusSeconds(20))
        );
        when(txRepo.findByUserId("USR001")).thenReturn(existing);

        Optional<FraudEvent> result = detector.detect(tx);
        assertThat(result).isPresent();
        assertThat(result.get().getType()).isEqualTo(FraudType.HIGH_VELOCITY);
        assertThat(result.get().getSeverity()).isEqualTo(FraudSeverity.HIGH);
    }

    // Rule B: 1 prior + current = 2nd transaction → no velocity fraud
    @Test
    void detect_velocityRule_onePriorPlusCurrent_returnsEmpty() {
        Transaccion tx = makeTx("TX-NEW", 100.0, "USR001");

        List<Transaccion> existing = List.of(
                makeSuccessfulTx("TX-A", "USR001", NOW.minusSeconds(10))
        );
        when(txRepo.findByUserId("USR001")).thenReturn(existing);

        Optional<FraudEvent> result = detector.detect(tx);
        assertThat(result).isEmpty();
    }

    // Rule B: same id excluded (own tx should not count against itself)
    @Test
    void detect_velocityRule_excludesOwnTxId() {
        Transaccion tx = makeTx("TX-SELF", 100.0, "USR001");

        // If own id is included, would be 2 but it must be excluded → only 1 valid prior → no fraud
        List<Transaccion> existing = List.of(
                makeSuccessfulTx("TX-SELF", "USR001", NOW.minusSeconds(5)),  // same id — must be excluded
                makeSuccessfulTx("TX-A", "USR001", NOW.minusSeconds(10))
        );
        when(txRepo.findByUserId("USR001")).thenReturn(existing);

        // Only 1 valid (excluding self) → no fraud (threshold requires ≥2 prior)
        Optional<FraudEvent> result = detector.detect(tx);
        assertThat(result).isEmpty();
    }

    // When both rules match, Rule A wins (LARGE_TRANSACTION)
    @Test
    void detect_bothRulesMatch_ruleAWins() {
        Transaccion tx = makeTx("TX-BIG", 15000.0, "USR001");

        List<Transaccion> existing = List.of(
                makeSuccessfulTx("TX-A", "USR001", NOW.minusSeconds(10)),
                makeSuccessfulTx("TX-B", "USR001", NOW.minusSeconds(20)),
                makeSuccessfulTx("TX-C", "USR001", NOW.minusSeconds(30))
        );
        when(txRepo.findByUserId("USR001")).thenReturn(existing);

        Optional<FraudEvent> result = detector.detect(tx);
        assertThat(result).isPresent();
        assertThat(result.get().getType()).isEqualTo(FraudType.LARGE_TRANSACTION);
    }

    // Neither rule → empty
    @Test
    void detect_noRuleMatches_returnsEmpty() {
        Transaccion tx = makeTx("TX-NORMAL", 500.0, "USR001");
        when(txRepo.findByUserId("USR001")).thenReturn(List.of());

        Optional<FraudEvent> result = detector.detect(tx);
        assertThat(result).isEmpty();
    }

    // Helper: build a transfer tx with target user/wallet
    private Transaccion makeTransferTx(String id, String userId, String sourceWalletId,
                                        String targetUserId, String targetWalletId, Instant ts) {
        return new Transaccion(
                id, ts, TransactionType.EXTERNAL_TRANSFER_SENT, 100.0,
                sourceWalletId, targetWalletId, userId, targetUserId,
                TransactionStatus.SUCCESSFUL, 0.0, null, true
        );
    }

    // ─── Rule C: REPEATED_DESTINATION ────────────────────────────────────────

    // T1.2.1 — 3 transfers to same target within 5 min → REPEATED_DESTINATION HIGH
    @Test
    void detect_repeatedDestination_threeWithin5min_emitsHigh() {
        Instant t1 = NOW.minusSeconds(240); // 4 min ago
        Instant t2 = NOW.minusSeconds(120); // 2 min ago
        // current tx is NOW
        Transaccion tx = makeTransferTx("TX-NEW", "USR001", "W001", "USR002", "W002", NOW);

        List<Transaccion> hist = List.of(
                makeTransferTx("TX-A", "USR001", "W001", "USR002", "W002", t1),
                makeTransferTx("TX-B", "USR001", "W001", "USR002", "W002", t2)
        );
        when(txRepo.findByUserId("USR001")).thenReturn(hist);

        Optional<FraudEvent> result = detector.detect(tx);

        assertThat(result).isPresent();
        assertThat(result.get().getType()).isEqualTo(FraudType.REPEATED_DESTINATION);
        assertThat(result.get().getSeverity()).isEqualTo(FraudSeverity.HIGH);
    }

    // T1.2.2 — only 2 prior to same target + current to different → no REPEATED_DESTINATION
    @Test
    void detect_repeatedDestination_twoOnly_doesNotFire() {
        Transaccion tx = makeTransferTx("TX-NEW", "USR001", "W001", "USR003", "W003", NOW);

        List<Transaccion> hist = List.of(
                makeTransferTx("TX-A", "USR001", "W001", "USR002", "W002", NOW.minusSeconds(60)),
                makeTransferTx("TX-B", "USR001", "W001", "USR002", "W002", NOW.minusSeconds(120))
        );
        when(txRepo.findByUserId("USR001")).thenReturn(hist);

        Optional<FraudEvent> result = detector.detect(tx);
        assertThat(result).isEmpty();
    }

    // T1.2.3 — all 3 prior transfers to same target outside 5min window → no event
    @Test
    void detect_repeatedDestination_windowExpired_doesNotFire() {
        // All 3 prior transfers to same target are > 5 min ago (outside window)
        Transaccion tx = makeTransferTx("TX-NEW", "USR001", "W001", "USR002", "W002", NOW);

        List<Transaccion> hist = List.of(
                makeTransferTx("TX-A", "USR001", "W001", "USR002", "W002", NOW.minusSeconds(400)),
                makeTransferTx("TX-B", "USR001", "W001", "USR002", "W002", NOW.minusSeconds(360)),
                makeTransferTx("TX-C", "USR001", "W001", "USR002", "W002", NOW.minusSeconds(310))
        );
        when(txRepo.findByUserId("USR001")).thenReturn(hist);

        Optional<FraudEvent> result = detector.detect(tx);
        // 0 in window + current = 1 → below threshold of 3 → no event
        assertThat(result).isEmpty();
    }

    // T1.1.1 — FraudType all 7 constants defined
    @Test
    void fraudType_allSevenConstantsDefined() {
        assertThat(FraudType.LARGE_TRANSACTION).isNotNull().isNotEmpty();
        assertThat(FraudType.HIGH_VELOCITY).isNotNull().isNotEmpty();
        assertThat(FraudType.CYCLE_DETECTED).isNotNull().isNotEmpty();
        assertThat(FraudType.REPEATED_DESTINATION).isNotNull().isNotEmpty();
        assertThat(FraudType.WALLET_FRAGMENTATION).isNotNull().isNotEmpty();
        assertThat(FraudType.FREQUENCY_BURST).isNotNull().isNotEmpty();
        assertThat(FraudType.OFF_HOURS).isNotNull().isNotEmpty();
    }

    // ─── Rule D: WALLET_FRAGMENTATION ────────────────────────────────────────

    private Transaccion makeWalletTx(String id, String userId, String sourceWallet,
                                      double amount, Instant ts) {
        // Use unique targetWalletId per sourceWallet to avoid triggering Rule C
        return new Transaccion(id, ts, TransactionType.INTERNAL_TRANSFER, amount,
                sourceWallet, "TGT-" + sourceWallet, userId, "DIFFERENT-USER-" + id,
                TransactionStatus.SUCCESSFUL, 0.0, null, true);
    }

    // T1.3.1 — 3 distinct source wallets, combined > 5000 → WALLET_FRAGMENTATION HIGH
    // History txs placed at 65s and 75s ago (outside 60s velocity window, inside 120s fragmentation window)
    @Test
    void detect_amountFragmentation_threeWalletsOverThreshold_emitsHigh() {
        Instant t1 = NOW.minusSeconds(75);
        Instant t2 = NOW.minusSeconds(65);
        // current tx is NOW from W-C
        Transaccion tx = makeWalletTx("TX-NEW", "USR001", "W-C", 2000.0, NOW);

        List<Transaccion> hist = List.of(
                makeWalletTx("TX-A", "USR001", "W-A", 2001.0, t1),
                makeWalletTx("TX-B", "USR001", "W-B", 2000.0, t2)
        );
        when(txRepo.findByUserId("USR001")).thenReturn(hist);

        Optional<FraudEvent> result = detector.detect(tx);
        // 3 distinct wallets, combined = 2001 + 2000 + 2000 = 6001 > 5000
        // History txs are outside velocity window (>60s), so Rule B does not fire; Rule D does
        assertThat(result).isPresent();
        assertThat(result.get().getType()).isEqualTo(FraudType.WALLET_FRAGMENTATION);
        assertThat(result.get().getSeverity()).isEqualTo(FraudSeverity.HIGH);
    }

    // T1.3.2 — 3 distinct wallets but combined amount <= 5000 → no event
    // History txs placed outside velocity window (>60s ago) to avoid Rule B interference
    @Test
    void detect_amountFragmentation_belowAmount_doesNotFire() {
        Transaccion tx = makeWalletTx("TX-NEW", "USR001", "W-C", 1000.0, NOW);

        List<Transaccion> hist = List.of(
                makeWalletTx("TX-A", "USR001", "W-A", 1666.0, NOW.minusSeconds(75)),
                makeWalletTx("TX-B", "USR001", "W-B", 1666.0, NOW.minusSeconds(65))
        );
        when(txRepo.findByUserId("USR001")).thenReturn(hist);

        Optional<FraudEvent> result = detector.detect(tx);
        // 3 wallets, 1666 + 1666 + 1000 = 4332 ≤ 5000 → no event
        assertThat(result).isEmpty();
    }

    // T1.3.3 — only 2 distinct wallets within window → no event
    // One history tx outside velocity window, one within; only 1 within velocity → no Rule B
    @Test
    void detect_amountFragmentation_twoWalletsOnly_doesNotFire() {
        Transaccion tx = makeWalletTx("TX-NEW", "USR001", "W-A", 5000.0, NOW);

        List<Transaccion> hist = List.of(
                makeWalletTx("TX-A", "USR001", "W-A", 3000.0, NOW.minusSeconds(75)),
                makeWalletTx("TX-B", "USR001", "W-B", 3000.0, NOW.minusSeconds(65))
        );
        when(txRepo.findByUserId("USR001")).thenReturn(hist);

        Optional<FraudEvent> result = detector.detect(tx);
        // 2 distinct wallets (W-A appears twice, W-B once) → only 2 distinct → no fragmentation event
        // Both hist txs outside velocity window → no velocity event either
        assertThat(result).isEmpty();
    }

    // ─── Rule E: FREQUENCY_BURST ──────────────────────────────────────────────

    // Build helper: make N txs spread over N hours starting from given base
    private List<Transaccion> makeHistory(String userId, int count, Instant start, long intervalSeconds) {
        List<Transaccion> list = new java.util.ArrayList<>();
        for (int i = 0; i < count; i++) {
            Instant ts = start.plusSeconds((long) i * intervalSeconds);
            list.add(makeSuccessfulTx("HX-" + i, userId, ts));
        }
        return list;
    }

    // T1.4.1 — 50 historical txs over 25 hours (avg 2/hr), 12 in last 1h → FREQUENCY_BURST MEDIUM
    @Test
    void detect_frequencySpike_tenHistoryFiveXRate_emitsMedium() {
        // 50 txs spread over 25h: oldest at NOW-25h, one every 30min
        // last 12 within 1h
        Instant base = NOW.minusSeconds(25 * 3600L);
        List<Transaccion> hist = makeHistory("USR001", 38, base, 1800); // 38 txs before 1h ago

        // Add 12 txs in last hour
        for (int i = 0; i < 12; i++) {
            Instant ts = NOW.minusSeconds(3500L - i * 200L);
            hist = new java.util.ArrayList<>(hist);
            ((java.util.ArrayList<Transaccion>) hist).add(makeSuccessfulTx("HL-" + i, "USR001", ts));
        }
        when(txRepo.findByUserId("USR001")).thenReturn(hist);

        Transaccion tx = makeSuccessfulTx("TX-NEW", "USR001", NOW);
        Optional<FraudEvent> result = detector.detect(tx);
        assertThat(result).isPresent();
        assertThat(result.get().getType()).isEqualTo(FraudType.FREQUENCY_BURST);
        assertThat(result.get().getSeverity()).isEqualTo(FraudSeverity.MEDIUM);
    }

    // T1.4.2 — only 9 historical txs → no event (min 10 required)
    @Test
    void detect_frequencySpike_insufficientHistory_doesNotFire() {
        Instant base = NOW.minusSeconds(10 * 3600L);
        List<Transaccion> hist = makeHistory("USR001", 9, base, 3600);
        when(txRepo.findByUserId("USR001")).thenReturn(hist);

        Transaccion tx = makeSuccessfulTx("TX-NEW", "USR001", NOW);
        Optional<FraudEvent> result = detector.detect(tx);
        assertThat(result).isEmpty();
    }

    // T1.4.3 — exact threshold: 10 historical, avg 1/hr, last-hour = 5 → fires
    @Test
    void detect_frequencySpike_exactThreshold_fires() {
        // 10 historical txs spread over 10 hours = avg 1/hr
        // 5 of those in the last hour
        Instant base = NOW.minusSeconds(10 * 3600L);
        List<Transaccion> hist = new java.util.ArrayList<>();
        // 5 txs older than 1h
        for (int i = 0; i < 5; i++) {
            hist.add(makeSuccessfulTx("OLD-" + i, "USR001", base.plusSeconds((long) i * 3600)));
        }
        // 5 txs within last hour
        for (int i = 0; i < 5; i++) {
            hist.add(makeSuccessfulTx("NEW-" + i, "USR001", NOW.minusSeconds(100L + i * 100L)));
        }
        when(txRepo.findByUserId("USR001")).thenReturn(hist);

        Transaccion tx = makeSuccessfulTx("TX-NEW", "USR001", NOW);
        Optional<FraudEvent> result = detector.detect(tx);
        assertThat(result).isPresent();
        assertThat(result.get().getType()).isEqualTo(FraudType.FREQUENCY_BURST);
    }

    // ─── Rule F: OFF_HOURS ────────────────────────────────────────────────────

    // T1.5.1 — 20+ historical txs all in hours 9-17, current at hour 2 → OFF_HOURS LOW
    @Test
    void detect_unusualHours_twentyHistoryNewHour_emitsLow() {
        // NOW is 2026-01-01T00:00:00Z — hour 0
        // We need a tx at hour 2. Let's use 2026-01-01T02:00:00Z
        Instant txTime = Instant.parse("2026-01-01T02:00:00Z");
        List<Transaccion> hist = new java.util.ArrayList<>();
        // 20 txs all in hours 9-17
        for (int i = 0; i < 20; i++) {
            int hour = 9 + (i % 9); // hours 9..17
            Instant ts = Instant.parse("2025-12-31T" + String.format("%02d", hour) + ":00:00Z");
            hist.add(makeSuccessfulTx("H-" + i, "USR001", ts));
        }
        when(txRepo.findByUserId("USR001")).thenReturn(hist);

        Transaccion tx = makeSuccessfulTx("TX-NEW", "USR001", txTime);
        Optional<FraudEvent> result = detector.detect(tx);
        assertThat(result).isPresent();
        assertThat(result.get().getType()).isEqualTo(FraudType.OFF_HOURS);
        assertThat(result.get().getSeverity()).isEqualTo(FraudSeverity.LOW);
    }

    // T1.5.2 — hour 2 present in history → no event
    @Test
    void detect_unusualHours_hourPresent_doesNotFire() {
        Instant txTime = Instant.parse("2026-01-01T02:00:00Z");
        List<Transaccion> hist = new java.util.ArrayList<>();
        for (int i = 0; i < 20; i++) {
            int hour = i % 24; // includes hour 2
            Instant ts = Instant.parse("2025-12-31T" + String.format("%02d", hour) + ":00:00Z");
            hist.add(makeSuccessfulTx("H-" + i, "USR001", ts));
        }
        when(txRepo.findByUserId("USR001")).thenReturn(hist);

        Transaccion tx = makeSuccessfulTx("TX-NEW", "USR001", txTime);
        Optional<FraudEvent> result = detector.detect(tx);
        assertThat(result).isEmpty();
    }

    // T1.5.3 — only 19 historical txs → no event
    @Test
    void detect_unusualHours_insufficientHistory_doesNotFire() {
        List<Transaccion> hist = new java.util.ArrayList<>();
        for (int i = 0; i < 19; i++) {
            hist.add(makeSuccessfulTx("H-" + i, "USR001", NOW.minusSeconds(i * 3600L)));
        }
        when(txRepo.findByUserId("USR001")).thenReturn(hist);

        Instant txTime = Instant.parse("2026-01-01T02:00:00Z");
        Transaccion tx = makeSuccessfulTx("TX-NEW", "USR001", txTime);
        Optional<FraudEvent> result = detector.detect(tx);
        assertThat(result).isEmpty();
    }

    // ─── T1.6.1 — Multi-rule: first fires (Rule C wins over Rule E) ──────────

    @Test
    void detect_multipleRulesSatisfied_onlyFirstFires() {
        // Setup that triggers BOTH Rule C (repeated destination) and Rule E (frequency spike)
        // Rule C fires at position 3, Rule E at position 5 → Rule C wins
        Instant t1 = NOW.minusSeconds(120);
        Instant t2 = NOW.minusSeconds(60);
        Transaccion tx = makeTransferTx("TX-NEW", "USR001", "W001", "USR002", "W002", NOW);

        // 2 prior transfers to same destination (within 5min) → triggers Rule C with current = 3
        List<Transaccion> hist = new java.util.ArrayList<>();
        hist.add(makeTransferTx("TX-A", "USR001", "W001", "USR002", "W002", t1));
        hist.add(makeTransferTx("TX-B", "USR001", "W001", "USR002", "W002", t2));
        // Plus 10+ txs for Rule E
        for (int i = 0; i < 30; i++) {
            hist.add(makeSuccessfulTx("HA-" + i, "USR001", NOW.minusSeconds(25 * 3600L - i * 1800L)));
        }
        // 8 more txs in last hour to ensure frequency spike
        for (int i = 0; i < 8; i++) {
            hist.add(makeSuccessfulTx("HB-" + i, "USR001", NOW.minusSeconds(500L + i * 50L)));
        }
        when(txRepo.findByUserId("USR001")).thenReturn(hist);

        Optional<FraudEvent> result = detector.detect(tx);
        assertThat(result).isPresent();
        // Rule C fires first (position 3 in chain A→B→C→D→E→F)
        assertThat(result.get().getType()).isEqualTo(FraudType.REPEATED_DESTINATION);
    }
}
