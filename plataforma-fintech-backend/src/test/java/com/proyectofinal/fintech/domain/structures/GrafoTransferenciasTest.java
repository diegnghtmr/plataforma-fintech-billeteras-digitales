package com.proyectofinal.fintech.domain.structures;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for GrafoTransferencias (directed weighted transfer graph).
 * TDD RED → GREEN order.
 */
class GrafoTransferenciasTest {

    private GrafoTransferencias grafo;

    @BeforeEach
    void setUp() {
        grafo = new GrafoTransferencias();
    }

    // ── addNode ───────────────────────────────────────────────────────────────

    @Test
    void addNodeIncreasesNodeCount() {
        grafo.addNode("alice");
        assertThat(grafo.nodeCount()).isEqualTo(1);
    }

    @Test
    void addNodeIsIdempotent() {
        grafo.addNode("alice");
        grafo.addNode("alice");
        assertThat(grafo.nodeCount()).isEqualTo(1);
    }

    @Test
    void addNullNodeThrowsIAE() {
        assertThatThrownBy(() -> grafo.addNode(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void addBlankNodeThrowsIAE() {
        assertThatThrownBy(() -> grafo.addNode("  "))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // ── addEdge ───────────────────────────────────────────────────────────────

    @Test
    void addEdgeAutoCreatesNodesAndAccumulates() {
        grafo.addEdge("alice", "bob", 50.0);
        grafo.addEdge("alice", "bob", 50.0);
        assertThat(grafo.nodeCount()).isEqualTo(2);

        List<GrafoTransferencias.Edge> edges = toList(grafo.outEdges("alice"));
        assertThat(edges).hasSize(1);
        GrafoTransferencias.Edge e = edges.get(0);
        assertThat(e.target()).isEqualTo("bob");
        assertThat(e.count()).isEqualTo(2);
        assertThat(e.totalAmount()).isEqualTo(100.0);
    }

    @Test
    void addEdgeThreeTimesAccumulates() {
        grafo.addEdge("alice", "bob", 50.0);
        grafo.addEdge("alice", "bob", 50.0);
        grafo.addEdge("alice", "bob", 50.0);
        List<GrafoTransferencias.Edge> edges = toList(grafo.outEdges("alice"));
        assertThat(edges).hasSize(1);
        assertThat(edges.get(0).count()).isEqualTo(3);
        assertThat(edges.get(0).totalAmount()).isEqualTo(150.0);
    }

    @Test
    void addEdgeNullSourceThrowsIAE() {
        assertThatThrownBy(() -> grafo.addEdge(null, "bob", 10.0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void addEdgeNullTargetThrowsIAE() {
        assertThatThrownBy(() -> grafo.addEdge("alice", null, 10.0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void addEdgeBlankSourceThrowsIAE() {
        assertThatThrownBy(() -> grafo.addEdge("", "bob", 10.0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void addEdgeNonPositiveAmountThrowsIAE() {
        assertThatThrownBy(() -> grafo.addEdge("alice", "bob", -1.0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> grafo.addEdge("alice", "bob", 0.0))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // ── outEdges ──────────────────────────────────────────────────────────────

    @Test
    void outEdgesDoesNotIncludeIncomingEdges() {
        grafo.addEdge("alice", "bob", 10.0);
        List<GrafoTransferencias.Edge> bobEdges = toList(grafo.outEdges("bob"));
        assertThat(bobEdges).isEmpty();
    }

    @Test
    void outEdgesForUnknownNodeReturnsEmpty() {
        List<GrafoTransferencias.Edge> edges = toList(grafo.outEdges("unknown"));
        assertThat(edges).isEmpty();
    }

    // ── edgeCount ────────────────────────────────────────────────────────────

    @Test
    void edgeCountReflectsUniqueEdges() {
        grafo.addEdge("alice", "bob", 10.0);
        grafo.addEdge("alice", "bob", 20.0); // same edge, accumulated
        grafo.addEdge("alice", "carol", 5.0); // different edge
        assertThat(grafo.edgeCount()).isEqualTo(2);
    }

    // ── frequentRoutes ────────────────────────────────────────────────────────

    @Test
    void frequentRoutesFiltersAboveThreshold() {
        grafo.addEdge("alice", "bob", 50.0);
        grafo.addEdge("alice", "bob", 50.0);
        grafo.addEdge("alice", "bob", 50.0); // count=3
        grafo.addEdge("carol", "dave", 10.0); // count=1

        List<GrafoTransferencias.Route> routes = toList(grafo.frequentRoutes(2));
        assertThat(routes).hasSize(1);
        GrafoTransferencias.Route r = routes.get(0);
        assertThat(r.source()).isEqualTo("alice");
        assertThat(r.target()).isEqualTo("bob");
        assertThat(r.count()).isEqualTo(3);
    }

    @Test
    void frequentRoutesWithHighThresholdReturnsEmpty() {
        grafo.addEdge("alice", "bob", 50.0);
        List<GrafoTransferencias.Route> routes = toList(grafo.frequentRoutes(5));
        assertThat(routes).isEmpty();
    }

    // ── nodes ─────────────────────────────────────────────────────────────────

    @Test
    void nodesReturnsAllKnownNodes() {
        grafo.addNode("alice");
        grafo.addEdge("alice", "bob", 10.0);
        List<String> nodes = toList(grafo.nodes());
        assertThat(nodes).containsExactlyInAnyOrder("alice", "bob");
    }

    // ── findCycles (T11-H01) ─────────────────────────────────────────────────

    @Test
    void findCycles_emptyGraph_returnsEmpty() {
        assertThat(grafo.findCycles()).isEmpty();
    }

    @Test
    void findCycles_linearChain_returnsEmpty() {
        grafo.addEdge("A", "B", 10.0);
        grafo.addEdge("B", "C", 10.0);
        assertThat(grafo.findCycles()).isEmpty();
    }

    @Test
    void findCycles_singleCycle_returnsNormalizedCycle() {
        grafo.addEdge("B", "A", 10.0);
        grafo.addEdge("A", "B", 10.0);
        List<List<String>> cycles = grafo.findCycles();
        assertThat(cycles).hasSize(1);
        // Smallest id first (A < B)
        assertThat(cycles.get(0).get(0)).isEqualTo("A");
        assertThat(cycles.get(0)).containsExactly("A", "B");
    }

    @Test
    void findCycles_selfLoop_returnsCycle() {
        grafo.addEdge("A", "A", 10.0);
        List<List<String>> cycles = grafo.findCycles();
        assertThat(cycles).hasSize(1);
        assertThat(cycles.get(0)).containsExactly("A");
    }

    @Test
    void findCycles_triangleCycle_returnsNormalized() {
        grafo.addEdge("C", "A", 10.0);
        grafo.addEdge("A", "B", 10.0);
        grafo.addEdge("B", "C", 10.0);
        List<List<String>> cycles = grafo.findCycles();
        assertThat(cycles).hasSize(1);
        assertThat(cycles.get(0).get(0)).isEqualTo("A");
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private static <T> List<T> toList(Iterable<T> it) {
        List<T> list = new ArrayList<>();
        it.forEach(list::add);
        return list;
    }
}
