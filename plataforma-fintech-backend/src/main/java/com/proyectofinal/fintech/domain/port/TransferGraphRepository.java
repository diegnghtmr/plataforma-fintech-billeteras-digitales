package com.proyectofinal.fintech.domain.port;

import com.proyectofinal.fintech.domain.structures.GrafoTransferencias;
import com.proyectofinal.fintech.domain.structures.MiLista;

/**
 * Output port for the transfer graph.
 * ZERO Spring/Jakarta imports — pure domain interface.
 * REQ-F3.2: findCycles() returns MiLista<MiLista<String>>; no java.util.List in this interface.
 */
public interface TransferGraphRepository {

    void addNode(String userId);

    void addEdge(String source, String target, double amount);

    Iterable<GrafoTransferencias.Route> frequentRoutes(int minTransfers);

    /**
     * Returns all cycles detected in the transfer graph.
     * Each cycle is a MiLista of node ids normalized to start at the smallest id.
     * REQ-F3.2: returns MiLista<MiLista<String>> — no JDK List.
     */
    MiLista<MiLista<String>> findCycles();
}
