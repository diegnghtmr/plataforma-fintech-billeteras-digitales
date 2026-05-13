package com.proyectofinal.fintech.application.usecase;

import com.proyectofinal.fintech.domain.port.TransferGraphRepository;

import java.util.List;

/**
 * Use case: returns all transfer cycles detected in the graph.
 * Plain class — ZERO Spring/Jakarta imports.
 */
public class GetCyclesUseCase {

    private final TransferGraphRepository transferGraphRepository;

    public GetCyclesUseCase(TransferGraphRepository transferGraphRepository) {
        this.transferGraphRepository = transferGraphRepository;
    }

    /**
     * Returns all cycles in the transfer graph.
     * Each cycle is a list of user ids, normalized to start at the smallest id.
     *
     * @return list of cycles (may be empty)
     */
    public List<List<String>> execute() {
        return transferGraphRepository.findCycles();
    }
}
