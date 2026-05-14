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
import static org.mockito.Mockito.*;

/**
 * T05-B09 / T11-A05 / T11-C03 (RED) — RechargeWalletUseCase tests.
 * Updated for PDF-compliant points, instance FraudDetector, and level-up notifications.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RechargeWalletUseCaseTest {

    @Mock private UserRepository userRepository;
    @Mock private WalletRepository walletRepository;
    @Mock private TransactionRepository transactionRepository;
    @Mock private TransactionIdGenerator idGenerator;
    @Mock private ReversibleOperationsStack stack;
    @Mock private FraudEventRepository fraudRepository;
    @Mock private FraudDetector fraudDetector;
    @Mock private NotificationEmitter notificationEmitter;

    private Clock fixedClock;
    private RechargeWalletUseCase useCase;

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

    @BeforeEach
    void setUp() {
        fixedClock = Clock.fixed(NOW, ZoneId.of("UTC"));
        useCase = new RechargeWalletUseCase(userRepository, walletRepository,
                transactionRepository, idGenerator, fixedClock, stack,
                fraudRepository, fraudDetector, notificationEmitter);
        // Default: no fraud detected
        when(fraudDetector.detect(any())).thenReturn(Optional.empty());
    }

    // S-UC1-01: happy path — PDF points: recharge 1000 → floor(1000/100)*1 = 10
    @Test
    void execute_happyPath_updatesBalancePointsAndReturnsTransaction() {
        Usuario user = new Usuario("USR001", "Ana", "ana@test.com", NOW, 0.0, LoyaltyLevel.BRONZE);
        Billetera wallet = new Billetera("W001", "Ahorros", "SAVINGS", "USR001", 0.0, true, NOW, 0);

        when(userRepository.findById("USR001")).thenReturn(Optional.of(user));
        when(walletRepository.findByOwnerIdAndCode("USR001", "W001")).thenReturn(Optional.of(wallet));
        when(idGenerator.next()).thenReturn("TX-000001");
        when(transactionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Transaccion tx = useCase.execute("USR001", "W001", 1000.0, null);

        assertThat(wallet.getBalance()).isEqualTo(1000.0);
        assertThat(wallet.getTransactionCount()).isEqualTo(1);
        // PDF: floor(1000/100)*1 = 10
        assertThat(user.getPoints()).isEqualTo(10.0);
        assertThat(tx.getId()).isEqualTo("TX-000001");
        assertThat(tx.getType()).isEqualTo(TransactionType.RECHARGE);
        assertThat(tx.getStatus()).isEqualTo(TransactionStatus.SUCCESSFUL);
        assertThat(tx.getTimestamp()).isEqualTo(NOW);
        verify(stack, times(1)).push(tx);
    }

    // S-UC1-02: invalid amount → BusinessRuleException(VALIDATION_ERROR)
    @Test
    void execute_zeroAmount_throwsValidationError() {
        assertThatThrownBy(() -> useCase.execute("USR001", "W001", 0.0, null))
                .isInstanceOf(BusinessRuleException.class)
                .satisfies(ex -> assertThat(((BusinessRuleException) ex).code())
                        .isEqualTo(ErrorCode.VALIDATION_ERROR));
        verifyNoInteractions(userRepository, walletRepository, transactionRepository);
    }

    @Test
    void execute_negativeAmount_throwsValidationError() {
        assertThatThrownBy(() -> useCase.execute("USR001", "W001", -50.0, null))
                .isInstanceOf(BusinessRuleException.class)
                .satisfies(ex -> assertThat(((BusinessRuleException) ex).code())
                        .isEqualTo(ErrorCode.VALIDATION_ERROR));
    }

    // S-UC1-03: unknown user → NotFoundException(USER_NOT_FOUND)
    @Test
    void execute_unknownUser_throwsUserNotFound() {
        when(userRepository.findById("GHOST")).thenReturn(Optional.empty());
        assertThatThrownBy(() -> useCase.execute("GHOST", "W001", 100.0, null))
                .isInstanceOf(NotFoundException.class)
                .satisfies(ex -> assertThat(((NotFoundException) ex).code())
                        .isEqualTo(ErrorCode.USER_NOT_FOUND));
    }

    @Test
    void execute_unknownWallet_throwsWalletNotFound() {
        Usuario user = new Usuario("USR001", "Ana", "ana@test.com", NOW, 0.0, LoyaltyLevel.BRONZE);
        when(userRepository.findById("USR001")).thenReturn(Optional.of(user));
        when(walletRepository.findByOwnerIdAndCode("USR001", "GHOST_WALLET")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute("USR001", "GHOST_WALLET", 100.0, null))
                .isInstanceOf(NotFoundException.class)
                .satisfies(ex -> assertThat(((NotFoundException) ex).code())
                        .isEqualTo(ErrorCode.WALLET_NOT_FOUND));
    }

    // T11-A05: loyalty level updated with new PDF thresholds — recharge 55000 → 550 pts → SILVER
    @Test
    void execute_happyPath_loyaltyLevelUpdatedToSilver() {
        Usuario user = new Usuario("USR001", "Ana", "ana@test.com", NOW, 0.0, LoyaltyLevel.BRONZE);
        Billetera wallet = new Billetera("W001", "Ahorros", "SAVINGS", "USR001", 0.0, true, NOW, 0);

        when(userRepository.findById("USR001")).thenReturn(Optional.of(user));
        when(walletRepository.findByOwnerIdAndCode("USR001", "W001")).thenReturn(Optional.of(wallet));
        when(idGenerator.next()).thenReturn("TX-000001");
        when(transactionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        useCase.execute("USR001", "W001", 55000.0, null);

        // floor(55000/100)*1 = 550 pts → SILVER (501-1000)
        assertThat(user.getPoints()).isEqualTo(550.0);
        assertThat(user.getLoyaltyLevel()).isEqualTo(LoyaltyLevel.SILVER);
    }

    // T11-C03: fraud detected → fraudRepo.save + tx.markRiskLevel called
    @Test
    void execute_fraudDetected_savesEventAndMarksRiskLevel() {
        Usuario user = new Usuario("USR001", "Ana", "ana@test.com", NOW, 0.0, LoyaltyLevel.BRONZE);
        Billetera wallet = new Billetera("W001", "Ahorros", "SAVINGS", "USR001", 0.0, true, NOW, 0);

        when(userRepository.findById("USR001")).thenReturn(Optional.of(user));
        when(walletRepository.findByOwnerIdAndCode("USR001", "W001")).thenReturn(Optional.of(wallet));
        when(idGenerator.next()).thenReturn("TX-000001");
        when(transactionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        FraudEvent fakeEvent = new FraudEvent("FRD-001", "USR001", "TX-000001",
                "LARGE_TRANSACTION", FraudSeverity.HIGH, "Test", NOW);
        when(fraudDetector.detect(any())).thenReturn(Optional.of(fakeEvent));

        Transaccion tx = useCase.execute("USR001", "W001", 15000.0, null);

        verify(fraudRepository, times(1)).save(fakeEvent);
        assertThat(tx.getRiskLevel()).isEqualTo(FraudSeverity.HIGH);
    }

    // T11-C03: no fraud → fraudRepo.save NOT called
    @Test
    void execute_noFraud_doesNotSaveFraudEvent() {
        Usuario user = new Usuario("USR001", "Ana", "ana@test.com", NOW, 0.0, LoyaltyLevel.BRONZE);
        Billetera wallet = new Billetera("W001", "Ahorros", "SAVINGS", "USR001", 0.0, true, NOW, 0);

        when(userRepository.findById("USR001")).thenReturn(Optional.of(user));
        when(walletRepository.findByOwnerIdAndCode("USR001", "W001")).thenReturn(Optional.of(wallet));
        when(idGenerator.next()).thenReturn("TX-000001");
        when(transactionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        useCase.execute("USR001", "W001", 100.0, null);

        verify(fraudRepository, never()).save(any());
    }

    // T11-D05: level-up notification when addPoints returns true
    @Test
    void execute_levelUp_emitsLevelUpNotification() {
        // 480 pts → BRONZE; recharge 2100 → floor(2100/100)*1 = 21 pts → 501 total → SILVER
        Usuario user = new Usuario("USR001", "Ana", "ana@test.com", NOW, 480.0, LoyaltyLevel.BRONZE);
        Billetera wallet = new Billetera("W001", "Ahorros", "SAVINGS", "USR001", 0.0, true, NOW, 0);

        when(userRepository.findById("USR001")).thenReturn(Optional.of(user));
        when(walletRepository.findByOwnerIdAndCode("USR001", "W001")).thenReturn(Optional.of(wallet));
        when(idGenerator.next()).thenReturn("TX-001");
        when(transactionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        useCase.execute("USR001", "W001", 2100.0, null);

        verify(notificationEmitter, times(1)).emitLevelUp("USR001", LoyaltyLevel.SILVER);
    }

    // T3.3.3 — domain validation failure → OPERATION_REJECTED notification emitted
    @Test
    void execute_validationFailure_emitsOperationRejected() {
        assertThatThrownBy(() -> useCase.execute("USR001", "W001", -1.0, null))
                .isInstanceOf(BusinessRuleException.class);
        verify(notificationEmitter, times(1)).emitOperationRejected(eq("USR001"), any());
    }

    @Test
    void execute_withDescription_propagatesDescription() {
        Usuario user = new Usuario("USR001", "Ana", "ana@test.com", NOW, 0.0, LoyaltyLevel.BRONZE);
        Billetera wallet = new Billetera("W001", "Ahorros", "SAVINGS", "USR001", 0.0, true, NOW, 0);

        when(userRepository.findById("USR001")).thenReturn(Optional.of(user));
        when(walletRepository.findByOwnerIdAndCode("USR001", "W001")).thenReturn(Optional.of(wallet));
        when(idGenerator.next()).thenReturn("TX-000001");
        when(transactionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Transaccion tx = useCase.execute("USR001", "W001", 500.0, "recarga navidad");

        assertThat(tx.getDescription()).isEqualTo("recarga navidad");
    }
}
