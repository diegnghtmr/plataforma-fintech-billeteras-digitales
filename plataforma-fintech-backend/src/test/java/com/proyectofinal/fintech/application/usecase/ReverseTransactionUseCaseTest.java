package com.proyectofinal.fintech.application.usecase;

import com.proyectofinal.fintech.domain.exception.BusinessRuleException;
import com.proyectofinal.fintech.domain.exception.ErrorCode;
import com.proyectofinal.fintech.domain.exception.NotFoundException;
import com.proyectofinal.fintech.domain.model.*;
import com.proyectofinal.fintech.domain.port.ReversibleOperationsStack;
import com.proyectofinal.fintech.domain.port.TransactionRepository;
import com.proyectofinal.fintech.domain.port.UserRepository;
import com.proyectofinal.fintech.domain.port.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * T06-B05 (RED) — ReverseTransactionUseCase tests.
 * Covers S3 (RECHARGE), S4 (WITHDRAWAL), S5 (INTERNAL_TRANSFER), S6 (EXTERNAL_TRANSFER_SENT),
 * S5 reject RECEIVED, S7 insufficient funds, S8 already-REVERSED, S9 not found, points clamp.
 */
@ExtendWith(MockitoExtension.class)
class ReverseTransactionUseCaseTest {

    @Mock
    private WalletRepository walletRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private ReversibleOperationsStack stack;

    private ReverseTransactionUseCase useCase;

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

    @BeforeEach
    void setUp() {
        useCase = new ReverseTransactionUseCase(walletRepository, userRepository, transactionRepository, stack);
    }

    private Transaccion makeTx(String id, TransactionType type, TransactionStatus status,
                                double amount, String srcWallet, String tgtWallet,
                                String srcUser, String tgtUser, boolean reversible) {
        return new Transaccion(id, NOW, type, amount, srcWallet, tgtWallet,
                srcUser, tgtUser, status, 5.0, null, reversible);
    }

    private Transaccion makeTx(String id, TransactionType type, TransactionStatus status,
                                double amount, String srcWallet, String tgtWallet,
                                String srcUser, String tgtUser, boolean reversible, double points) {
        return new Transaccion(id, NOW, type, amount, srcWallet, tgtWallet,
                srcUser, tgtUser, status, points, null, reversible);
    }

    private Usuario makeUser(String id, double points) {
        return new Usuario(id, "User " + id, id + "@test.com", NOW, points, LoyaltyLevel.BRONZE);
    }

    private Usuario makeUser(String id, double points, LoyaltyLevel level) {
        return new Usuario(id, "User " + id, id + "@test.com", NOW, points, level);
    }

    private Billetera makeWallet(String code, String ownerId, double balance) {
        return new Billetera(code, "Wallet " + code, "SAVINGS", ownerId, balance, true, NOW, 0);
    }

    // S3: reverse RECHARGE — balance decremented, status REVERSED, points subtracted
    @Test
    void execute_reverseRecharge_decrementsBalanceAndReversesStatus() {
        Transaccion tx = makeTx("TX-001", TransactionType.RECHARGE, TransactionStatus.SUCCESSFUL,
                100.0, "W001", null, "USR001", null, true, 10.0);
        Usuario user = makeUser("USR001", 20.0);
        Billetera wallet = makeWallet("W001", "USR001", 200.0);

        when(transactionRepository.findById("TX-001")).thenReturn(Optional.of(tx));
        when(userRepository.findById("USR001")).thenReturn(Optional.of(user));
        when(walletRepository.findByOwnerIdAndCode("USR001", "W001")).thenReturn(Optional.of(wallet));
        when(transactionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Transaccion result = useCase.execute("TX-001");

        assertThat(wallet.getBalance()).isEqualTo(100.0);
        assertThat(result.getStatus()).isEqualTo(TransactionStatus.REVERSED);
        assertThat(user.getPoints()).isEqualTo(10.0); // 20 - 10 = 10
        verify(stack).removeById("USR001", "TX-001");
    }

    // S4: reverse WITHDRAWAL — balance incremented, status REVERSED
    @Test
    void execute_reverseWithdrawal_incrementsBalance() {
        Transaccion tx = makeTx("TX-002", TransactionType.WITHDRAWAL, TransactionStatus.SUCCESSFUL,
                50.0, "W001", null, "USR001", null, true, 0.0);
        Usuario user = makeUser("USR001", 5.0);
        Billetera wallet = makeWallet("W001", "USR001", 100.0);

        when(transactionRepository.findById("TX-002")).thenReturn(Optional.of(tx));
        when(userRepository.findById("USR001")).thenReturn(Optional.of(user));
        when(walletRepository.findByOwnerIdAndCode("USR001", "W001")).thenReturn(Optional.of(wallet));
        when(transactionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Transaccion result = useCase.execute("TX-002");

        assertThat(wallet.getBalance()).isEqualTo(150.0);
        assertThat(result.getStatus()).isEqualTo(TransactionStatus.REVERSED);
    }

    // S5: reverse INTERNAL_TRANSFER — source credited, target debited
    @Test
    void execute_reverseInternalTransfer_swapsBalances() {
        Transaccion tx = makeTx("TX-003", TransactionType.INTERNAL_TRANSFER, TransactionStatus.SUCCESSFUL,
                200.0, "W001", "W002", "USR001", "USR001", true, 1.0);
        Usuario user = makeUser("USR001", 10.0);
        Billetera source = makeWallet("W001", "USR001", 300.0);
        Billetera target = makeWallet("W002", "USR001", 200.0);

        when(transactionRepository.findById("TX-003")).thenReturn(Optional.of(tx));
        when(userRepository.findById("USR001")).thenReturn(Optional.of(user));
        when(walletRepository.findByOwnerIdAndCode("USR001", "W001")).thenReturn(Optional.of(source));
        when(walletRepository.findByOwnerIdAndCode("USR001", "W002")).thenReturn(Optional.of(target));
        when(transactionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Transaccion result = useCase.execute("TX-003");

        assertThat(source.getBalance()).isEqualTo(500.0); // 300 + 200
        assertThat(target.getBalance()).isEqualTo(0.0);   // 200 - 200
        assertThat(result.getStatus()).isEqualTo(TransactionStatus.REVERSED);
    }

    // S6: reverse EXTERNAL_TRANSFER_SENT — source credited, target debited, RECEIVED tx also REVERSED
    @Test
    void execute_reverseExternalTransferSent_marksBothReversed() {
        Transaccion sentTx = makeTx("TX-S", TransactionType.EXTERNAL_TRANSFER_SENT, TransactionStatus.SUCCESSFUL,
                100.0, "W_SRC", "W_TGT", "USR_A", "USR_B", true, 5.0);
        Transaccion rcvdTx = makeTx("TX-R", TransactionType.EXTERNAL_TRANSFER_RECEIVED, TransactionStatus.SUCCESSFUL,
                100.0, "W_SRC", "W_TGT", "USR_A", "USR_B", true, 0.0);

        Usuario srcUser = makeUser("USR_A", 10.0);
        Billetera srcWallet = makeWallet("W_SRC", "USR_A", 500.0);
        Billetera tgtWallet = makeWallet("W_TGT", "USR_B", 300.0);

        when(transactionRepository.findById("TX-S")).thenReturn(Optional.of(sentTx));
        when(userRepository.findById("USR_A")).thenReturn(Optional.of(srcUser));
        when(walletRepository.findByOwnerIdAndCode("USR_A", "W_SRC")).thenReturn(Optional.of(srcWallet));
        // Target wallet belongs to USR_B; use findByCode or findByOwnerIdAndCode for target
        when(walletRepository.findByOwnerIdAndCode("USR_B", "W_TGT")).thenReturn(Optional.of(tgtWallet));
        // Paired RECEIVED tx lookup
        when(transactionRepository.findByWalletId("W_TGT")).thenReturn(List.of(rcvdTx));
        when(transactionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Transaccion result = useCase.execute("TX-S");

        assertThat(srcWallet.getBalance()).isEqualTo(600.0); // 500 + 100
        assertThat(tgtWallet.getBalance()).isEqualTo(200.0); // 300 - 100
        assertThat(result.getStatus()).isEqualTo(TransactionStatus.REVERSED);
        assertThat(rcvdTx.getStatus()).isEqualTo(TransactionStatus.REVERSED);
        // Two saves: sent tx + received tx
        verify(transactionRepository, atLeast(2)).save(any());
    }

    // S5 reject: reverse EXTERNAL_TRANSFER_RECEIVED → 422 TRANSACTION_NOT_REVERSIBLE
    @Test
    void execute_reverseExternalTransferReceived_throws422() {
        Transaccion received = makeTx("TX-R", TransactionType.EXTERNAL_TRANSFER_RECEIVED,
                TransactionStatus.SUCCESSFUL, 100.0, "W_SRC", "W_TGT", "USR_A", "USR_B", true);

        when(transactionRepository.findById("TX-R")).thenReturn(Optional.of(received));

        assertThatThrownBy(() -> useCase.execute("TX-R"))
                .isInstanceOf(BusinessRuleException.class)
                .satisfies(ex -> assertThat(((BusinessRuleException) ex).code())
                        .isEqualTo(ErrorCode.TRANSACTION_NOT_REVERSIBLE));
        verifyNoInteractions(walletRepository, userRepository);
    }

    // S7: insufficient funds on reversal (RECHARGE case: wallet.balance < amount) → 422, no mutation
    @Test
    void execute_reverseRechargeInsufficientFunds_throws422NoMutation() {
        Transaccion tx = makeTx("TX-004", TransactionType.RECHARGE, TransactionStatus.SUCCESSFUL,
                200.0, "W001", null, "USR001", null, true, 5.0);
        Usuario user = makeUser("USR001", 10.0);
        Billetera wallet = makeWallet("W001", "USR001", 100.0); // balance < amount

        when(transactionRepository.findById("TX-004")).thenReturn(Optional.of(tx));
        when(userRepository.findById("USR001")).thenReturn(Optional.of(user));
        when(walletRepository.findByOwnerIdAndCode("USR001", "W001")).thenReturn(Optional.of(wallet));

        assertThatThrownBy(() -> useCase.execute("TX-004"))
                .isInstanceOf(BusinessRuleException.class)
                .satisfies(ex -> assertThat(((BusinessRuleException) ex).code())
                        .isEqualTo(ErrorCode.INSUFFICIENT_FUNDS));

        // No mutation: balance unchanged, tx still SUCCESSFUL
        assertThat(wallet.getBalance()).isEqualTo(100.0);
        assertThat(tx.getStatus()).isEqualTo(TransactionStatus.SUCCESSFUL);
        verify(transactionRepository, never()).save(any());
    }

    // S8: already-REVERSED → 422 TRANSACTION_NOT_REVERSIBLE
    @Test
    void execute_alreadyReversed_throws422() {
        Transaccion tx = makeTx("TX-005", TransactionType.RECHARGE, TransactionStatus.REVERSED,
                100.0, "W001", null, "USR001", null, true);

        when(transactionRepository.findById("TX-005")).thenReturn(Optional.of(tx));

        assertThatThrownBy(() -> useCase.execute("TX-005"))
                .isInstanceOf(BusinessRuleException.class)
                .satisfies(ex -> assertThat(((BusinessRuleException) ex).code())
                        .isEqualTo(ErrorCode.TRANSACTION_NOT_REVERSIBLE));
    }

    // S8b: reversible=false → 422 TRANSACTION_NOT_REVERSIBLE
    @Test
    void execute_notReversible_throws422() {
        Transaccion tx = makeTx("TX-006", TransactionType.RECHARGE, TransactionStatus.SUCCESSFUL,
                100.0, "W001", null, "USR001", null, false);

        when(transactionRepository.findById("TX-006")).thenReturn(Optional.of(tx));

        assertThatThrownBy(() -> useCase.execute("TX-006"))
                .isInstanceOf(BusinessRuleException.class)
                .satisfies(ex -> assertThat(((BusinessRuleException) ex).code())
                        .isEqualTo(ErrorCode.TRANSACTION_NOT_REVERSIBLE));
    }

    // S9: transaction not found → 404 TRANSACTION_NOT_FOUND
    @Test
    void execute_notFound_throws404() {
        when(transactionRepository.findById("TX-GHOST")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute("TX-GHOST"))
                .isInstanceOf(NotFoundException.class)
                .satisfies(ex -> assertThat(((NotFoundException) ex).code())
                        .isEqualTo(ErrorCode.TRANSACTION_NOT_FOUND));
    }

    // T07-B06 (RED): reverse causes level demotion SILVER → BRONZE
    @Test
    void execute_reverseRecharge_silverDemotesToBronze() {
        // User starts at SILVER (1000 pts), tx generated 1000 pts → after reverse: 0 pts, BRONZE
        Transaccion tx = makeTx("TX-010", TransactionType.RECHARGE, TransactionStatus.SUCCESSFUL,
                100000.0, "W001", null, "USR001", null, true, 1000.0);
        Usuario user = makeUser("USR001", 1000.0, LoyaltyLevel.SILVER);
        Billetera wallet = makeWallet("W001", "USR001", 200000.0);

        when(transactionRepository.findById("TX-010")).thenReturn(Optional.of(tx));
        when(userRepository.findById("USR001")).thenReturn(Optional.of(user));
        when(walletRepository.findByOwnerIdAndCode("USR001", "W001")).thenReturn(Optional.of(wallet));
        when(transactionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        useCase.execute("TX-010");

        assertThat(user.getPoints()).isEqualTo(0.0);
        assertThat(user.getLoyaltyLevel()).isEqualTo(LoyaltyLevel.BRONZE);
    }

    // T07-B06 (RED): reverse with fewer points than tx.pointsGenerated — clamped + BRONZE
    @Test
    void execute_reverseWithFewerPointsThanTx_clampedAndBronze() {
        Transaccion tx = makeTx("TX-011", TransactionType.RECHARGE, TransactionStatus.SUCCESSFUL,
                50000.0, "W001", null, "USR001", null, true, 500.0);
        Usuario user = makeUser("USR001", 200.0, LoyaltyLevel.BRONZE);
        Billetera wallet = makeWallet("W001", "USR001", 200000.0);

        when(transactionRepository.findById("TX-011")).thenReturn(Optional.of(tx));
        when(userRepository.findById("USR001")).thenReturn(Optional.of(user));
        when(walletRepository.findByOwnerIdAndCode("USR001", "W001")).thenReturn(Optional.of(wallet));
        when(transactionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        useCase.execute("TX-011");

        assertThat(user.getPoints()).isEqualTo(0.0);
        assertThat(user.getLoyaltyLevel()).isEqualTo(LoyaltyLevel.BRONZE);
    }

    // Points clamped at 0 when pointsGenerated > user.points
    @Test
    void execute_pointsClampedAtZero() {
        Transaccion tx = makeTx("TX-007", TransactionType.RECHARGE, TransactionStatus.SUCCESSFUL,
                100.0, "W001", null, "USR001", null, true, 50.0); // 50 points to subtract
        Usuario user = makeUser("USR001", 10.0); // only 10 points
        Billetera wallet = makeWallet("W001", "USR001", 200.0);

        when(transactionRepository.findById("TX-007")).thenReturn(Optional.of(tx));
        when(userRepository.findById("USR001")).thenReturn(Optional.of(user));
        when(walletRepository.findByOwnerIdAndCode("USR001", "W001")).thenReturn(Optional.of(wallet));
        when(transactionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        useCase.execute("TX-007");

        // user.points must be 0, NOT negative
        assertThat(user.getPoints()).isEqualTo(0.0);
    }
}
