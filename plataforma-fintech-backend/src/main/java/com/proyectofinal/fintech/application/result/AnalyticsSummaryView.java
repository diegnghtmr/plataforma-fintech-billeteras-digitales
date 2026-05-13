package com.proyectofinal.fintech.application.result;

/**
 * Summary view for analytics dashboard.
 * Plain record — ZERO Spring/Jakarta imports.
 */
public record AnalyticsSummaryView(
        long totalUsers,
        long totalWallets,
        long totalTransactions,
        double totalMovedAmount,
        long fraudEventCount,
        long unreadNotificationCount
) {}
