package com.proyectofinal.fintech.infrastructure.input.rest.dto;

/**
 * DTO for a frequent transfer route metric.
 */
public record RouteMetricResponseDto(
        String sourceUserId,
        String targetUserId,
        int transferCount,
        double totalAmount
) {}
