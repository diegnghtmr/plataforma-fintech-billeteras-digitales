package com.proyectofinal.fintech.domain.model;

import java.time.Instant;

/**
 * Domain entity representing a user notification.
 * Plain POJO — ZERO Spring/Jakarta imports.
 * Only {@code read} is mutable via {@link #markRead()}.
 */
public class Notificacion {

    private final String id;
    private final String userId;
    private final NotificationType type;
    private final NotificationSeverity severity;
    private final String title;
    private final String message;
    private boolean read;
    private final Instant createdAt;

    public Notificacion(String id, String userId, NotificationType type, NotificationSeverity severity,
                         String title, String message, boolean read, Instant createdAt) {
        this.id = id;
        this.userId = userId;
        this.type = type;
        this.severity = severity;
        this.title = title;
        this.message = message;
        this.read = read;
        this.createdAt = createdAt;
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public String getId() { return id; }
    public String getUserId() { return userId; }
    public NotificationType getType() { return type; }
    public NotificationSeverity getSeverity() { return severity; }
    public String getTitle() { return title; }
    public String getMessage() { return message; }
    public boolean isRead() { return read; }
    public Instant getCreatedAt() { return createdAt; }

    // ── Mutation ──────────────────────────────────────────────────────────────

    /**
     * Marks this notification as read.
     */
    public void markRead() {
        this.read = true;
    }
}
