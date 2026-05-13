package com.proyectofinal.fintech.application.usecase;

import com.proyectofinal.fintech.domain.exception.DuplicatedResourceException;
import com.proyectofinal.fintech.domain.exception.ErrorCode;
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
import java.util.Optional;

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
    void execute_happyPath_savesAndReturnsUser() {
        when(userRepository.existsById("USR001")).thenReturn(false);

        Usuario result = useCase.execute("USR001", "Juan Pérez", "juan@example.com");

        assertEquals("USR001", result.getId());
        assertEquals("Juan Pérez", result.getName());
        assertEquals("juan@example.com", result.getEmail());
        assertEquals(FIXED_NOW, result.getRegisteredAt());
        assertEquals(0.0, result.getPoints());
        assertEquals(LoyaltyLevel.BRONZE, result.getLoyaltyLevel());

        verify(userRepository).save(any(Usuario.class));
    }

    @Test
    void execute_duplicateId_throwsDuplicatedResourceException() {
        when(userRepository.existsById("USR001")).thenReturn(true);

        DuplicatedResourceException ex = assertThrows(
                DuplicatedResourceException.class,
                () -> useCase.execute("USR001", "Juan", "juan@example.com")
        );

        assertEquals(ErrorCode.DUPLICATED_RESOURCE, ex.code());
        verify(userRepository, never()).save(any());
    }
}
