package com.proyectofinal.fintech.infrastructure.input.rest.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for internal transfer operations (same user, different wallets).
 * Jakarta Validation annotations live only in this infrastructure layer.
 */
public record InternalTransferRequestDto(
        @NotBlank(message = "sourceWalletId must not be blank")
        String sourceWalletId,

        @NotBlank(message = "targetWalletId must not be blank")
        String targetWalletId,

        @NotNull
        @DecimalMin(value = "0.01", message = "Amount must be at least 0.01")
        Double amount,

        String description
) {
}
