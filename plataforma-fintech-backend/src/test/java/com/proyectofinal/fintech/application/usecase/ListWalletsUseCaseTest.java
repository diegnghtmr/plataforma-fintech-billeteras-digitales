package com.proyectofinal.fintech.application.usecase;

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

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ListWalletsUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private WalletRepository walletRepository;

    private ListWalletsUseCase useCase;

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

    @BeforeEach
    void setUp() {
        useCase = new ListWalletsUseCase(userRepository, walletRepository);
    }

    @Test
    void execute_happyPath_returnsWallets() {
        when(userRepository.existsById("USR001")).thenReturn(true);
        List<Billetera> wallets = List.of(
                new Billetera("W001", "Ahorros", "SAVINGS", "USR001", 500.0, true, NOW, 0),
                new Billetera("W002", "Gastos", "DAILY", "USR001", 300.0, true, NOW, 0)
        );
        when(walletRepository.findByOwnerId("USR001")).thenReturn(wallets);

        Iterable<Billetera> result = useCase.execute("USR001");

        int count = 0;
        for (Billetera b : result) {
            count++;
        }
        assertEquals(2, count);
    }

    @Test
    void execute_userNotFound_throwsNotFoundException() {
        when(userRepository.existsById("UNKNOWN")).thenReturn(false);

        NotFoundException ex = assertThrows(
                NotFoundException.class,
                () -> useCase.execute("UNKNOWN")
        );

        assertEquals(ErrorCode.USER_NOT_FOUND, ex.code());
        verify(walletRepository, never()).findByOwnerId(any());
    }
}
