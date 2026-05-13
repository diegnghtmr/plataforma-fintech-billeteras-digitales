package com.proyectofinal.fintech.application.usecase;

import com.proyectofinal.fintech.application.result.UserView;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ListUsersUseCaseTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private WalletRepository walletRepository;

    private ListUsersUseCase useCase;

    private static final Instant FIXED_NOW = Instant.parse("2026-01-01T00:00:00Z");

    @BeforeEach
    void setUp() {
        useCase = new ListUsersUseCase(userRepository, walletRepository);
    }

    @Test
    void execute_happyPath_returnsAllUsersWithWalletAggregates() {
        Usuario user1 = new Usuario("USR001", "Juan Pérez", "juan@example.com", FIXED_NOW, 100.0, LoyaltyLevel.SILVER);
        Usuario user2 = new Usuario("USR002", "Ana López", "ana@example.com", FIXED_NOW, 0.0, LoyaltyLevel.BRONZE);

        when(userRepository.findAll()).thenReturn(List.of(user1, user2));
        when(walletRepository.countByOwnerId("USR001")).thenReturn(2);
        when(walletRepository.sumBalanceByOwnerId("USR001")).thenReturn(1500.0);
        when(walletRepository.countByOwnerId("USR002")).thenReturn(0);
        when(walletRepository.sumBalanceByOwnerId("USR002")).thenReturn(0.0);

        List<UserView> result = useCase.execute();

        assertEquals(2, result.size());

        UserView view1 = result.stream().filter(v -> v.id().equals("USR001")).findFirst().orElseThrow();
        assertEquals("Juan Pérez", view1.name());
        assertEquals("juan@example.com", view1.email());
        assertEquals(2, view1.walletCount());
        assertEquals(1500.0, view1.totalBalance());
        assertEquals(LoyaltyLevel.SILVER, view1.loyaltyLevel());

        UserView view2 = result.stream().filter(v -> v.id().equals("USR002")).findFirst().orElseThrow();
        assertEquals(0, view2.walletCount());
        assertEquals(0.0, view2.totalBalance());
    }

    @Test
    void execute_emptyRepository_returnsEmptyList() {
        when(userRepository.findAll()).thenReturn(List.of());

        List<UserView> result = useCase.execute();

        assertTrue(result.isEmpty());
        verify(walletRepository, never()).countByOwnerId(any());
        verify(walletRepository, never()).sumBalanceByOwnerId(any());
    }
}
