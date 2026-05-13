package com.proyectofinal.fintech.application.usecase;

import com.proyectofinal.fintech.domain.exception.DuplicatedResourceException;
import com.proyectofinal.fintech.domain.exception.ErrorCode;
import com.proyectofinal.fintech.domain.exception.NotFoundException;
import com.proyectofinal.fintech.domain.model.Billetera;
import com.proyectofinal.fintech.domain.port.UserRepository;
import com.proyectofinal.fintech.domain.port.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateWalletUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private WalletRepository walletRepository;

    private Clock fixedClock;
    private CreateWalletUseCase useCase;

    private static final Instant FIXED_NOW = Instant.parse("2026-01-01T00:00:00Z");

    @BeforeEach
    void setUp() {
        fixedClock = Clock.fixed(FIXED_NOW, ZoneOffset.UTC);
        useCase = new CreateWalletUseCase(userRepository, walletRepository, fixedClock);
    }

    @Test
    void execute_happyPath_savesAndReturnsBilletera() {
        when(userRepository.existsById("USR001")).thenReturn(true);
        when(walletRepository.existsByOwnerIdAndCode("USR001", "W001")).thenReturn(false);

        Billetera result = useCase.execute("USR001", "W001", "Ahorros", "SAVINGS");

        assertEquals("W001", result.getCode());
        assertEquals("Ahorros", result.getName());
        assertEquals("SAVINGS", result.getType());
        assertEquals("USR001", result.getOwnerId());
        assertEquals(0.0, result.getBalance());
        assertTrue(result.isActive());
        assertEquals(FIXED_NOW, result.getCreatedAt());
        assertEquals(0, result.getTransactionCount());

        verify(walletRepository).save(any(Billetera.class));
    }

    @Test
    void execute_userNotFound_throwsNotFoundException() {
        when(userRepository.existsById("UNKNOWN")).thenReturn(false);

        NotFoundException ex = assertThrows(
                NotFoundException.class,
                () -> useCase.execute("UNKNOWN", "W001", "Ahorros", "SAVINGS")
        );

        assertEquals(ErrorCode.USER_NOT_FOUND, ex.code());
        verify(walletRepository, never()).save(any());
    }

    @Test
    void execute_duplicateCode_throwsDuplicatedResourceException() {
        when(userRepository.existsById("USR001")).thenReturn(true);
        when(walletRepository.existsByOwnerIdAndCode("USR001", "W001")).thenReturn(true);

        DuplicatedResourceException ex = assertThrows(
                DuplicatedResourceException.class,
                () -> useCase.execute("USR001", "W001", "Dup", "SAVINGS")
        );

        assertEquals(ErrorCode.DUPLICATED_RESOURCE, ex.code());
        verify(walletRepository, never()).save(any());
    }
}
