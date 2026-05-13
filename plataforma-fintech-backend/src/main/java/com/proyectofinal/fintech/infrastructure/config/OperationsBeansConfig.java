package com.proyectofinal.fintech.infrastructure.config;

import com.proyectofinal.fintech.application.service.NotificationEmitter;
import com.proyectofinal.fintech.application.usecase.*;
import com.proyectofinal.fintech.domain.port.*;
import com.proyectofinal.fintech.domain.service.FraudDetector;
import com.proyectofinal.fintech.infrastructure.mapper.TransactionMapper;
import com.proyectofinal.fintech.infrastructure.output.memory.InMemoryReversibleOperationsStack;
import com.proyectofinal.fintech.infrastructure.output.memory.InMemoryTransactionRepository;
import com.proyectofinal.fintech.infrastructure.output.memory.SequentialTransactionIdGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Wires all beans for SDD 05 (financial operations) and SDD 06 (history/reversal).
 * SDD-09: use case beans now include FraudEventRepository, FraudEventIdGenerator, and
 * TransferGraphRepository (for ExternalTransfer).
 * SDD-11: all 4 financial use case beans now accept FraudDetector + NotificationEmitter.
 */
@Configuration
public class OperationsBeansConfig {

    @Bean
    public InMemoryTransactionRepository inMemoryTransactionRepository() {
        return new InMemoryTransactionRepository();
    }

    @Bean
    public SequentialTransactionIdGenerator sequentialTransactionIdGenerator() {
        return new SequentialTransactionIdGenerator();
    }

    @Bean
    public ReversibleOperationsStack reversibleOperationsStack() {
        return new InMemoryReversibleOperationsStack();
    }

    @Bean
    public RechargeWalletUseCase rechargeWalletUseCase(UserRepository userRepository,
                                                        WalletRepository walletRepository,
                                                        TransactionRepository transactionRepository,
                                                        TransactionIdGenerator transactionIdGenerator,
                                                        Clock clock,
                                                        ReversibleOperationsStack reversibleOperationsStack,
                                                        FraudEventRepository fraudEventRepository,
                                                        FraudDetector fraudDetector,
                                                        NotificationEmitter notificationEmitter) {
        return new RechargeWalletUseCase(userRepository, walletRepository,
                transactionRepository, transactionIdGenerator, clock, reversibleOperationsStack,
                fraudEventRepository, fraudDetector, notificationEmitter);
    }

    @Bean
    public WithdrawWalletUseCase withdrawWalletUseCase(UserRepository userRepository,
                                                        WalletRepository walletRepository,
                                                        TransactionRepository transactionRepository,
                                                        TransactionIdGenerator transactionIdGenerator,
                                                        Clock clock,
                                                        ReversibleOperationsStack reversibleOperationsStack,
                                                        FraudEventRepository fraudEventRepository,
                                                        FraudDetector fraudDetector,
                                                        NotificationEmitter notificationEmitter) {
        return new WithdrawWalletUseCase(userRepository, walletRepository,
                transactionRepository, transactionIdGenerator, clock, reversibleOperationsStack,
                fraudEventRepository, fraudDetector, notificationEmitter);
    }

    @Bean
    public InternalTransferUseCase internalTransferUseCase(UserRepository userRepository,
                                                             WalletRepository walletRepository,
                                                             TransactionRepository transactionRepository,
                                                             TransactionIdGenerator transactionIdGenerator,
                                                             Clock clock,
                                                             ReversibleOperationsStack reversibleOperationsStack,
                                                             FraudEventRepository fraudEventRepository,
                                                             FraudDetector fraudDetector,
                                                             NotificationEmitter notificationEmitter) {
        return new InternalTransferUseCase(userRepository, walletRepository,
                transactionRepository, transactionIdGenerator, clock, reversibleOperationsStack,
                fraudEventRepository, fraudDetector, notificationEmitter);
    }

    @Bean
    public ExternalTransferUseCase externalTransferUseCase(UserRepository userRepository,
                                                             WalletRepository walletRepository,
                                                             TransactionRepository transactionRepository,
                                                             TransactionIdGenerator transactionIdGenerator,
                                                             Clock clock,
                                                             ReversibleOperationsStack reversibleOperationsStack,
                                                             FraudEventRepository fraudEventRepository,
                                                             TransferGraphRepository transferGraphRepository,
                                                             FraudDetector fraudDetector,
                                                             NotificationEmitter notificationEmitter) {
        return new ExternalTransferUseCase(userRepository, walletRepository,
                transactionRepository, transactionIdGenerator, clock, reversibleOperationsStack,
                fraudEventRepository, transferGraphRepository, fraudDetector, notificationEmitter);
    }

    @Bean
    public ListUserTransactionsUseCase listUserTransactionsUseCase(UserRepository userRepository,
                                                                     TransactionRepository transactionRepository) {
        return new ListUserTransactionsUseCase(userRepository, transactionRepository);
    }

    @Bean
    public ListWalletTransactionsUseCase listWalletTransactionsUseCase(UserRepository userRepository,
                                                                         WalletRepository walletRepository,
                                                                         TransactionRepository transactionRepository) {
        return new ListWalletTransactionsUseCase(userRepository, walletRepository, transactionRepository);
    }

    @Bean
    public ReverseTransactionUseCase reverseTransactionUseCase(WalletRepository walletRepository,
                                                                UserRepository userRepository,
                                                                TransactionRepository transactionRepository,
                                                                ReversibleOperationsStack reversibleOperationsStack) {
        return new ReverseTransactionUseCase(walletRepository, userRepository,
                transactionRepository, reversibleOperationsStack);
    }

    @Bean
    public TransactionMapper transactionMapper() {
        return new TransactionMapper();
    }
}
