package com.proyectofinal.fintech.infrastructure.input.rest.dto;

import jakarta.validation.constraints.Email;

/**
 * Request DTO for PUT /users/{userId}.
 * Both fields are optional (null means "keep current value").
 */
public record UpdateUserRequestDto(
        String name,
        @Email String email
) {}
