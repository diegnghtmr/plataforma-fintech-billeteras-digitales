package com.proyectofinal.fintech.infrastructure.output.memory;

import com.proyectofinal.fintech.domain.model.*;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * T11-E04 (RED→GREEN) — deleteBy* methods in 6 in-memory adapters.
 * Covers: items deleted; other users unaffected; no stale references.
 */
class DeleteByUserAdapterTest {

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

    // ── UserRepository ────────────────────────────────────────────────────────

    @Test
    void userRepo_deleteById_removesUser_otherUnaffected() {
        InMemoryUserRepository repo = new InMemoryUserRepository();
        repo.save(new Usuario("USR001", "Ana", "ana@test.com", NOW, 0.0, LoyaltyLevel.BRONZE));
        repo.save(new Usuario("USR002", "Bob", "bob@test.com", NOW, 0.0, LoyaltyLevel.BRONZE));

        repo.deleteById("USR001");

        assertThat(repo.findById("USR001")).isEmpty();
        assertThat(repo.findById("USR002")).isPresent();
    }

    // ── WalletRepository ──────────────────────────────────────────────────────

    @Test
    void walletRepo_deleteByOwnerId_removesOwnerWallets_otherUnaffected() {
        InMemoryWalletRepository repo = new InMemoryWalletRepository();
        repo.save(new Billetera("W001", "Ahorro", "SAVINGS", "USR001", 100.0, true, NOW, 0));
        repo.save(new Billetera("W002", "Daily", "DAILY", "USR001", 200.0, true, NOW, 0));
        repo.save(new Billetera("W003", "Ahorro", "SAVINGS", "USR002", 50.0, true, NOW, 0));

        repo.deleteByOwnerId("USR001");

        List<Billetera> u1 = toList(repo.findByOwnerId("USR001"));
        List<Billetera> u2 = toList(repo.findByOwnerId("USR002"));
        assertThat(u1).isEmpty();
        assertThat(u2).hasSize(1);
    }

    // ── TransactionRepository ─────────────────────────────────────────────────

    @Test
    void txRepo_deleteByUserId_removesAllTxForUser_otherUnaffected() {
        InMemoryTransactionRepository repo = new InMemoryTransactionRepository();
        Transaccion tx1 = new Transaccion("TX1", NOW, TransactionType.RECHARGE, 100.0,
                "W001", null, "USR001", null, TransactionStatus.SUCCESSFUL, 1.0, null, true);
        Transaccion tx2 = new Transaccion("TX2", NOW, TransactionType.WITHDRAWAL, 50.0,
                "W001", null, "USR001", null, TransactionStatus.SUCCESSFUL, 1.0, null, true);
        Transaccion tx3 = new Transaccion("TX3", NOW, TransactionType.RECHARGE, 200.0,
                "W002", null, "USR002", null, TransactionStatus.SUCCESSFUL, 2.0, null, true);
        repo.save(tx1);
        repo.save(tx2);
        repo.save(tx3);

        repo.deleteByUserId("USR001");

        List<Transaccion> u1txs = toList(repo.findByUserId("USR001"));
        List<Transaccion> u2txs = toList(repo.findByUserId("USR002"));
        assertThat(u1txs).isEmpty();
        assertThat(u2txs).hasSize(1);
        assertThat(u2txs.get(0).getId()).isEqualTo("TX3");
    }

    // ── ScheduledOperationRepository ─────────────────────────────────────────

    @Test
    void scheduledRepo_deleteBySourceUserId_removesOps_otherUnaffected() {
        InMemoryScheduledOperationRepository repo = new InMemoryScheduledOperationRepository();
        OperacionProgramada op1 = new OperacionProgramada("OP1", ScheduledOperationType.RECHARGE,
                ScheduledOperationStatus.PENDING, "USR001", "W001", null, null, 100.0, NOW, null);
        OperacionProgramada op2 = new OperacionProgramada("OP2", ScheduledOperationType.WITHDRAWAL,
                ScheduledOperationStatus.PENDING, "USR001", "W001", null, null, 50.0, NOW, null);
        OperacionProgramada op3 = new OperacionProgramada("OP3", ScheduledOperationType.RECHARGE,
                ScheduledOperationStatus.PENDING, "USR002", "W002", null, null, 200.0, NOW, null);
        repo.save(op1);
        repo.save(op2);
        repo.save(op3);

        repo.deleteBySourceUserId("USR001");

        List<OperacionProgramada> remaining = toList(repo.findAll());
        assertThat(remaining).hasSize(1);
        assertThat(remaining.get(0).getId()).isEqualTo("OP3");
    }

    // ── NotificationRepository ────────────────────────────────────────────────

    @Test
    void notifRepo_deleteByUserId_removesNotifs_otherUnaffected() {
        InMemoryNotificationRepository repo = new InMemoryNotificationRepository();
        repo.save(new Notificacion("N1", "USR001", NotificationType.LOW_BALANCE,
                NotificationSeverity.WARNING, "Saldo bajo", "Tu billetera tiene saldo bajo", false, NOW));
        repo.save(new Notificacion("N2", "USR001", NotificationType.POINTS_LEVEL,
                NotificationSeverity.INFO, "Nivel", "Nuevo nivel", false, NOW));
        repo.save(new Notificacion("N3", "USR002", NotificationType.LOW_BALANCE,
                NotificationSeverity.WARNING, "Saldo bajo", "Tu billetera tiene saldo bajo", false, NOW));

        repo.deleteByUserId("USR001");

        List<Notificacion> u1 = toList(repo.findByUserId("USR001", false));
        List<Notificacion> u2 = toList(repo.findByUserId("USR002", false));
        assertThat(u1).isEmpty();
        assertThat(u2).hasSize(1);
    }

    // ── FraudEventRepository ──────────────────────────────────────────────────

    @Test
    void fraudRepo_deleteByUserId_removesEvents_otherUnaffected() {
        InMemoryFraudEventRepository repo = new InMemoryFraudEventRepository();
        repo.save(new FraudEvent("FRD1", "USR001", "TX1", "LARGE_TRANSACTION", FraudSeverity.HIGH, "t", NOW));
        repo.save(new FraudEvent("FRD2", "USR001", "TX2", "HIGH_VELOCITY", FraudSeverity.HIGH, "t", NOW));
        repo.save(new FraudEvent("FRD3", "USR002", "TX3", "LARGE_TRANSACTION", FraudSeverity.HIGH, "t", NOW));

        repo.deleteByUserId("USR001");

        List<FraudEvent> all = toList(repo.findAll());
        assertThat(all).hasSize(1);
        assertThat(all.get(0).getId()).isEqualTo("FRD3");

        List<FraudEvent> u1 = toList(repo.findByUserId("USR001"));
        assertThat(u1).isEmpty();
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private <T> List<T> toList(Iterable<T> iterable) {
        List<T> list = new ArrayList<>();
        iterable.forEach(list::add);
        return list;
    }
}
