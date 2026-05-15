package com.proyectofinal.fintech.domain.model;

/**
 * Recurrence type for scheduled operations.
 * NONE means the operation runs once and does not recur.
 * Other values trigger automatic spawning of a next occurrence after execution.
 */
public enum RecurrenceType {
    NONE,
    DAILY,
    WEEKLY,
    MONTHLY
}
