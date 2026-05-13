package com.proyectofinal.fintech.infrastructure.input.rest.dto;

/**
 * HTTP response DTO for a single ranking entry in GET /points/ranking.
 * Field names match the OpenAPI schema (RankingItemResponse).
 */
public record RankingItemResponseDto(int position, String userId, String userName,
                                      double points, String loyaltyLevel) {
}
