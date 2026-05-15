package com.proyectofinal.fintech.application.usecase;

import com.proyectofinal.fintech.application.result.BenefitView;
import com.proyectofinal.fintech.domain.model.Beneficio;
import com.proyectofinal.fintech.domain.port.BeneficioRepository;
import com.proyectofinal.fintech.domain.structures.MiLista;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * F-37 (RED) — ListBenefitsUseCase test.
 * REQ-F1.1: returns all active benefits.
 */
@ExtendWith(MockitoExtension.class)
class ListBenefitsUseCaseTest {

    @Mock
    private BeneficioRepository beneficioRepository;

    private ListBenefitsUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ListBenefitsUseCase(beneficioRepository);
    }

    @Test
    void execute_returnsAllActiveBenefits() {
        MiLista<Beneficio> active = new MiLista<>();
        active.add(new Beneficio("BEN-001", "Cashback 1%", "desc", 100, true));
        active.add(new Beneficio("BEN-002", "Cine", "desc", 200, true));
        when(beneficioRepository.findAllActive()).thenReturn(active);

        List<BenefitView> result = useCase.execute();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).id()).isEqualTo("BEN-001");
        assertThat(result.get(1).id()).isEqualTo("BEN-002");
    }

    @Test
    void execute_noActiveBenefits_returnsEmptyList() {
        when(beneficioRepository.findAllActive()).thenReturn(new MiLista<>());

        List<BenefitView> result = useCase.execute();

        assertThat(result).isEmpty();
    }
}
