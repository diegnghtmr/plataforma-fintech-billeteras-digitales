package com.proyectofinal.fintech.application.result.ai;

import com.proyectofinal.fintech.domain.structures.MiLista;

/**
 * Fraud data snapshot for AI context (aggregate counts, no raw transaction data).
 * Zero Spring imports.
 */
public record FraudSnapshot(
        int totalFraudEvents,
        MiLista<String> recentFraudEventIds
) {
}
