package com.proyectofinal.fintech.application.usecase;

import com.proyectofinal.fintech.application.result.MetricItem;
import com.proyectofinal.fintech.domain.model.Billetera;
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
 * T11-I01 (RED→GREEN) — GetTopWalletCategoriesUseCase tests.
 */
@ExtendWith(MockitoExtension.class)
class GetTopWalletCategoriesUseCaseTest {

    @Mock
    private WalletRepository walletRepository;

    private GetTopWalletCategoriesUseCase useCase;

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

    @BeforeEach
    void setUp() {
        useCase = new GetTopWalletCategoriesUseCase(walletRepository);
    }

    private Billetera makeWallet(String code, String type) {
        return new Billetera(code, code, type, "USR001", 0.0, true, NOW, 0);
    }

    @Test
    void execute_multipleCats_returnsDescByCount() {
        when(walletRepository.findAll()).thenReturn(List.of(
                makeWallet("W1", "SAVINGS"),
                makeWallet("W2", "SAVINGS"),
                makeWallet("W3", "SAVINGS"),
                makeWallet("W4", "CHECKING"),
                makeWallet("W5", "CHECKING"),
                makeWallet("W6", "INVESTMENT")
        ));

        List<MetricItem> result = useCase.execute(10);

        assertThat(result).hasSize(3);
        assertThat(result.get(0).id()).isEqualTo("SAVINGS");
        assertThat(result.get(0).value()).isEqualTo(3.0);
        assertThat(result.get(1).id()).isEqualTo("CHECKING");
        assertThat(result.get(1).value()).isEqualTo(2.0);
        assertThat(result.get(2).id()).isEqualTo("INVESTMENT");
        assertThat(result.get(2).value()).isEqualTo(1.0);
    }

    @Test
    void execute_limitApplied_returnsOnlyTop() {
        when(walletRepository.findAll()).thenReturn(List.of(
                makeWallet("W1", "SAVINGS"),
                makeWallet("W2", "SAVINGS"),
                makeWallet("W3", "CHECKING"),
                makeWallet("W4", "INVESTMENT")
        ));

        List<MetricItem> result = useCase.execute(2);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).id()).isEqualTo("SAVINGS");
    }

    @Test
    void execute_empty_returnsEmpty() {
        when(walletRepository.findAll()).thenReturn(List.of());

        List<MetricItem> result = useCase.execute(10);

        assertThat(result).isEmpty();
    }
}
