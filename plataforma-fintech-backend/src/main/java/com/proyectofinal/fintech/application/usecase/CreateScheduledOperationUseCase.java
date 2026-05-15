package com.proyectofinal.fintech.application.usecase;

import com.proyectofinal.fintech.domain.exception.BusinessRuleException;
import com.proyectofinal.fintech.domain.exception.ErrorCode;
import com.proyectofinal.fintech.domain.exception.NotFoundException;
import com.proyectofinal.fintech.domain.model.*;
import com.proyectofinal.fintech.domain.port.*;

import java.time.Instant;

/**
 * Use case: create a scheduled financial operation.
 * Plain class — ZERO Spring/Jakarta imports.
 */
public class CreateScheduledOperationUseCase {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final ScheduledOperationRepository operationRepository;
    private final ScheduledOperationIdGenerator idGenerator;

    public CreateScheduledOperationUseCase(UserRepository userRepository,
                                            WalletRepository walletRepository,
                                            ScheduledOperationRepository operationRepository,
                                            ScheduledOperationIdGenerator idGenerator) {
        this.userRepository = userRepository;
        this.walletRepository = walletRepository;
        this.operationRepository = operationRepository;
        this.idGenerator = idGenerator;
    }

    /**
     * Creates and persists a scheduled operation with status PENDING.
     * @param recurrence recurrence type; null defaults to NONE; invalid string throws IllegalArgumentException
     */
    public OperacionProgramada execute(ScheduledOperationType type,
                                        String sourceUserId, String sourceWalletId,
                                        String targetUserId, String targetWalletId,
                                        double amount, Instant scheduledAt, String description,
                                        RecurrenceType recurrence) {
        // Backward-compat overload for callers not passing recurrence handled by the 8-arg method
        if (recurrence == null) recurrence = RecurrenceType.NONE;
        // Validate amount
        if (amount < 0.01) {
            throw new BusinessRuleException(ErrorCode.VALIDATION_ERROR,
                    "Amount must be >= 0.01; got: " + amount);
        }

        // Validate source user exists
        userRepository.findById(sourceUserId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND,
                        "User not found: " + sourceUserId));

        // Validate source wallet exists
        walletRepository.findByOwnerIdAndCode(sourceUserId, sourceWalletId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.WALLET_NOT_FOUND,
                        "Wallet not found: " + sourceWalletId + " for user: " + sourceUserId));

        // Validate INTERNAL/EXTERNAL transfer invariants
        if (type == ScheduledOperationType.INTERNAL_TRANSFER
                || type == ScheduledOperationType.EXTERNAL_TRANSFER) {
            if (targetUserId == null || targetWalletId == null) {
                throw new BusinessRuleException(ErrorCode.VALIDATION_ERROR,
                        "targetUserId and targetWalletId are required for " + type);
            }
        }

        // EXTERNAL_TRANSFER: source user must differ from target user
        if (type == ScheduledOperationType.EXTERNAL_TRANSFER) {
            if (sourceUserId.equals(targetUserId)) {
                throw new BusinessRuleException(ErrorCode.VALIDATION_ERROR,
                        "EXTERNAL_TRANSFER requires distinct source and target users");
            }
        }

        OperacionProgramada op = new OperacionProgramada(
                idGenerator.next(), type, ScheduledOperationStatus.PENDING,
                sourceUserId, sourceWalletId, targetUserId, targetWalletId,
                amount, scheduledAt, description, recurrence);

        operationRepository.save(op);
        return op;
    }

    /**
     * Backward-compatible overload — defaults recurrence to NONE.
     */
    public OperacionProgramada execute(ScheduledOperationType type,
                                        String sourceUserId, String sourceWalletId,
                                        String targetUserId, String targetWalletId,
                                        double amount, Instant scheduledAt, String description) {
        return execute(type, sourceUserId, sourceWalletId, targetUserId, targetWalletId,
                amount, scheduledAt, description, RecurrenceType.NONE);
    }
}
