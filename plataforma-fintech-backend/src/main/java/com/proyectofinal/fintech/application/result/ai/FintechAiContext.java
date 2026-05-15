package com.proyectofinal.fintech.application.result.ai;

/**
 * Composite context passed to the AI port containing all aggregated data for the request.
 * contextHash is SHA-256 hex of the serialized context (computed by BuildFintechAiContextService).
 * userFinancials is null for ADMIN-scoped requests.
 * Zero Spring imports.
 */
public record FintechAiContext(
        String actorRole,
        AiScope scope,
        String range,
        AnalyticsSnapshot analytics,
        UserFinancialSnapshot userFinancials,
        FraudSnapshot fraud,
        ScheduledOperationsSnapshot scheduled,
        NotificationSnapshot notifications,
        DataQualityNotes notes,
        String contextHash
) {
}
