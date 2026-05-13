package com.proyectofinal.fintech.domain.port;

import com.proyectofinal.fintech.domain.model.Transaccion;

import java.util.Optional;

/**
 * Output port for the per-user reversible-operations stack.
 * ZERO Spring/Jakarta imports — pure domain interface.
 * Backed by a Pila<Transaccion> per userId.
 */
public interface ReversibleOperationsStack {

    /**
     * Pushes a transaction onto the user's stack.
     * Implementations MUST skip {@code EXTERNAL_TRANSFER_RECEIVED} silently.
     *
     * @param tx the transaction to push
     */
    void push(Transaccion tx);

    /**
     * Removes the transaction with the given id from the user's stack.
     * Order of remaining entries is preserved.
     *
     * @param userId        the user whose stack to search
     * @param transactionId the transaction id to remove
     * @return {@code true} if the transaction was found and removed; {@code false} otherwise
     */
    boolean removeById(String userId, String transactionId);

    /**
     * Non-destructive peek at the most recently pushed transaction for the user.
     *
     * @param userId the user
     * @return optional top transaction, or empty if no entries exist
     */
    Optional<Transaccion> peekLatestFor(String userId);
}
