package com.proyectofinal.fintech.infrastructure.input.rest.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Test-only DTO used by TestErrorTriggerController to trigger MethodArgumentNotValidException.
 */
public record ValidatedBean(@NotBlank(message = "name is required") String name) {}
