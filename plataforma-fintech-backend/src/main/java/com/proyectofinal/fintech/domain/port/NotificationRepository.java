package com.proyectofinal.fintech.domain.port;

import com.proyectofinal.fintech.domain.model.Notificacion;

import java.util.Optional;

/**
 * Output port for notification persistence.
 * ZERO Spring/Jakarta imports — pure domain interface.
 */
public interface NotificationRepository {

    void save(Notificacion n);

    Optional<Notificacion> findById(String id);

    /**
     * Returns notifications for the given user.
     *
     * @param userId    the user whose notifications to find
     * @param unreadOnly if true, return only unread notifications in FIFO insertion order;
     *                   if false, return all in insertion order
     */
    Iterable<Notificacion> findByUserId(String userId, boolean unreadOnly);

    /**
     * Removes all notifications for the given user. No-op if none found.
     */
    void deleteByUserId(String userId);
}
