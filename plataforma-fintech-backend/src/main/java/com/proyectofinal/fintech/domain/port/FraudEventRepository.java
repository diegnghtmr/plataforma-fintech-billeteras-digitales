package com.proyectofinal.fintech.domain.port;

import com.proyectofinal.fintech.domain.model.FraudEvent;
import com.proyectofinal.fintech.domain.model.FraudSeverity;

/**
 * Output port for fraud event persistence.
 * ZERO Spring/Jakarta imports — pure domain interface.
 */
public interface FraudEventRepository {

    void save(FraudEvent event);

    java.util.Optional<FraudEvent> findById(String id);

    Iterable<FraudEvent> findAll();

    Iterable<FraudEvent> findByUserId(String userId);

    Iterable<FraudEvent> findBySeverity(FraudSeverity severity);

    Iterable<FraudEvent> findByUserIdAndSeverity(String userId, FraudSeverity severity);

    int count();

    /**
     * Removes all fraud events for the given user. No-op if none found.
     */
    void deleteByUserId(String userId);
}
