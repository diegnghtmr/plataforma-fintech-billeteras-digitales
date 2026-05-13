package com.proyectofinal.fintech.application.usecase;

import com.proyectofinal.fintech.domain.exception.ErrorCode;
import com.proyectofinal.fintech.domain.exception.NotFoundException;
import com.proyectofinal.fintech.domain.model.Notificacion;
import com.proyectofinal.fintech.domain.port.NotificationRepository;

/**
 * Use case: mark a notification as read.
 * Plain class — ZERO Spring/Jakarta imports.
 */
public class MarkNotificationAsReadUseCase {

    private final NotificationRepository notificationRepository;

    public MarkNotificationAsReadUseCase(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    /**
     * Marks the notification as read and persists the change.
     *
     * @param notificationId the notification to mark as read
     * @throws NotFoundException if the notification does not exist (NOTIFICATION_NOT_FOUND)
     */
    public Notificacion execute(String notificationId) {
        Notificacion n = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.NOTIFICATION_NOT_FOUND,
                        "Notification not found: " + notificationId));

        n.markRead();
        notificationRepository.save(n);
        return n;
    }
}
