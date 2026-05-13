package com.proyectofinal.fintech.application.usecase;

import com.proyectofinal.fintech.domain.model.*;
import com.proyectofinal.fintech.domain.port.ScheduledOperationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * T08-B16 (RED) — ListScheduledOperationsUseCase tests.
 * Covers scenario S4.
 */
@ExtendWith(MockitoExtension.class)
class ListScheduledOperationsUseCaseTest {

    @Mock
    private ScheduledOperationRepository operationRepository;

    private ListScheduledOperationsUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ListScheduledOperationsUseCase(operationRepository);
    }

    private OperacionProgramada makeOp(String id) {
        return new OperacionProgramada(id, ScheduledOperationType.RECHARGE,
                ScheduledOperationStatus.PENDING, "USR001", "W001", null, null,
                100.0, Instant.parse("2026-06-01T00:00:00Z"), null);
    }

    // S4: List all returns both operations
    @Test
    void execute_twoOps_returnsBoth() {
        OperacionProgramada op1 = makeOp("SOP-000001");
        OperacionProgramada op2 = makeOp("SOP-000002");
        when(operationRepository.findAll()).thenReturn(List.of(op1, op2));

        Iterable<OperacionProgramada> result = useCase.execute();
        List<OperacionProgramada> list = new java.util.ArrayList<>();
        result.forEach(list::add);

        assertEquals(2, list.size());
    }

    @Test
    void execute_delegatesToFindAll() {
        when(operationRepository.findAll()).thenReturn(List.of());
        useCase.execute();
        verify(operationRepository).findAll();
    }
}
