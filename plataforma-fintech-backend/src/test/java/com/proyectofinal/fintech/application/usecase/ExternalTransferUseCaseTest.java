package com.proyectofinal.fintech.application.usecase;

import com.proyectofinal.fintech.application.result.ExternalTransferResult;
import com.proyectofinal.fintech.application.service.NotificationEmitter;
import com.proyectofinal.fintech.domain.exception.BusinessRuleException;
import com.proyectofinal.fintech.domain.exception.ErrorCode;
import com.proyectofinal.fintech.domain.exception.NotFoundException;
import com.proyectofinal.fintech.domain.model.*;
import com.proyectofinal.fintech.domain.port.FraudEventRepository;
import com.proyectofinal.fintech.domain.port.ReversibleOperationsStack;
import com.proyectofinal.fintech.domain.port.TransactionIdGenerator;
import com.proyectofinal.fintech.domain.port.TransactionRepository;
import com.proyectofinal.fintech.domain.port.TransferGraphRepository;
import com.proyectofinal.fintech.domain.port.UserRepository;
import com.proyectofinal.fintech.domain.port.WalletRepository;
import com.proyectofinal.fintech.domain.service.FraudDetector;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * T05-B15 / T11-A05 / T11-C03 / T11-D03 (RED) — ExternalTransferUseCase tests.
 * Updated: PDF-compliant points, instance FraudDetector, LOW_BALANCE, level-up.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ExternalTransferUseCaseTest {

    @Mock private UserRepository userRepository;
    @Mock private WalletRepository walletRepository;
    @Mock private TransactionRepository transactionRepository;
    @Mock private TransactionIdGenerator idGenerator;
    @Mock private ReversibleOperationsStack stack;
    @Mock private FraudEventRepository fraudRepository;
    @Mock private TransferGraphRepository graphRepository;
    @Mock private FraudDetector fraudDetector;
    @Mock private NotificationEmitter notificationEmitter;

    private Clock fixedClock;
    private ExternalTransferUseCase useCase;

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

    @BeforeEach
    void setUp() {
        fixedClock = Clock.fixed(NOW, ZoneId.of("UTC"));
        useCase = new ExternalTransferUseCase(userRepository, walletRepository,
                transactionRepository, idGenerator, fixedClock, stack,
                fraudRepository, graphRepository,
                fraudDetector, notificationEmitter);
        when(fraudDetector.detect(any())).thenReturn(Optional.empty());
    }

    // S-UC4-01: happy path — 2 tx stored, same timestamp, PDF points
    @Test
    void execute_happyPath_createsTwoTransactionsWithSharedFields() {
        Usuario srcUser = new Usuario("USR_A", "Ana", "ana@test.com", NOW, 0.0, LoyaltyLevel.BRONZE);
        Usuario tgtUser = new Usuario("USR_B", "Bob", "bob@test.com", NOW, 0.0, LoyaltyLevel.BRONZE);
        Billetera srcWallet = new Billetera("W_A", "Ahorros", "SAVINGS", "USR_A", 1000.0, true, NOW, 0);
        Billetera tgtWallet = new Billetera("W_B", "Ahorros", "SAVINGS", "USR_B", 0.0, true, NOW, 0);

        when(userRepository.findById("USR_A")).thenReturn(Optional.of(srcUser));
        when(userRepository.findById("USR_B")).thenReturn(Optional.of(tgtUser));
        when(walletRepository.findByOwnerIdAndCode("USR_A", "W_A")).thenReturn(Optional.of(srcWallet));
        when(walletRepository.findByOwnerIdAndCode("USR_B", "W_B")).thenReturn(Optional.of(tgtWallet));
        when(idGenerator.next()).thenReturn("TX-000001", "TX-000002");
        when(transactionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        ExternalTransferResult result = useCase.execute("USR_A", "W_A", "USR_B", "W_B", 500.0, null);

        assertThat(srcWallet.getBalance()).isEqualTo(500.0);
        assertThat(tgtWallet.getBalance()).isEqualTo(500.0);

        // PDF: floor(500/100)*3 = 15 pts for SENT; RECEIVED = 0
        assertThat(srcUser.getPoints()).isEqualTo(15.0);
        assertThat(tgtUser.getPoints()).isEqualTo(0.0);

        Transaccion outgoing = result.outgoing();
        Transaccion incoming = result.incoming();

        assertThat(outgoing.getId()).isEqualTo("TX-000001");
        assertThat(incoming.getId()).isEqualTo("TX-000002");
        assertThat(outgoing.getType()).isEqualTo(TransactionType.EXTERNAL_TRANSFER_SENT);
        assertThat(incoming.getType()).isEqualTo(TransactionType.EXTERNAL_TRANSFER_RECEIVED);
        assertThat(outgoing.getTimestamp()).isEqualTo(incoming.getTimestamp());
        assertThat(outgoing.isReversible()).isTrue();
        assertThat(incoming.isReversible()).isFalse();

        verify(transactionRepository, atLeastOnce()).save(any());
        verify(stack, times(1)).push(result.outgoing());
    }

    // S-UC4-02: same user → VALIDATION_ERROR
    @Test
    void execute_sameUser_throwsValidationError() {
        assertThatThrownBy(() -> useCase.execute("USR_A", "W1", "USR_A", "W2", 100.0, null))
                .isInstanceOf(BusinessRuleException.class)
                .satisfies(ex -> assertThat(((BusinessRuleException) ex).code())
                        .isEqualTo(ErrorCode.VALIDATION_ERROR));
        verifyNoInteractions(userRepository, walletRepository);
    }

    // S-UC4-03: wallet not owned by target → WALLET_NOT_FOUND
    @Test
    void execute_targetWalletNotOwnedByTarget_throwsWalletNotFound() {
        Usuario srcUser = new Usuario("USR_A", "Ana", "ana@test.com", NOW, 0.0, LoyaltyLevel.BRONZE);
        Usuario tgtUser = new Usuario("USR_B", "Bob", "bob@test.com", NOW, 0.0, LoyaltyLevel.BRONZE);
        Billetera srcWallet = new Billetera("W_A", "Ahorros", "SAVINGS", "USR_A", 500.0, true, NOW, 0);

        when(userRepository.findById("USR_A")).thenReturn(Optional.of(srcUser));
        when(userRepository.findById("USR_B")).thenReturn(Optional.of(tgtUser));
        when(walletRepository.findByOwnerIdAndCode("USR_A", "W_A")).thenReturn(Optional.of(srcWallet));
        when(walletRepository.findByOwnerIdAndCode("USR_B", "W_B")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute("USR_A", "W_A", "USR_B", "W_B", 100.0, null))
                .isInstanceOf(NotFoundException.class)
                .satisfies(ex -> assertThat(((NotFoundException) ex).code())
                        .isEqualTo(ErrorCode.WALLET_NOT_FOUND));
    }

    // insufficient funds
    @Test
    void execute_insufficientFunds_throwsInsufficientFunds() {
        Usuario srcUser = new Usuario("USR_A", "Ana", "ana@test.com", NOW, 0.0, LoyaltyLevel.BRONZE);
        Usuario tgtUser = new Usuario("USR_B", "Bob", "bob@test.com", NOW, 0.0, LoyaltyLevel.BRONZE);
        Billetera srcWallet = new Billetera("W_A", "Ahorros", "SAVINGS", "USR_A", 50.0, true, NOW, 0);
        Billetera tgtWallet = new Billetera("W_B", "Ahorros", "SAVINGS", "USR_B", 0.0, true, NOW, 0);

        when(userRepository.findById("USR_A")).thenReturn(Optional.of(srcUser));
        when(userRepository.findById("USR_B")).thenReturn(Optional.of(tgtUser));
        when(walletRepository.findByOwnerIdAndCode("USR_A", "W_A")).thenReturn(Optional.of(srcWallet));
        when(walletRepository.findByOwnerIdAndCode("USR_B", "W_B")).thenReturn(Optional.of(tgtWallet));

        assertThatThrownBy(() -> useCase.execute("USR_A", "W_A", "USR_B", "W_B", 200.0, null))
                .isInstanceOf(BusinessRuleException.class)
                .satisfies(ex -> assertThat(((BusinessRuleException) ex).code())
                        .isEqualTo(ErrorCode.INSUFFICIENT_FUNDS));
        verify(transactionRepository, never()).save(any());
    }

    // T11-D03: LOW_BALANCE alert on source wallet
    @Test
    void execute_sourceBalanceDropsBelow100_emitsLowBalance() {
        Usuario srcUser = new Usuario("USR_A", "Ana", "ana@test.com", NOW, 0.0, LoyaltyLevel.BRONZE);
        Usuario tgtUser = new Usuario("USR_B", "Bob", "bob@test.com", NOW, 0.0, LoyaltyLevel.BRONZE);
        Billetera srcWallet = new Billetera("W_A", "Ahorros", "SAVINGS", "USR_A", 150.0, true, NOW, 0);
        Billetera tgtWallet = new Billetera("W_B", "Ahorros", "SAVINGS", "USR_B", 0.0, true, NOW, 0);

        when(userRepository.findById("USR_A")).thenReturn(Optional.of(srcUser));
        when(userRepository.findById("USR_B")).thenReturn(Optional.of(tgtUser));
        when(walletRepository.findByOwnerIdAndCode("USR_A", "W_A")).thenReturn(Optional.of(srcWallet));
        when(walletRepository.findByOwnerIdAndCode("USR_B", "W_B")).thenReturn(Optional.of(tgtWallet));
        when(idGenerator.next()).thenReturn("TX-001", "TX-002");
        when(transactionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        useCase.execute("USR_A", "W_A", "USR_B", "W_B", 100.0, null);

        // srcWallet = 50 < 100 → LOW_BALANCE
        verify(notificationEmitter, times(1)).emitLowBalance("USR_A", "W_A");
    }

    // T09-B11: graph.addEdge called once
    @Test
    void execute_happyPath_callsGraphAddEdge() {
        Usuario srcUser = new Usuario("USR_A", "Ana", "ana@test.com", NOW, 0.0, LoyaltyLevel.BRONZE);
        Usuario tgtUser = new Usuario("USR_B", "Bob", "bob@test.com", NOW, 0.0, LoyaltyLevel.BRONZE);
        Billetera srcWallet = new Billetera("W_A", "Ahorros", "SAVINGS", "USR_A", 1000.0, true, NOW, 0);
        Billetera tgtWallet = new Billetera("W_B", "Ahorros", "SAVINGS", "USR_B", 0.0, true, NOW, 0);

        when(userRepository.findById("USR_A")).thenReturn(Optional.of(srcUser));
        when(userRepository.findById("USR_B")).thenReturn(Optional.of(tgtUser));
        when(walletRepository.findByOwnerIdAndCode("USR_A", "W_A")).thenReturn(Optional.of(srcWallet));
        when(walletRepository.findByOwnerIdAndCode("USR_B", "W_B")).thenReturn(Optional.of(tgtWallet));
        when(idGenerator.next()).thenReturn("TX-000001", "TX-000002");
        when(transactionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        useCase.execute("USR_A", "W_A", "USR_B", "W_B", 200.0, null);

        verify(graphRepository, times(1)).addEdge("USR_A", "USR_B", 200.0);
    }

    // T11-C03: fraud detected → saves event
    @Test
    void execute_amountAboveThreshold_savesFraudEventOnce() {
        Usuario srcUser = new Usuario("USR_A", "Ana", "ana@test.com", NOW, 0.0, LoyaltyLevel.BRONZE);
        Usuario tgtUser = new Usuario("USR_B", "Bob", "bob@test.com", NOW, 0.0, LoyaltyLevel.BRONZE);
        Billetera srcWallet = new Billetera("W_A", "Ahorros", "SAVINGS", "USR_A", 200000.0, true, NOW, 0);
        Billetera tgtWallet = new Billetera("W_B", "Ahorros", "SAVINGS", "USR_B", 0.0, true, NOW, 0);

        when(userRepository.findById("USR_A")).thenReturn(Optional.of(srcUser));
        when(userRepository.findById("USR_B")).thenReturn(Optional.of(tgtUser));
        when(walletRepository.findByOwnerIdAndCode("USR_A", "W_A")).thenReturn(Optional.of(srcWallet));
        when(walletRepository.findByOwnerIdAndCode("USR_B", "W_B")).thenReturn(Optional.of(tgtWallet));
        when(idGenerator.next()).thenReturn("TX-000001", "TX-000002");
        when(transactionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        FraudEvent fakeEvent = new FraudEvent("FRD-001", "USR_A", "TX-000001",
                "LARGE_TRANSACTION", FraudSeverity.HIGH, "Test", NOW);
        when(fraudDetector.detect(any())).thenReturn(Optional.of(fakeEvent));

        useCase.execute("USR_A", "W_A", "USR_B", "W_B", 15000.0, null);

        verify(fraudRepository, times(1)).save(fakeEvent);
    }

    // unknown target user → USER_NOT_FOUND
    @Test
    void execute_unknownTargetUser_throwsUserNotFound() {
        Usuario srcUser = new Usuario("USR_A", "Ana", "ana@test.com", NOW, 0.0, LoyaltyLevel.BRONZE);
        Billetera srcWallet = new Billetera("W_A", "Ahorros", "SAVINGS", "USR_A", 500.0, true, NOW, 0);

        when(userRepository.findById("USR_A")).thenReturn(Optional.of(srcUser));
        when(userRepository.findById("GHOST")).thenReturn(Optional.empty());
        when(walletRepository.findByOwnerIdAndCode("USR_A", "W_A")).thenReturn(Optional.of(srcWallet));

        assertThatThrownBy(() -> useCase.execute("USR_A", "W_A", "GHOST", "W_B", 100.0, null))
                .isInstanceOf(NotFoundException.class)
                .satisfies(ex -> assertThat(((NotFoundException) ex).code())
                        .isEqualTo(ErrorCode.USER_NOT_FOUND));
    }

    // T3.3.3 — domain validation failure → OPERATION_REJECTED notification emitted
    @Test
    void execute_validationFailure_emitsOperationRejected() {
        // same source and target user → VALIDATION_ERROR
        assertThatThrownBy(() -> useCase.execute("USR_A", "W_A", "USR_A", "W_B", 100.0, null))
                .isInstanceOf(BusinessRuleException.class);
        verify(notificationEmitter, times(1)).emitOperationRejected(eq("USR_A"), any());
    }
}
