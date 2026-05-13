package com.proyectofinal.fintech.infrastructure.input.rest.dto;

import java.util.List;

/**
 * Canonical API error response DTO.
 * Shape matches the ApiError schema in docs/openapi.yaml exactly.
 * fields: code, message, details (nullable/omittable array of strings).
 */
public record ApiErrorDto(String code, String message, List<String> details) {}
