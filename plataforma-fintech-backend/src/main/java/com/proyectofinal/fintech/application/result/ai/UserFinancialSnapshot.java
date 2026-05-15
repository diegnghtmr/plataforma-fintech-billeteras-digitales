package com.proyectofinal.fintech.application.result.ai;

import com.proyectofinal.fintech.domain.structures.MiLista;

/**
 * User-scoped financial snapshot for AI context.
 * Only includes data belonging to the requesting user (RF-AI-07).
 * Zero Spring imports.
 */
public record UserFinancialSnapshot(
        String userId,
        double totalBalance,
        MiLista<String> walletIds,
        int transactionCount,
        int loyaltyPoints,
        String loyaltyLevel
) {
}
