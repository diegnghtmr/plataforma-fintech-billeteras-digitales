package com.proyectofinal.fintech.application.usecase;

import com.proyectofinal.fintech.application.result.MetricItem;
import com.proyectofinal.fintech.domain.model.*;
import com.proyectofinal.fintech.domain.port.TransactionRepository;
import com.proyectofinal.fintech.domain.port.WalletRepository;
import com.proyectofinal.fintech.domain.structures.MiLista;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * D-15 (RED) — GetTopWalletCategoriesUseCase tests.
 * Replaced wallet-count-based tests with transaction-activity-based tests (REQ-F5.1..F5.4).
 */
@ExtendWith(MockitoExtension.class)
class GetTopWalletCategoriesUseCaseTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private TransactionRepository transactionRepository;

    private GetTopWalletCategoriesUseCase useCase;

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

    @BeforeEach
    void setUp() {
        useCase = new GetTopWalletCategoriesUseCase(walletRepository, transactionRepository);
    }

    private Billetera makeWallet(String code, String type, String ownerId) {
        return new Billetera(code, code, type, ownerId, 0.0, true, NOW, 0);
    }

    private MiLista<Billetera> miLista(Billetera... wallets) {
        MiLista<Billetera> list = new MiLista<>();
        for (Billetera w : wallets) list.add(w);
        return list;
    }

    private Transaccion makeTx(String id, String sourceWalletId, String sourceUserId) {
        return new Transaccion(id, NOW, TransactionType.RECHARGE, 100.0,
                sourceWalletId, null, sourceUserId, null,
                TransactionStatus.SUCCESSFUL, 0.0, null, true);
    }

    private Transaccion makeTransferTx(String id, String sourceWalletId, String srcUser,
                                        String targetWalletId, String tgtUser) {
        return new Transaccion(id, NOW, TransactionType.INTERNAL_TRANSFER, 100.0,
                sourceWalletId, targetWalletId, srcUser, tgtUser,
                TransactionStatus.SUCCESSFUL, 0.0, null, true);
    }

    private List<Transaccion> txList(Transaccion... txs) {
        List<Transaccion> list = new ArrayList<>();
        for (Transaccion tx : txs) list.add(tx);
        return list;
    }

    /**
     * REQ-F5.1: rank by transaction count (sourceWalletId → walletType → count)
     * SAVINGS wallets: W1,W2 have 5 txs total
     * CHECKING wallets: W3 has 3 txs
     * INVESTMENT wallets: W4 has 1 tx
     */
    @Test
    void execute_ranksWalletTypesByTransactionCount() {
        when(walletRepository.findAll()).thenReturn(miLista(
                makeWallet("W1", "SAVINGS", "USR001"),
                makeWallet("W2", "SAVINGS", "USR001"),
                makeWallet("W3", "CHECKING", "USR002"),
                makeWallet("W4", "INVESTMENT", "USR003")
        ));
        when(transactionRepository.findAll()).thenReturn(txList(
                makeTx("T1", "W1", "USR001"),
                makeTx("T2", "W1", "USR001"),
                makeTx("T3", "W1", "USR001"),
                makeTx("T4", "W2", "USR001"),
                makeTx("T5", "W2", "USR001"),
                makeTx("T6", "W3", "USR002"),
                makeTx("T7", "W3", "USR002"),
                makeTx("T8", "W3", "USR002"),
                makeTx("T9", "W4", "USR003")
        ));

        List<MetricItem> result = useCase.execute(10);

        assertThat(result).hasSize(3);
        assertThat(result.get(0).id()).isEqualTo("SAVINGS");
        assertThat(result.get(0).value()).isEqualTo(5.0);
        assertThat(result.get(1).id()).isEqualTo("CHECKING");
        assertThat(result.get(1).value()).isEqualTo(3.0);
        assertThat(result.get(2).id()).isEqualTo("INVESTMENT");
        assertThat(result.get(2).value()).isEqualTo(1.0);
    }

    /**
     * REQ-F5.2: ties broken alphabetically ascending
     * CHECKING and SAVINGS each have 4 transactions → CHECKING before SAVINGS
     */
    @Test
    void execute_tiesBrokenAlphabetically() {
        when(walletRepository.findAll()).thenReturn(miLista(
                makeWallet("W1", "SAVINGS", "USR001"),
                makeWallet("W2", "CHECKING", "USR002")
        ));
        when(transactionRepository.findAll()).thenReturn(txList(
                makeTx("T1", "W1", "USR001"),
                makeTx("T2", "W1", "USR001"),
                makeTx("T3", "W1", "USR001"),
                makeTx("T4", "W1", "USR001"),
                makeTx("T5", "W2", "USR002"),
                makeTx("T6", "W2", "USR002"),
                makeTx("T7", "W2", "USR002"),
                makeTx("T8", "W2", "USR002")
        ));

        List<MetricItem> result = useCase.execute(10);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).id()).isEqualTo("CHECKING");
        assertThat(result.get(1).id()).isEqualTo("SAVINGS");
    }

    /**
     * W2-RED: transfer tx counts BOTH source-type AND target-type.
     * 1 transfer: source=SAVINGS, target=CHECKING → SAVINGS=1, CHECKING=1.
     */
    @Test
    void execute_countsTargetWalletIdContribution() {
        when(walletRepository.findAll()).thenReturn(miLista(
                makeWallet("W1", "SAVINGS", "USR001"),
                makeWallet("W2", "CHECKING", "USR002")
        ));
        // 1 transfer from W1 (SAVINGS) to W2 (CHECKING)
        when(transactionRepository.findAll()).thenReturn(txList(
                makeTransferTx("T1", "W1", "USR001", "W2", "USR002")
        ));

        List<MetricItem> result = useCase.execute(10);

        // Both wallet types should have count=1
        assertThat(result).hasSize(2);
        // CHECKING comes first alphabetically when tied
        assertThat(result.get(0).id()).isEqualTo("CHECKING");
        assertThat(result.get(0).value()).isEqualTo(1.0);
        assertThat(result.get(1).id()).isEqualTo("SAVINGS");
        assertThat(result.get(1).value()).isEqualTo(1.0);
    }

    /**
     * REQ-F5.3: limit parameter is respected
     */
    @Test
    void execute_limitApplied() {
        when(walletRepository.findAll()).thenReturn(miLista(
                makeWallet("W1", "SAVINGS", "USR001"),
                makeWallet("W2", "CHECKING", "USR002"),
                makeWallet("W3", "INVESTMENT", "USR003"),
                makeWallet("W4", "TRANSPORT", "USR004"),
                makeWallet("W5", "SHOPPING", "USR005")
        ));
        when(transactionRepository.findAll()).thenReturn(txList(
                makeTx("T1", "W1", "USR001"), makeTx("T2", "W1", "USR001"),
                makeTx("T3", "W2", "USR002"),
                makeTx("T4", "W3", "USR003"),
                makeTx("T5", "W4", "USR004"),
                makeTx("T6", "W5", "USR005")
        ));

        List<MetricItem> result = useCase.execute(2);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).id()).isEqualTo("SAVINGS");
    }
}
