package com.proyectofinal.fintech.application.usecase;

import com.proyectofinal.fintech.application.result.ExecutionReport;
import com.proyectofinal.fintech.application.service.NotificationEmitter;
import com.proyectofinal.fintech.domain.model.OperacionProgramada;
import com.proyectofinal.fintech.domain.model.ScheduledOperationType;
import com.proyectofinal.fintech.domain.port.ScheduledOperationRepository;
import com.proyectofinal.fintech.domain.port.UserRepository;
import com.proyectofinal.fintech.domain.service.PuntosCalculator;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Use case: execute all scheduled operations that are due (scheduledAt <= now).
 * Plain class — ZERO Spring/Jakarta imports.
 * ADR-11.8: bonus +5 pts per executed operation, credited to sourceUserId.
 */
public class ExecuteDueScheduledOperationsUseCase {

    private final ScheduledOperationRepository scheduledRepo;
    private final UserRepository userRepository;
    private final NotificationEmitter notificationEmitter;
    private final Clock clock;
    private final RechargeWalletUseCase rechargeUseCase;
    private final WithdrawWalletUseCase withdrawUseCase;
    private final InternalTransferUseCase internalTransferUseCase;
    private final ExternalTransferUseCase externalTransferUseCase;

    public ExecuteDueScheduledOperationsUseCase(
            ScheduledOperationRepository scheduledRepo,
            UserRepository userRepository,
            NotificationEmitter notificationEmitter,
            Clock clock,
            RechargeWalletUseCase rechargeUseCase,
            WithdrawWalletUseCase withdrawUseCase,
            InternalTransferUseCase internalTransferUseCase,
            ExternalTransferUseCase externalTransferUseCase) {
        this.scheduledRepo = scheduledRepo;
        this.userRepository = userRepository;
        this.notificationEmitter = notificationEmitter;
        this.clock = clock;
        this.rechargeUseCase = rechargeUseCase;
        this.withdrawUseCase = withdrawUseCase;
        this.internalTransferUseCase = internalTransferUseCase;
        this.externalTransferUseCase = externalTransferUseCase;
    }

    /**
     * Executes all due PENDING scheduled operations.
     *
     * @return ExecutionReport with counts and ids of executed/failed operations
     */
    public ExecutionReport execute() {
        Instant now = Instant.now(clock);
        List<String> executedIds = new ArrayList<>();
        List<String> failedIds = new ArrayList<>();

        for (OperacionProgramada op : scheduledRepo.findPendingInPriorityOrder()) {
            // Skip future operations
            if (op.getScheduledAt().isAfter(now)) {
                continue;
            }

            try {
                dispatch(op);
                op.markExecuted();
                scheduledRepo.save(op);

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

    private void dispatch(OperacionProgramada op) {
        switch (op.getType()) {
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
                    op.getAmount(), op.getDescription());
            default -> throw new IllegalArgumentException("Unknown operation type: " + op.getType());
        }
    }
}
