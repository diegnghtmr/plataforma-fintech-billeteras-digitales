package com.proyectofinal.fintech.infrastructure.input.rest;

import com.proyectofinal.fintech.application.result.ExternalTransferResult;
import com.proyectofinal.fintech.application.usecase.ExternalTransferUseCase;
import com.proyectofinal.fintech.infrastructure.input.rest.dto.ExternalTransferRequestDto;
import com.proyectofinal.fintech.infrastructure.input.rest.dto.ExternalTransferResponseDto;
import com.proyectofinal.fintech.infrastructure.mapper.TransactionMapper;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST adapter for external transfer operations (different users).
 * Context-path /api/v1 set globally (SDD 1).
 */
@RestController
@RequestMapping("/transfers/external")
public class ExternalTransferController {

    private final ExternalTransferUseCase externalTransferUseCase;
    private final TransactionMapper transactionMapper;

    public ExternalTransferController(ExternalTransferUseCase externalTransferUseCase,
                                       TransactionMapper transactionMapper) {
        this.externalTransferUseCase = externalTransferUseCase;
        this.transactionMapper = transactionMapper;
    }

    @PostMapping
    public ResponseEntity<ExternalTransferResponseDto> externalTransfer(
            @Valid @RequestBody ExternalTransferRequestDto request) {
        ExternalTransferResult result = externalTransferUseCase.execute(
                request.sourceUserId(), request.sourceWalletId(),
                request.targetUserId(), request.targetWalletId(),
                request.amount(), request.description());
        return ResponseEntity.ok(transactionMapper.toDto(result));
    }
}
