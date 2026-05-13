package com.proyectofinal.fintech.application.usecase;

import com.proyectofinal.fintech.domain.exception.ErrorCode;
import com.proyectofinal.fintech.domain.exception.NotFoundException;
import com.proyectofinal.fintech.domain.model.Billetera;
import com.proyectofinal.fintech.domain.port.UserRepository;
import com.proyectofinal.fintech.domain.port.WalletRepository;

/**
 * Use case: list wallets for a given user.
 * Plain class — ZERO Spring/Jakarta imports.
 */
public class ListWalletsUseCase {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;

    public ListWalletsUseCase(UserRepository userRepository, WalletRepository walletRepository) {
        this.userRepository = userRepository;
        this.walletRepository = walletRepository;
    }

    public Iterable<Billetera> execute(String userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException(
                    ErrorCode.USER_NOT_FOUND,
                    "User with id=" + userId + " not found");
        }

        return walletRepository.findByOwnerId(userId);
    }
}
