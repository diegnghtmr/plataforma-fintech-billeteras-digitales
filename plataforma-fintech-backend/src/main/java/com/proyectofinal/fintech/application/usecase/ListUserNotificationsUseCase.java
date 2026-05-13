package com.proyectofinal.fintech.application.usecase;

import com.proyectofinal.fintech.domain.exception.ErrorCode;
import com.proyectofinal.fintech.domain.exception.NotFoundException;
import com.proyectofinal.fintech.domain.model.Notificacion;
import com.proyectofinal.fintech.domain.port.NotificationRepository;
import com.proyectofinal.fintech.domain.port.UserRepository;

/**
 * Use case: list notifications for a user.
 * Plain class — ZERO Spring/Jakarta imports.
 */
public class ListUserNotificationsUseCase {

    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;

    public ListUserNotificationsUseCase(UserRepository userRepository,
                                         NotificationRepository notificationRepository) {
        this.userRepository = userRepository;
        this.notificationRepository = notificationRepository;
    }

    /**
     * Returns notifications for the given user.
     *
     * @param userId     the user whose notifications to list
     * @param unreadOnly if true, return only unread notifications in FIFO order
     * @throws NotFoundException if the user does not exist (USER_NOT_FOUND)
     */
    public Iterable<Notificacion> execute(String userId, boolean unreadOnly) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND,
                        "User not found: " + userId));

        return notificationRepository.findByUserId(userId, unreadOnly);
    }
}
