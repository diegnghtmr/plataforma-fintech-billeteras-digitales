package com.proyectofinal.fintech.infrastructure.input.rest.dto;

/**
 * HTTP response DTO for GET /users/{userId}/points.
 * Field names match the OpenAPI schema (PointsResponse).
 */
public record PointsResponseDto(String userId, double points, String loyaltyLevel) {
}
