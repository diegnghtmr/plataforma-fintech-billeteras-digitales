package com.proyectofinal.fintech.infrastructure.input.rest.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

/**
 * Request DTO for recharge and withdrawal operations.
 * Jakarta Validation annotations live only in this infrastructure layer.
 */
public record MoneyOperationRequestDto(
        @NotNull
        @DecimalMin(value = "0.01", message = "Amount must be at least 0.01")
        Double amount,

        String description
) {
}
