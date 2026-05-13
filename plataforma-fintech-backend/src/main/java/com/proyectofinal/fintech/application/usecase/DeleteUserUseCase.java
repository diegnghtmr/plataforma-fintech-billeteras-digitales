package com.proyectofinal.fintech.application.usecase;

import com.proyectofinal.fintech.domain.exception.ErrorCode;
import com.proyectofinal.fintech.domain.exception.NotFoundException;
import com.proyectofinal.fintech.domain.port.*;

/**
 * Use case: delete a user and all associated data (cascade).
 * Plain class — ZERO Spring/Jakarta imports.
 *
 * Cascade order (ADR-11.7):
 *   1. deleteByUserId(transaction)
 *   2. deleteByOwnerId(wallet)
 *   3. deleteBySourceUserId(scheduledOperation)
 *   4. deleteByUserId(notification)
 *   5. deleteByUserId(fraudEvent)
 *   6. deleteById(user)
 */
public class DeleteUserUseCase {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final ScheduledOperationRepository scheduledOperationRepository;
    private final NotificationRepository notificationRepository;
    private final FraudEventRepository fraudEventRepository;

    public DeleteUserUseCase(UserRepository userRepository,
                              WalletRepository walletRepository,
                              TransactionRepository transactionRepository,
                              ScheduledOperationRepository scheduledOperationRepository,
                              NotificationRepository notificationRepository,
                              FraudEventRepository fraudEventRepository) {
        this.userRepository = userRepository;
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
        this.scheduledOperationRepository = scheduledOperationRepository;
        this.notificationRepository = notificationRepository;
        this.fraudEventRepository = fraudEventRepository;
    }

    /**
     * Deletes a user and all associated data.
     *
     * @param userId the user to delete
     * @throws NotFoundException if user not found
     */
    public void execute(String userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(
                        ErrorCode.USER_NOT_FOUND,
                        "User with id=" + userId + " not found"));

        transactionRepository.deleteByUserId(userId);
        walletRepository.deleteByOwnerId(userId);
        scheduledOperationRepository.deleteBySourceUserId(userId);
        notificationRepository.deleteByUserId(userId);
        fraudEventRepository.deleteByUserId(userId);
        userRepository.deleteById(userId);
    }
}
