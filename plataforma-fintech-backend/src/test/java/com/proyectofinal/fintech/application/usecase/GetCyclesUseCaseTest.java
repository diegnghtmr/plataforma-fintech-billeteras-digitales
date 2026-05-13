package com.proyectofinal.fintech.application.usecase;

import com.proyectofinal.fintech.domain.port.TransferGraphRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * T11-H03 (RED→GREEN) — GetCyclesUseCase tests.
 */
@ExtendWith(MockitoExtension.class)
class GetCyclesUseCaseTest {

    @Mock
    private TransferGraphRepository transferGraphRepository;

    private GetCyclesUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new GetCyclesUseCase(transferGraphRepository);
    }

    @Test
    void execute_noCycles_returnsEmpty() {
        when(transferGraphRepository.findCycles()).thenReturn(List.of());

        List<List<String>> result = useCase.execute();

        assertThat(result).isEmpty();
    }

    @Test
    void execute_oneCycle_returnsCycle() {
        when(transferGraphRepository.findCycles()).thenReturn(List.of(List.of("A", "B")));

        List<List<String>> result = useCase.execute();

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).containsExactly("A", "B");
    }

    @Test
    void execute_multipleCycles_returnsAll() {
        when(transferGraphRepository.findCycles()).thenReturn(List.of(
                List.of("A", "B"),
                List.of("C", "D", "E")
        ));

        List<List<String>> result = useCase.execute();

        assertThat(result).hasSize(2);
    }
}
