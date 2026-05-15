package com.proyectofinal.fintech.infrastructure.mapper;

import com.proyectofinal.fintech.domain.model.OperacionProgramada;
import com.proyectofinal.fintech.infrastructure.input.rest.dto.ScheduledOperationResponseDto;

/**
 * Pure mapper — converts domain OperacionProgramada objects to REST DTOs.
 * No Spring annotations — used as a @Bean.
 */
public class ScheduledOperationMapper {

    /**
     * Converts a domain OperacionProgramada to a ScheduledOperationResponseDto.
     * All 10 fields are mapped; nulls preserved; enum names serialized as strings.
     */
    public ScheduledOperationResponseDto toDto(OperacionProgramada op) {
        return new ScheduledOperationResponseDto(
                op.getId(),
                op.getType().name(),
                op.getStatus().name(),
                op.getSourceUserId(),
                op.getSourceWalletId(),
                op.getTargetUserId(),
                op.getTargetWalletId(),
                op.getAmount(),
                op.getScheduledAt().toString(),
                op.getDescription(),
                op.getRecurrence().name()
        );
    }
}
