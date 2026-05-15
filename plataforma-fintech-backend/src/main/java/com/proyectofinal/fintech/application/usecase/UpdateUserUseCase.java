package com.proyectofinal.fintech.application.usecase;

import com.proyectofinal.fintech.application.result.UserView;
import com.proyectofinal.fintech.domain.exception.DuplicatedResourceException;
import com.proyectofinal.fintech.domain.exception.ErrorCode;
import com.proyectofinal.fintech.domain.exception.NotFoundException;
import com.proyectofinal.fintech.domain.model.Usuario;
import com.proyectofinal.fintech.domain.port.UserRepository;
import com.proyectofinal.fintech.domain.port.WalletRepository;

import java.util.Optional;

/**
 * Use case: update user's name and/or email.
 * Plain class — ZERO Spring/Jakarta imports.
 * ADR-11.7: creates a new Usuario instance (replacing the existing one via upsert in repo).
 */
public class UpdateUserUseCase {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;

    public UpdateUserUseCase(UserRepository userRepository, WalletRepository walletRepository) {
        this.userRepository = userRepository;
        this.walletRepository = walletRepository;
    }

    /**
     * Updates a user's name and/or email. Fields wrapped in Optional.empty() are left unchanged.
     *
     * @param userId  the user to update
     * @param newName new name, or empty to keep current
     * @param newEmail new email, or empty to keep current
     * @return updated UserView
     * @throws NotFoundException if user not found
     */
    public UserView execute(String userId, Optional<String> newName, Optional<String> newEmail) {
        Usuario existing = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(
                        ErrorCode.USER_NOT_FOUND,
                        "User with id=" + userId + " not found"));

        String name = newName.orElse(existing.getName());
        String email = newEmail.orElse(existing.getEmail());

        // Enforce email uniqueness across users (only when email is actually changing)
        if (!email.equals(existing.getEmail())) {
            userRepository.findByEmail(email).ifPresent(other -> {
                if (!other.getId().equals(existing.getId())) {
                    throw new DuplicatedResourceException(
                            ErrorCode.DUPLICATED_RESOURCE,
                            "User with email=" + email + " already exists");
                }
            });
        }

        // Create updated user preserving all immutable / computed state
        Usuario updated = new Usuario(
                existing.getId(),
                name,
                email,
                existing.getRegisteredAt(),
                existing.getPoints(),
                existing.getLoyaltyLevel()
        );

        userRepository.save(updated);

        int walletCount = walletRepository.countByOwnerId(userId);
        double totalBalance = walletRepository.sumBalanceByOwnerId(userId);

        return new UserView(
                updated.getId(),
                updated.getName(),
                updated.getEmail(),
                updated.getRegisteredAt(),
                updated.getPoints(),
                updated.getLoyaltyLevel(),
                walletCount,
                totalBalance
        );
    }
}
