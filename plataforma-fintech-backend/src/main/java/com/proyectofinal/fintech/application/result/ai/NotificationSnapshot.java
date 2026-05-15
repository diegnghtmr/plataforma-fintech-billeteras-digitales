package com.proyectofinal.fintech.application.result.ai;

import com.proyectofinal.fintech.domain.structures.MiLista;

/**
 * Snapshot of notifications for AI context.
 * Zero Spring imports.
 */
public record NotificationSnapshot(
        int unreadCount,
        MiLista<String> recentNotificationIds
) {
}
