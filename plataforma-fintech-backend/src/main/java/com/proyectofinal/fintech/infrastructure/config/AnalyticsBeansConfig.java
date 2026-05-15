package com.proyectofinal.fintech.infrastructure.config;

import com.proyectofinal.fintech.application.usecase.*;
import com.proyectofinal.fintech.domain.port.*;
import com.proyectofinal.fintech.infrastructure.mapper.AnalyticsMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Wires all analytics-related beans.
 * SDD-11: adds GetTopTransactionsUseCase, GetCyclesUseCase, GetTopWalletCategoriesUseCase,
 *         GetMovementByTypeUseCase, GetTotalMovedInRangeUseCase, TransactionMapper.
 */
@Configuration
public class AnalyticsBeansConfig {

    @Bean
    public GetAnalyticsSummaryUseCase getAnalyticsSummaryUseCase(
            UserRepository userRepository,
            WalletRepository walletRepository,
            TransactionRepository transactionRepository,
            FraudEventRepository fraudEventRepository,
            NotificationRepository notificationRepository,
            ScheduledOperationRepository scheduledOperationRepository) {
        return new GetAnalyticsSummaryUseCase(userRepository, walletRepository,
                transactionRepository, fraudEventRepository, notificationRepository,
                scheduledOperationRepository);
    }

    @Bean
    public GetTopUsersUseCase getTopUsersUseCase(UserRepository userRepository,
                                                  TransactionRepository transactionRepository) {
        return new GetTopUsersUseCase(userRepository, transactionRepository);
    }

    @Bean
    public GetTopWalletsUseCase getTopWalletsUseCase(UserRepository userRepository,
                                                      WalletRepository walletRepository) {
        return new GetTopWalletsUseCase(userRepository, walletRepository);
    }

    @Bean
    public GetFrequentRoutesUseCase getFrequentRoutesUseCase(TransferGraphRepository transferGraphRepository) {
        return new GetFrequentRoutesUseCase(transferGraphRepository);
    }

    @Bean
    public GetTopTransactionsUseCase getTopTransactionsUseCase(TransactionRepository transactionRepository) {
        return new GetTopTransactionsUseCase(transactionRepository);
    }

    @Bean
    public GetCyclesUseCase getCyclesUseCase(TransferGraphRepository transferGraphRepository) {
        return new GetCyclesUseCase(transferGraphRepository);
    }

    @Bean
    public GetTopWalletCategoriesUseCase getTopWalletCategoriesUseCase(WalletRepository walletRepository,
                                                                         TransactionRepository transactionRepository) {
        return new GetTopWalletCategoriesUseCase(walletRepository, transactionRepository);
    }

    @Bean
    public GetMovementByTypeUseCase getMovementByTypeUseCase(TransactionRepository transactionRepository) {
        return new GetMovementByTypeUseCase(transactionRepository);
    }

    @Bean
    public GetTotalMovedInRangeUseCase getTotalMovedInRangeUseCase(TransactionRepository transactionRepository) {
        return new GetTotalMovedInRangeUseCase(transactionRepository);
    }

    @Bean
    public AnalyticsMapper analyticsMapper() {
        return new AnalyticsMapper();
    }
}
