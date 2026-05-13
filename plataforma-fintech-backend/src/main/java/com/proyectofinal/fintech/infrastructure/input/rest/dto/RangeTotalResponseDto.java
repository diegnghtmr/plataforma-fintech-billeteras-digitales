package com.proyectofinal.fintech.infrastructure.input.rest.dto;

/**
 * Response DTO for total-moved-in-range analytics endpoint.
 */
public record RangeTotalResponseDto(
        double totalAmount,
        int count,
        String from,
        String to
) {
}
