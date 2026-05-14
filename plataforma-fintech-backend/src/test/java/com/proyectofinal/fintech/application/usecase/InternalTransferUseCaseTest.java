package com.proyectofinal.fintech.application.usecase;

import com.proyectofinal.fintech.application.service.NotificationEmitter;
import com.proyectofinal.fintech.domain.exception.BusinessRuleException;
import com.proyectofinal.fintech.domain.exception.ErrorCode;
import com.proyectofinal.fintech.domain.exception.NotFoundException;
import com.proyectofinal.fintech.domain.model.*;
import com.proyectofinal.fintech.domain.port.FraudEventRepository;
import com.proyectofinal.fintech.domain.port.ReversibleOperationsStack;
import com.proyectofinal.fintech.domain.port.TransactionIdGenerator;
import com.proyectofinal.fintech.domain.port.TransactionRepository;
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
 * T05-B13 / T11-A05 / T11-C03 / T11-D03 (RED) — InternalTransferUseCase tests.
 * Updated: PDF-compliant points, instance FraudDetector, LOW_BALANCE, level-up.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class InternalTransferUseCaseTest {

    @Mock private UserRepository userRepository;
    @Mock private WalletRepository walletRepository;
    @Mock private TransactionRepository transactionRepository;
    @Mock private TransactionIdGenerator idGenerator;
    @Mock private ReversibleOperationsStack stack;
    @Mock private FraudEventRepository fraudRepository;
    @Mock private FraudDetector fraudDetector;
    @Mock private NotificationEmitter notificationEmitter;

    private Clock fixedClock;
    private InternalTransferUseCase useCase;

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

    @BeforeEach
    void setUp() {
        fixedClock = Clock.fixed(NOW, ZoneId.of("UTC"));
        useCase = new InternalTransferUseCase(userRepository, walletRepository,
                transactionRepository, idGenerator, fixedClock, stack,
                fraudRepository, fraudDetector, notificationEmitter);
        when(fraudDetector.detect(any())).thenReturn(Optional.empty());
    }

    // S-UC3-01: happy path — PDF: internal 1000 → floor(1000/100)*3 = 30 pts
    @Test
    void execute_happyPath_transfersBalanceAndUpdatesPoints() {
        Usuario user = new Usuario("USR001", "Ana", "ana@test.com", NOW, 0.0, LoyaltyLevel.BRONZE);
        Billetera src = new Billetera("W001", "Ahorros", "SAVINGS", "USR001", 1500.0, true, NOW, 0);
        Billetera tgt = new Billetera("W002", "Gastos", "DAILY", "USR001", 0.0, true, NOW, 0);

        when(userRepository.findById("USR001")).thenReturn(Optional.of(user));
        when(walletRepository.findByOwnerIdAndCode("USR001", "W001")).thenReturn(Optional.of(src));
        when(walletRepository.findByOwnerIdAndCode("USR001", "W002")).thenReturn(Optional.of(tgt));
        when(idGenerator.next()).thenReturn("TX-000001");
        when(transactionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Transaccion tx = useCase.execute("USR001", "W001", "W002", 1000.0, null);

        assertThat(src.getBalance()).isEqualTo(500.0);
        assertThat(tgt.getBalance()).isEqualTo(1000.0);
        assertThat(src.getTransactionCount()).isEqualTo(1);
        assertThat(tgt.getTransactionCount()).isEqualTo(1);
        // PDF: floor(1000/100)*3 = 30 pts
        assertThat(user.getPoints()).isEqualTo(30.0);
        assertThat(tx.getType()).isEqualTo(TransactionType.INTERNAL_TRANSFER);
        verify(stack, times(1)).push(tx);
    }

    // S-UC3-02: same wallet → VALIDATION_ERROR
    @Test
    void execute_sameSourceAndTarget_throwsValidationError() {
        assertThatThrownBy(() -> useCase.execute("USR001", "W001", "W001", 100.0, null))
                .isInstanceOf(BusinessRuleException.class)
                .satisfies(ex -> assertThat(((BusinessRuleException) ex).code())
                        .isEqualTo(ErrorCode.VALIDATION_ERROR));
        verifyNoInteractions(userRepository, walletRepository);
    }

    // S-UC3-03: wallet not owned by user → WALLET_NOT_FOUND
    @Test
    void execute_walletNotOwnedByUser_throwsWalletNotFound() {
        Usuario user = new Usuario("USR001", "Ana", "ana@test.com", NOW, 0.0, LoyaltyLevel.BRONZE);
        Billetera src = new Billetera("W001", "Ahorros", "SAVINGS", "USR001", 500.0, true, NOW, 0);

        when(userRepository.findById("USR001")).thenReturn(Optional.of(user));
        when(walletRepository.findByOwnerIdAndCode("USR001", "W001")).thenReturn(Optional.of(src));
        when(walletRepository.findByOwnerIdAndCode("USR001", "W002")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute("USR001", "W001", "W002", 100.0, null))
                .isInstanceOf(NotFoundException.class)
                .satisfies(ex -> assertThat(((NotFoundException) ex).code())
                        .isEqualTo(ErrorCode.WALLET_NOT_FOUND));
    }

    // insufficient funds → INSUFFICIENT_FUNDS
    @Test
    void execute_insufficientFunds_throwsInsufficientFunds() {
        Usuario user = new Usuario("USR001", "Ana", "ana@test.com", NOW, 0.0, LoyaltyLevel.BRONZE);
        Billetera src = new Billetera("W001", "Ahorros", "SAVINGS", "USR001", 50.0, true, NOW, 0);
        Billetera tgt = new Billetera("W002", "Gastos", "DAILY", "USR001", 0.0, true, NOW, 0);

        when(userRepository.findById("USR001")).thenReturn(Optional.of(user));
        when(walletRepository.findByOwnerIdAndCode("USR001", "W001")).thenReturn(Optional.of(src));
        when(walletRepository.findByOwnerIdAndCode("USR001", "W002")).thenReturn(Optional.of(tgt));

        assertThatThrownBy(() -> useCase.execute("USR001", "W001", "W002", 200.0, null))
                .isInstanceOf(BusinessRuleException.class)
                .satisfies(ex -> assertThat(((BusinessRuleException) ex).code())
                        .isEqualTo(ErrorCode.INSUFFICIENT_FUNDS));

        assertThat(src.getBalance()).isEqualTo(50.0);
        verify(transactionRepository, never()).save(any());
    }

    // T11-D03: LOW_BALANCE alert on source wallet
    @Test
    void execute_sourceBalanceDropsBelow100_emitsLowBalance() {
        Usuario user = new Usuario("USR001", "Ana", "ana@test.com", NOW, 0.0, LoyaltyLevel.BRONZE);
        Billetera src = new Billetera("W001", "Ahorros", "SAVINGS", "USR001", 150.0, true, NOW, 0);
        Billetera tgt = new Billetera("W002", "Gastos", "DAILY", "USR001", 0.0, true, NOW, 0);

        when(userRepository.findById("USR001")).thenReturn(Optional.of(user));
        when(walletRepository.findByOwnerIdAndCode("USR001", "W001")).thenReturn(Optional.of(src));
        when(walletRepository.findByOwnerIdAndCode("USR001", "W002")).thenReturn(Optional.of(tgt));
        when(idGenerator.next()).thenReturn("TX-001");
        when(transactionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        useCase.execute("USR001", "W001", "W002", 100.0, null);

        // src balance = 50 < 100 → LOW_BALANCE
        verify(notificationEmitter, times(1)).emitLowBalance("USR001", "W001");
    }

    // T11-C03: above fraud threshold → saves event
    @Test
    void execute_amountAboveThreshold_savesFraudEvent() {
        Usuario user = new Usuario("USR001", "Ana", "ana@test.com", NOW, 0.0, LoyaltyLevel.BRONZE);
        Billetera src = new Billetera("W001", "Ahorros", "SAVINGS", "USR001", 500000.0, true, NOW, 0);
        Billetera tgt = new Billetera("W002", "Gastos", "DAILY", "USR001", 0.0, true, NOW, 0);

        when(userRepository.findById("USR001")).thenReturn(Optional.of(user));
        when(walletRepository.findByOwnerIdAndCode("USR001", "W001")).thenReturn(Optional.of(src));
        when(walletRepository.findByOwnerIdAndCode("USR001", "W002")).thenReturn(Optional.of(tgt));
        when(idGenerator.next()).thenReturn("TX-000001");
        when(transactionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        FraudEvent fakeEvent = new FraudEvent("FRD-001", "USR001", "TX-000001",
                "LARGE_TRANSACTION", FraudSeverity.HIGH, "Test", NOW);
        when(fraudDetector.detect(any())).thenReturn(Optional.of(fakeEvent));

        useCase.execute("USR001", "W001", "W002", 20000.0, null);

        verify(fraudRepository, times(1)).save(fakeEvent);
    }

    // zero amount → VALIDATION_ERROR
    @Test
    void execute_zeroAmount_throwsValidationError() {
        assertThatThrownBy(() -> useCase.execute("USR001", "W001", "W002", 0.0, null))
                .isInstanceOf(BusinessRuleException.class)
                .satisfies(ex -> assertThat(((BusinessRuleException) ex).code())
                        .isEqualTo(ErrorCode.VALIDATION_ERROR));
    }

    // T3.3.3 — domain validation failure → OPERATION_REJECTED notification emitted
    @Test
    void execute_validationFailure_emitsOperationRejected() {
        // same source and target wallet → VALIDATION_ERROR
        assertThatThrownBy(() -> useCase.execute("USR001", "W001", "W001", 100.0, null))
                .isInstanceOf(BusinessRuleException.class);
        verify(notificationEmitter, times(1)).emitOperationRejected(eq("USR001"), any());
    }
}
