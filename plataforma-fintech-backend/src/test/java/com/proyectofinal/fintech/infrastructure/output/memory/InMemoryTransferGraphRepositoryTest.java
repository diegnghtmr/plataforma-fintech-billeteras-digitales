package com.proyectofinal.fintech.infrastructure.output.memory;

import com.proyectofinal.fintech.domain.structures.GrafoTransferencias;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * T09-B09 (RED) — InMemoryTransferGraphRepository tests.
 */
class InMemoryTransferGraphRepositoryTest {

    private InMemoryTransferGraphRepository repo;

    @BeforeEach
    void setUp() {
        repo = new InMemoryTransferGraphRepository();
    }

    @Test
    void addEdge_accumulatesCountAndAmount() {
        repo.addEdge("A", "B", 100.0);
        repo.addEdge("A", "B", 200.0);

        List<GrafoTransferencias.Route> routes = toList(repo.frequentRoutes(1));
        assertThat(routes).hasSize(1);
        GrafoTransferencias.Route route = routes.get(0);
        assertThat(route.source()).isEqualTo("A");
        assertThat(route.target()).isEqualTo("B");
        assertThat(route.count()).isEqualTo(2);
        assertThat(route.totalAmount()).isEqualTo(300.0);
    }

    @Test
    void frequentRoutes_filtersLessThanMinTransfers() {
        repo.addEdge("A", "B", 100.0);
        repo.addEdge("A", "B", 100.0);
        repo.addEdge("A", "B", 100.0);
        repo.addEdge("A", "C", 50.0);

        List<GrafoTransferencias.Route> routes = toList(repo.frequentRoutes(2));
        assertThat(routes).hasSize(1);
        assertThat(routes.get(0).target()).isEqualTo("B");
    }

    @Test
    void frequentRoutes_minTransfers1_returnsAll() {
        repo.addEdge("A", "B", 100.0);
        repo.addEdge("X", "Y", 50.0);

        List<GrafoTransferencias.Route> routes = toList(repo.frequentRoutes(1));
        assertThat(routes).hasSize(2);
    }

    private <T> List<T> toList(Iterable<T> it) {
        List<T> list = new ArrayList<>();
        it.forEach(list::add);
        return list;
    }

    // C-9: findCycles returns MiLista<MiLista<String>>
    @Test
    void findCycles_returnsMiLista() {
        // Build cycle A→B→C→A
        repo.addNode("A");
        repo.addNode("B");
        repo.addNode("C");
        repo.addEdge("A", "B", 100.0);
        repo.addEdge("B", "C", 100.0);
        repo.addEdge("C", "A", 100.0);

        com.proyectofinal.fintech.domain.structures.MiLista<com.proyectofinal.fintech.domain.structures.MiLista<String>> cycles =
                repo.findCycles();

        assertThat(cycles).isNotNull();
        int count = 0;
        for (var cycle : cycles) { count++; }
        assertThat(count).isGreaterThan(0);
    }

    @Test
    void findCycles_noCycles_returnsEmptyMiLista() {
        repo.addNode("A");
        repo.addNode("B");
        repo.addEdge("A", "B", 100.0);

        com.proyectofinal.fintech.domain.structures.MiLista<com.proyectofinal.fintech.domain.structures.MiLista<String>> cycles =
                repo.findCycles();

        assertThat(cycles).isNotNull();
        int count = 0;
        for (var ignored : cycles) { count++; }
        assertThat(count).isEqualTo(0);
    }
}
