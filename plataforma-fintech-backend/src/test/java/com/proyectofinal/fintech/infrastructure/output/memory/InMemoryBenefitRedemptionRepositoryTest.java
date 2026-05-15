package com.proyectofinal.fintech.infrastructure.output.memory;

import com.proyectofinal.fintech.domain.model.BenefitRedemption;
import com.proyectofinal.fintech.domain.structures.MiLista;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * F-35 (RED) — InMemoryBenefitRedemptionRepository tests.
 */
class InMemoryBenefitRedemptionRepositoryTest {

    private InMemoryBenefitRedemptionRepository repository;
    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

    @BeforeEach
    void setUp() {
        repository = new InMemoryBenefitRedemptionRepository();
    }

    @Test
    void save_andFindByUserId_returnsRedemptions() {
        BenefitRedemption r = new BenefitRedemption("RED-001", "USR001", "BEN-001", 100, NOW);
        repository.save(r);

        MiLista<BenefitRedemption> found = repository.findByUserId("USR001");
        int count = 0;
        for (BenefitRedemption br : found) count++;
        assertThat(count).isEqualTo(1);
    }

    @Test
    void findByUserId_differentUser_returnsEmpty() {
        BenefitRedemption r = new BenefitRedemption("RED-001", "USR001", "BEN-001", 100, NOW);
        repository.save(r);

        MiLista<BenefitRedemption> found = repository.findByUserId("USR002");
        int count = 0;
        for (BenefitRedemption br : found) count++;
        assertThat(count).isEqualTo(0);
    }
}
