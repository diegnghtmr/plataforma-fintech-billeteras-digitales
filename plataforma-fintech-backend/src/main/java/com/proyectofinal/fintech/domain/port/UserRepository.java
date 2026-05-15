package com.proyectofinal.fintech.domain.port;

import com.proyectofinal.fintech.domain.model.Usuario;

import java.util.Optional;

/**
 * Output port for user persistence.
 * ZERO Spring/Jakarta imports — pure domain interface.
 */
public interface UserRepository {

    void save(Usuario usuario);

    Optional<Usuario> findById(String id);

    boolean existsById(String id);

    Iterable<Usuario> findAll();

    /**
     * Removes the user with the given id. No-op if not found.
     */
    void deleteById(String id);

    /**
     * Finds a user by exact (case-sensitive) email match.
     * C3: required by CreateUserUseCase to enforce email uniqueness.
     *
     * @param email the email to search for
     * @return an Optional containing the matching user, or empty if not found
     */
    Optional<Usuario> findByEmail(String email);
}
