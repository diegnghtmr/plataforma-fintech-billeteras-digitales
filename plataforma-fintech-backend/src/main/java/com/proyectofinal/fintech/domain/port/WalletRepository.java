package com.proyectofinal.fintech.domain.port;

import com.proyectofinal.fintech.domain.model.Billetera;
import com.proyectofinal.fintech.domain.structures.MiLista;

import java.util.Optional;

/**
 * Output port for wallet persistence.
 * ZERO Spring/Jakarta imports — pure domain interface.
 * REQ-F3.1: findAll() returns MiLista<Billetera>; no java.util.List in this interface.
 */
public interface WalletRepository {

    void save(Billetera billetera);

    Iterable<Billetera> findByOwnerId(String ownerId);

    Optional<Billetera> findByOwnerIdAndCode(String ownerId, String code);

    boolean existsByOwnerIdAndCode(String ownerId, String code);

    int countByOwnerId(String ownerId);

    double sumBalanceByOwnerId(String ownerId);

    /**
     * Returns all wallets across all owners.
     * REQ-F3.1: returns MiLista<Billetera> — no JDK List.
     */
    MiLista<Billetera> findAll();

    /**
     * Removes all wallets owned by the given user. No-op if none found.
     */
    void deleteByOwnerId(String ownerId);
}
