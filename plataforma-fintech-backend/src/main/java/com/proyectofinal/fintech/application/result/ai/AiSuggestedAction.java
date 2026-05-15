package com.proyectofinal.fintech.application.result.ai;

/**
 * Represents a suggested follow-up action returned by the AI.
 * Immutable record. Zero Spring imports.
 */
public record AiSuggestedAction(AiIntent type, String label) {
}
