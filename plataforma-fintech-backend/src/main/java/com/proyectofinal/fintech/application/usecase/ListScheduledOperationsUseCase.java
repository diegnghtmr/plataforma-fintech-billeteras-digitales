package com.proyectofinal.fintech.application.usecase;

import com.proyectofinal.fintech.domain.model.OperacionProgramada;
import com.proyectofinal.fintech.domain.port.ScheduledOperationRepository;

/**
 * Use case: list all scheduled operations.
 * Plain class — ZERO Spring/Jakarta imports.
 */
public class ListScheduledOperationsUseCase {

    private final ScheduledOperationRepository operationRepository;

    public ListScheduledOperationsUseCase(ScheduledOperationRepository operationRepository) {
        this.operationRepository = operationRepository;
    }

    /**
     * Returns all scheduled operations without filtering.
     */
    public Iterable<OperacionProgramada> execute() {
        return operationRepository.findAll();
    }
}
