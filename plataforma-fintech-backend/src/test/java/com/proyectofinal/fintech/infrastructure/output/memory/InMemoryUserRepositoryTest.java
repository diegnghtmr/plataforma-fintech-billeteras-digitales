package com.proyectofinal.fintech.infrastructure.output.memory;

import com.proyectofinal.fintech.domain.model.LoyaltyLevel;
import com.proyectofinal.fintech.domain.model.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryUserRepositoryTest {

    private InMemoryUserRepository repository;
    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

    @BeforeEach
    void setUp() {
        repository = new InMemoryUserRepository();
    }

    @Test
    void save_thenFindById_returnsUser() {
        Usuario user = new Usuario("USR001", "Juan", "juan@example.com", NOW, 0.0, LoyaltyLevel.BRONZE);
        repository.save(user);

        Optional<Usuario> found = repository.findById("USR001");
        assertTrue(found.isPresent());
        assertEquals("USR001", found.get().getId());
        assertEquals("Juan", found.get().getName());
    }

    @Test
    void findById_unknown_returnsEmpty() {
        Optional<Usuario> found = repository.findById("UNKNOWN");
        assertTrue(found.isEmpty());
    }

    @Test
    void existsById_true_whenSaved() {
        Usuario user = new Usuario("USR001", "Juan", "juan@example.com", NOW, 0.0, LoyaltyLevel.BRONZE);
        repository.save(user);
        assertTrue(repository.existsById("USR001"));
    }

    @Test
    void existsById_false_whenNotSaved() {
        assertFalse(repository.existsById("UNKNOWN"));
    }

    @Test
    void findAll_returnsAllSavedUsers() {
        repository.save(new Usuario("USR001", "Juan", "juan@example.com", NOW, 0.0, LoyaltyLevel.BRONZE));
        repository.save(new Usuario("USR002", "Maria", "maria@example.com", NOW, 0.0, LoyaltyLevel.BRONZE));

        int count = 0;
        for (Usuario u : repository.findAll()) {
            count++;
        }
        assertEquals(2, count);
    }

    @Test
    void save_duplicate_overwrites() {
        repository.save(new Usuario("USR001", "Juan", "juan@example.com", NOW, 0.0, LoyaltyLevel.BRONZE));
        repository.save(new Usuario("USR001", "Juan Updated", "juan2@example.com", NOW, 50.0, LoyaltyLevel.SILVER));

        Optional<Usuario> found = repository.findById("USR001");
        assertTrue(found.isPresent());
        assertEquals("Juan Updated", found.get().getName());
        assertEquals(50.0, found.get().getPoints());
    }
}
