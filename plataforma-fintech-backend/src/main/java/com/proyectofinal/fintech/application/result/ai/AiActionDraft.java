package com.proyectofinal.fintech.application.result.ai;

import com.proyectofinal.fintech.domain.structures.MiLista;
import com.proyectofinal.fintech.domain.structures.TablaHash;

/**
 * Structured action draft returned by the AI port.
 * draft uses TablaHash<String,String> for slot values (no java.util.HashMap).
 * Immutable record. Zero Spring imports.
 */
public record AiActionDraft(
        AiIntent intent,
        boolean requiresConfirmation,
        TablaHash<String, String> draft,
        MiLista<String> missingFields,
        String explanation
) {
}
