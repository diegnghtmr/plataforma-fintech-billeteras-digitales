package com.proyectofinal.fintech.application.usecase;

import com.proyectofinal.fintech.domain.exception.BusinessRuleException;
import com.proyectofinal.fintech.domain.exception.ErrorCode;
import com.proyectofinal.fintech.domain.exception.NotFoundException;
import com.proyectofinal.fintech.domain.model.*;
import com.proyectofinal.fintech.domain.port.ReversibleOperationsStack;
import com.proyectofinal.fintech.domain.port.TransactionRepository;
import com.proyectofinal.fintech.domain.port.UserRepository;
import com.proyectofinal.fintech.domain.port.WalletRepository;

/**
 * Use case: reverse a financial transaction.
 * Plain class — ZERO Spring/Jakarta imports.
 * ADR-6.2: only SENT side of external transfers is reversible.
 * ADR-6.3: user.points clamped at 0.
 */
public class ReverseTransactionUseCase {

    private final WalletRepository walletRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final ReversibleOperationsStack stack;

    public ReverseTransactionUseCase(WalletRepository walletRepository,
                                      UserRepository userRepository,
                                      TransactionRepository transactionRepository,
                                      ReversibleOperationsStack stack) {
        this.walletRepository = walletRepository;
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
        this.stack = stack;
    }

    /**
     * Reverses the transaction with the given id.
     *
     * @param transactionId the transaction to reverse
     * @return the mutated transaction with status=REVERSED
     */
    public Transaccion execute(String transactionId) {
        // Step 1: load or 404
        Transaccion tx = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.TRANSACTION_NOT_FOUND,
                        "Transaction with id=" + transactionId + " not found"));

        // Step 2-4: guard checks
        if (tx.getStatus() != TransactionStatus.SUCCESSFUL
                || !tx.isReversible()
                || tx.getType() == TransactionType.EXTERNAL_TRANSFER_RECEIVED) {
            throw new BusinessRuleException(ErrorCode.TRANSACTION_NOT_REVERSIBLE,
                    "Transaction " + transactionId + " cannot be reversed");
        }

        // Step 5: load source user
        Usuario sourceUser = userRepository.findById(tx.getSourceUserId())
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND,
                        "User with id=" + tx.getSourceUserId() + " not found"));

        // Step 6: apply inverse effects per type
        applyInverseEffect(tx, sourceUser);

        // Step 7: subtract points via addPoints(-delta); clamping and level recomputation handled internally
        sourceUser.addPoints(-tx.getPointsGenerated());

        // Step 8: persist
        tx.setStatus(TransactionStatus.REVERSED);
        transactionRepository.save(tx);
        userRepository.save(sourceUser);

        // Step 9: remove from stack
        stack.removeById(tx.getSourceUserId(), tx.getId());

        return tx;
    }

    private void applyInverseEffect(Transaccion tx, Usuario sourceUser) {
        switch (tx.getType()) {
            case RECHARGE -> applyReverseRecharge(tx);
            case WITHDRAWAL -> applyReverseWithdrawal(tx);
            case INTERNAL_TRANSFER -> applyReverseInternalTransfer(tx, sourceUser);
            case EXTERNAL_TRANSFER_SENT -> applyReverseExternalTransfer(tx);
            default -> throw new BusinessRuleException(ErrorCode.TRANSACTION_NOT_REVERSIBLE,
                    "Unsupported transaction type for reversal: " + tx.getType());
        }
    }

    private void applyReverseRecharge(Transaccion tx) {
        Billetera srcWallet = walletRepository.findByOwnerIdAndCode(tx.getSourceUserId(), tx.getSourceWalletId())
                .orElseThrow(() -> new NotFoundException(ErrorCode.WALLET_NOT_FOUND,
                        "Source wallet=" + tx.getSourceWalletId() + " not found"));

        // RECHARGE inverse: deduct amount — requires sufficient balance
        if (srcWallet.getBalance() < tx.getAmount()) {
            throw new BusinessRuleException(ErrorCode.INSUFFICIENT_FUNDS,
                    "Insufficient balance to reverse RECHARGE: balance=" + srcWallet.getBalance());
        }
        srcWallet.setBalance(srcWallet.getBalance() - tx.getAmount());
        walletRepository.save(srcWallet);
    }

    private void applyReverseWithdrawal(Transaccion tx) {
        Billetera srcWallet = walletRepository.findByOwnerIdAndCode(tx.getSourceUserId(), tx.getSourceWalletId())
                .orElseThrow(() -> new NotFoundException(ErrorCode.WALLET_NOT_FOUND,
                        "Source wallet=" + tx.getSourceWalletId() + " not found"));

        // WITHDRAWAL inverse: credit back — no balance check needed
        srcWallet.setBalance(srcWallet.getBalance() + tx.getAmount());
        walletRepository.save(srcWallet);
    }

    private void applyReverseInternalTransfer(Transaccion tx, Usuario sourceUser) {
        Billetera srcWallet = walletRepository.findByOwnerIdAndCode(tx.getSourceUserId(), tx.getSourceWalletId())
                .orElseThrow(() -> new NotFoundException(ErrorCode.WALLET_NOT_FOUND,
                        "Source wallet=" + tx.getSourceWalletId() + " not found"));
        // For internal transfer, source and target are same user
        Billetera tgtWallet = walletRepository.findByOwnerIdAndCode(tx.getSourceUserId(), tx.getTargetWalletId())
                .orElseThrow(() -> new NotFoundException(ErrorCode.WALLET_NOT_FOUND,
                        "Target wallet=" + tx.getTargetWalletId() + " not found"));

        // INTERNAL_TRANSFER inverse: target -= amount (check), source += amount
        if (tgtWallet.getBalance() < tx.getAmount()) {
            throw new BusinessRuleException(ErrorCode.INSUFFICIENT_FUNDS,
                    "Insufficient balance in target wallet to reverse INTERNAL_TRANSFER: balance=" + tgtWallet.getBalance());
        }
        tgtWallet.setBalance(tgtWallet.getBalance() - tx.getAmount());
        srcWallet.setBalance(srcWallet.getBalance() + tx.getAmount());
        walletRepository.save(srcWallet);
        walletRepository.save(tgtWallet);
    }

    private void applyReverseExternalTransfer(Transaccion tx) {
        // Load source wallet (sender's wallet)
        Billetera srcWallet = walletRepository.findByOwnerIdAndCode(tx.getSourceUserId(), tx.getSourceWalletId())
                .orElseThrow(() -> new NotFoundException(ErrorCode.WALLET_NOT_FOUND,
                        "Source wallet=" + tx.getSourceWalletId() + " not found"));

        // Load target wallet (receiver's wallet) — belongs to targetUserId
        Billetera tgtWallet = walletRepository.findByOwnerIdAndCode(tx.getTargetUserId(), tx.getTargetWalletId())
                .orElseThrow(() -> new NotFoundException(ErrorCode.WALLET_NOT_FOUND,
                        "Target wallet=" + tx.getTargetWalletId() + " not found"));

        // EXTERNAL_TRANSFER_SENT inverse: target -= amount (check), source += amount
        if (tgtWallet.getBalance() < tx.getAmount()) {
            throw new BusinessRuleException(ErrorCode.INSUFFICIENT_FUNDS,
                    "Insufficient balance in target wallet to reverse EXTERNAL_TRANSFER_SENT: balance=" + tgtWallet.getBalance());
        }
        tgtWallet.setBalance(tgtWallet.getBalance() - tx.getAmount());
        srcWallet.setBalance(srcWallet.getBalance() + tx.getAmount());
        walletRepository.save(srcWallet);
        walletRepository.save(tgtWallet);

        // Find and mark paired RECEIVED tx as REVERSED
        Iterable<Transaccion> tgtTxs = transactionRepository.findByWalletId(tx.getTargetWalletId());
        for (Transaccion candidate : tgtTxs) {
            if (candidate.getType() == TransactionType.EXTERNAL_TRANSFER_RECEIVED
                    && candidate.getStatus() == TransactionStatus.SUCCESSFUL
                    && candidate.getSourceWalletId().equals(tx.getSourceWalletId())
                    && candidate.getTargetWalletId().equals(tx.getTargetWalletId())
                    && candidate.getTimestamp().equals(tx.getTimestamp())) {
                candidate.setStatus(TransactionStatus.REVERSED);
                transactionRepository.save(candidate);
                break;
            }
        }
    }
}
