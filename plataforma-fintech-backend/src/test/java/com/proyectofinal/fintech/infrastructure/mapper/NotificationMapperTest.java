package com.proyectofinal.fintech.infrastructure.mapper;

import com.proyectofinal.fintech.domain.model.*;
import com.proyectofinal.fintech.infrastructure.input.rest.dto.NotificationResponseDto;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

/**
 * T08-B26 (RED) — NotificationMapper tests.
 */
class NotificationMapperTest {

    private final NotificationMapper mapper = new NotificationMapper();
    private static final Instant CREATED = Instant.parse("2026-01-01T00:00:00Z");

    @Test
    void toDto_allFieldsMapped() {
        Notificacion n = new Notificacion("NTF-000001", "USR001", NotificationType.TRANSACTION,
                NotificationSeverity.WARNING, "Title", "Message", false, CREATED);

        NotificationResponseDto dto = mapper.toDto(n);

        assertEquals("NTF-000001", dto.id());
        assertEquals("USR001", dto.userId());
        assertEquals("TRANSACTION", dto.type());
        assertEquals("WARNING", dto.severity());
        assertEquals("Title", dto.title());
        assertEquals("Message", dto.message());
        assertFalse(dto.read());
        assertEquals(CREATED.toString(), dto.createdAt());
    }

    @Test
    void toDto_readNotification_readIsTrue() {
        Notificacion n = new Notificacion("NTF-000001", "USR001", NotificationType.SYSTEM,
                NotificationSeverity.INFO, "T", "M", true, CREATED);

        NotificationResponseDto dto = mapper.toDto(n);
        assertTrue(dto.read());
    }
}
