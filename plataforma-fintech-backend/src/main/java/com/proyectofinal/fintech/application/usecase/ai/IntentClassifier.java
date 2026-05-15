package com.proyectofinal.fintech.application.usecase.ai;

import com.proyectofinal.fintech.application.result.ai.AiIntent;
import com.proyectofinal.fintech.application.result.ai.AiScope;

import java.text.Normalizer;

/**
 * Deterministic keyword-based intent classifier for Spanish-language messages.
 * Uses JDK arrays (not java.util collections) for keyword groups — ArchUnit compliant.
 * Zero Spring imports.
 */
public class IntentClassifier {

    // Keyword groups — static final arrays, not java.util collections (ArchUnit ADR-9.1)
    private static final String[] BALANCE_CHANGE_KEYWORDS =
            {"balance", "saldo", "cambio", "cambio", "bajo", "bajo", "baj"};
    private static final String[] SUMMARIZE_ACTIVITY_KEYWORDS =
            {"resumen", "actividad", "ltimos", "ltimas", "recientes", "movi"};
    private static final String[] FRAUD_KEYWORDS =
            {"fraude", "sospechoso", "alerta", "fraude"};
    private static final String[] ANALYTICS_KEYWORDS =
            {"analytics", "metricas", "top", "frecuente", "ciclo", "plataforma"};
    private static final String[] DRAFT_SCHEDULED_KEYWORDS =
            {"programar", "agendar", "schedule", "recurrente"};
    private static final String[] DRAFT_TRANSFER_KEYWORDS =
            {"transferir", "enviame", "enviar", "transfer", "envio"};
    private static final String[] NOTIFICATIONS_KEYWORDS =
            {"notificaci", "alertas pendientes", "notificaciones"};

    /**
     * Classifies the intent of the user's message given their scope.
     *
     * @param scope   the actor's scope
     * @param message the user's natural-language message
     * @return classified intent, or UNKNOWN if no keywords match
     */
    public AiIntent classify(AiScope scope, String message) {
        if (message == null || message.isBlank()) {
            return AiIntent.UNKNOWN;
        }
        String normalized = normalize(message);

        if (containsAny(normalized, BALANCE_CHANGE_KEYWORDS)) {
            return AiIntent.EXPLAIN_BALANCE_CHANGE;
        }
        if (containsAny(normalized, FRAUD_KEYWORDS)) {
            return AiIntent.EXPLAIN_FRAUD_EVENT;
        }
        if (containsAny(normalized, ANALYTICS_KEYWORDS)) {
            return AiIntent.EXPLAIN_ANALYTICS;
        }
        if (containsAny(normalized, DRAFT_SCHEDULED_KEYWORDS)) {
            return AiIntent.DRAFT_SCHEDULED_OPERATION;
        }
        if (containsAny(normalized, DRAFT_TRANSFER_KEYWORDS)) {
            return AiIntent.DRAFT_TRANSFER;
        }
        if (containsAny(normalized, NOTIFICATIONS_KEYWORDS)) {
            return AiIntent.SHOW_NOTIFICATIONS;
        }
        if (containsAny(normalized, SUMMARIZE_ACTIVITY_KEYWORDS)) {
            return AiIntent.SUMMARIZE_ACTIVITY;
        }
        return AiIntent.UNKNOWN;
    }

    private String normalize(String input) {
        String lower = input.toLowerCase();
        // Strip diacritics: é→e, ó→o, etc.
        String decomposed = Normalizer.normalize(lower, Normalizer.Form.NFD);
        return decomposed.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }

    private boolean containsAny(String text, String[] keywords) {
        for (String kw : keywords) {
            if (text.contains(kw)) {
                return true;
            }
        }
        return false;
    }
}
