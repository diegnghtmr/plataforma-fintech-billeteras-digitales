package com.proyectofinal.fintech.application.usecase;

import com.proyectofinal.fintech.application.result.ExecutionReport;
import com.proyectofinal.fintech.application.service.NotificationEmitter;
import com.proyectofinal.fintech.domain.exception.BusinessRuleException;
import com.proyectofinal.fintech.domain.exception.ErrorCode;
import com.proyectofinal.fintech.domain.model.*;
import com.proyectofinal.fintech.domain.port.ScheduledOperationRepository;
import com.proyectofinal.fintech.domain.port.UserRepository;
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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * T11-F01 (RED) — ExecuteDueScheduledOperationsUseCase tests.
 * Scenarios: RECHARGE due → executes + bonus; FAILED if UC throws; skips future; empty → {0,0}.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ExecuteDueScheduledOperationsUseCaseTest {

    @Mock private ScheduledOperationRepository scheduledRepo;
    @Mock private UserRepository userRepository;
    @Mock private NotificationEmitter notificationEmitter;
    @Mock private RechargeWalletUseCase rechargeUseCase;
    @Mock private WithdrawWalletUseCase withdrawUseCase;
    @Mock private InternalTransferUseCase internalTransferUseCase;
    @Mock private ExternalTransferUseCase externalTransferUseCase;

    private ExecuteDueScheduledOperationsUseCase useCase;

    private static final Instant NOW = Instant.parse("2026-01-01T12:00:00Z");
    private Clock clock;

    @BeforeEach
    void setUp() {
        clock = Clock.fixed(NOW, ZoneId.of("UTC"));
        useCase = new ExecuteDueScheduledOperationsUseCase(
                scheduledRepo, userRepository, notificationEmitter, clock,
                rechargeUseCase, withdrawUseCase, internalTransferUseCase, externalTransferUseCase
        );
    }

    private OperacionProgramada makeOp(String id, ScheduledOperationType type, Instant scheduledAt) {
        return new OperacionProgramada(id, type, ScheduledOperationStatus.PENDING,
                "USR001", "W001", null, null, 100.0, scheduledAt, null);
    }

    private Transaccion makeTx(String id) {
        return new Transaccion(id, NOW, TransactionType.RECHARGE, 100.0,
                "W001", null, "USR001", null, TransactionStatus.SUCCESSFUL, 1.0, null, true);
    }

    // S-F01: empty repo → {executed:0, failed:0}
    @Test
    void execute_emptyRepo_returnsZeroReport() {
        when(scheduledRepo.findPendingInPriorityOrder()).thenReturn(List.of());

        ExecutionReport report = useCase.execute();

        assertThat(report.executed()).isEqualTo(0);
        assertThat(report.failed()).isEqualTo(0);
        assertThat(report.executedIds()).isEmpty();
        assertThat(report.failedIds()).isEmpty();
    }

    // S-F02: RECHARGE due → executes, user gains bonus +5
    @Test
    void execute_rechargeDue_executesAndGivesBonus() {
        OperacionProgramada op = makeOp("OP1", ScheduledOperationType.RECHARGE, NOW.minusSeconds(60));
        when(scheduledRepo.findPendingInPriorityOrder()).thenReturn(List.of(op));

        Transaccion tx = makeTx("TX-001");
        when(rechargeUseCase.execute(eq("USR001"), eq("W001"), eq(100.0), any()))
                .thenReturn(tx);

        Usuario user = new Usuario("USR001", "Ana", "ana@test.com", NOW, 0.0, LoyaltyLevel.BRONZE);
        when(userRepository.findById("USR001")).thenReturn(Optional.of(user));

        ExecutionReport report = useCase.execute();

        assertThat(report.executed()).isEqualTo(1);
        assertThat(report.failed()).isEqualTo(0);
        assertThat(report.executedIds()).contains("OP1");
        // Bonus: user points = bonus 5.0
        assertThat(user.getPoints()).isGreaterThanOrEqualTo(5.0);
    }

    // S-F03: future op → skipped (not executed, not failed)
    @Test
    void execute_futureOp_skipsIt() {
        OperacionProgramada op = makeOp("OP1", ScheduledOperationType.RECHARGE, NOW.plusSeconds(60));
        when(scheduledRepo.findPendingInPriorityOrder()).thenReturn(List.of(op));

        ExecutionReport report = useCase.execute();

        assertThat(report.executed()).isEqualTo(0);
        assertThat(report.failed()).isEqualTo(0);
        verifyNoInteractions(rechargeUseCase);
    }

    // S-F04: RECHARGE throws → op marked FAILED, report counts it
    @Test
    void execute_rechargeFails_marksOpFailedAndCountsIt() {
        OperacionProgramada op = makeOp("OP1", ScheduledOperationType.RECHARGE, NOW.minusSeconds(10));
        when(scheduledRepo.findPendingInPriorityOrder()).thenReturn(List.of(op));
        when(rechargeUseCase.execute(any(), any(), anyDouble(), any()))
                .thenThrow(new BusinessRuleException(ErrorCode.INSUFFICIENT_FUNDS, "No funds"));

        ExecutionReport report = useCase.execute();

        assertThat(report.executed()).isEqualTo(0);
        assertThat(report.failed()).isEqualTo(1);
        assertThat(report.failedIds()).contains("OP1");
    }

    // S-F05: executed op emits SCHEDULED_EXECUTED notification
    @Test
    void execute_successfulOp_emitsExecutedNotification() {
        OperacionProgramada op = makeOp("OP1", ScheduledOperationType.RECHARGE, NOW.minusSeconds(10));
        when(scheduledRepo.findPendingInPriorityOrder()).thenReturn(List.of(op));
        when(rechargeUseCase.execute(any(), any(), anyDouble(), any())).thenReturn(makeTx("TX-001"));
        when(userRepository.findById("USR001"))
                .thenReturn(Optional.of(new Usuario("USR001", "Ana", "ana@test.com", NOW, 0.0, LoyaltyLevel.BRONZE)));

        useCase.execute();

        verify(notificationEmitter).emitScheduledExecuted(eq("USR001"), eq("OP1"));
    }
}
