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
 * Use case: recharge a wallet with a monetary amount.
 * Plain class — ZERO Spring/Jakarta imports.
 * ADR-5.5: Defensive amount validation in addition to DTO @DecimalMin(0.01).
 * SDD-11: FraudDetector now instance-injected; addPoints returns boolean for level-up notification.
 */
public class RechargeWalletUseCase {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionIdGenerator idGenerator;
    private final Clock clock;
    private final ReversibleOperationsStack stack;
    private final FraudEventRepository fraudRepo;
    private final FraudDetector fraudDetector;
    private final NotificationEmitter notificationEmitter;

    public RechargeWalletUseCase(UserRepository userRepository,
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

    public Transaccion execute(String userId, String walletId, double amount, String description) {
        if (amount <= 0) {
            throw new BusinessRuleException(ErrorCode.VALIDATION_ERROR,
                    "Amount must be greater than zero; got: " + amount);
        }

        Usuario user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND,
                        "User with id=" + userId + " not found"));

        Billetera wallet = walletRepository.findByOwnerIdAndCode(userId, walletId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.WALLET_NOT_FOUND,
                        "Wallet with code=" + walletId + " not found for user=" + userId));

        wallet.setBalance(wallet.getBalance() + amount);
        wallet.setTransactionCount(wallet.getTransactionCount() + 1);

        double points = PuntosCalculator.compute(TransactionType.RECHARGE, amount);
        boolean levelChanged = user.addPoints(points);

        Transaccion tx = new Transaccion(
                idGenerator.next(),
                Instant.now(clock),
                TransactionType.RECHARGE,
                amount,
                walletId,
                null,
                userId,
                null,
                TransactionStatus.SUCCESSFUL,
                points,
                description,
                true
        );

        transactionRepository.save(tx);

        // SDD-11: instance FraudDetector called after save; markRiskLevel + re-save if fraud
        fraudDetector.detect(tx).ifPresent(ev -> {
            tx.markRiskLevel(ev.getSeverity());
            transactionRepository.save(tx);
            fraudRepo.save(ev);
        });

        stack.push(tx);
        walletRepository.save(wallet);
        userRepository.save(user);

        // SDD-11: emit level-up notification if level increased
        if (levelChanged) {
            notificationEmitter.emitLevelUp(userId, user.getLoyaltyLevel());
        }

        return tx;
    }
}
