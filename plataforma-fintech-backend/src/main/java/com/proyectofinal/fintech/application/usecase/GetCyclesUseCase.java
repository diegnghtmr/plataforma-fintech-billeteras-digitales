package com.proyectofinal.fintech.application.usecase;

import com.proyectofinal.fintech.domain.port.TransferGraphRepository;
import com.proyectofinal.fintech.domain.structures.MiLista;

/**
 * Use case: returns all transfer cycles detected in the graph.
 * Plain class — ZERO Spring/Jakarta imports.
 * REQ-F3.4: iterates MiLista directly (Iterable); no JDK List cast.
 */
public class GetCyclesUseCase {

    private final TransferGraphRepository transferGraphRepository;

    public GetCyclesUseCase(TransferGraphRepository transferGraphRepository) {
        this.transferGraphRepository = transferGraphRepository;
    }

    /**
     * Returns all cycles in the transfer graph.
     * Each cycle is a MiLista of user ids, normalized to start at the smallest id.
     *
     * @return MiLista of cycles (may be empty)
     */
    public MiLista<MiLista<String>> execute() {
        return transferGraphRepository.findCycles();
    }
}
