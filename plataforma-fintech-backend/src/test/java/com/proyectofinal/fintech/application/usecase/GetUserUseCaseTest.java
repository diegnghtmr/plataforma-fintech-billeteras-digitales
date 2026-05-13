package com.proyectofinal.fintech.application.usecase;

import com.proyectofinal.fintech.application.result.UserView;
import com.proyectofinal.fintech.domain.exception.ErrorCode;
import com.proyectofinal.fintech.domain.exception.NotFoundException;
import com.proyectofinal.fintech.domain.model.LoyaltyLevel;
import com.proyectofinal.fintech.domain.model.Usuario;
import com.proyectofinal.fintech.domain.port.UserRepository;
import com.proyectofinal.fintech.domain.port.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetUserUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private WalletRepository walletRepository;

    private GetUserUseCase useCase;

    private static final Instant FIXED_NOW = Instant.parse("2026-01-01T00:00:00Z");

    @BeforeEach
    void setUp() {
        useCase = new GetUserUseCase(userRepository, walletRepository);
    }

    @Test
    void execute_happyPath_returnsUserViewWithAggregates() {
        Usuario usuario = new Usuario("USR001", "Juan", "juan@example.com", FIXED_NOW, 0.0, LoyaltyLevel.BRONZE);
        when(userRepository.findById("USR001")).thenReturn(Optional.of(usuario));
        when(walletRepository.countByOwnerId("USR001")).thenReturn(2);
        when(walletRepository.sumBalanceByOwnerId("USR001")).thenReturn(1500.0);

        UserView view = useCase.execute("USR001");

        assertEquals("USR001", view.id());
        assertEquals("Juan", view.name());
        assertEquals("juan@example.com", view.email());
        assertEquals(FIXED_NOW, view.registeredAt());
        assertEquals(0.0, view.points());
        assertEquals(LoyaltyLevel.BRONZE, view.loyaltyLevel());
        assertEquals(2, view.walletCount());
        assertEquals(1500.0, view.totalBalance(), 0.001);
    }

    @Test
    void execute_unknownUser_throwsNotFoundException() {
        when(userRepository.findById("UNKNOWN")).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(
                NotFoundException.class,
                () -> useCase.execute("UNKNOWN")
        );

        assertEquals(ErrorCode.USER_NOT_FOUND, ex.code());
        verify(walletRepository, never()).countByOwnerId(any());
    }
}
