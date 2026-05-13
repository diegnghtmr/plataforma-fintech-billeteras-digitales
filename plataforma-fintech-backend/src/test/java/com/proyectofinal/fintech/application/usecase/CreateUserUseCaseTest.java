package com.proyectofinal.fintech.application.usecase;

import com.proyectofinal.fintech.domain.model.LoyaltyLevel;
import com.proyectofinal.fintech.domain.model.Usuario;
import com.proyectofinal.fintech.domain.port.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateUserUseCaseTest {

    @Mock
    private UserRepository userRepository;

    private Clock fixedClock;
    private CreateUserUseCase useCase;

    private static final Instant FIXED_NOW = Instant.parse("2026-01-01T00:00:00Z");

    @BeforeEach
    void setUp() {
        fixedClock = Clock.fixed(FIXED_NOW, ZoneOffset.UTC);
        useCase = new CreateUserUseCase(userRepository, fixedClock);
    }

    @Test
    void execute_emptyRepo_assignsUSR001() {
        when(userRepository.findAll()).thenReturn(List.of());

        Usuario result = useCase.execute("Juan Pérez", "juan@example.com");

        assertEquals("USR001", result.getId());
        assertEquals("Juan Pérez", result.getName());
        assertEquals("juan@example.com", result.getEmail());
        assertEquals(FIXED_NOW, result.getRegisteredAt());
        assertEquals(0.0, result.getPoints());
        assertEquals(LoyaltyLevel.BRONZE, result.getLoyaltyLevel());

        verify(userRepository).save(any(Usuario.class));
    }

    @Test
    void execute_existingUsers_incrementsId() {
        Usuario existing = new Usuario("USR003", "Existente", "x@x.com", FIXED_NOW, 0.0, LoyaltyLevel.BRONZE);
        when(userRepository.findAll()).thenReturn(List.of(existing));

        Usuario result = useCase.execute("Nuevo", "nuevo@example.com");

        assertEquals("USR004", result.getId());
        verify(userRepository).save(any(Usuario.class));
    }

    @Test
    void execute_idFollowsUSRFormat() {
        when(userRepository.findAll()).thenReturn(List.of());

        Usuario result = useCase.execute("Test", "test@example.com");

        assertTrue(result.getId().matches("USR\\d{3,}"),
                "ID must match USR followed by at least 3 digits, got: " + result.getId());
    }

    @Test
    void execute_nonUSRIdsAreIgnored_fallsBackToUSR001() {
        Usuario existing = new Usuario("WAL001", "Other", "o@o.com", FIXED_NOW, 0.0, LoyaltyLevel.BRONZE);
        when(userRepository.findAll()).thenReturn(List.of(existing));

        Usuario result = useCase.execute("Juan", "juan@example.com");

        assertEquals("USR001", result.getId());
    }
}
