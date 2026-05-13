package com.proyectofinal.fintech.infrastructure.input.rest.dto;

/**
 * Response DTO for an external transfer operation.
 * Contains both the outgoing (SENT) and incoming (RECEIVED) transaction details.
 */
public record ExternalTransferResponseDto(
        TransactionResponseDto outgoingTransaction,
        TransactionResponseDto incomingTransaction
) {
}
