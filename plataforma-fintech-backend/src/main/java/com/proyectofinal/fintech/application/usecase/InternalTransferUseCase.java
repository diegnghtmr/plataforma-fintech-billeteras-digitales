package com.proyectofinal.fintech.application.usecase;

import com.proyectofinal.fintech.application.service.NotificationEmitter;
import com.proyectofinal.fintech.domain.exception.BusinessRuleException;
import com.proyectofinal.fintech.domain.exception.ErrorCode;
import com.proyectofinal.fintech.domain.exception.NotFoundException;
import com.proyectofinal.fintech.domain.model.*;
import com.proyectofinal.fintech.domain.port.FraudEventRepository;
import com.proyectofinal.fintech.domain.port.ReversibleOperationsStack;
import com.proyectofinal.fintech.domain.port.TransactionIdGenerator;
import com.proyectofinal.fintech.domain.port.TransactionRepository;
import com.proyectofinal.fintech.domain.port.UserRepository;
import com.proyectofinal.fintech.domain.port.WalletRepository;
import com.proyectofinal.fintech.domain.service.FraudDetector;
import com.proyectofinal.fintech.domain.service.PuntosCalculator;

import java.time.Clock;
import java.time.Instant;

/**
 * Use case: transfer between two wallets owned by the same user.
 * Plain class — ZERO Spring/Jakarta imports.
 * SDD-11: FraudDetector instance; LOW_BALANCE alert on source; level-up notification.
 */
public class InternalTransferUseCase {

    private static final double LOW_BALANCE_THRESHOLD = 100.0;

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionIdGenerator idGenerator;
    private final Clock clock;
    private final ReversibleOperationsStack stack;
    private final FraudEventRepository fraudRepo;
    private final FraudDetector fraudDetector;
    private final NotificationEmitter notificationEmitter;

    public InternalTransferUseCase(UserRepository userRepository,
                                    WalletRepository walletRepository,
                                    TransactionRepository transactionRepository,
                                    TransactionIdGenerator idGenerator,
                                    Clock clock,
                                    ReversibleOperationsStack stack,
                                    FraudEventRepository fraudRepo,
                                    FraudDetector fraudDetector,
                                    NotificationEmitter notificationEmitter) {
        this.userRepository = userRepository;
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
        this.idGenerator = idGenerator;
        this.clock = clock;
        this.stack = stack;
        this.fraudRepo = fraudRepo;
        this.fraudDetector = fraudDetector;
        this.notificationEmitter = notificationEmitter;
    }

    public Transaccion execute(String userId, String sourceWalletId, String targetWalletId,
                                double amount, String description) {
        if (sourceWalletId.equals(targetWalletId)) {
            throw new BusinessRuleException(ErrorCode.VALIDATION_ERROR,
                    "Source and target wallets must be different");
        }

        if (amount <= 0) {
            throw new BusinessRuleException(ErrorCode.VALIDATION_ERROR,
                    "Amount must be greater than zero; got: " + amount);
        }

        Usuario user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND,
                        "User with id=" + userId + " not found"));

        Billetera source = walletRepository.findByOwnerIdAndCode(userId, sourceWalletId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.WALLET_NOT_FOUND,
                        "Source wallet=" + sourceWalletId + " not found for user=" + userId));

        Billetera target = walletRepository.findByOwnerIdAndCode(userId, targetWalletId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.WALLET_NOT_FOUND,
                        "Target wallet=" + targetWalletId + " not found for user=" + userId));

        if (amount > source.getBalance()) {
            throw new BusinessRuleException(ErrorCode.INSUFFICIENT_FUNDS,
                    "Insufficient funds: balance=" + source.getBalance() + ", requested=" + amount);
        }

        source.setBalance(source.getBalance() - amount);
        target.setBalance(target.getBalance() + amount);
        source.setTransactionCount(source.getTransactionCount() + 1);
        target.setTransactionCount(target.getTransactionCount() + 1);

        double points = PuntosCalculator.compute(TransactionType.INTERNAL_TRANSFER, amount);
        boolean levelChanged = user.addPoints(points);

        Transaccion tx = new Transaccion(
                idGenerator.next(),
                Instant.now(clock),
                TransactionType.INTERNAL_TRANSFER,
                amount,
                sourceWalletId,
                targetWalletId,
                userId,
                userId,
                TransactionStatus.SUCCESSFUL,
                points,
                description,
                true
        );

        transactionRepository.save(tx);

        fraudDetector.detect(tx).ifPresent(ev -> {
            tx.markRiskLevel(ev.getSeverity());
            transactionRepository.save(tx);
            fraudRepo.save(ev);
        });

        stack.push(tx);
        walletRepository.save(source);
        walletRepository.save(target);
        userRepository.save(user);

        // SDD-11: LOW_BALANCE alert on source wallet
        if (source.getBalance() < LOW_BALANCE_THRESHOLD) {
            notificationEmitter.emitLowBalance(userId, sourceWalletId);
        }

        // SDD-11: level-up notification
        if (levelChanged) {
            notificationEmitter.emitLevelUp(userId, user.getLoyaltyLevel());
        }

        return tx;
    }
}
