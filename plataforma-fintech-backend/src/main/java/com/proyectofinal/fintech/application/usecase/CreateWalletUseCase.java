package com.proyectofinal.fintech.application.usecase;

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
 * Wallet code is auto-generated in WAL### format (e.g. WAL001, WAL002…).
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

    public Billetera execute(String ownerId, String name, String type) {
        if (!userRepository.existsById(ownerId)) {
            throw new NotFoundException(
                    ErrorCode.USER_NOT_FOUND,
                    "User with id=" + ownerId + " not found");
        }

        String code = generateNextCode();

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

    private String generateNextCode() {
        int maxN = 0;
        for (Billetera w : walletRepository.findAll()) {
            String code = w.getCode();
            if (code != null && code.startsWith("WAL") && code.length() > 3) {
                try {
                    int n = Integer.parseInt(code.substring(3));
                    if (n > maxN) maxN = n;
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return String.format("WAL%03d", maxN + 1);
    }
}
