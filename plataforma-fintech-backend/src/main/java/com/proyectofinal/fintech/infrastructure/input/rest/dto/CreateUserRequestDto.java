package com.proyectofinal.fintech.infrastructure.input.rest.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for creating a user.
 * Jakarta Validation annotations on record components are valid in Spring Boot 3+.
 */
public record CreateUserRequestDto(
        @NotBlank String id,
        @NotBlank @Size(min = 2) String name,
        @NotBlank @Email String email
) {}
