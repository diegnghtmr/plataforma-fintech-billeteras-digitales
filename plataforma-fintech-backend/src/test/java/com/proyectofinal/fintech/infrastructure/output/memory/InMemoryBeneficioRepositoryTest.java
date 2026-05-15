package com.proyectofinal.fintech.infrastructure.output.memory;

import com.proyectofinal.fintech.domain.model.Beneficio;
import com.proyectofinal.fintech.domain.structures.MiLista;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * F-35 (RED) — InMemoryBeneficioRepository tests.
 */
class InMemoryBeneficioRepositoryTest {

    private InMemoryBeneficioRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryBeneficioRepository();
    }

    @Test
    void save_andFindById_returnsSavedBeneficio() {
        Beneficio b = new Beneficio("BEN-001", "Cashback", "desc", 100, true);
        repository.save(b);

        Optional<Beneficio> found = repository.findById("BEN-001");
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Cashback");
    }

    @Test
    void findAllActive_returnsOnlyActiveBenefits() {
        repository.save(new Beneficio("BEN-001", "Active1", "d", 100, true));
        repository.save(new Beneficio("BEN-002", "Inactive", "d", 200, false));
        repository.save(new Beneficio("BEN-003", "Active2", "d", 300, true));

        MiLista<Beneficio> active = repository.findAllActive();
        int count = 0;
        for (Beneficio b : active) count++;
        assertThat(count).isEqualTo(2);
    }

    @Test
    void findById_notFound_returnsEmpty() {
        assertThat(repository.findById("GHOST")).isEmpty();
    }
}
