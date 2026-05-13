package com.proyectofinal.fintech.domain.port;

import com.proyectofinal.fintech.domain.model.Transaccion;

import java.util.Optional;

/**
 * Output port for transaction persistence.
 * ZERO Spring/Jakarta imports — pure domain interface.
 */
public interface TransactionRepository {

    /**
     * Persists a transaction and returns it.
     *
     * @param transaccion the transaction to save
     * @return the saved transaction
     */
    Transaccion save(Transaccion transaccion);

    /**
     * Finds a transaction by its unique ID.
     *
     * @param id the transaction ID
     * @return optional transaction
     */
    Optional<Transaccion> findById(String id);

    /**
     * Returns all transactions associated with the given userId (source or target).
     * Results are in insertion/chronological order.
     *
     * @param userId the user ID
     * @return iterable of transactions (empty if none)
     */
    Iterable<Transaccion> findByUserId(String userId);

    /**
     * Returns all transactions associated with the given walletId (source or target).
     * Results are in insertion/chronological order.
     *
     * @param walletId the wallet ID
     * @return iterable of transactions (empty if none)
     */
    Iterable<Transaccion> findByWalletId(String walletId);

    /**
     * Returns all stored transactions.
     *
     * @return iterable of all transactions
     */
    Iterable<Transaccion> findAll();

    /**
     * Removes all transactions where sourceUserId or targetUserId equals the given userId.
     * No-op if none found.
     */
    void deleteByUserId(String userId);
}
