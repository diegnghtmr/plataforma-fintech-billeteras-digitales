package com.proyectofinal.fintech.application.usecase;

import com.proyectofinal.fintech.domain.exception.ErrorCode;
import com.proyectofinal.fintech.domain.exception.NotFoundException;
import com.proyectofinal.fintech.domain.model.Billetera;
import com.proyectofinal.fintech.domain.port.UserRepository;
import com.proyectofinal.fintech.domain.port.WalletRepository;
import com.proyectofinal.fintech.domain.structures.MiLista;
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
    void execute_emptyRepo_generatesWAL001() {
        when(userRepository.existsById("USR001")).thenReturn(true);
        when(walletRepository.findAll()).thenReturn(new MiLista<>());

        Billetera result = useCase.execute("USR001", "Ahorros", "SAVINGS");

        assertEquals("WAL001", result.getCode());
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
    void execute_withFiveExistingWallets_generatesWAL006() {
        when(userRepository.existsById("USR001")).thenReturn(true);

        MiLista<Billetera> existing = new MiLista<>();
        existing.add(wallet("WAL001")); existing.add(wallet("WAL002")); existing.add(wallet("WAL003"));
        existing.add(wallet("WAL004")); existing.add(wallet("WAL005"));
        when(walletRepository.findAll()).thenReturn(existing);

        Billetera result = useCase.execute("USR001", "Inversión", "INVESTMENT");

        assertEquals("WAL006", result.getCode());
        verify(walletRepository).save(any(Billetera.class));
    }

    @Test
    void execute_ignoresNonWALCodes_generatesWAL001() {
        when(userRepository.existsById("USR001")).thenReturn(true);

        MiLista<Billetera> existing = new MiLista<>();
        existing.add(wallet("W001")); existing.add(wallet("OTHER-123"));
        when(walletRepository.findAll()).thenReturn(existing);

        Billetera result = useCase.execute("USR001", "Ahorros", "SAVINGS");

        assertEquals("WAL001", result.getCode());
    }

    @Test
    void execute_userNotFound_throwsNotFoundException() {
        when(userRepository.existsById("UNKNOWN")).thenReturn(false);

        NotFoundException ex = assertThrows(
                NotFoundException.class,
                () -> useCase.execute("UNKNOWN", "Ahorros", "SAVINGS")
        );

        assertEquals(ErrorCode.USER_NOT_FOUND, ex.code());
        verify(walletRepository, never()).save(any());
    }

    // ---------------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------------

    private Billetera wallet(String code) {
        return new Billetera(code, "name", "SAVINGS", "USR001", 0.0, true, FIXED_NOW, 0);
    }
}
