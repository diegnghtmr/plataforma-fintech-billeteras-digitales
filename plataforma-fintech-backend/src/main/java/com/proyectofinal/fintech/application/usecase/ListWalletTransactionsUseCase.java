package com.proyectofinal.fintech.application.usecase;

import com.proyectofinal.fintech.domain.exception.ErrorCode;
import com.proyectofinal.fintech.domain.exception.NotFoundException;
import com.proyectofinal.fintech.domain.model.Transaccion;
import com.proyectofinal.fintech.domain.port.TransactionRepository;
import com.proyectofinal.fintech.domain.port.UserRepository;
import com.proyectofinal.fintech.domain.port.WalletRepository;

/**
 * Use case: list all transactions for a specific wallet.
 * Plain class — ZERO Spring/Jakarta imports.
 */
public class ListWalletTransactionsUseCase {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;

    public ListWalletTransactionsUseCase(UserRepository userRepository,
                                          WalletRepository walletRepository,
                                          TransactionRepository transactionRepository) {
        this.userRepository = userRepository;
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
    }

    /**
     * Returns all transactions associated with the wallet.
     *
     * @param userId   user who owns the wallet
     * @param walletId wallet code
     * @return iterable of transactions in repository order
     */
    public Iterable<Transaccion> execute(String userId, String walletId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND,
                        "User with id=" + userId + " not found"));

        walletRepository.findByOwnerIdAndCode(userId, walletId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.WALLET_NOT_FOUND,
                        "Wallet with code=" + walletId + " not found for user=" + userId));

        return transactionRepository.findByWalletId(walletId);
    }
}
