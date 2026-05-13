package com.proyectofinal.fintech.domain.service;

import com.proyectofinal.fintech.domain.model.*;
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
        assertThat(event.getType()).isEqualTo("LARGE_TRANSACTION");
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
        assertThat(result.get().getType()).isEqualTo("HIGH_VELOCITY");
        assertThat(result.get().getSeverity()).isEqualTo(FraudSeverity.HIGH);
    }

    // Rule B: only 2 recent → no velocity fraud
    @Test
    void detect_velocityRule_twoRecentSuccessful_returnsEmpty() {
        Transaccion tx = makeTx("TX-NEW", 100.0, "USR001");

        List<Transaccion> existing = List.of(
                makeSuccessfulTx("TX-A", "USR001", NOW.minusSeconds(10)),
                makeSuccessfulTx("TX-B", "USR001", NOW.minusSeconds(20))
        );
        when(txRepo.findByUserId("USR001")).thenReturn(existing);

        Optional<FraudEvent> result = detector.detect(tx);
        assertThat(result).isEmpty();
    }

    // Rule B: same id excluded (own tx should not count against itself)
    @Test
    void detect_velocityRule_excludesOwnTxId() {
        Transaccion tx = makeTx("TX-SELF", 100.0, "USR001");

        // If own id is included, would be 3 but it must be excluded
        List<Transaccion> existing = List.of(
                makeSuccessfulTx("TX-SELF", "USR001", NOW.minusSeconds(5)),  // same id — must be excluded
                makeSuccessfulTx("TX-A", "USR001", NOW.minusSeconds(10)),
                makeSuccessfulTx("TX-B", "USR001", NOW.minusSeconds(20))
        );
        when(txRepo.findByUserId("USR001")).thenReturn(existing);

        // Only 2 valid (excluding self) → no fraud
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
        assertThat(result.get().getType()).isEqualTo("LARGE_TRANSACTION");
    }

    // Neither rule → empty
    @Test
    void detect_noRuleMatches_returnsEmpty() {
        Transaccion tx = makeTx("TX-NORMAL", 500.0, "USR001");
        when(txRepo.findByUserId("USR001")).thenReturn(List.of());

        Optional<FraudEvent> result = detector.detect(tx);
        assertThat(result).isEmpty();
    }
}
