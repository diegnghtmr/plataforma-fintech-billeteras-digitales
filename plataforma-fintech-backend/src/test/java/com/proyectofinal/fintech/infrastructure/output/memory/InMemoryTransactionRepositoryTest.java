package com.proyectofinal.fintech.infrastructure.output.memory;

import com.proyectofinal.fintech.domain.model.Transaccion;
import com.proyectofinal.fintech.domain.model.TransactionStatus;
import com.proyectofinal.fintech.domain.model.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * T05-B07 (RED) — InMemoryTransactionRepository tests.
 * Covers S-REPO-01 to S-REPO-03, S-INFRA-01, S-INFRA-02.
 */
class InMemoryTransactionRepositoryTest {

    private InMemoryTransactionRepository repo;
    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

    @BeforeEach
    void setUp() {
        repo = new InMemoryTransactionRepository();
    }

    private Transaccion makeRecharge(String id, String walletId, String userId) {
        return new Transaccion(id, NOW, TransactionType.RECHARGE, 100.0,
                walletId, null, userId, null,
                TransactionStatus.SUCCESSFUL, 1.0, null, true);
    }

    private Transaccion makeExternal(String id, String srcWallet, String srcUser,
                                     String tgtWallet, String tgtUser) {
        return new Transaccion(id, NOW, TransactionType.EXTERNAL_TRANSFER_SENT, 200.0,
                srcWallet, tgtWallet, srcUser, tgtUser,
                TransactionStatus.SUCCESSFUL, 2.0, null, true);
    }

    private Transaccion makeInternal(String id, String srcWallet, String tgtWallet, String userId) {
        return new Transaccion(id, NOW, TransactionType.INTERNAL_TRANSFER, 150.0,
                srcWallet, tgtWallet, userId, userId,
                TransactionStatus.SUCCESSFUL, 0.75, null, true);
    }

    // S-REPO-01: save then findById returns same object
    @Test
    void saveAndFindById_returnsSameObject() {
        Transaccion tx = makeRecharge("TX-000001", "W001", "USR001");
        repo.save(tx);

        assertThat(repo.findById("TX-000001")).isPresent().contains(tx);
    }

    @Test
    void findById_missing_returnsEmpty() {
        assertThat(repo.findById("TX-GHOST")).isEmpty();
    }

    // S-REPO-02: findByUserId returns only matching user's tx in insertion order
    @Test
    void findByUserId_returnsOnlyMatchingTransactions() {
        Transaccion tx1 = makeRecharge("TX-000001", "W001", "USR001");
        Transaccion tx2 = makeRecharge("TX-000002", "W002", "USR001");
        Transaccion tx3 = makeRecharge("TX-000003", "W003", "USR002");
        repo.save(tx1);
        repo.save(tx2);
        repo.save(tx3);

        List<Transaccion> result = toList(repo.findByUserId("USR001"));

        assertThat(result).hasSize(2).containsExactly(tx1, tx2);
    }

    @Test
    void findByUserId_noTransactions_returnsEmpty() {
        assertThat(toList(repo.findByUserId("GHOST"))).isEmpty();
    }

    // S-REPO-03: findByWalletId returns only matching wallet's tx
    @Test
    void findByWalletId_returnsOnlyMatchingTransactions() {
        Transaccion tx1 = makeRecharge("TX-000001", "W001", "USR001");
        Transaccion tx2 = makeRecharge("TX-000002", "W001", "USR001");
        Transaccion tx3 = makeRecharge("TX-000003", "W002", "USR001");
        repo.save(tx1);
        repo.save(tx2);
        repo.save(tx3);

        List<Transaccion> result = toList(repo.findByWalletId("W001"));

        assertThat(result).hasSize(2).containsExactly(tx1, tx2);
    }

    // S-INFRA-01: external tx indexed under BOTH sourceUserId AND targetUserId, AND both wallets
    @Test
    void save_externalTx_indexedUnderBothUsersAndBothWallets() {
        Transaccion tx = makeExternal("TX-000001", "W_A", "USR_A", "W_B", "USR_B");
        repo.save(tx);

        // Both users can find it
        assertThat(toList(repo.findByUserId("USR_A"))).contains(tx);
        assertThat(toList(repo.findByUserId("USR_B"))).contains(tx);

        // Both wallets can find it
        assertThat(toList(repo.findByWalletId("W_A"))).contains(tx);
        assertThat(toList(repo.findByWalletId("W_B"))).contains(tx);
    }

    // S-INFRA-02: internal tx findByUserId("A") returns it exactly once (no duplicate)
    @Test
    void save_internalTx_findByUserIdReturnsExactlyOnce() {
        Transaccion tx = makeInternal("TX-000001", "W001", "W002", "USR001");
        repo.save(tx);

        List<Transaccion> result = toList(repo.findByUserId("USR001"));

        assertThat(result).hasSize(1).containsExactly(tx);
    }

    // findAll returns all saved transactions
    @Test
    void findAll_returnsAllSavedTransactions() {
        Transaccion tx1 = makeRecharge("TX-000001", "W001", "USR001");
        Transaccion tx2 = makeRecharge("TX-000002", "W002", "USR002");
        repo.save(tx1);
        repo.save(tx2);

        assertThat(toList(repo.findAll())).hasSize(2);
    }

    // ── helper ───────────────────────────────────────────────────────────────

    private <T> List<T> toList(Iterable<T> iterable) {
        List<T> result = new ArrayList<>();
        iterable.forEach(result::add);
        return result;
    }
}
