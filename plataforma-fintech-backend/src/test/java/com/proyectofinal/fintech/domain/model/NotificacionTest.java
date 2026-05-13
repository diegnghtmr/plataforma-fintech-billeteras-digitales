package com.proyectofinal.fintech.domain.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

/**
 * T08-B04 (RED) — Notificacion domain model tests.
 * Covers S-DOM-3.
 */
class NotificacionTest {

    private Notificacion make(String id, String userId) {
        return new Notificacion(id, userId, NotificationType.SYSTEM, NotificationSeverity.INFO,
                "Test title", "Test message", false, Instant.parse("2026-01-01T00:00:00Z"));
    }

    // S-DOM-3: default read = false
    @Test
    void defaultRead_isFalse() {
        Notificacion n = make("NTF-000001", "USR001");
        assertFalse(n.isRead());
    }

    // S-DOM-3: markRead() sets read = true
    @Test
    void markRead_setsReadToTrue() {
        Notificacion n = make("NTF-000001", "USR001");
        n.markRead();
        assertTrue(n.isRead());
    }

    @Test
    void fieldsAreAccessible() {
        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        Notificacion n = new Notificacion("NTF-000001", "USR001", NotificationType.TRANSACTION,
                NotificationSeverity.WARNING, "Title", "Message", false, now);

        assertEquals("NTF-000001", n.getId());
        assertEquals("USR001", n.getUserId());
        assertEquals(NotificationType.TRANSACTION, n.getType());
        assertEquals(NotificationSeverity.WARNING, n.getSeverity());
        assertEquals("Title", n.getTitle());
        assertEquals("Message", n.getMessage());
        assertFalse(n.isRead());
        assertEquals(now, n.getCreatedAt());
    }
}
