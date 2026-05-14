package com.proyectofinal.fintech.application.usecase;

import com.proyectofinal.fintech.application.result.ExternalTransferResult;
import com.proyectofinal.fintech.application.service.NotificationEmitter;
import com.proyectofinal.fintech.domain.exception.BusinessRuleException;
import com.proyectofinal.fintech.domain.exception.DomainException;
import com.proyectofinal.fintech.domain.exception.ErrorCode;
import com.proyectofinal.fintech.domain.exception.NotFoundException;
import com.proyectofinal.fintech.domain.model.*;
import com.proyectofinal.fintech.domain.port.FraudEventRepository;
import com.proyectofinal.fintech.domain.port.ReversibleOperationsStack;
import com.proyectofinal.fintech.domain.port.TransactionIdGenerator;
import com.proyectofinal.fintech.domain.port.TransactionRepository;
import com.proyectofinal.fintech.domain.port.TransferGraphRepository;
import com.proyectofinal.fintech.domain.port.UserRepository;
import com.proyectofinal.fintech.domain.port.WalletRepository;
import com.proyectofinal.fintech.domain.service.FraudDetector;
import com.proyectofinal.fintech.domain.service.PuntosCalculator;

import java.time.Clock;
import java.time.Instant;

/**
 * Use case: transfer between wallets owned by different users.
 * Plain class — ZERO Spring/Jakarta imports.
 * ADR-5.2: produces two Transaccion entries (SENT + RECEIVED) with identical timestamp and shared fields.
 * SDD-11: FraudDetector instance; LOW_BALANCE alert on source; level-up notifications.
 */
public class ExternalTransferUseCase {

    private static final double LOW_BALANCE_THRESHOLD = 100.0;

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final TransactionIdGenerator idGenerator;
    private final Clock clock;
    private final ReversibleOperationsStack stack;
    private final FraudEventRepository fraudRepo;
    private final TransferGraphRepository graph;
    private final FraudDetector fraudDetector;
    private final NotificationEmitter notificationEmitter;

    public ExternalTransferUseCase(UserRepository userRepository,
                                    WalletRepository walletRepository,
                                    TransactionRepository transactionRepository,
                                    TransactionIdGenerator idGenerator,
                                    Clock clock,
                                    ReversibleOperationsStack stack,
                                    FraudEventRepository fraudRepo,
                                    TransferGraphRepository graph,
                                    FraudDetector fraudDetector,
                                    NotificationEmitter notificationEmitter) {
        this.userRepository = userRepository;
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
        this.idGenerator = idGenerator;
        this.clock = clock;
        this.stack = stack;
        this.fraudRepo = fraudRepo;
        this.graph = graph;
        this.fraudDetector = fraudDetector;
        this.notificationEmitter = notificationEmitter;
    }

    public ExternalTransferResult execute(String sourceUserId, String sourceWalletId,
                                           String targetUserId, String targetWalletId,
                                           double amount, String description) {
        try {
            return executeInternal(sourceUserId, sourceWalletId, targetUserId, targetWalletId, amount, description);
        } catch (DomainException e) {
            notificationEmitter.emitOperationRejected(sourceUserId, e.getMessage());
            throw e;
        }
    }

    private ExternalTransferResult executeInternal(String sourceUserId, String sourceWalletId,
                                                    String targetUserId, String targetWalletId,
                                                    double amount, String description) {
        if (sourceUserId.equals(targetUserId)) {
            throw new BusinessRuleException(ErrorCode.VALIDATION_ERROR,
                    "Source and target users must be different");
        }

        if (amount <= 0) {
            throw new BusinessRuleException(ErrorCode.VALIDATION_ERROR,
                    "Amount must be greater than zero; got: " + amount);
        }

        Usuario sourceUser = userRepository.findById(sourceUserId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND,
                        "User with id=" + sourceUserId + " not found"));

        Billetera sourceWallet = walletRepository.findByOwnerIdAndCode(sourceUserId, sourceWalletId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.WALLET_NOT_FOUND,
                        "Source wallet=" + sourceWalletId + " not found for user=" + sourceUserId));

        Usuario targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND,
                        "User with id=" + targetUserId + " not found"));

        Billetera targetWallet = walletRepository.findByOwnerIdAndCode(targetUserId, targetWalletId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.WALLET_NOT_FOUND,
                        "Target wallet=" + targetWalletId + " not found for user=" + targetUserId));

        if (amount > sourceWallet.getBalance()) {
            throw new BusinessRuleException(ErrorCode.INSUFFICIENT_FUNDS,
                    "Insufficient funds: balance=" + sourceWallet.getBalance() + ", requested=" + amount);
        }

        sourceWallet.setBalance(sourceWallet.getBalance() - amount);
        targetWallet.setBalance(targetWallet.getBalance() + amount);
        sourceWallet.setTransactionCount(sourceWallet.getTransactionCount() + 1);
        targetWallet.setTransactionCount(targetWallet.getTransactionCount() + 1);

        double sentPoints = PuntosCalculator.compute(TransactionType.EXTERNAL_TRANSFER_SENT, amount);
        double receivedPoints = PuntosCalculator.compute(TransactionType.EXTERNAL_TRANSFER_RECEIVED, amount);
        boolean sourceLevelChanged = sourceUser.addPoints(sentPoints);
        boolean targetLevelChanged = targetUser.addPoints(receivedPoints);

        Instant sharedTimestamp = Instant.now(clock);

        Transaccion outgoing = new Transaccion(
                idGenerator.next(),
                sharedTimestamp,
                TransactionType.EXTERNAL_TRANSFER_SENT,
                amount,
                sourceWalletId,
                targetWalletId,
                sourceUserId,
                targetUserId,
                TransactionStatus.SUCCESSFUL,
                sentPoints,
                description,
                true
        );

        Transaccion incoming = new Transaccion(
                idGenerator.next(),
                sharedTimestamp,
                TransactionType.EXTERNAL_TRANSFER_RECEIVED,
                amount,
                sourceWalletId,
                targetWalletId,
                sourceUserId,
                targetUserId,
                TransactionStatus.SUCCESSFUL,
                receivedPoints,
                description,
                false
        );

        transactionRepository.save(outgoing);
        transactionRepository.save(incoming);

        // SDD-11: fraud check on outgoing tx only (amount is same on both legs — avoid double event)
        fraudDetector.detect(outgoing).ifPresent(ev -> {
            outgoing.markRiskLevel(ev.getSeverity());
            transactionRepository.save(outgoing);
            fraudRepo.save(ev);
        });

        // SDD-09: record the inter-user transfer in the graph
        graph.addEdge(sourceUserId, targetUserId, amount);

        stack.push(outgoing);
        walletRepository.save(sourceWallet);
        walletRepository.save(targetWallet);
        userRepository.save(sourceUser);
        userRepository.save(targetUser);

        // SDD-11: LOW_BALANCE alert on source wallet
        if (sourceWallet.getBalance() < LOW_BALANCE_THRESHOLD) {
            notificationEmitter.emitLowBalance(sourceUserId, sourceWalletId);
        }

        // SDD-11: level-up notifications
        if (sourceLevelChanged) {
            notificationEmitter.emitLevelUp(sourceUserId, sourceUser.getLoyaltyLevel());
        }
        if (targetLevelChanged) {
            notificationEmitter.emitLevelUp(targetUserId, targetUser.getLoyaltyLevel());
        }

        return new ExternalTransferResult(outgoing, incoming);
    }
}
