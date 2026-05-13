package com.proyectofinal.fintech.application.usecase;

import com.proyectofinal.fintech.application.result.UserView;
import com.proyectofinal.fintech.domain.model.Usuario;
import com.proyectofinal.fintech.domain.port.UserRepository;
import com.proyectofinal.fintech.domain.port.WalletRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * Use case: list all users with computed wallet aggregates.
 * Plain class — ZERO Spring/Jakarta imports.
 */
public class ListUsersUseCase {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;

    public ListUsersUseCase(UserRepository userRepository, WalletRepository walletRepository) {
        this.userRepository = userRepository;
        this.walletRepository = walletRepository;
    }

    public List<UserView> execute() {
        List<UserView> result = new ArrayList<>();

        for (Usuario usuario : userRepository.findAll()) {
            int walletCount = walletRepository.countByOwnerId(usuario.getId());
            double totalBalance = walletRepository.sumBalanceByOwnerId(usuario.getId());

            result.add(new UserView(
                    usuario.getId(),
                    usuario.getName(),
                    usuario.getEmail(),
                    usuario.getRegisteredAt(),
                    usuario.getPoints(),
                    usuario.getLoyaltyLevel(),
                    walletCount,
                    totalBalance
            ));
        }

        return result;
    }
}
