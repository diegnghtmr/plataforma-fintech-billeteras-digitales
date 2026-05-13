package com.proyectofinal.fintech.application.usecase;

import com.proyectofinal.fintech.application.result.AnalyticsSummaryView;
import com.proyectofinal.fintech.domain.model.*;
import com.proyectofinal.fintech.domain.port.*;

import java.util.Set;

/**
 * Aggregates analytics summary counts.
 * Plain class — ZERO Spring/Jakarta imports.
 */
public class GetAnalyticsSummaryUseCase {

    private static final Set<TransactionType> MOVED_AMOUNT_TYPES = Set.of(
            TransactionType.RECHARGE,
            TransactionType.WITHDRAWAL,
            TransactionType.INTERNAL_TRANSFER,
            TransactionType.EXTERNAL_TRANSFER_SENT
    );

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final FraudEventRepository fraudEventRepository;
    private final NotificationRepository notificationRepository;

    public GetAnalyticsSummaryUseCase(UserRepository userRepository,
                                       WalletRepository walletRepository,
                                       TransactionRepository transactionRepository,
                                       FraudEventRepository fraudEventRepository,
                                       NotificationRepository notificationRepository) {
        this.userRepository = userRepository;
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
        this.fraudEventRepository = fraudEventRepository;
        this.notificationRepository = notificationRepository;
    }

    public AnalyticsSummaryView execute() {
        long totalUsers = 0;
        long totalWallets = 0;
        long unreadNotificationCount = 0;

        for (Usuario u : userRepository.findAll()) {
            totalUsers++;
            totalWallets += walletRepository.countByOwnerId(u.getId());
            for (Notificacion ignored : notificationRepository.findByUserId(u.getId(), true)) {
                unreadNotificationCount++;
            }
        }

        long totalTransactions = 0;
        double totalMovedAmount = 0;
        for (Transaccion tx : transactionRepository.findAll()) {
            totalTransactions++;
            if (tx.getStatus() == TransactionStatus.SUCCESSFUL
                    && MOVED_AMOUNT_TYPES.contains(tx.getType())) {
                totalMovedAmount += tx.getAmount();
            }
        }

        long fraudEventCount = fraudEventRepository.count();

        return new AnalyticsSummaryView(
                totalUsers, totalWallets, totalTransactions,
                totalMovedAmount, fraudEventCount, unreadNotificationCount
        );
    }
}
