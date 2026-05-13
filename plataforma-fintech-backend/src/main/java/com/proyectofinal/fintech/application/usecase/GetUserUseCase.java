package com.proyectofinal.fintech.application.usecase;

import com.proyectofinal.fintech.application.result.UserView;
import com.proyectofinal.fintech.domain.exception.ErrorCode;
import com.proyectofinal.fintech.domain.exception.NotFoundException;
import com.proyectofinal.fintech.domain.model.Usuario;
import com.proyectofinal.fintech.domain.port.UserRepository;
import com.proyectofinal.fintech.domain.port.WalletRepository;

/**
 * Use case: get a user by id with computed wallet aggregates.
 * Plain class — ZERO Spring/Jakarta imports.
 */
public class GetUserUseCase {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;

    public GetUserUseCase(UserRepository userRepository, WalletRepository walletRepository) {
        this.userRepository = userRepository;
        this.walletRepository = walletRepository;
    }

    public UserView execute(String userId) {
        Usuario usuario = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(
                        ErrorCode.USER_NOT_FOUND,
                        "User with id=" + userId + " not found"));

        int walletCount = walletRepository.countByOwnerId(userId);
        double totalBalance = walletRepository.sumBalanceByOwnerId(userId);

        return new UserView(
                usuario.getId(),
                usuario.getName(),
                usuario.getEmail(),
                usuario.getRegisteredAt(),
                usuario.getPoints(),
                usuario.getLoyaltyLevel(),
                walletCount,
                totalBalance
        );
    }
}
