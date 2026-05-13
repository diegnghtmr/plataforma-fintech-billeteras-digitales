package com.proyectofinal.fintech.domain.port;

import com.proyectofinal.fintech.domain.structures.GrafoTransferencias;

import java.util.List;

/**
 * Output port for the transfer graph.
 * ZERO Spring/Jakarta imports — pure domain interface.
 */
public interface TransferGraphRepository {

    void addNode(String userId);

    void addEdge(String source, String target, double amount);

    Iterable<GrafoTransferencias.Route> frequentRoutes(int minTransfers);

    /**
     * Returns all cycles detected in the transfer graph.
     * Each cycle is a list of node ids normalized to start at the smallest id.
     */
    List<List<String>> findCycles();
}
