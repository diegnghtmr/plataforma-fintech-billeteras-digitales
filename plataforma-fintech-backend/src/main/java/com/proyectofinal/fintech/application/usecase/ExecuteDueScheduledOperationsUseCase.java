package com.proyectofinal.fintech.application.usecase;

import com.proyectofinal.fintech.application.result.ExecutionReport;
import com.proyectofinal.fintech.application.service.NotificationEmitter;
import com.proyectofinal.fintech.domain.model.OperacionProgramada;
import com.proyectofinal.fintech.domain.model.RecurrenceType;
import com.proyectofinal.fintech.domain.model.ScheduledOperationStatus;
import com.proyectofinal.fintech.domain.model.ScheduledOperationType;
import com.proyectofinal.fintech.domain.model.Transaccion;
import com.proyectofinal.fintech.domain.port.ScheduledOperationIdGenerator;
import com.proyectofinal.fintech.domain.port.ScheduledOperationRepository;
import com.proyectofinal.fintech.domain.port.TransactionRepository;
import com.proyectofinal.fintech.domain.port.UserRepository;
import com.proyectofinal.fintech.domain.service.PuntosCalculator;

import com.proyectofinal.fintech.domain.structures.MiLista;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

/**
 * Use case: execute all scheduled operations that are due (scheduledAt <= now).
 * Plain class — ZERO Spring/Jakarta imports.
 * ADR-11.8: bonus +5 pts per executed operation, credited to sourceUserId.
 */
public class ExecuteDueScheduledOperationsUseCase {

    private final ScheduledOperationRepository scheduledRepo;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final NotificationEmitter notificationEmitter;
    private final Clock clock;
    private final ScheduledOperationIdGenerator idGenerator;
    private final RechargeWalletUseCase rechargeUseCase;
    private final WithdrawWalletUseCase withdrawUseCase;
    private final InternalTransferUseCase internalTransferUseCase;
    private final ExternalTransferUseCase externalTransferUseCase;

    public ExecuteDueScheduledOperationsUseCase(
            ScheduledOperationRepository scheduledRepo,
            UserRepository userRepository,
            TransactionRepository transactionRepository,
            NotificationEmitter notificationEmitter,
            Clock clock,
            ScheduledOperationIdGenerator idGenerator,
            RechargeWalletUseCase rechargeUseCase,
            WithdrawWalletUseCase withdrawUseCase,
            InternalTransferUseCase internalTransferUseCase,
            ExternalTransferUseCase externalTransferUseCase) {
        this.scheduledRepo = scheduledRepo;
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
        this.notificationEmitter = notificationEmitter;
        this.clock = clock;
        this.idGenerator = idGenerator;
        this.rechargeUseCase = rechargeUseCase;
        this.withdrawUseCase = withdrawUseCase;
        this.internalTransferUseCase = internalTransferUseCase;
        this.externalTransferUseCase = externalTransferUseCase;
    }

    /**
     * Executes all due PENDING scheduled operations.
     * BEFORE the due-now dispatch loop, emits SCHEDULED_REMINDER for operations
     * that are within 1 hour (but not yet due) and not yet reminded (idempotent).
     *
     * @return ExecutionReport with counts and ids of executed/failed operations
     */
    public ExecutionReport execute() {
        Instant now = Instant.now(clock);
        Instant horizon = now.plusSeconds(3600L); // 1 hour ahead
        MiLista<String> executedIds = new MiLista<>();
        MiLista<String> failedIds = new MiLista<>();

        // SCHEDULED_REMINDER loop: emit for upcoming PENDING ops within 1h window
        for (OperacionProgramada op : scheduledRepo.findPendingInPriorityOrder()) {
            Instant scheduledAt = op.getScheduledAt();
            if (scheduledAt.isAfter(now) && scheduledAt.isBefore(horizon)
                    && op.getStatus() == com.proyectofinal.fintech.domain.model.ScheduledOperationStatus.PENDING) {
                if (!scheduledRepo.isReminded(op.getId())) {
                    notificationEmitter.emitScheduledNear(op.getSourceUserId(), op.getId(), op.getScheduledAt());
                    scheduledRepo.markReminded(op.getId());
                }
            }
        }

        for (OperacionProgramada op : scheduledRepo.findPendingInPriorityOrder()) {
            // Skip future operations
            if (op.getScheduledAt().isAfter(now)) {
                continue;
            }

            try {
                // C2/ADR-13.3: dispatch returns the produced Transaccion for bonus folding
                Transaccion tx = dispatch(op);
                op.markExecuted();
                scheduledRepo.save(op);

                // C2: fold SCHEDULED_EXECUTION_BONUS into tx.pointsGenerated, then re-persist
                // (idempotent thanks to C1 — does not duplicate)
                tx.addBonusPoints(PuntosCalculator.SCHEDULED_EXECUTION_BONUS);
                transactionRepository.save(tx);

                // Bonus +5 points for the source user
                userRepository.findById(op.getSourceUserId()).ifPresent(user -> {
                    boolean levelUp = user.addPoints(PuntosCalculator.SCHEDULED_EXECUTION_BONUS);
                    userRepository.save(user);
                    if (levelUp) {
                        notificationEmitter.emitLevelUp(op.getSourceUserId(), user.getLoyaltyLevel());
                    }
                });

                notificationEmitter.emitScheduledExecuted(op.getSourceUserId(), op.getId());
                executedIds.add(op.getId());

                // Spawn next occurrence if recurrence is not NONE
                spawnNextIfRecurring(op);

            } catch (Exception e) {
                op.markFailed();
                scheduledRepo.save(op);
                notificationEmitter.emitScheduledRejected(
                        op.getSourceUserId(), op.getId(), e.getMessage());
                failedIds.add(op.getId());
            }
        }

        return new ExecutionReport(executedIds.size(), failedIds.size(), executedIds, failedIds);
    }

    private void spawnNextIfRecurring(OperacionProgramada op) {
        RecurrenceType recurrence = op.getRecurrence();
        if (recurrence == RecurrenceType.NONE) return;

        Duration period = switch (recurrence) {
            case DAILY -> Duration.ofDays(1);
            case WEEKLY -> Duration.ofDays(7);
            case MONTHLY -> Duration.ofDays(30);
            default -> throw new IllegalStateException("Unexpected recurrence: " + recurrence);
        };

        Instant nextScheduledAt = op.getScheduledAt().plus(period);
        OperacionProgramada next = new OperacionProgramada(
                idGenerator.next(),
                op.getType(),
                ScheduledOperationStatus.PENDING,
                op.getSourceUserId(),
                op.getSourceWalletId(),
                op.getTargetUserId(),
                op.getTargetWalletId(),
                op.getAmount(),
                nextScheduledAt,
                op.getDescription(),
                recurrence);
        scheduledRepo.save(next);
    }

    /**
     * Dispatches the scheduled operation to the appropriate sub-use-case and returns
     * the produced Transaccion. For EXTERNAL_TRANSFER, returns the outgoing (source-side) leg.
     * ADR-13.3: private, single caller — zero external blast radius.
     */
    private Transaccion dispatch(OperacionProgramada op) {
        return switch (op.getType()) {
            case RECHARGE -> rechargeUseCase.execute(
                    op.getSourceUserId(), op.getSourceWalletId(), op.getAmount(), op.getDescription());
            case WITHDRAWAL -> withdrawUseCase.execute(
                    op.getSourceUserId(), op.getSourceWalletId(), op.getAmount(), op.getDescription());
            case INTERNAL_TRANSFER -> internalTransferUseCase.execute(
                    op.getSourceUserId(), op.getSourceWalletId(), op.getTargetWalletId(),
                    op.getAmount(), op.getDescription());
            case EXTERNAL_TRANSFER -> externalTransferUseCase.execute(
                    op.getSourceUserId(), op.getSourceWalletId(),
                    op.getTargetUserId(), op.getTargetWalletId(),
                    op.getAmount(), op.getDescription()).outgoing();
            default -> throw new IllegalArgumentException("Unknown operation type: " + op.getType());
        };
    }
}
