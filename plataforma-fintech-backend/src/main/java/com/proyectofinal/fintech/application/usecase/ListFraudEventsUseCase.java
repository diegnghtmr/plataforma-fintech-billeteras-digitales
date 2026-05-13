package com.proyectofinal.fintech.application.usecase;

import com.proyectofinal.fintech.domain.model.FraudEvent;
import com.proyectofinal.fintech.domain.model.FraudSeverity;
import com.proyectofinal.fintech.domain.port.FraudEventRepository;

/**
 * Lists fraud events with optional filtering by userId and/or severity.
 * Plain class — ZERO Spring/Jakarta imports.
 */
public class ListFraudEventsUseCase {

    private final FraudEventRepository fraudEventRepository;

    public ListFraudEventsUseCase(FraudEventRepository fraudEventRepository) {
        this.fraudEventRepository = fraudEventRepository;
    }

    /**
     * @param userId   optional user ID filter (null = no filter)
     * @param severity optional severity filter (null = no filter)
     * @return matching fraud events
     */
    public Iterable<FraudEvent> execute(String userId, FraudSeverity severity) {
        if (userId != null && severity != null) {
            return fraudEventRepository.findByUserIdAndSeverity(userId, severity);
        } else if (userId != null) {
            return fraudEventRepository.findByUserId(userId);
        } else if (severity != null) {
            return fraudEventRepository.findBySeverity(severity);
        } else {
            return fraudEventRepository.findAll();
        }
    }
}
