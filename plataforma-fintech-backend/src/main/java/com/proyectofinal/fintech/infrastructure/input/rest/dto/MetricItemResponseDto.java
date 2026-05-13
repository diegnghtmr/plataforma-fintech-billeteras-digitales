package com.proyectofinal.fintech.infrastructure.input.rest.dto;

/**
 * DTO for a single metric item in analytics responses.
 */
public record MetricItemResponseDto(String id, String label, double value) {}
