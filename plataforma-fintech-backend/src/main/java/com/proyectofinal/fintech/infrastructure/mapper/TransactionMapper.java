package com.proyectofinal.fintech.infrastructure.mapper;

import com.proyectofinal.fintech.application.result.ExternalTransferResult;
import com.proyectofinal.fintech.domain.model.Transaccion;
import com.proyectofinal.fintech.infrastructure.input.rest.dto.ExternalTransferResponseDto;
import com.proyectofinal.fintech.infrastructure.input.rest.dto.TransactionResponseDto;

/**
 * Pure mapper — converts domain Transaccion objects to REST DTOs.
 * No Spring annotations — can be used standalone or as a @Bean.
 */
public class TransactionMapper {

    /**
     * Converts a domain Transaccion to a TransactionResponseDto.
     * All 12 fields are mapped; nulls preserved; enum names serialized as strings.
     *
     * @param tx the domain transaction
     * @return the response DTO
     */
    public TransactionResponseDto toDto(Transaccion tx) {
        return new TransactionResponseDto(
                tx.getId(),
                tx.getTimestamp().toString(),
                tx.getType().name(),
                tx.getAmount(),
                tx.getSourceWalletId(),
                tx.getTargetWalletId(),
                tx.getSourceUserId(),
                tx.getTargetUserId(),
                tx.getStatus().name(),
                tx.getPointsGenerated(),
                tx.getDescription(),
                tx.isReversible(),
                tx.getRiskLevel().name()
        );
    }

    /**
     * Converts an ExternalTransferResult to an ExternalTransferResponseDto.
     *
     * @param result the external transfer result
     * @return the response DTO containing both outgoing and incoming transactions
     */
    public ExternalTransferResponseDto toDto(ExternalTransferResult result) {
        return new ExternalTransferResponseDto(
                toDto(result.outgoing()),
                toDto(result.incoming())
        );
    }
}
