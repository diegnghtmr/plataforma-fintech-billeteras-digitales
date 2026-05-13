package com.proyectofinal.fintech.application.usecase;

import com.proyectofinal.fintech.application.result.MetricItem;
import com.proyectofinal.fintech.domain.model.*;
import com.proyectofinal.fintech.domain.port.UserRepository;
import com.proyectofinal.fintech.domain.port.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * T09-B15 (RED) — GetTopWalletsUseCase tests.
 */
@ExtendWith(MockitoExtension.class)
class GetTopWalletsUseCaseTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private WalletRepository walletRepository;

    private GetTopWalletsUseCase useCase;
    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

    @BeforeEach
    void setUp() {
        useCase = new GetTopWalletsUseCase(userRepository, walletRepository);
    }

    @Test
    void execute_walletWithHighestTransactionCountIsFirst() {
        Usuario userA = new Usuario("USR_A", "Ana", "ana@test.com", NOW, 0.0, LoyaltyLevel.BRONZE);
        Billetera w1 = new Billetera("W1", "Savings", "SAVINGS", "USR_A", 1000.0, true, NOW, 10);
        Billetera w2 = new Billetera("W2", "Daily", "DAILY", "USR_A", 500.0, true, NOW, 3);

        when(userRepository.findAll()).thenReturn(List.of(userA));
        when(walletRepository.findByOwnerId("USR_A")).thenReturn(List.of(w1, w2));

        List<MetricItem> result = useCase.execute(10);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).id()).isEqualTo("USR_A/W1");
        assertThat(result.get(0).value()).isEqualTo(10.0);
        assertThat(result.get(1).id()).isEqualTo("USR_A/W2");
    }

    @Test
    void execute_limitCapsResultSize() {
        Usuario userA = new Usuario("USR_A", "Ana", "ana@test.com", NOW, 0.0, LoyaltyLevel.BRONZE);
        Billetera w1 = new Billetera("W1", "S1", "S1", "USR_A", 0.0, true, NOW, 5);
        Billetera w2 = new Billetera("W2", "S2", "S2", "USR_A", 0.0, true, NOW, 4);
        Billetera w3 = new Billetera("W3", "S3", "S3", "USR_A", 0.0, true, NOW, 3);
        Billetera w4 = new Billetera("W4", "S4", "S4", "USR_A", 0.0, true, NOW, 2);
        Billetera w5 = new Billetera("W5", "S5", "S5", "USR_A", 0.0, true, NOW, 1);
        Billetera w6 = new Billetera("W6", "S6", "S6", "USR_A", 0.0, true, NOW, 0);

        when(userRepository.findAll()).thenReturn(List.of(userA));
        when(walletRepository.findByOwnerId("USR_A")).thenReturn(List.of(w1, w2, w3, w4, w5, w6));

        List<MetricItem> result = useCase.execute(3);
        assertThat(result).hasSize(3);
    }

    @Test
    void execute_multipleUsers_acrossAllWallets() {
        Usuario userA = new Usuario("USR_A", "Ana", "ana@test.com", NOW, 0.0, LoyaltyLevel.BRONZE);
        Usuario userB = new Usuario("USR_B", "Bob", "bob@test.com", NOW, 0.0, LoyaltyLevel.BRONZE);
        Billetera wA = new Billetera("WA", "Main", "MAIN", "USR_A", 0.0, true, NOW, 7);
        Billetera wB = new Billetera("WB", "Main", "MAIN", "USR_B", 0.0, true, NOW, 12);

        when(userRepository.findAll()).thenReturn(List.of(userA, userB));
        when(walletRepository.findByOwnerId("USR_A")).thenReturn(List.of(wA));
        when(walletRepository.findByOwnerId("USR_B")).thenReturn(List.of(wB));

        List<MetricItem> result = useCase.execute(10);

        assertThat(result.get(0).value()).isEqualTo(12.0);
        assertThat(result.get(1).value()).isEqualTo(7.0);
    }
}
