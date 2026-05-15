package com.proyectofinal.fintech.application.usecase;

import com.proyectofinal.fintech.domain.exception.DuplicatedResourceException;
import com.proyectofinal.fintech.domain.exception.ErrorCode;
import com.proyectofinal.fintech.domain.model.LoyaltyLevel;
import com.proyectofinal.fintech.domain.model.Usuario;
import com.proyectofinal.fintech.domain.port.UserRepository;

import java.time.Clock;
import java.time.Instant;

/**
 * Use case: create a new user.
 * Plain class — ZERO Spring/Jakarta imports.
 * Clock injected for testability.
 * ID is auto-generated in USR### format (e.g. USR001, USR002…).
 */
public class CreateUserUseCase {

    private final UserRepository userRepository;
    private final Clock clock;

    public CreateUserUseCase(UserRepository userRepository, Clock clock) {
        this.userRepository = userRepository;
        this.clock = clock;
    }

    public Usuario execute(String name, String email) {
        // C3: enforce email uniqueness before id generation
        userRepository.findByEmail(email).ifPresent(u -> {
            throw new DuplicatedResourceException(
                    ErrorCode.DUPLICATED_RESOURCE,
                    "User with email=" + email + " already exists");
        });

        String nextId = generateNextId();

        Usuario usuario = new Usuario(
                nextId,
                name,
                email,
                Instant.now(clock),
                0.0,
                LoyaltyLevel.BRONZE
        );

        userRepository.save(usuario);
        return usuario;
    }

    private String generateNextId() {
        int maxN = 0;
        for (Usuario u : userRepository.findAll()) {
            String id = u.getId();
            if (id != null && id.startsWith("USR")) {
                try {
                    int n = Integer.parseInt(id.substring(3));
                    if (n > maxN) maxN = n;
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return String.format("USR%03d", maxN + 1);
    }
}
