package com.proyectofinal.fintech.infrastructure.input.rest.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for external transfer operations (different users).
 * Jakarta Validation annotations live only in this infrastructure layer.
 */
public record ExternalTransferRequestDto(
        @NotBlank(message = "sourceUserId must not be blank")
        String sourceUserId,

        @NotBlank(message = "sourceWalletId must not be blank")
        String sourceWalletId,

        @NotBlank(message = "targetUserId must not be blank")
        String targetUserId,

        @NotBlank(message = "targetWalletId must not be blank")
        String targetWalletId,

        @NotNull
        @DecimalMin(value = "0.01", message = "Amount must be at least 0.01")
        Double amount,

        String description
) {
}
