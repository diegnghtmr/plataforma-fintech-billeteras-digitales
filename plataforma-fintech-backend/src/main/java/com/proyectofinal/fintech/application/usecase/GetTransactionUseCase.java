package com.proyectofinal.fintech.application.usecase;

import com.proyectofinal.fintech.domain.exception.ErrorCode;
import com.proyectofinal.fintech.domain.exception.NotFoundException;
import com.proyectofinal.fintech.domain.model.Transaccion;
import com.proyectofinal.fintech.domain.port.TransactionRepository;

/**
 * Use case: fetch a single transaction by ID for the transfer-flow view.
 * Plain class — ZERO Spring/Jakarta imports.
 */
public class GetTransactionUseCase {

    private final TransactionRepository transactionRepository;

    public GetTransactionUseCase(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public Transaccion execute(String transactionId) {
        return transactionRepository.findById(transactionId)
                .orElseThrow(() -> new NotFoundException(
                        ErrorCode.TRANSACTION_NOT_FOUND,
                        "Transaction with id=" + transactionId + " not found"));
    }
}
