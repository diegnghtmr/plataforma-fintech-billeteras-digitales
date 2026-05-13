package com.proyectofinal.fintech.application.usecase;

import com.proyectofinal.fintech.domain.exception.BusinessRuleException;
import com.proyectofinal.fintech.domain.exception.ErrorCode;
import com.proyectofinal.fintech.domain.exception.NotFoundException;
import com.proyectofinal.fintech.domain.model.*;
import com.proyectofinal.fintech.domain.port.ScheduledOperationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * T08-B18 (RED) — CancelScheduledOperationUseCase tests.
 * Covers scenarios S5, S6, S7.
 */
@ExtendWith(MockitoExtension.class)
class CancelScheduledOperationUseCaseTest {

    @Mock
    private ScheduledOperationRepository operationRepository;

    private CancelScheduledOperationUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new CancelScheduledOperationUseCase(operationRepository);
    }

    private OperacionProgramada makeOp(String id, ScheduledOperationStatus status) {
        return new OperacionProgramada(id, ScheduledOperationType.RECHARGE, status,
                "USR001", "W001", null, null,
                100.0, Instant.parse("2026-06-01T00:00:00Z"), null);
    }

    // S5: Happy path — PENDING → CANCELLED
    @Test
    void execute_pendingOp_cancelledAndSaved() {
        OperacionProgramada op = makeOp("SOP-000001", ScheduledOperationStatus.PENDING);
        when(operationRepository.findById("SOP-000001")).thenReturn(Optional.of(op));

        OperacionProgramada result = useCase.execute("SOP-000001");

        assertEquals(ScheduledOperationStatus.CANCELLED, result.getStatus());
        verify(operationRepository).save(op);
    }

    // S6: Operation not found — 404
    @Test
    void execute_notFound_throwsNotFoundException() {
        when(operationRepository.findById("GHOST")).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> useCase.execute("GHOST"));

        assertEquals(ErrorCode.OPERATION_NOT_FOUND, ex.code());
        verify(operationRepository, never()).save(any());
    }

    // S7: Already CANCELLED — 422
    @Test
    void execute_alreadyCancelled_throwsBusinessRuleException() {
        OperacionProgramada op = makeOp("SOP-000001", ScheduledOperationStatus.CANCELLED);
        when(operationRepository.findById("SOP-000001")).thenReturn(Optional.of(op));

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> useCase.execute("SOP-000001"));

        assertEquals(ErrorCode.VALIDATION_ERROR, ex.code());
        verify(operationRepository, never()).save(any());
    }

    // Non-PENDING (EXECUTED) — 422
    @Test
    void execute_executedOp_throwsBusinessRuleException() {
        OperacionProgramada op = makeOp("SOP-000001", ScheduledOperationStatus.EXECUTED);
        when(operationRepository.findById("SOP-000001")).thenReturn(Optional.of(op));

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> useCase.execute("SOP-000001"));

        assertEquals(ErrorCode.VALIDATION_ERROR, ex.code());
    }
}
