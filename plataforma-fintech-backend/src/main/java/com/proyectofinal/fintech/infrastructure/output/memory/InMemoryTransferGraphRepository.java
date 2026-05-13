package com.proyectofinal.fintech.infrastructure.output.memory;

import com.proyectofinal.fintech.domain.port.TransferGraphRepository;
import com.proyectofinal.fintech.domain.structures.GrafoTransferencias;

import java.util.List;

/**
 * In-memory implementation of TransferGraphRepository.
 * Wraps a singleton GrafoTransferencias — state persists across requests.
 * ADR-9.4: Singleton bean so graph state is preserved.
 */
public class InMemoryTransferGraphRepository implements TransferGraphRepository {

    private final GrafoTransferencias graph = new GrafoTransferencias();

    @Override
    public void addNode(String userId) {
        graph.addNode(userId);
    }

    @Override
    public void addEdge(String source, String target, double amount) {
        graph.addEdge(source, target, amount);
    }

    @Override
    public Iterable<GrafoTransferencias.Route> frequentRoutes(int minTransfers) {
        return graph.frequentRoutes(minTransfers);
    }

    @Override
    public List<List<String>> findCycles() {
        return graph.findCycles();
    }
}
