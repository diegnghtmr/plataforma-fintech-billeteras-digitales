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
 */
public class CreateUserUseCase {

    private final UserRepository userRepository;
    private final Clock clock;

    public CreateUserUseCase(UserRepository userRepository, Clock clock) {
        this.userRepository = userRepository;
        this.clock = clock;
    }

    public Usuario execute(String id, String name, String email) {
        if (userRepository.existsById(id)) {
            throw new DuplicatedResourceException(
                    ErrorCode.DUPLICATED_RESOURCE,
                    "User with id=" + id + " already exists");
        }

        Usuario usuario = new Usuario(
                id,
                name,
                email,
                Instant.now(clock),
                0.0,
                LoyaltyLevel.BRONZE
        );

        userRepository.save(usuario);
        return usuario;
    }
}
