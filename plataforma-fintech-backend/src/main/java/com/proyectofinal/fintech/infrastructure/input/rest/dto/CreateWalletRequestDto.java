package com.proyectofinal.fintech.infrastructure.input.rest.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for creating a wallet.
 */
public record CreateWalletRequestDto(
        @NotBlank String code,
        @NotBlank String name,
        @NotBlank String type
) {}
