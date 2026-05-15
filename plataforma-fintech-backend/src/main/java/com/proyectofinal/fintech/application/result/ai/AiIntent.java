package com.proyectofinal.fintech.application.result.ai;

/**
 * Allowlisted AI intent values.
 * Any intent returned by the LLM that is not in this enum is rejected by AiIntentValidator.
 * Zero Spring imports.
 */
public enum AiIntent {
    EXPLAIN_BALANCE_CHANGE,
    SUMMARIZE_ACTIVITY,
    EXPLAIN_FRAUD_EVENT,
    EXPLAIN_ANALYTICS,
    DRAFT_SCHEDULED_OPERATION,
    DRAFT_TRANSFER,
    SHOW_NOTIFICATIONS,
    UNKNOWN
}
