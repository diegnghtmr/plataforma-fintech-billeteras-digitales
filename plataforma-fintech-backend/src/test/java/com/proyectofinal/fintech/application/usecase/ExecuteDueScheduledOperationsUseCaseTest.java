package com.proyectofinal.fintech.application.usecase;

import com.proyectofinal.fintech.application.result.ExecutionReport;
import com.proyectofinal.fintech.application.service.NotificationEmitter;
import com.proyectofinal.fintech.domain.exception.BusinessRuleException;
import com.proyectofinal.fintech.domain.exception.ErrorCode;
import com.proyectofinal.fintech.domain.model.*;
import com.proyectofinal.fintech.domain.port.ScheduledOperationIdGenerator;
import com.proyectofinal.fintech.domain.port.ScheduledOperationRepository;
import com.proyectofinal.fintech.domain.port.TransactionRepository;
import com.proyectofinal.fintech.domain.port.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.Clock;
import java.time.Duration;
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
    @Mock private TransactionRepository transactionRepository;
    @Mock private NotificationEmitter notificationEmitter;
    @Mock private ScheduledOperationIdGenerator idGenerator;
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
                scheduledRepo, userRepository, transactionRepository, notificationEmitter, clock, idGenerator,
                rechargeUseCase, withdrawUseCase, internalTransferUseCase, externalTransferUseCase
        );
    }

    private OperacionProgramada makeOp(String id, ScheduledOperationType type, Instant scheduledAt) {
        return new OperacionProgramada(id, type, ScheduledOperationStatus.PENDING,
                "USR001", "W001", null, null, 100.0, scheduledAt, null);
    }

    private OperacionProgramada makeOpWithRecurrence(String id, ScheduledOperationType type,
                                                       Instant scheduledAt, RecurrenceType recurrence) {
        return new OperacionProgramada(id, type, ScheduledOperationStatus.PENDING,
                "USR001", "W001", null, null, 100.0, scheduledAt, null, recurrence);
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

    // T3.2.3 — PENDING op scheduledAt now+45min → emitScheduledNear called once
    @Test
    void execute_scheduledWithin1Hour_pendingStatus_emitsReminderOnce() {
        Instant soon = NOW.plusSeconds(45 * 60); // 45 min from now
        OperacionProgramada op = makeOp("OP-R1", ScheduledOperationType.RECHARGE, soon);
        // findPendingInPriorityOrder returns this op (pending, future)
        when(scheduledRepo.findPendingInPriorityOrder()).thenReturn(List.of(op));
        when(scheduledRepo.isReminded("OP-R1")).thenReturn(false);

        useCase.execute();

        verify(notificationEmitter, times(1)).emitScheduledNear(eq("USR001"), eq("OP-R1"), eq(soon));
        verify(scheduledRepo).markReminded("OP-R1");
    }

    // T3.2.4 — already reminded → emitScheduledNear NOT called again
    @Test
    void execute_alreadyReminded_doesNotEmitTwice() {
        Instant soon = NOW.plusSeconds(30 * 60); // 30 min from now
        OperacionProgramada op = makeOp("OP-R2", ScheduledOperationType.RECHARGE, soon);
        when(scheduledRepo.findPendingInPriorityOrder()).thenReturn(List.of(op));
        when(scheduledRepo.isReminded("OP-R2")).thenReturn(true); // already reminded

        useCase.execute();

        verify(notificationEmitter, never()).emitScheduledNear(any(), any(), any());
    }

    // T3.2.5 — scheduledAt now+90min → emitScheduledNear NOT called
    @Test
    void execute_scheduledBeyond1Hour_doesNotEmitReminder() {
        Instant farFuture = NOW.plusSeconds(90 * 60); // 90 min from now
        OperacionProgramada op = makeOp("OP-R3", ScheduledOperationType.RECHARGE, farFuture);
        when(scheduledRepo.findPendingInPriorityOrder()).thenReturn(List.of(op));

        useCase.execute();

        verify(notificationEmitter, never()).emitScheduledNear(any(), any(), any());
    }

    // T3.2.6 — status EXECUTED, scheduledAt now+30min → emitScheduledNear NOT called
    @Test
    void execute_alreadyExecuted_doesNotEmitReminder() {
        Instant soon = NOW.plusSeconds(30 * 60);
        OperacionProgramada op = new OperacionProgramada("OP-R4", ScheduledOperationType.RECHARGE,
                ScheduledOperationStatus.EXECUTED,
                "USR001", "W001", null, null, 100.0, soon, null);
        when(scheduledRepo.findPendingInPriorityOrder()).thenReturn(List.of(op));

        useCase.execute();

        verify(notificationEmitter, never()).emitScheduledNear(any(), any(), any());
    }

    // E-23 (RED) — REQ-F4.4: DAILY recurrence spawns next occurrence +24h

    @Test
    void execute_dailyRecurrence_spawnsNextOccurrencePlus24h() {
        Instant scheduledAt = NOW.minusSeconds(10);
        OperacionProgramada op = makeOpWithRecurrence("OP-D1", ScheduledOperationType.RECHARGE,
                scheduledAt, RecurrenceType.DAILY);
        when(scheduledRepo.findPendingInPriorityOrder()).thenReturn(List.of(op));
        when(rechargeUseCase.execute(any(), any(), anyDouble(), any())).thenReturn(makeTx("TX-D1"));
        when(userRepository.findById("USR001"))
                .thenReturn(Optional.of(new Usuario("USR001", "Ana", "ana@test.com", NOW, 0.0, LoyaltyLevel.BRONZE)));
        when(idGenerator.next()).thenReturn("OP-D1-NEXT");

        useCase.execute();

        ArgumentCaptor<OperacionProgramada> captor = ArgumentCaptor.forClass(OperacionProgramada.class);
        // save is called at least twice: once for markExecuted, once for spawned op
        verify(scheduledRepo, atLeast(2)).save(captor.capture());
        OperacionProgramada spawned = captor.getAllValues().stream()
                .filter(o -> "OP-D1-NEXT".equals(o.getId()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Spawned op not saved"));
        assertThat(spawned.getScheduledAt()).isEqualTo(scheduledAt.plus(Duration.ofDays(1)));
        assertThat(spawned.getRecurrence()).isEqualTo(RecurrenceType.DAILY);
        assertThat(spawned.getStatus()).isEqualTo(ScheduledOperationStatus.PENDING);
    }

    @Test
    void execute_weeklyRecurrence_spawnsNextOccurrencePlus7d() {
        Instant scheduledAt = NOW.minusSeconds(10);
        OperacionProgramada op = makeOpWithRecurrence("OP-W1", ScheduledOperationType.RECHARGE,
                scheduledAt, RecurrenceType.WEEKLY);
        when(scheduledRepo.findPendingInPriorityOrder()).thenReturn(List.of(op));
        when(rechargeUseCase.execute(any(), any(), anyDouble(), any())).thenReturn(makeTx("TX-W1"));
        when(userRepository.findById("USR001"))
                .thenReturn(Optional.of(new Usuario("USR001", "Ana", "ana@test.com", NOW, 0.0, LoyaltyLevel.BRONZE)));
        when(idGenerator.next()).thenReturn("OP-W1-NEXT");

        useCase.execute();

        ArgumentCaptor<OperacionProgramada> captor = ArgumentCaptor.forClass(OperacionProgramada.class);
        verify(scheduledRepo, atLeast(2)).save(captor.capture());
        OperacionProgramada spawned = captor.getAllValues().stream()
                .filter(o -> "OP-W1-NEXT".equals(o.getId()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Spawned op not saved"));
        assertThat(spawned.getScheduledAt()).isEqualTo(scheduledAt.plus(Duration.ofDays(7)));
        assertThat(spawned.getRecurrence()).isEqualTo(RecurrenceType.WEEKLY);
    }

    @Test
    void execute_monthlyRecurrence_spawnsNextOccurrencePlus30d() {
        Instant scheduledAt = NOW.minusSeconds(10);
        OperacionProgramada op = makeOpWithRecurrence("OP-M1", ScheduledOperationType.RECHARGE,
                scheduledAt, RecurrenceType.MONTHLY);
        when(scheduledRepo.findPendingInPriorityOrder()).thenReturn(List.of(op));
        when(rechargeUseCase.execute(any(), any(), anyDouble(), any())).thenReturn(makeTx("TX-M1"));
        when(userRepository.findById("USR001"))
                .thenReturn(Optional.of(new Usuario("USR001", "Ana", "ana@test.com", NOW, 0.0, LoyaltyLevel.BRONZE)));
        when(idGenerator.next()).thenReturn("OP-M1-NEXT");

        useCase.execute();

        ArgumentCaptor<OperacionProgramada> captor = ArgumentCaptor.forClass(OperacionProgramada.class);
        verify(scheduledRepo, atLeast(2)).save(captor.capture());
        OperacionProgramada spawned = captor.getAllValues().stream()
                .filter(o -> "OP-M1-NEXT".equals(o.getId()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Spawned op not saved"));
        assertThat(spawned.getScheduledAt()).isEqualTo(scheduledAt.plus(Duration.ofDays(30)));
        assertThat(spawned.getRecurrence()).isEqualTo(RecurrenceType.MONTHLY);
    }

    @Test
    void execute_noneRecurrence_noNewOpSpawned() {
        Instant scheduledAt = NOW.minusSeconds(10);
        OperacionProgramada op = makeOpWithRecurrence("OP-N1", ScheduledOperationType.RECHARGE,
                scheduledAt, RecurrenceType.NONE);
        when(scheduledRepo.findPendingInPriorityOrder()).thenReturn(List.of(op));
        when(rechargeUseCase.execute(any(), any(), anyDouble(), any())).thenReturn(makeTx("TX-N1"));
        when(userRepository.findById("USR001"))
                .thenReturn(Optional.of(new Usuario("USR001", "Ana", "ana@test.com", NOW, 0.0, LoyaltyLevel.BRONZE)));

        useCase.execute();

        // Only one save: the markExecuted save. No spawned op.
        ArgumentCaptor<OperacionProgramada> captor = ArgumentCaptor.forClass(OperacionProgramada.class);
        verify(scheduledRepo, atMost(2)).save(captor.capture()); // user save doesn't go here
        captor.getAllValues().forEach(saved ->
                assertThat(saved.getId()).isEqualTo("OP-N1")); // only the original op saved
    }

    // C2-RED: scheduled recharge tx pointsGenerated = base + SCHEDULED_EXECUTION_BONUS
    @Test
    void execute_scheduledRecharge_txPointsGeneratedEqualsBasePlusBonus() {
        OperacionProgramada op = makeOp("OP-C2", ScheduledOperationType.RECHARGE, NOW.minusSeconds(60));
        when(scheduledRepo.findPendingInPriorityOrder()).thenReturn(List.of(op));

        // base points = 1.0 (recharge of 100)
        Transaccion tx = makeTx("TX-C2");  // pointsGenerated = 1.0
        when(rechargeUseCase.execute(eq("USR001"), eq("W001"), eq(100.0), any()))
                .thenReturn(tx);

        Usuario user = new Usuario("USR001", "Ana", "ana@test.com", NOW, 0.0, LoyaltyLevel.BRONZE);
        when(userRepository.findById("USR001")).thenReturn(Optional.of(user));

        useCase.execute();

        // tx.pointsGenerated must equal base (1.0) + SCHEDULED_EXECUTION_BONUS (5.0) = 6.0
        assertThat(tx.getPointsGenerated()).isEqualTo(6.0);

        // transactionRepository.save(tx) must be called with the augmented tx
        ArgumentCaptor<Transaccion> txCaptor = ArgumentCaptor.forClass(Transaccion.class);
        verify(transactionRepository).save(txCaptor.capture());
        assertThat(txCaptor.getValue().getPointsGenerated()).isEqualTo(6.0);
    }
}
