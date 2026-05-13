package com.proyectofinal.fintech.infrastructure.input.rest.dto;

/**
 * DTO for analytics summary response.
 */
public record AnalyticsSummaryResponseDto(
        long totalUsers,
        long totalWallets,
        long totalTransactions,
        double totalMovedAmount,
        long fraudEventCount,
        long unreadNotificationCount
) {}
