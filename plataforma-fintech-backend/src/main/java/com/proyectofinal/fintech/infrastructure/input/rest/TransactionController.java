package com.proyectofinal.fintech.infrastructure.input.rest;

import com.proyectofinal.fintech.application.usecase.GetTransactionUseCase;
import com.proyectofinal.fintech.application.usecase.ListUserTransactionsUseCase;
import com.proyectofinal.fintech.application.usecase.ListWalletTransactionsUseCase;
import com.proyectofinal.fintech.application.usecase.ReverseTransactionUseCase;
import com.proyectofinal.fintech.domain.model.Transaccion;
import com.proyectofinal.fintech.domain.model.TransactionStatus;
import com.proyectofinal.fintech.domain.model.TransactionType;
import com.proyectofinal.fintech.infrastructure.input.rest.dto.TransactionResponseDto;
import com.proyectofinal.fintech.infrastructure.mapper.TransactionMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * REST adapter for transaction history and reversal (SDD 6).
 * Context-path /api/v1 set globally.
 */
@RestController
public class TransactionController {

    private final ListUserTransactionsUseCase listUserTransactionsUseCase;
    private final ListWalletTransactionsUseCase listWalletTransactionsUseCase;
    private final GetTransactionUseCase getTransactionUseCase;
    private final ReverseTransactionUseCase reverseTransactionUseCase;
    private final TransactionMapper transactionMapper;

    public TransactionController(ListUserTransactionsUseCase listUserTransactionsUseCase,
                                  ListWalletTransactionsUseCase listWalletTransactionsUseCase,
                                  GetTransactionUseCase getTransactionUseCase,
                                  ReverseTransactionUseCase reverseTransactionUseCase,
                                  TransactionMapper transactionMapper) {
        this.listUserTransactionsUseCase = listUserTransactionsUseCase;
        this.listWalletTransactionsUseCase = listWalletTransactionsUseCase;
        this.getTransactionUseCase = getTransactionUseCase;
        this.reverseTransactionUseCase = reverseTransactionUseCase;
        this.transactionMapper = transactionMapper;
    }

    /**
     * GET /users/{userId}/transactions?type=&status=
     * Returns all transactions for the user with optional filters.
     */
    @GetMapping("/users/{userId}/transactions")
    public ResponseEntity<List<TransactionResponseDto>> getUserTransactions(
            @PathVariable String userId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String status) {

        Optional<TransactionType> typeFilter = Optional.ofNullable(type)
                .map(TransactionType::valueOf);
        Optional<TransactionStatus> statusFilter = Optional.ofNullable(status)
                .map(TransactionStatus::valueOf);

        Iterable<Transaccion> txs = listUserTransactionsUseCase.execute(userId, typeFilter, statusFilter);
        List<TransactionResponseDto> dtos = toList(txs);
        return ResponseEntity.ok(dtos);
    }

    /**
     * GET /users/{userId}/wallets/{walletId}/transactions
     * Returns all transactions for the specified wallet.
     */
    @GetMapping("/users/{userId}/wallets/{walletId}/transactions")
    public ResponseEntity<List<TransactionResponseDto>> getWalletTransactions(
            @PathVariable String userId,
            @PathVariable String walletId) {

        Iterable<Transaccion> txs = listWalletTransactionsUseCase.execute(userId, walletId);
        List<TransactionResponseDto> dtos = toList(txs);
        return ResponseEntity.ok(dtos);
    }

    /**
     * GET /transactions/{transactionId}
     * Returns a single transaction by ID (powers the transfer-flow view).
     */
    @GetMapping("/transactions/{transactionId}")
    public ResponseEntity<TransactionResponseDto> getTransaction(
            @PathVariable String transactionId) {
        Transaccion tx = getTransactionUseCase.execute(transactionId);
        return ResponseEntity.ok(transactionMapper.toDto(tx));
    }

    /**
     * POST /transactions/{transactionId}/reverse
     * Reverses the specified transaction.
     */
    @PostMapping("/transactions/{transactionId}/reverse")
    public ResponseEntity<TransactionResponseDto> reverseTransaction(
            @PathVariable String transactionId) {

        Transaccion tx = reverseTransactionUseCase.execute(transactionId);
        return ResponseEntity.ok(transactionMapper.toDto(tx));
    }

    private List<TransactionResponseDto> toList(Iterable<Transaccion> txs) {
        List<TransactionResponseDto> result = new ArrayList<>();
        for (Transaccion tx : txs) {
            result.add(transactionMapper.toDto(tx));
        }
        return result;
    }
}
