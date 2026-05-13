package com.proyectofinal.fintech.infrastructure.output.memory;

import com.proyectofinal.fintech.domain.model.FraudEvent;
import com.proyectofinal.fintech.domain.model.FraudSeverity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * T09-B07 (RED) — InMemoryFraudEventRepository tests.
 */
class InMemoryFraudEventRepositoryTest {

    private InMemoryFraudEventRepository repo;
    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

    private FraudEvent make(String id, String userId, FraudSeverity severity) {
        return new FraudEvent(id, userId, "TX-" + id, "LARGE_TRANSACTION", severity, "desc", NOW);
    }

    @BeforeEach
    void setUp() {
        repo = new InMemoryFraudEventRepository();
    }

    @Test
    void saveAndFindAll_returnsAllEvents() {
        repo.save(make("1", "USR_A", FraudSeverity.HIGH));
        repo.save(make("2", "USR_B", FraudSeverity.LOW));

        List<FraudEvent> all = toList(repo.findAll());
        assertThat(all).hasSize(2);
    }

    @Test
    void findByUserId_returnsOnlyMatchingUser() {
        repo.save(make("1", "USR_A", FraudSeverity.HIGH));
        repo.save(make("2", "USR_A", FraudSeverity.LOW));
        repo.save(make("3", "USR_B", FraudSeverity.HIGH));

        List<FraudEvent> result = toList(repo.findByUserId("USR_A"));
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(e -> e.getUserId().equals("USR_A"));
    }

    @Test
    void findBySeverity_returnsOnlyMatchingSeverity() {
        repo.save(make("1", "USR_A", FraudSeverity.HIGH));
        repo.save(make("2", "USR_B", FraudSeverity.LOW));
        repo.save(make("3", "USR_A", FraudSeverity.HIGH));

        List<FraudEvent> result = toList(repo.findBySeverity(FraudSeverity.HIGH));
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(e -> e.getSeverity() == FraudSeverity.HIGH);
    }

    @Test
    void findByUserIdAndSeverity_filtersBothCriteria() {
        repo.save(make("1", "USR_A", FraudSeverity.HIGH));
        repo.save(make("2", "USR_A", FraudSeverity.LOW));
        repo.save(make("3", "USR_B", FraudSeverity.HIGH));

        List<FraudEvent> result = toList(repo.findByUserIdAndSeverity("USR_A", FraudSeverity.HIGH));
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo("1");
    }

    @Test
    void count_reflectsTotalSavedEvents() {
        repo.save(make("1", "USR_A", FraudSeverity.HIGH));
        repo.save(make("2", "USR_B", FraudSeverity.LOW));
        repo.save(make("3", "USR_A", FraudSeverity.MEDIUM));

        assertThat(repo.count()).isEqualTo(3);
    }

    private <T> List<T> toList(Iterable<T> it) {
        List<T> list = new ArrayList<>();
        it.forEach(list::add);
        return list;
    }
}
