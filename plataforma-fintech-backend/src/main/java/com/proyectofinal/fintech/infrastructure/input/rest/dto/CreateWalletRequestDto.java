package com.proyectofinal.fintech.infrastructure.input.rest.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for creating a wallet.
 * Code is auto-generated server-side — not accepted from client.
 */
public record CreateWalletRequestDto(
        @NotBlank String name,
        @NotBlank String type
) {}
