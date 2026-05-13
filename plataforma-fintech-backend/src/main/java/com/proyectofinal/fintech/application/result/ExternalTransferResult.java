package com.proyectofinal.fintech.application.result;

import com.proyectofinal.fintech.domain.model.Transaccion;

/**
 * Result returned by ExternalTransferUseCase.
 * Carries both the outgoing (EXTERNAL_TRANSFER_SENT) and incoming (EXTERNAL_TRANSFER_RECEIVED)
 * transaction entries produced by a single external transfer operation.
 * ADR-5.2: two Transaccion entries per external transfer.
 */
public record ExternalTransferResult(Transaccion outgoing, Transaccion incoming) {
}
