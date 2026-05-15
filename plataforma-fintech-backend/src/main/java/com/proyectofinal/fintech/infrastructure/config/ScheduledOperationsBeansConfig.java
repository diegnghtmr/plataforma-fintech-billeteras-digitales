package com.proyectofinal.fintech.infrastructure.config;

import com.proyectofinal.fintech.application.service.NotificationEmitter;
import com.proyectofinal.fintech.application.usecase.*;
import com.proyectofinal.fintech.domain.port.*;
import com.proyectofinal.fintech.infrastructure.mapper.ScheduledOperationMapper;
import com.proyectofinal.fintech.infrastructure.output.memory.InMemoryScheduledOperationRepository;
import com.proyectofinal.fintech.infrastructure.output.memory.SequentialScheduledOperationIdGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

// C2: ExecuteDueScheduledOperationsUseCase now requires TransactionRepository to fold bonus into tx

/**
 * Wires all beans for SDD 08 scheduled operations slice.
 * SDD-11: adds ExecuteDueScheduledOperationsUseCase.
 */
@Configuration
public class ScheduledOperationsBeansConfig {

    @Bean
    public InMemoryScheduledOperationRepository inMemoryScheduledOperationRepository() {
        return new InMemoryScheduledOperationRepository();
    }

    @Bean
    public SequentialScheduledOperationIdGenerator sequentialScheduledOperationIdGenerator() {
        return new SequentialScheduledOperationIdGenerator();
    }

    @Bean
    public CreateScheduledOperationUseCase createScheduledOperationUseCase(
            UserRepository userRepository,
            WalletRepository walletRepository,
            ScheduledOperationRepository scheduledOperationRepository,
            ScheduledOperationIdGenerator scheduledOperationIdGenerator) {
        return new CreateScheduledOperationUseCase(userRepository, walletRepository,
                scheduledOperationRepository, scheduledOperationIdGenerator);
    }

    @Bean
    public ListScheduledOperationsUseCase listScheduledOperationsUseCase(
            ScheduledOperationRepository scheduledOperationRepository) {
        return new ListScheduledOperationsUseCase(scheduledOperationRepository);
    }

    @Bean
    public CancelScheduledOperationUseCase cancelScheduledOperationUseCase(
            ScheduledOperationRepository scheduledOperationRepository) {
        return new CancelScheduledOperationUseCase(scheduledOperationRepository);
    }

    @Bean
    public ExecuteDueScheduledOperationsUseCase executeDueScheduledOperationsUseCase(
            ScheduledOperationRepository scheduledOperationRepository,
            UserRepository userRepository,
            TransactionRepository transactionRepository,
            NotificationEmitter notificationEmitter,
            Clock clock,
            ScheduledOperationIdGenerator scheduledOperationIdGenerator,
            RechargeWalletUseCase rechargeWalletUseCase,
            WithdrawWalletUseCase withdrawWalletUseCase,
            InternalTransferUseCase internalTransferUseCase,
            ExternalTransferUseCase externalTransferUseCase) {
        return new ExecuteDueScheduledOperationsUseCase(
                scheduledOperationRepository, userRepository, transactionRepository,
                notificationEmitter, clock, scheduledOperationIdGenerator,
                rechargeWalletUseCase, withdrawWalletUseCase, internalTransferUseCase, externalTransferUseCase);
    }

    @Bean
    public ScheduledOperationMapper scheduledOperationMapper() {
        return new ScheduledOperationMapper();
    }
}
