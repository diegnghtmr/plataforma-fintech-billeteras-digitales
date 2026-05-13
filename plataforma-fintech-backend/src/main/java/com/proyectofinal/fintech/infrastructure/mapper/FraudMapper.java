package com.proyectofinal.fintech.infrastructure.mapper;

import com.proyectofinal.fintech.domain.model.FraudEvent;
import com.proyectofinal.fintech.infrastructure.input.rest.dto.FraudEventResponseDto;

/**
 * Maps fraud domain events to response DTOs.
 */
public class FraudMapper {

    public FraudEventResponseDto toDto(FraudEvent event) {
        return new FraudEventResponseDto(
                event.getId(),
                event.getUserId(),
                event.getTransactionId(),
                event.getType(),
                event.getSeverity().name(),
                event.getDescription(),
                event.getCreatedAt()
        );
    }
}
