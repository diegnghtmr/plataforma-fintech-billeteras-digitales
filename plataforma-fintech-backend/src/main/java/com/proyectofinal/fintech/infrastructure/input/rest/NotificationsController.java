package com.proyectofinal.fintech.infrastructure.input.rest;

import com.proyectofinal.fintech.application.usecase.ListUserNotificationsUseCase;
import com.proyectofinal.fintech.application.usecase.MarkNotificationAsReadUseCase;
import com.proyectofinal.fintech.domain.model.Notificacion;
import com.proyectofinal.fintech.infrastructure.input.rest.dto.NotificationResponseDto;
import com.proyectofinal.fintech.infrastructure.mapper.NotificationMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * REST adapter for notifications.
 * Context-path /api/v1 set globally (SDD 1).
 */
@RestController
@RequestMapping("/notifications")
public class NotificationsController {

    private final ListUserNotificationsUseCase listUseCase;
    private final MarkNotificationAsReadUseCase markReadUseCase;
    private final NotificationMapper mapper;

    public NotificationsController(ListUserNotificationsUseCase listUseCase,
                                    MarkNotificationAsReadUseCase markReadUseCase,
                                    NotificationMapper mapper) {
        this.listUseCase = listUseCase;
        this.markReadUseCase = markReadUseCase;
        this.mapper = mapper;
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<List<NotificationResponseDto>> listByUser(
            @PathVariable String userId,
            @RequestParam(defaultValue = "false") boolean unreadOnly) {
        List<NotificationResponseDto> result = new ArrayList<>();
        for (Notificacion n : listUseCase.execute(userId, unreadOnly)) {
            result.add(mapper.toDto(n));
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping("/{notificationId}/read")
    public ResponseEntity<NotificationResponseDto> markAsRead(
            @PathVariable String notificationId) {
        Notificacion n = markReadUseCase.execute(notificationId);
        return ResponseEntity.ok(mapper.toDto(n));
    }
}
