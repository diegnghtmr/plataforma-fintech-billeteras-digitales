package com.proyectofinal.fintech.application.result.ai;

import com.proyectofinal.fintech.domain.structures.MiLista;

/**
 * Structured explanation of a fraud event returned by the AI port.
 * Immutable record. Zero Spring imports.
 */
public record AiFraudExplanation(
        String fraudEventId,
        String summary,
        String severityExplanation,
        MiLista<String> evidence,
        String recommendation
) {
}
