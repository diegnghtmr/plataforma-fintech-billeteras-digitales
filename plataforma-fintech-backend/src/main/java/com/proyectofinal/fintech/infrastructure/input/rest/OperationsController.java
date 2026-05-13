package com.proyectofinal.fintech.infrastructure.input.rest;

import com.proyectofinal.fintech.application.usecase.InternalTransferUseCase;
import com.proyectofinal.fintech.application.usecase.RechargeWalletUseCase;
import com.proyectofinal.fintech.application.usecase.WithdrawWalletUseCase;
import com.proyectofinal.fintech.domain.model.Transaccion;
import com.proyectofinal.fintech.infrastructure.input.rest.dto.InternalTransferRequestDto;
import com.proyectofinal.fintech.infrastructure.input.rest.dto.MoneyOperationRequestDto;
import com.proyectofinal.fintech.infrastructure.input.rest.dto.TransactionResponseDto;
import com.proyectofinal.fintech.infrastructure.mapper.TransactionMapper;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST adapter for wallet financial operations (recharge, withdraw, internal transfer).
 * Context-path /api/v1 set globally (SDD 1).
 * No /api/v1 prefix here — handled by global server.servlet.context-path config.
 */
@RestController
public class OperationsController {

    private final RechargeWalletUseCase rechargeWalletUseCase;
    private final WithdrawWalletUseCase withdrawWalletUseCase;
    private final InternalTransferUseCase internalTransferUseCase;
    private final TransactionMapper transactionMapper;

    public OperationsController(RechargeWalletUseCase rechargeWalletUseCase,
                                 WithdrawWalletUseCase withdrawWalletUseCase,
                                 InternalTransferUseCase internalTransferUseCase,
                                 TransactionMapper transactionMapper) {
        this.rechargeWalletUseCase = rechargeWalletUseCase;
        this.withdrawWalletUseCase = withdrawWalletUseCase;
        this.internalTransferUseCase = internalTransferUseCase;
        this.transactionMapper = transactionMapper;
    }

    @PostMapping("/users/{userId}/wallets/{walletId}/recharge")
    public ResponseEntity<TransactionResponseDto> recharge(
            @PathVariable String userId,
            @PathVariable String walletId,
            @Valid @RequestBody MoneyOperationRequestDto request) {
        Transaccion tx = rechargeWalletUseCase.execute(
                userId, walletId, request.amount(), request.description());
        return ResponseEntity.ok(transactionMapper.toDto(tx));
    }

    @PostMapping("/users/{userId}/wallets/{walletId}/withdraw")
    public ResponseEntity<TransactionResponseDto> withdraw(
            @PathVariable String userId,
            @PathVariable String walletId,
            @Valid @RequestBody MoneyOperationRequestDto request) {
        Transaccion tx = withdrawWalletUseCase.execute(
                userId, walletId, request.amount(), request.description());
        return ResponseEntity.ok(transactionMapper.toDto(tx));
    }

    @PostMapping("/users/{userId}/transfers/internal")
    public ResponseEntity<TransactionResponseDto> internalTransfer(
            @PathVariable String userId,
            @Valid @RequestBody InternalTransferRequestDto request) {
        Transaccion tx = internalTransferUseCase.execute(
                userId, request.sourceWalletId(), request.targetWalletId(),
                request.amount(), request.description());
        return ResponseEntity.ok(transactionMapper.toDto(tx));
    }
}
