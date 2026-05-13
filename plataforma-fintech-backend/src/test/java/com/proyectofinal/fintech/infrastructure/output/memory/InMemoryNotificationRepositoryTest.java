package com.proyectofinal.fintech.infrastructure.output.memory;

import com.proyectofinal.fintech.domain.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * T08-B12 (RED) — InMemoryNotificationRepository tests.
 */
class InMemoryNotificationRepositoryTest {

    private InMemoryNotificationRepository repo;

    @BeforeEach
    void setUp() {
        repo = new InMemoryNotificationRepository();
    }

    private Notificacion makeNotif(String id, String userId, boolean read, Instant createdAt) {
        return new Notificacion(id, userId, NotificationType.SYSTEM, NotificationSeverity.INFO,
                "title", "msg", read, createdAt);
    }

    @Test
    void save_newNotification_appearsInHistory() {
        Notificacion n = makeNotif("NTF-000001", "USR001", false, Instant.parse("2026-01-01T00:00:00Z"));
        repo.save(n);

        List<Notificacion> all = toList(repo.findByUserId("USR001", false));
        assertEquals(1, all.size());
        assertEquals("NTF-000001", all.get(0).getId());
    }

    @Test
    void save_unreadNotification_appearsInPendingQueue() {
        Notificacion n = makeNotif("NTF-000001", "USR001", false, Instant.parse("2026-01-01T00:00:00Z"));
        repo.save(n);

        List<Notificacion> unread = toList(repo.findByUserId("USR001", true));
        assertEquals(1, unread.size());
    }

    @Test
    void save_readNotification_doesNotAppearInPendingQueue() {
        Notificacion n = makeNotif("NTF-000001", "USR001", true, Instant.parse("2026-01-01T00:00:00Z"));
        repo.save(n);

        List<Notificacion> unread = toList(repo.findByUserId("USR001", true));
        assertEquals(0, unread.size());
    }

    @Test
    void save_markRead_removesFromPending() {
        Notificacion n = makeNotif("NTF-000001", "USR001", false, Instant.parse("2026-01-01T00:00:00Z"));
        repo.save(n);

        // Transition to read
        n.markRead();
        repo.save(n);

        List<Notificacion> unread = toList(repo.findByUserId("USR001", true));
        assertEquals(0, unread.size());

        // Still in history
        List<Notificacion> all = toList(repo.findByUserId("USR001", false));
        assertEquals(1, all.size());
    }

    @Test
    void multiUserIsolation() {
        Notificacion n1 = makeNotif("NTF-000001", "USR001", false, Instant.parse("2026-01-01T00:00:00Z"));
        Notificacion n2 = makeNotif("NTF-000002", "USR002", false, Instant.parse("2026-01-01T00:00:00Z"));
        repo.save(n1);
        repo.save(n2);

        List<Notificacion> usr1 = toList(repo.findByUserId("USR001", false));
        List<Notificacion> usr2 = toList(repo.findByUserId("USR002", false));
        assertEquals(1, usr1.size());
        assertEquals("NTF-000001", usr1.get(0).getId());
        assertEquals(1, usr2.size());
        assertEquals("NTF-000002", usr2.get(0).getId());
    }

    @Test
    void findByUserId_allMode_insertionOrder() {
        Instant t1 = Instant.parse("2026-01-01T00:00:00Z");
        Instant t2 = Instant.parse("2026-01-02T00:00:00Z");
        Instant t3 = Instant.parse("2026-01-03T00:00:00Z");
        repo.save(makeNotif("NTF-000001", "USR001", false, t1));
        repo.save(makeNotif("NTF-000002", "USR001", true, t2));
        repo.save(makeNotif("NTF-000003", "USR001", false, t3));

        List<Notificacion> all = toList(repo.findByUserId("USR001", false));
        assertEquals(3, all.size());
        assertEquals("NTF-000001", all.get(0).getId());
        assertEquals("NTF-000002", all.get(1).getId());
        assertEquals("NTF-000003", all.get(2).getId());
    }

    @Test
    void findByUserId_unreadOnlyMode_fifoOrder() {
        Instant t1 = Instant.parse("2026-01-01T00:00:00Z");
        Instant t2 = Instant.parse("2026-01-02T00:00:00Z");
        repo.save(makeNotif("NTF-000001", "USR001", false, t1));
        repo.save(makeNotif("NTF-000002", "USR001", false, t2));

        List<Notificacion> unread = toList(repo.findByUserId("USR001", true));
        assertEquals(2, unread.size());
        assertEquals("NTF-000001", unread.get(0).getId());
        assertEquals("NTF-000002", unread.get(1).getId());
    }

    @Test
    void findByUserId_nonDestructive_unreadMode() {
        repo.save(makeNotif("NTF-000001", "USR001", false, Instant.parse("2026-01-01T00:00:00Z")));

        List<Notificacion> first = toList(repo.findByUserId("USR001", true));
        List<Notificacion> second = toList(repo.findByUserId("USR001", true));

        assertEquals(1, first.size());
        assertEquals(1, second.size());
    }

    @Test
    void findById_returnsCorrectNotification() {
        Notificacion n = makeNotif("NTF-000001", "USR001", false, Instant.parse("2026-01-01T00:00:00Z"));
        repo.save(n);
        assertTrue(repo.findById("NTF-000001").isPresent());
        assertTrue(repo.findById("GHOST").isEmpty());
    }

    private <T> List<T> toList(Iterable<T> iterable) {
        List<T> list = new ArrayList<>();
        for (T item : iterable) {
            list.add(item);
        }
        return list;
    }
}
