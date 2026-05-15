package com.proyectofinal.fintech.application.usecase;

import com.proyectofinal.fintech.domain.port.TransferGraphRepository;
import com.proyectofinal.fintech.domain.structures.MiLista;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * T11-H03 (RED→GREEN) — GetCyclesUseCase tests.
 * Updated for MiLista return type (REQ-F3.2).
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

    private static MiLista<String> cycle(String... nodes) {
        MiLista<String> list = new MiLista<>();
        for (String n : nodes) list.add(n);
        return list;
    }

    @Test
    void execute_noCycles_returnsEmpty() {
        when(transferGraphRepository.findCycles()).thenReturn(new MiLista<>());

        MiLista<MiLista<String>> result = useCase.execute();

        int count = 0;
        for (var ignored : result) count++;
        assertThat(count).isEqualTo(0);
    }

    @Test
    void execute_oneCycle_returnsCycle() {
        MiLista<MiLista<String>> cycles = new MiLista<>();
        cycles.add(cycle("A", "B"));
        when(transferGraphRepository.findCycles()).thenReturn(cycles);

        MiLista<MiLista<String>> result = useCase.execute();

        int count = 0;
        for (MiLista<String> c : result) {
            count++;
            java.util.List<String> items = new java.util.ArrayList<>();
            for (String n : c) items.add(n);
            assertThat(items).containsExactly("A", "B");
        }
        assertThat(count).isEqualTo(1);
    }

    @Test
    void execute_multipleCycles_returnsAll() {
        MiLista<MiLista<String>> cycles = new MiLista<>();
        cycles.add(cycle("A", "B"));
        cycles.add(cycle("C", "D", "E"));
        when(transferGraphRepository.findCycles()).thenReturn(cycles);

        MiLista<MiLista<String>> result = useCase.execute();

        int count = 0;
        for (var ignored : result) count++;
        assertThat(count).isEqualTo(2);
    }
}
