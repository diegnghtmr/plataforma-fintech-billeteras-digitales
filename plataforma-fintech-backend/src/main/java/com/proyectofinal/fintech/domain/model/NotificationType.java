package com.proyectofinal.fintech.domain.model;

public enum NotificationType {
    LOW_BALANCE,
    TRANSACTION,
    FRAUD_ALERT,
    POINTS_LEVEL,
    SYSTEM,
    SCHEDULED_REMINDER,
    OPERATION_REJECTED,
    /** REQ-3.6: enum-only slot — reserved for future benefit-redemption flow (not yet implemented). */
    BENEFIT_REDEEMED
}
