package com.proyectofinal.fintech.application.usecase;

import com.proyectofinal.fintech.domain.exception.ErrorCode;
import com.proyectofinal.fintech.domain.exception.NotFoundException;
import com.proyectofinal.fintech.domain.model.Transaccion;
import com.proyectofinal.fintech.domain.model.TransactionStatus;
import com.proyectofinal.fintech.domain.model.TransactionType;
import com.proyectofinal.fintech.domain.port.TransactionRepository;
import com.proyectofinal.fintech.domain.port.UserRepository;
import com.proyectofinal.fintech.domain.structures.MiLista;

import java.util.Optional;

/**
 * Use case: list all transactions for a user with optional type/status filters.
 * Plain class — ZERO Spring/Jakarta imports.
 */
public class ListUserTransactionsUseCase {

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    public ListUserTransactionsUseCase(UserRepository userRepository,
                                        TransactionRepository transactionRepository) {
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
    }

    /**
     * Returns transactions for the given user, optionally filtered by type and/or status.
     *
     * @param userId          user to query
     * @param typeFilter      optional type filter
     * @param statusFilter    optional status filter
     * @return iterable of matching transactions in repository order
     */
    public Iterable<Transaccion> execute(String userId,
                                          Optional<TransactionType> typeFilter,
                                          Optional<TransactionStatus> statusFilter) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND,
                        "User with id=" + userId + " not found"));

        Iterable<Transaccion> all = transactionRepository.findByUserId(userId);

        MiLista<Transaccion> result = new MiLista<>();
        for (Transaccion tx : all) {
            if (typeFilter.isPresent() && tx.getType() != typeFilter.get()) {
                continue;
            }
            if (statusFilter.isPresent() && tx.getStatus() != statusFilter.get()) {
                continue;
            }
            result.add(tx);
        }
        return result;
    }
}
