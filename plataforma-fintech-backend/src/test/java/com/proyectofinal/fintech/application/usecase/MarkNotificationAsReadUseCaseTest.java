package com.proyectofinal.fintech.application.usecase;

import com.proyectofinal.fintech.domain.exception.ErrorCode;
import com.proyectofinal.fintech.domain.exception.NotFoundException;
import com.proyectofinal.fintech.domain.model.*;
import com.proyectofinal.fintech.domain.port.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * T08-B22 (RED) — MarkNotificationAsReadUseCase tests.
 * Covers scenarios S11, S12.
 */
@ExtendWith(MockitoExtension.class)
class MarkNotificationAsReadUseCaseTest {

    @Mock
    private NotificationRepository notificationRepository;

    private MarkNotificationAsReadUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new MarkNotificationAsReadUseCase(notificationRepository);
    }

    private Notificacion makeUnread(String id, String userId) {
        return new Notificacion(id, userId, NotificationType.SYSTEM, NotificationSeverity.INFO,
                "title", "msg", false, Instant.parse("2026-01-01T00:00:00Z"));
    }

    // S12: Notification not found → 404 NOTIFICATION_NOT_FOUND
    @Test
    void execute_notFound_throwsNotFoundException() {
        when(notificationRepository.findById("GHOST")).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> useCase.execute("GHOST"));

        assertEquals(ErrorCode.NOTIFICATION_NOT_FOUND, ex.code());
        verify(notificationRepository, never()).save(any());
    }

    // S11: Happy path — read = true after markRead
    @Test
    void execute_happyPath_markReadAndSaved() {
        Notificacion n = makeUnread("NTF-000001", "USR001");
        when(notificationRepository.findById("NTF-000001")).thenReturn(Optional.of(n));

        Notificacion result = useCase.execute("NTF-000001");

        assertTrue(result.isRead());
        verify(notificationRepository).save(n);
    }
}
