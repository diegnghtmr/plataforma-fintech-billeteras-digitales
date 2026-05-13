package com.proyectofinal.fintech.infrastructure.output.memory;

import com.proyectofinal.fintech.domain.model.Notificacion;
import com.proyectofinal.fintech.domain.port.NotificationRepository;
import com.proyectofinal.fintech.domain.structures.ColaSimple;
import com.proyectofinal.fintech.domain.structures.MiLista;
import com.proyectofinal.fintech.domain.structures.TablaHash;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * In-memory adapter for NotificationRepository.
 *
 * <p>Three indices:
 * <ul>
 *   <li>{@code byId}: TablaHash for O(1) lookup by id</li>
 *   <li>{@code historyByUser}: TablaHash of MiLista (full insertion-ordered history per user)</li>
 *   <li>{@code pendingByUser}: TablaHash of ColaSimple (FIFO unread per user)</li>
 * </ul>
 *
 * <p>ADR-8.2: When a notification transitions unread→read, pendingByUser is rebuilt by
 * draining and re-enqueueing all items except the one that became read.
 */
public class InMemoryNotificationRepository implements NotificationRepository {

    private final TablaHash<String, Notificacion> byId = new TablaHash<>();
    private final TablaHash<String, MiLista<Notificacion>> historyByUser = new TablaHash<>();
    private final TablaHash<String, ColaSimple<Notificacion>> pendingByUser = new TablaHash<>();

    @Override
    public void save(Notificacion n) {
        boolean isNew = !byId.containsKey(n.getId());
        byId.put(n.getId(), n);

        if (isNew) {
            // Add to insertion-ordered history
            historyByUser
                    .get(n.getUserId())
                    .orElseGet(() -> {
                        MiLista<Notificacion> list = new MiLista<>();
                        historyByUser.put(n.getUserId(), list);
                        return list;
                    })
                    .add(n);

            // If unread, enqueue to pending FIFO
            if (!n.isRead()) {
                pendingByUser
                        .get(n.getUserId())
                        .orElseGet(() -> {
                            ColaSimple<Notificacion> q = new ColaSimple<>();
                            pendingByUser.put(n.getUserId(), q);
                            return q;
                        })
                        .enqueue(n);
            }
        } else {
            // Existing notification updated — if now read, rebuild pending queue without it
            if (n.isRead()) {
                pendingByUser.get(n.getUserId()).ifPresent(queue -> {
                    ColaSimple<Notificacion> rebuilt = rebuildQueueExcluding(queue, n.getId());
                    pendingByUser.put(n.getUserId(), rebuilt);
                });
            }
        }
    }

    @Override
    public Optional<Notificacion> findById(String id) {
        return byId.get(id);
    }

    @Override
    public Iterable<Notificacion> findByUserId(String userId, boolean unreadOnly) {
        if (unreadOnly) {
            return pendingByUser.get(userId)
                    .map(q -> (Iterable<Notificacion>) snapshotQueue(q))
                    .orElse(List.of());
        } else {
            return historyByUser.get(userId)
                    .map(list -> (Iterable<Notificacion>) list.toList())
                    .orElse(List.of());
        }
    }

    /**
     * Non-destructive snapshot of a ColaSimple — drains into ArrayList while rebuilding
     * the queue for preservation (ADR-8.2 snapshotQueue helper).
     */
    private List<Notificacion> snapshotQueue(ColaSimple<Notificacion> queue) {
        List<Notificacion> snapshot = new ArrayList<>();
        ColaSimple<Notificacion> temp = new ColaSimple<>();

        while (!queue.isEmpty()) {
            queue.dequeue().ifPresent(item -> {
                snapshot.add(item);
                temp.enqueue(item);
            });
        }

        // Restore queue
        while (!temp.isEmpty()) {
            temp.dequeue().ifPresent(queue::enqueue);
        }

        return snapshot;
    }

    @Override
    public void deleteByUserId(String userId) {
        // Remove all notifications for the user from byId
        historyByUser.get(userId).ifPresent(list -> {
            for (Notificacion n : list) {
                byId.remove(n.getId());
            }
        });
        // Remove the user's indexes
        historyByUser.remove(userId);
        pendingByUser.remove(userId);
    }

    /**
     * Rebuilds a ColaSimple, excluding the notification with the given id.
     * O(n) per user — acceptable at academic scale (ADR-8.2).
     */
    private ColaSimple<Notificacion> rebuildQueueExcluding(ColaSimple<Notificacion> queue, String excludedId) {
        ColaSimple<Notificacion> drained = new ColaSimple<>();
        ColaSimple<Notificacion> rebuilt = new ColaSimple<>();

        while (!queue.isEmpty()) {
            queue.dequeue().ifPresent(drained::enqueue);
        }

        while (!drained.isEmpty()) {
            drained.dequeue().ifPresent(item -> {
                if (!item.getId().equals(excludedId)) {
                    rebuilt.enqueue(item);
                }
            });
        }

        return rebuilt;
    }
}
