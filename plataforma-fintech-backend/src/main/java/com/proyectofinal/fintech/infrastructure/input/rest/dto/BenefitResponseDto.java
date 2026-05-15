package com.proyectofinal.fintech.infrastructure.input.rest.dto;

/**
 * Response DTO for a benefit.
 * REQ-F1.1.
 */
public record BenefitResponseDto(
        String id,
        String name,
        String description,
        int pointsCost,
        boolean active
) {}
