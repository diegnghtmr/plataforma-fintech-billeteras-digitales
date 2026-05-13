package com.proyectofinal.fintech.infrastructure.input.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyectofinal.fintech.application.usecase.ListUserNotificationsUseCase;
import com.proyectofinal.fintech.application.usecase.MarkNotificationAsReadUseCase;
import com.proyectofinal.fintech.domain.exception.ErrorCode;
import com.proyectofinal.fintech.domain.exception.NotFoundException;
import com.proyectofinal.fintech.domain.model.*;
import com.proyectofinal.fintech.infrastructure.input.rest.dto.NotificationResponseDto;
import com.proyectofinal.fintech.infrastructure.input.rest.exception.GlobalExceptionHandler;
import com.proyectofinal.fintech.infrastructure.mapper.NotificationMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * T08-B30 (RED) — NotificationsController @WebMvcTest.
 * Covers S8-S12.
 */
@WebMvcTest({NotificationsController.class})
@Import(GlobalExceptionHandler.class)
class NotificationsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ListUserNotificationsUseCase listUseCase;
    @MockBean
    private MarkNotificationAsReadUseCase markReadUseCase;
    @MockBean
    private NotificationMapper mapper;

    private static final Instant CREATED = Instant.parse("2026-01-01T00:00:00Z");

    private Notificacion makeNotif(String id, String userId, boolean read) {
        return new Notificacion(id, userId, NotificationType.SYSTEM, NotificationSeverity.INFO,
                "Title", "Msg", read, CREATED);
    }

    private NotificationResponseDto makeDto(Notificacion n) {
        return new NotificationResponseDto(n.getId(), n.getUserId(), n.getType().name(),
                n.getSeverity().name(), n.getTitle(), n.getMessage(), n.isRead(),
                n.getCreatedAt().toString());
    }

    // S8: GET list all → 200
    @Test
    void listAll_returns200() throws Exception {
        Notificacion n1 = makeNotif("NTF-000001", "USR001", true);
        Notificacion n2 = makeNotif("NTF-000002", "USR001", false);
        Notificacion n3 = makeNotif("NTF-000003", "USR001", false);
        NotificationResponseDto dto1 = makeDto(n1);

        when(listUseCase.execute("USR001", false)).thenReturn(List.of(n1, n2, n3));
        when(mapper.toDto(any())).thenReturn(dto1);

        mockMvc.perform(get("/notifications/users/USR001").param("unreadOnly", "false"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    // S9: GET unreadOnly=true → 200
    @Test
    void listUnreadOnly_returns200() throws Exception {
        Notificacion n1 = makeNotif("NTF-000002", "USR001", false);
        NotificationResponseDto dto1 = makeDto(n1);

        when(listUseCase.execute("USR001", true)).thenReturn(List.of(n1));
        when(mapper.toDto(n1)).thenReturn(dto1);

        mockMvc.perform(get("/notifications/users/USR001").param("unreadOnly", "true"))
                .andExpect(status().isOk());
    }

    // Default unreadOnly=false
    @Test
    void list_defaultUnreadOnly_isFalse() throws Exception {
        when(listUseCase.execute("USR001", false)).thenReturn(List.of());

        mockMvc.perform(get("/notifications/users/USR001"))
                .andExpect(status().isOk());

        verify(listUseCase).execute("USR001", false);
    }

    // S10: User not found → 404
    @Test
    void list_userNotFound_returns404() throws Exception {
        when(listUseCase.execute("GHOST", false))
                .thenThrow(new NotFoundException(ErrorCode.USER_NOT_FOUND, "User not found"));

        mockMvc.perform(get("/notifications/users/GHOST"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("USER_NOT_FOUND"));
    }

    // S11: POST mark-read happy → 200
    @Test
    void markRead_happyPath_returns200() throws Exception {
        Notificacion n = makeNotif("NTF-000001", "USR001", true);
        NotificationResponseDto dto = makeDto(n);

        when(markReadUseCase.execute("NTF-000001")).thenReturn(n);
        when(mapper.toDto(n)).thenReturn(dto);

        mockMvc.perform(post("/notifications/NTF-000001/read"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("NTF-000001"));
    }

    // S12: POST mark-read not found → 404
    @Test
    void markRead_notFound_returns404() throws Exception {
        when(markReadUseCase.execute("GHOST"))
                .thenThrow(new NotFoundException(ErrorCode.NOTIFICATION_NOT_FOUND, "Not found"));

        mockMvc.perform(post("/notifications/GHOST/read"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("NOTIFICATION_NOT_FOUND"));
    }
}
