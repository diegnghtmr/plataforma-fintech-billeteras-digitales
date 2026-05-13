package com.proyectofinal.fintech.application.usecase;

import com.proyectofinal.fintech.application.result.RouteMetric;
import com.proyectofinal.fintech.domain.port.TransferGraphRepository;
import com.proyectofinal.fintech.domain.structures.GrafoTransferencias;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * T09-B17 (RED) — GetFrequentRoutesUseCase tests.
 */
@ExtendWith(MockitoExtension.class)
class GetFrequentRoutesUseCaseTest {

    @Mock
    private TransferGraphRepository graphRepository;

    private GetFrequentRoutesUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new GetFrequentRoutesUseCase(graphRepository);
    }

    @Test
    void execute_minTransfers2_returnsOnlyMatchingRoutes() {
        when(graphRepository.frequentRoutes(2)).thenReturn(List.of(
                new GrafoTransferencias.Route("USR_A", "USR_B", 3, 900.0)
        ));

        List<RouteMetric> result = useCase.execute(2);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).sourceUserId()).isEqualTo("USR_A");
        assertThat(result.get(0).targetUserId()).isEqualTo("USR_B");
        assertThat(result.get(0).transferCount()).isEqualTo(3);
        assertThat(result.get(0).totalAmount()).isEqualTo(900.0);
    }

    @Test
    void execute_minTransfers1_returnsAllRoutes() {
        when(graphRepository.frequentRoutes(1)).thenReturn(List.of(
                new GrafoTransferencias.Route("A", "B", 1, 100.0),
                new GrafoTransferencias.Route("X", "Y", 2, 200.0)
        ));

        List<RouteMetric> result = useCase.execute(1);
        assertThat(result).hasSize(2);
    }

    @Test
    void execute_emptyGraph_returnsEmptyList() {
        when(graphRepository.frequentRoutes(1)).thenReturn(List.of());
        List<RouteMetric> result = useCase.execute(1);
        assertThat(result).isEmpty();
    }
}
