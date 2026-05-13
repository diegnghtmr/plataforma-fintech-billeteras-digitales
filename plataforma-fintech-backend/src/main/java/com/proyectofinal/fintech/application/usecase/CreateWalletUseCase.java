package com.proyectofinal.fintech.application.usecase;

import com.proyectofinal.fintech.domain.exception.DuplicatedResourceException;
import com.proyectofinal.fintech.domain.exception.ErrorCode;
import com.proyectofinal.fintech.domain.exception.NotFoundException;
import com.proyectofinal.fintech.domain.model.Billetera;
import com.proyectofinal.fintech.domain.port.UserRepository;
import com.proyectofinal.fintech.domain.port.WalletRepository;

import java.time.Clock;
import java.time.Instant;

/**
 * Use case: create a new wallet for an existing user.
 * Plain class — ZERO Spring/Jakarta imports.
 * Clock injected for testability.
 */
public class CreateWalletUseCase {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final Clock clock;

    public CreateWalletUseCase(UserRepository userRepository,
                                WalletRepository walletRepository,
                                Clock clock) {
        this.userRepository = userRepository;
        this.walletRepository = walletRepository;
        this.clock = clock;
    }

    public Billetera execute(String ownerId, String code, String name, String type) {
        if (!userRepository.existsById(ownerId)) {
            throw new NotFoundException(
                    ErrorCode.USER_NOT_FOUND,
                    "User with id=" + ownerId + " not found");
        }

        if (walletRepository.existsByOwnerIdAndCode(ownerId, code)) {
            throw new DuplicatedResourceException(
                    ErrorCode.DUPLICATED_RESOURCE,
                    "Wallet with code=" + code + " already exists for user=" + ownerId);
        }

        Billetera billetera = new Billetera(
                code,
                name,
                type,
                ownerId,
                0.0,
                true,
                Instant.now(clock),
                0
        );

        walletRepository.save(billetera);
        return billetera;
    }
}
