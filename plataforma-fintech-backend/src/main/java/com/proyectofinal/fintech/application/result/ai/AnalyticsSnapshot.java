package com.proyectofinal.fintech.application.result.ai;

import com.proyectofinal.fintech.domain.structures.MiLista;

/**
 * Aggregated analytics snapshot for AI context.
 * Uses MiLista for collections (ADR-9.1). Zero Spring imports.
 */
public record AnalyticsSnapshot(
        MiLista<String> topUserIds,
        MiLista<String> movementByType,
        MiLista<String> topWalletCategories,
        int totalTransactionCount
) {
}
