package com.proyectofinal.fintech.application.service;

import com.proyectofinal.fintech.domain.model.*;
import com.proyectofinal.fintech.domain.port.NotificationIdGenerator;
import com.proyectofinal.fintech.domain.port.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * T11-D01 (RED) — NotificationEmitter tests.
 * Covers all 5 emit methods with expected type/severity/title.
 */
@ExtendWith(MockitoExtension.class)
class NotificationEmitterTest {

    @Mock
    private NotificationRepository notificationRepository;
    @Mock
    private NotificationIdGenerator notificationIdGenerator;

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");
    private Clock clock;
    private NotificationEmitter emitter;

    @BeforeEach
    void setUp() {
        clock = Clock.fixed(NOW, ZoneId.of("UTC"));
        emitter = new NotificationEmitter(notificationRepository, notificationIdGenerator, clock);
        when(notificationIdGenerator.next()).thenReturn("NOTIF-001");
    }

    @Test
    void emitLowBalance_savesLowBalanceWarning() {
        emitter.emitLowBalance("USR001", "W-SAVINGS");

        ArgumentCaptor<Notificacion> captor = ArgumentCaptor.forClass(Notificacion.class);
        verify(notificationRepository).save(captor.capture());

        Notificacion n = captor.getValue();
        assertThat(n.getUserId()).isEqualTo("USR001");
        assertThat(n.getType()).isEqualTo(NotificationType.LOW_BALANCE);
        assertThat(n.getSeverity()).isEqualTo(NotificationSeverity.WARNING);
        assertThat(n.getTitle()).contains("W-SAVINGS");
        assertThat(n.isRead()).isFalse();
    }

    @Test
    void emitLevelUp_savesPointsLevelInfo() {
        emitter.emitLevelUp("USR001", LoyaltyLevel.SILVER);

        ArgumentCaptor<Notificacion> captor = ArgumentCaptor.forClass(Notificacion.class);
        verify(notificationRepository).save(captor.capture());

        Notificacion n = captor.getValue();
        assertThat(n.getUserId()).isEqualTo("USR001");
        assertThat(n.getType()).isEqualTo(NotificationType.POINTS_LEVEL);
        assertThat(n.getSeverity()).isEqualTo(NotificationSeverity.INFO);
        assertThat(n.getTitle()).contains("SILVER");
        assertThat(n.isRead()).isFalse();
    }

    @Test
    void emitScheduledNear_savesScheduledReminderInfo() {
        Instant scheduledAt = Instant.parse("2026-06-01T10:00:00Z");
        emitter.emitScheduledNear("USR001", "OP-001", scheduledAt);

        ArgumentCaptor<Notificacion> captor = ArgumentCaptor.forClass(Notificacion.class);
        verify(notificationRepository).save(captor.capture());

        Notificacion n = captor.getValue();
        assertThat(n.getUserId()).isEqualTo("USR001");
        assertThat(n.getType()).isEqualTo(NotificationType.SCHEDULED_REMINDER); // REQ-3.4
        assertThat(n.getSeverity()).isEqualTo(NotificationSeverity.INFO);
        assertThat(n.getTitle()).contains("próxima");
        assertThat(n.getMessage()).contains("2026-06-01T10:00:00Z"); // scheduledAt in message
    }

    @Test
    void emitScheduledExecuted_savesTransactionInfo() {
        emitter.emitScheduledExecuted("USR001", "OP-001");

        ArgumentCaptor<Notificacion> captor = ArgumentCaptor.forClass(Notificacion.class);
        verify(notificationRepository).save(captor.capture());

        Notificacion n = captor.getValue();
        assertThat(n.getType()).isEqualTo(NotificationType.TRANSACTION);
        assertThat(n.getSeverity()).isEqualTo(NotificationSeverity.INFO);
        assertThat(n.getTitle()).contains("ejecutada");
    }

    @Test
    void emitScheduledRejected_savesOperationRejectedWarning() {
        emitter.emitScheduledRejected("USR001", "OP-001", "fondos insuficientes");

        ArgumentCaptor<Notificacion> captor = ArgumentCaptor.forClass(Notificacion.class);
        verify(notificationRepository).save(captor.capture());

        Notificacion n = captor.getValue();
        assertThat(n.getType()).isEqualTo(NotificationType.OPERATION_REJECTED); // REQ-3.5
        assertThat(n.getSeverity()).isEqualTo(NotificationSeverity.WARNING);
        assertThat(n.getTitle()).contains("rechazada");
    }
}
