package com.proyectofinal.fintech.infrastructure.input.rest.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for PUT /users/{userId}.
 * Both fields are optional (null means "keep current value").
 * When name is provided, must be at least 2 chars (matches OpenAPI minLength: 2).
 */
public record UpdateUserRequestDto(
        @Size(min = 2) String name,
        @Email String email
) {}
