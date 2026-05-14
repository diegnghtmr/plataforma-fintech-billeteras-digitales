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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * T05-B11 / T11-A05 / T11-C03 / T11-D03 (RED) — WithdrawWalletUseCase tests.
 * Updated: PDF-compliant points, instance FraudDetector, LOW_BALANCE notifications, level-up.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class WithdrawWalletUseCaseTest {

    @Mock private UserRepository userRepository;
    @Mock private WalletRepository walletRepository;
    @Mock private TransactionRepository transactionRepository;
    @Mock private TransactionIdGenerator idGenerator;
    @Mock private ReversibleOperationsStack stack;
    @Mock private FraudEventRepository fraudRepository;
    @Mock private FraudDetector fraudDetector;
    @Mock private NotificationEmitter notificationEmitter;

    private Clock fixedClock;
    private WithdrawWalletUseCase useCase;

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

    @BeforeEach
    void setUp() {
        fixedClock = Clock.fixed(NOW, ZoneId.of("UTC"));
        useCase = new WithdrawWalletUseCase(userRepository, walletRepository,
                transactionRepository, idGenerator, fixedClock, stack,
                fraudRepository, fraudDetector, notificationEmitter);
        when(fraudDetector.detect(any())).thenReturn(Optional.empty());
    }

    // S-UC2-01: happy path
    @Test
    void execute_happyPath_decrementsBalanceAndReturnsTransaction() {
        Usuario user = new Usuario("USR001", "Ana", "ana@test.com", NOW, 5.0, LoyaltyLevel.BRONZE);
        Billetera wallet = new Billetera("W001", "Ahorros", "SAVINGS", "USR001", 500.0, true, NOW, 3);

        when(userRepository.findById("USR001")).thenReturn(Optional.of(user));
        when(walletRepository.findByOwnerIdAndCode("USR001", "W001")).thenReturn(Optional.of(wallet));
        when(idGenerator.next()).thenReturn("TX-000001");
        when(transactionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Transaccion tx = useCase.execute("USR001", "W001", 200.0, null);

        assertThat(wallet.getBalance()).isEqualTo(300.0);
        assertThat(wallet.getTransactionCount()).isEqualTo(4);
        assertThat(tx.getType()).isEqualTo(TransactionType.WITHDRAWAL);
        verify(stack, times(1)).push(tx);
    }

    // PDF points: WITHDRAWAL 300 → floor(300/100)*2 = 6 pts
    @Test
    void execute_withdrawal300_earns6Points() {
        Usuario user = new Usuario("USR001", "Ana", "ana@test.com", NOW, 0.0, LoyaltyLevel.BRONZE);
        Billetera wallet = new Billetera("W001", "Ahorros", "SAVINGS", "USR001", 500.0, true, NOW, 0);

        when(userRepository.findById("USR001")).thenReturn(Optional.of(user));
        when(walletRepository.findByOwnerIdAndCode("USR001", "W001")).thenReturn(Optional.of(wallet));
        when(idGenerator.next()).thenReturn("TX-001");
        when(transactionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        useCase.execute("USR001", "W001", 300.0, null);

        assertThat(user.getPoints()).isEqualTo(6.0);
    }

    // S-UC2-02: insufficient funds → BusinessRuleException
    @Test
    void execute_insufficientFunds_throwsInsufficientFunds() {
        Usuario user = new Usuario("USR001", "Ana", "ana@test.com", NOW, 0.0, LoyaltyLevel.BRONZE);
        Billetera wallet = new Billetera("W001", "Ahorros", "SAVINGS", "USR001", 100.0, true, NOW, 0);

        when(userRepository.findById("USR001")).thenReturn(Optional.of(user));
        when(walletRepository.findByOwnerIdAndCode("USR001", "W001")).thenReturn(Optional.of(wallet));

        assertThatThrownBy(() -> useCase.execute("USR001", "W001", 200.0, null))
                .isInstanceOf(BusinessRuleException.class)
                .satisfies(ex -> assertThat(((BusinessRuleException) ex).code())
                        .isEqualTo(ErrorCode.INSUFFICIENT_FUNDS));

        assertThat(wallet.getBalance()).isEqualTo(100.0);
        verify(transactionRepository, never()).save(any());
    }

    // invalid amount
    @Test
    void execute_zeroAmount_throwsValidationError() {
        assertThatThrownBy(() -> useCase.execute("USR001", "W001", 0.0, null))
                .isInstanceOf(BusinessRuleException.class)
                .satisfies(ex -> assertThat(((BusinessRuleException) ex).code())
                        .isEqualTo(ErrorCode.VALIDATION_ERROR));
    }

    // unknown user
    @Test
    void execute_unknownUser_throwsUserNotFound() {
        when(userRepository.findById("GHOST")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> useCase.execute("GHOST", "W001", 100.0, null))
                .isInstanceOf(NotFoundException.class)
                .satisfies(ex -> assertThat(((NotFoundException) ex).code())
                        .isEqualTo(ErrorCode.USER_NOT_FOUND));
    }

    // T11-D03: LOW_BALANCE alert when balance drops below 100
    @Test
    void execute_balanceDropsBelow100_emitsLowBalanceAlert() {
        Usuario user = new Usuario("USR001", "Ana", "ana@test.com", NOW, 0.0, LoyaltyLevel.BRONZE);
        Billetera wallet = new Billetera("W001", "Ahorros", "SAVINGS", "USR001", 150.0, true, NOW, 0);

        when(userRepository.findById("USR001")).thenReturn(Optional.of(user));
        when(walletRepository.findByOwnerIdAndCode("USR001", "W001")).thenReturn(Optional.of(wallet));
        when(idGenerator.next()).thenReturn("TX-001");
        when(transactionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        useCase.execute("USR001", "W001", 100.0, null);

        // balance = 150 - 100 = 50 < 100 → LOW_BALANCE
        verify(notificationEmitter, times(1)).emitLowBalance("USR001", "W001");
    }

    @Test
    void execute_balanceAbove100_noLowBalanceAlert() {
        Usuario user = new Usuario("USR001", "Ana", "ana@test.com", NOW, 0.0, LoyaltyLevel.BRONZE);
        Billetera wallet = new Billetera("W001", "Ahorros", "SAVINGS", "USR001", 500.0, true, NOW, 0);

        when(userRepository.findById("USR001")).thenReturn(Optional.of(user));
        when(walletRepository.findByOwnerIdAndCode("USR001", "W001")).thenReturn(Optional.of(wallet));
        when(idGenerator.next()).thenReturn("TX-001");
        when(transactionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        useCase.execute("USR001", "W001", 100.0, null);

        // balance = 400 ≥ 100 → no LOW_BALANCE
        verify(notificationEmitter, never()).emitLowBalance(any(), any());
    }

    // T11-C03: fraud detected → saves event
    @Test
    void execute_fraudDetected_savesFraudEvent() {
        Usuario user = new Usuario("USR001", "Ana", "ana@test.com", NOW, 0.0, LoyaltyLevel.BRONZE);
        Billetera wallet = new Billetera("W001", "Ahorros", "SAVINGS", "USR001", 50000.0, true, NOW, 0);

        when(userRepository.findById("USR001")).thenReturn(Optional.of(user));
        when(walletRepository.findByOwnerIdAndCode("USR001", "W001")).thenReturn(Optional.of(wallet));
        when(idGenerator.next()).thenReturn("TX-001");
        when(transactionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        FraudEvent fakeEvent = new FraudEvent("FRD-001", "USR001", "TX-001",
                "LARGE_TRANSACTION", FraudSeverity.HIGH, "Test", NOW);
        when(fraudDetector.detect(any())).thenReturn(Optional.of(fakeEvent));

        useCase.execute("USR001", "W001", 15000.0, null);

        verify(fraudRepository, times(1)).save(fakeEvent);
    }

    // T3.3.3 — domain validation failure → OPERATION_REJECTED notification emitted
    @Test
    void execute_validationFailure_emitsOperationRejected() {
        assertThatThrownBy(() -> useCase.execute("USR001", "W001", -1.0, null))
                .isInstanceOf(BusinessRuleException.class);
        verify(notificationEmitter, times(1)).emitOperationRejected(eq("USR001"), any());
    }
}
