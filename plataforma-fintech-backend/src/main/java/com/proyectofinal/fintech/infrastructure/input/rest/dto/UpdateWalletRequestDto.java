package com.proyectofinal.fintech.infrastructure.input.rest.dto;

import jakarta.validation.constraints.Size;

/**
 * Partial update payload for a wallet.
 * Either field may be present; null means "leave unchanged".
 */
public record UpdateWalletRequestDto(
        @Size(min = 1, max = 80, message = "El nombre debe tener entre 1 y 80 caracteres")
        String name,
        Boolean active) {
}
