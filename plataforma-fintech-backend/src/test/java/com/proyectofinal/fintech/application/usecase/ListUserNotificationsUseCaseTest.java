package com.proyectofinal.fintech.application.usecase;

import com.proyectofinal.fintech.domain.exception.ErrorCode;
import com.proyectofinal.fintech.domain.exception.NotFoundException;
import com.proyectofinal.fintech.domain.model.*;
import com.proyectofinal.fintech.domain.port.NotificationRepository;
import com.proyectofinal.fintech.domain.port.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * T08-B20 (RED) — ListUserNotificationsUseCase tests.
 * Covers scenarios S8, S9, S10.
 */
@ExtendWith(MockitoExtension.class)
class ListUserNotificationsUseCaseTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private NotificationRepository notificationRepository;

    private ListUserNotificationsUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ListUserNotificationsUseCase(userRepository, notificationRepository);
    }

    private Usuario makeUser(String id) {
        return new Usuario(id, "Name", "email@test.com", Instant.now(), 0.0, LoyaltyLevel.BRONZE);
    }

    private Notificacion makeNotif(String id, String userId, boolean read) {
        return new Notificacion(id, userId, NotificationType.SYSTEM, NotificationSeverity.INFO,
                "title", "msg", read, Instant.now());
    }

    // S10: User not found → 404
    @Test
    void execute_userNotFound_throwsNotFoundException() {
        when(userRepository.findById("GHOST")).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> useCase.execute("GHOST", false));

        assertEquals(ErrorCode.USER_NOT_FOUND, ex.code());
        verifyNoInteractions(notificationRepository);
    }

    // S8: All notifications (3 total) in insertion order
    @Test
    void execute_allMode_returnsAllInOrder() {
        Notificacion n1 = makeNotif("NTF-000001", "USR001", true);
        Notificacion n2 = makeNotif("NTF-000002", "USR001", false);
        Notificacion n3 = makeNotif("NTF-000003", "USR001", false);
        when(userRepository.findById("USR001")).thenReturn(Optional.of(makeUser("USR001")));
        when(notificationRepository.findByUserId("USR001", false)).thenReturn(List.of(n1, n2, n3));

        Iterable<Notificacion> result = useCase.execute("USR001", false);
        List<Notificacion> list = new ArrayList<>();
        result.forEach(list::add);

        assertEquals(3, list.size());
        assertEquals("NTF-000001", list.get(0).getId());
    }

    // S9: Unread only (2 unread) in FIFO
    @Test
    void execute_unreadOnlyMode_returnsUnreadFifo() {
        Notificacion n1 = makeNotif("NTF-000002", "USR001", false);
        Notificacion n2 = makeNotif("NTF-000003", "USR001", false);
        when(userRepository.findById("USR001")).thenReturn(Optional.of(makeUser("USR001")));
        when(notificationRepository.findByUserId("USR001", true)).thenReturn(List.of(n1, n2));

        Iterable<Notificacion> result = useCase.execute("USR001", true);
        List<Notificacion> list = new ArrayList<>();
        result.forEach(list::add);

        assertEquals(2, list.size());
    }
}
