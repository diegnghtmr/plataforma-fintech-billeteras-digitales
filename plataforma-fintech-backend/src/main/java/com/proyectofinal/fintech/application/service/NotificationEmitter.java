package com.proyectofinal.fintech.application.service;

import com.proyectofinal.fintech.domain.model.*;
import com.proyectofinal.fintech.domain.port.NotificationIdGenerator;
import com.proyectofinal.fintech.domain.port.NotificationRepository;

import java.time.Clock;
import java.time.Instant;

/**
 * Application service for emitting system notifications.
 * ADR-11.5: Centralizes Notificacion construction; no Spring annotations.
 * Constructor: (NotificationRepository, NotificationIdGenerator, Clock).
 *
 * ZERO Spring/Jakarta imports.
 */
public class NotificationEmitter {

    private final NotificationRepository notificationRepository;
    private final NotificationIdGenerator idGenerator;
    private final Clock clock;

    public NotificationEmitter(NotificationRepository notificationRepository,
                                NotificationIdGenerator idGenerator,
                                Clock clock) {
        this.notificationRepository = notificationRepository;
        this.idGenerator = idGenerator;
        this.clock = clock;
    }

    /**
     * Emits LOW_BALANCE WARNING when source wallet drops below threshold.
     *
     * @param userId     the user to notify
     * @param walletCode the wallet code that has low balance
     */
    public void emitLowBalance(String userId, String walletCode) {
        notificationRepository.save(new Notificacion(
                idGenerator.next(),
                userId,
                NotificationType.LOW_BALANCE,
                NotificationSeverity.WARNING,
                "Saldo bajo en billetera " + walletCode,
                "Tu saldo en la billetera " + walletCode + " está por debajo del mínimo recomendado.",
                false,
                Instant.now(clock)
        ));
    }

    /**
     * Emits POINTS_LEVEL INFO when user crosses a loyalty level boundary.
     *
     * @param userId the user to notify
     * @param level  the new loyalty level achieved
     */
    public void emitLevelUp(String userId, LoyaltyLevel level) {
        notificationRepository.save(new Notificacion(
                idGenerator.next(),
                userId,
                NotificationType.POINTS_LEVEL,
                NotificationSeverity.INFO,
                "Has ascendido a " + level.name(),
                "Felicitaciones! Ahora eres nivel " + level.name() + ".",
                false,
                Instant.now(clock)
        ));
    }

    /**
     * Emits SCHEDULED_REMINDER INFO before dispatching a scheduled operation.
     * REQ-3.4: type is SCHEDULED_REMINDER; includes scheduledAt in message body.
     *
     * @param userId      the user to notify
     * @param operationId the scheduled operation id
     * @param scheduledAt the instant at which the operation is scheduled to execute
     */
    public void emitScheduledNear(String userId, String operationId, Instant scheduledAt) {
        String scheduledAtStr = scheduledAt.toString(); // ISO-8601 UTC, e.g. 2026-06-01T10:00:00Z
        notificationRepository.save(new Notificacion(
                idGenerator.next(),
                userId,
                NotificationType.SCHEDULED_REMINDER,
                NotificationSeverity.INFO,
                "Operación programada próxima a ejecutarse",
                "La operación programada " + operationId + " está a punto de ejecutarse el " + scheduledAtStr + ".",
                false,
                Instant.now(clock)
        ));
    }

    /**
     * Emits TRANSACTION INFO after a scheduled operation executes successfully.
     *
     * @param userId      the user to notify
     * @param operationId the scheduled operation id
     */
    public void emitScheduledExecuted(String userId, String operationId) {
        notificationRepository.save(new Notificacion(
                idGenerator.next(),
                userId,
                NotificationType.TRANSACTION,
                NotificationSeverity.INFO,
                "Operación programada ejecutada",
                "La operación programada " + operationId + " se ejecutó exitosamente.",
                false,
                Instant.now(clock)
        ));
    }

    /**
     * Emits OPERATION_REJECTED WARNING when a scheduled operation fails.
     * REQ-3.5: type changed from TRANSACTION to OPERATION_REJECTED.
     *
     * @param userId      the user to notify
     * @param operationId the scheduled operation id
     * @param reason      the failure reason
     */
    public void emitScheduledRejected(String userId, String operationId, String reason) {
        notificationRepository.save(new Notificacion(
                idGenerator.next(),
                userId,
                NotificationType.OPERATION_REJECTED,
                NotificationSeverity.WARNING,
                "Operación programada rechazada",
                "La operación programada " + operationId + " fue rechazada: " + reason,
                false,
                Instant.now(clock)
        ));
    }

    /**
     * Emits OPERATION_REJECTED WARNING when any use case encounters a domain validation failure.
     * REQ-3.5: covers transfer use case failure paths.
     *
     * @param userId the user to notify
     * @param reason the failure reason
     */
    public void emitOperationRejected(String userId, String reason) {
        notificationRepository.save(new Notificacion(
                idGenerator.next(),
                userId,
                NotificationType.OPERATION_REJECTED,
                NotificationSeverity.WARNING,
                "Operación rechazada",
                "La operación fue rechazada: " + reason,
                false,
                Instant.now(clock)
        ));
    }
}
