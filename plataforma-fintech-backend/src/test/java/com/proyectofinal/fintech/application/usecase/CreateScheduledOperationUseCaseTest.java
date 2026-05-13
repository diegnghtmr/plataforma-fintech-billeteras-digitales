package com.proyectofinal.fintech.application.usecase;

import com.proyectofinal.fintech.domain.exception.BusinessRuleException;
import com.proyectofinal.fintech.domain.exception.ErrorCode;
import com.proyectofinal.fintech.domain.exception.NotFoundException;
import com.proyectofinal.fintech.domain.model.*;
import com.proyectofinal.fintech.domain.port.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * T08-B14 (RED) — CreateScheduledOperationUseCase tests.
 * Covers scenarios S1, S2, S3.
 */
@ExtendWith(MockitoExtension.class)
class CreateScheduledOperationUseCaseTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private WalletRepository walletRepository;
    @Mock
    private ScheduledOperationRepository operationRepository;
    @Mock
    private ScheduledOperationIdGenerator idGenerator;

    private CreateScheduledOperationUseCase useCase;

    private static final Instant FUTURE = Instant.parse("2027-01-01T00:00:00Z");

    private Usuario makeUser(String id) {
        return new Usuario(id, "Name", "email@test.com", Instant.now(), 0.0, LoyaltyLevel.BRONZE);
    }

    private Billetera makeWallet(String code, String ownerId) {
        return new Billetera(code, "Wallet", "SAVINGS", ownerId, 1000.0, true, Instant.now(), 0);
    }

    @BeforeEach
    void setUp() {
        useCase = new CreateScheduledOperationUseCase(
                userRepository, walletRepository, operationRepository, idGenerator);
    }

    // S1: Happy path - RECHARGE
    @Test
    void execute_happyPath_recharge_returnsPendingOp() {
        when(idGenerator.next()).thenReturn("SOP-000001");
        when(userRepository.findById("USR001")).thenReturn(Optional.of(makeUser("USR001")));
        when(walletRepository.findByOwnerIdAndCode("USR001", "W001"))
                .thenReturn(Optional.of(makeWallet("W001", "USR001")));

        OperacionProgramada result = useCase.execute(
                ScheduledOperationType.RECHARGE, "USR001", "W001",
                null, null, 100.0, FUTURE, null);

        assertEquals("SOP-000001", result.getId());
        assertEquals(ScheduledOperationStatus.PENDING, result.getStatus());
        verify(operationRepository).save(any(OperacionProgramada.class));
    }

    // S2: Source user not found
    @Test
    void execute_userNotFound_throwsNotFoundException() {
        when(userRepository.findById("GHOST")).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> useCase.execute(ScheduledOperationType.RECHARGE, "GHOST", "W001",
                        null, null, 100.0, FUTURE, null));

        assertEquals(ErrorCode.USER_NOT_FOUND, ex.code());
        verify(operationRepository, never()).save(any());
    }

    // Source wallet not found
    @Test
    void execute_walletNotFound_throwsNotFoundException() {
        when(userRepository.findById("USR001")).thenReturn(Optional.of(makeUser("USR001")));
        when(walletRepository.findByOwnerIdAndCode("USR001", "GHOST")).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> useCase.execute(ScheduledOperationType.RECHARGE, "USR001", "GHOST",
                        null, null, 100.0, FUTURE, null));

        assertEquals(ErrorCode.WALLET_NOT_FOUND, ex.code());
    }

    // S3: EXTERNAL_TRANSFER missing target
    @Test
    void execute_externalTransfer_missingTarget_throwsBusinessRuleException() {
        when(userRepository.findById("USR001")).thenReturn(Optional.of(makeUser("USR001")));
        when(walletRepository.findByOwnerIdAndCode("USR001", "W001"))
                .thenReturn(Optional.of(makeWallet("W001", "USR001")));

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> useCase.execute(ScheduledOperationType.EXTERNAL_TRANSFER, "USR001", "W001",
                        null, null, 100.0, FUTURE, null));

        assertEquals(ErrorCode.VALIDATION_ERROR, ex.code());
    }

    // INTERNAL_TRANSFER missing target
    @Test
    void execute_internalTransfer_missingTarget_throwsBusinessRuleException() {
        when(userRepository.findById("USR001")).thenReturn(Optional.of(makeUser("USR001")));
        when(walletRepository.findByOwnerIdAndCode("USR001", "W001"))
                .thenReturn(Optional.of(makeWallet("W001", "USR001")));

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> useCase.execute(ScheduledOperationType.INTERNAL_TRANSFER, "USR001", "W001",
                        null, null, 100.0, FUTURE, null));

        assertEquals(ErrorCode.VALIDATION_ERROR, ex.code());
    }

    // EXTERNAL_TRANSFER same user
    @Test
    void execute_externalTransfer_sameUser_throwsBusinessRuleException() {
        when(userRepository.findById("USR001")).thenReturn(Optional.of(makeUser("USR001")));
        when(walletRepository.findByOwnerIdAndCode("USR001", "W001"))
                .thenReturn(Optional.of(makeWallet("W001", "USR001")));

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> useCase.execute(ScheduledOperationType.EXTERNAL_TRANSFER, "USR001", "W001",
                        "USR001", "W002", 100.0, FUTURE, null));

        assertEquals(ErrorCode.VALIDATION_ERROR, ex.code());
    }

    // Amount < 0.01
    @Test
    void execute_invalidAmount_throwsBusinessRuleException() {
        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> useCase.execute(ScheduledOperationType.RECHARGE, "USR001", "W001",
                        null, null, 0.0, FUTURE, null));

        assertEquals(ErrorCode.VALIDATION_ERROR, ex.code());
        verifyNoInteractions(userRepository);
    }

    // INTERNAL_TRANSFER happy path with target
    @Test
    void execute_internalTransfer_happyPath() {
        when(idGenerator.next()).thenReturn("SOP-000002");
        when(userRepository.findById("USR001")).thenReturn(Optional.of(makeUser("USR001")));
        when(walletRepository.findByOwnerIdAndCode("USR001", "W001"))
                .thenReturn(Optional.of(makeWallet("W001", "USR001")));

        OperacionProgramada result = useCase.execute(
                ScheduledOperationType.INTERNAL_TRANSFER, "USR001", "W001",
                "USR001", "W002", 50.0, FUTURE, null);

        assertEquals(ScheduledOperationStatus.PENDING, result.getStatus());
    }

    // EXTERNAL_TRANSFER happy path
    @Test
    void execute_externalTransfer_happyPath() {
        when(idGenerator.next()).thenReturn("SOP-000003");
        when(userRepository.findById("USR001")).thenReturn(Optional.of(makeUser("USR001")));
        when(walletRepository.findByOwnerIdAndCode("USR001", "W001"))
                .thenReturn(Optional.of(makeWallet("W001", "USR001")));

        OperacionProgramada result = useCase.execute(
                ScheduledOperationType.EXTERNAL_TRANSFER, "USR001", "W001",
                "USR002", "W002", 200.0, FUTURE, "desc");

        assertEquals(ScheduledOperationStatus.PENDING, result.getStatus());
        assertEquals("USR002", result.getTargetUserId());
    }
}
