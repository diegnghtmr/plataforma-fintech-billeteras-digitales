package com.proyectofinal.fintech.application.usecase;

import com.proyectofinal.fintech.application.result.UserView;
import com.proyectofinal.fintech.domain.model.Usuario;
import com.proyectofinal.fintech.domain.port.UserRepository;
import com.proyectofinal.fintech.domain.port.WalletRepository;
import com.proyectofinal.fintech.domain.structures.MiLista;

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

    // N+1 acknowledged: countByOwnerId + sumBalanceByOwnerId are called per user.
    // Acceptable because adapters are in-memory (TablaHash O(1) lookups); a future
    // persistent adapter would expose an aggregate port to flatten this loop.
    public List<UserView> execute() {
        MiLista<UserView> result = new MiLista<>();

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

        // Jackson requires java.util.List at the REST boundary; conversion is ADR-9.1 scope.
        return result.toList();
    }
}
