package com.proyectofinal.fintech.application.usecase;

import com.proyectofinal.fintech.domain.exception.BusinessRuleException;
import com.proyectofinal.fintech.domain.exception.ErrorCode;
import com.proyectofinal.fintech.domain.exception.NotFoundException;
import com.proyectofinal.fintech.domain.model.Billetera;
import com.proyectofinal.fintech.domain.port.UserRepository;
import com.proyectofinal.fintech.domain.port.WalletRepository;

import java.util.Optional;

/**
 * Use case: rename a wallet and/or toggle its active flag.
 * Plain class — ZERO Spring/Jakarta imports.
 *
 * Rules:
 *  - Wallet must exist for the given user.
 *  - newName, when present, must be non-blank.
 *  - Setting active=false (closing) requires balance == 0.
 *  - Reopening (active=true) has no constraint.
 *  - transactionCount and createdAt are preserved.
 */
public class UpdateWalletUseCase {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;

    public UpdateWalletUseCase(UserRepository userRepository, WalletRepository walletRepository) {
        this.userRepository = userRepository;
        this.walletRepository = walletRepository;
    }

    public Billetera execute(String userId,
                             String walletCode,
                             Optional<String> newName,
                             Optional<Boolean> newActive) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND,
                        "User with id=" + userId + " not found"));

        Billetera existing = walletRepository.findByOwnerIdAndCode(userId, walletCode)
                .orElseThrow(() -> new NotFoundException(ErrorCode.WALLET_NOT_FOUND,
                        "Wallet " + walletCode + " not found for user " + userId));

        String name = existing.getName();
        if (newName.isPresent()) {
            String trimmed = newName.get().trim();
            if (trimmed.isEmpty()) {
                throw new BusinessRuleException(ErrorCode.VALIDATION_ERROR,
                        "El nombre de la billetera no puede estar vacío");
            }
            name = trimmed;
        }

        boolean active = existing.isActive();
        if (newActive.isPresent()) {
            boolean nextActive = newActive.get();
            if (!nextActive && existing.getBalance() > 0.0) {
                throw new BusinessRuleException(ErrorCode.VALIDATION_ERROR,
                        "No se puede cerrar una billetera con saldo > 0 (saldo actual: " + existing.getBalance() + ")");
            }
            active = nextActive;
        }

        Billetera updated = new Billetera(
                existing.getCode(),
                name,
                existing.getType(),
                existing.getOwnerId(),
                existing.getBalance(),
                active,
                existing.getCreatedAt(),
                existing.getTransactionCount()
        );
        walletRepository.save(updated);
        return updated;
    }
}
