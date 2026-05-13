package com.proyectofinal.fintech.infrastructure.output.memory;

import com.proyectofinal.fintech.domain.model.TransactionStatus;
import com.proyectofinal.fintech.domain.model.TransactionType;
import com.proyectofinal.fintech.domain.model.Transaccion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * T06-B01 (RED) — InMemoryReversibleOperationsStack tests.
 * Covers: push/peek, EXTERNAL_TRANSFER_RECEIVED skip, removeById preserves order,
 * removeById returns false on absent, multi-user isolation.
 */
class InMemoryReversibleOperationsStackTest {

    private InMemoryReversibleOperationsStack stack;

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

    @BeforeEach
    void setUp() {
        stack = new InMemoryReversibleOperationsStack();
    }

    private Transaccion makeTx(String id, String userId, TransactionType type) {
        return new Transaccion(id, NOW, type, 100.0,
                "W001", null, userId, null,
                TransactionStatus.SUCCESSFUL, 1.0, null, true);
    }

    // ── push / peek ──────────────────────────────────────────────────────────

    @Test
    void push_singleTx_peekReturnsIt() {
        Transaccion tx = makeTx("TX-001", "USR001", TransactionType.RECHARGE);

        stack.push(tx);

        Optional<Transaccion> peeked = stack.peekLatestFor("USR001");
        assertThat(peeked).isPresent().contains(tx);
    }

    @Test
    void push_multipleTx_peekReturnsLatest() {
        Transaccion tx1 = makeTx("TX-001", "USR001", TransactionType.RECHARGE);
        Transaccion tx2 = makeTx("TX-002", "USR001", TransactionType.WITHDRAWAL);

        stack.push(tx1);
        stack.push(tx2);

        Optional<Transaccion> peeked = stack.peekLatestFor("USR001");
        assertThat(peeked).isPresent().contains(tx2);
    }

    @Test
    void peekLatestFor_emptyStack_returnsEmpty() {
        assertThat(stack.peekLatestFor("USR001")).isEmpty();
    }

    // ── push skips EXTERNAL_TRANSFER_RECEIVED ────────────────────────────────

    @Test
    void push_externalTransferReceived_isSkipped() {
        Transaccion received = makeTx("TX-003", "USR001", TransactionType.EXTERNAL_TRANSFER_RECEIVED);

        stack.push(received);

        assertThat(stack.peekLatestFor("USR001")).isEmpty();
    }

    @Test
    void push_externalTransferSent_isNotSkipped() {
        Transaccion sent = makeTx("TX-004", "USR001", TransactionType.EXTERNAL_TRANSFER_SENT);

        stack.push(sent);

        assertThat(stack.peekLatestFor("USR001")).isPresent().contains(sent);
    }

    // ── removeById preserves order ────────────────────────────────────────────

    @Test
    void removeById_middleElement_preservesRelativeOrder() {
        Transaccion txA = makeTx("TX-A", "USR001", TransactionType.RECHARGE);
        Transaccion txB = makeTx("TX-B", "USR001", TransactionType.WITHDRAWAL);
        Transaccion txC = makeTx("TX-C", "USR001", TransactionType.RECHARGE);

        stack.push(txA);
        stack.push(txB);
        stack.push(txC);

        // Stack is LIFO: C on top, then B, then A
        // Remove B
        boolean removed = stack.removeById("USR001", "TX-B");

        assertThat(removed).isTrue();

        // After removal, C should be on top, A at bottom
        Optional<Transaccion> top = stack.peekLatestFor("USR001");
        assertThat(top).isPresent().contains(txC);

        // Remove C — then A should be on top
        stack.removeById("USR001", "TX-C");
        assertThat(stack.peekLatestFor("USR001")).isPresent().contains(txA);
    }

    @Test
    void removeById_topElement_returnsTrue() {
        Transaccion tx = makeTx("TX-TOP", "USR001", TransactionType.RECHARGE);
        stack.push(tx);

        boolean removed = stack.removeById("USR001", "TX-TOP");

        assertThat(removed).isTrue();
        assertThat(stack.peekLatestFor("USR001")).isEmpty();
    }

    // ── removeById returns false when absent ──────────────────────────────────

    @Test
    void removeById_absentId_returnsFalse() {
        Transaccion tx = makeTx("TX-001", "USR001", TransactionType.RECHARGE);
        stack.push(tx);

        boolean result = stack.removeById("USR001", "TX-GHOST");

        assertThat(result).isFalse();
        // Original tx still there
        assertThat(stack.peekLatestFor("USR001")).isPresent().contains(tx);
    }

    @Test
    void removeById_emptyStack_returnsFalse() {
        assertThat(stack.removeById("USR001", "TX-GHOST")).isFalse();
    }

    // ── multi-user isolation ──────────────────────────────────────────────────

    @Test
    void multiUser_isolation_operationsDoNotCrossUser() {
        Transaccion u1Tx = makeTx("TX-U1", "USR001", TransactionType.RECHARGE);
        Transaccion u2Tx = makeTx("TX-U2", "USR002", TransactionType.RECHARGE);

        stack.push(u1Tx);
        stack.push(u2Tx);

        stack.removeById("USR001", "TX-U1");

        // U1 stack should be empty
        assertThat(stack.peekLatestFor("USR001")).isEmpty();
        // U2 stack must be unaffected
        assertThat(stack.peekLatestFor("USR002")).isPresent().contains(u2Tx);
    }
}
