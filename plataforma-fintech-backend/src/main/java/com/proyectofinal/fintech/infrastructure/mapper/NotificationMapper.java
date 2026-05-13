package com.proyectofinal.fintech.infrastructure.mapper;

import com.proyectofinal.fintech.domain.model.Notificacion;
import com.proyectofinal.fintech.infrastructure.input.rest.dto.NotificationResponseDto;

/**
 * Pure mapper — converts domain Notificacion objects to REST DTOs.
 * No Spring annotations — used as a @Bean.
 */
public class NotificationMapper {

    /**
     * Converts a domain Notificacion to a NotificationResponseDto.
     * All 8 fields are mapped; enum names serialized as strings.
     */
    public NotificationResponseDto toDto(Notificacion n) {
        return new NotificationResponseDto(
                n.getId(),
                n.getUserId(),
                n.getType().name(),
                n.getSeverity().name(),
                n.getTitle(),
                n.getMessage(),
                n.isRead(),
                n.getCreatedAt().toString()
        );
    }
}
