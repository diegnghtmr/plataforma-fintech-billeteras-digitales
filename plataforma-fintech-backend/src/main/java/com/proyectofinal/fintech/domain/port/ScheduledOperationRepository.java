package com.proyectofinal.fintech.domain.port;

import com.proyectofinal.fintech.domain.model.OperacionProgramada;

import java.util.Optional;

/**
 * Output port for scheduled operation persistence.
 * ZERO Spring/Jakarta imports — pure domain interface.
 */
public interface ScheduledOperationRepository {

    void save(OperacionProgramada op);

    Optional<OperacionProgramada> findById(String id);

    Iterable<OperacionProgramada> findAll();

    /**
     * Returns all PENDING operations ordered by scheduledAt ASC (id ASC tie-break).
     * MUST be non-destructive — repeated calls return the same logical set.
     */
    Iterable<OperacionProgramada> findPendingInPriorityOrder();

    /**
     * Removes all scheduled operations where sourceUserId equals the given userId.
     * No-op if none found.
     */
    void deleteBySourceUserId(String userId);

    /**
     * Marks an operation as already reminded for SCHEDULED_REMINDER idempotency.
     * Default no-op — override in adapters that track reminder state.
     *
     * @param opId the operation id to mark as reminded
     */
    default void markReminded(String opId) {}

    /**
     * Returns true if this operation has already had a SCHEDULED_REMINDER emitted.
     * Default returns false — override in adapters that track reminder state.
     *
     * @param opId the operation id to check
     * @return true if already reminded
     */
    default boolean isReminded(String opId) { return false; }
}
