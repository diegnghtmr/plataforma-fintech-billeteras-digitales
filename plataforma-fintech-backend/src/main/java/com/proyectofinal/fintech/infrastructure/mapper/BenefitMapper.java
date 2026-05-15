package com.proyectofinal.fintech.infrastructure.mapper;

import com.proyectofinal.fintech.application.result.BenefitRedemptionView;
import com.proyectofinal.fintech.application.result.BenefitView;
import com.proyectofinal.fintech.infrastructure.input.rest.dto.BenefitRedemptionResponseDto;
import com.proyectofinal.fintech.infrastructure.input.rest.dto.BenefitResponseDto;

import java.util.ArrayList;
import java.util.List;

/**
 * Mapper between application views and REST DTOs for benefits.
 * Converts MiLista → ArrayList at the REST boundary.
 * No Spring annotations — used as a @Bean.
 */
public class BenefitMapper {

    public BenefitResponseDto toDto(BenefitView view) {
        return new BenefitResponseDto(
                view.id(), view.name(), view.description(), view.pointsCost(), view.active());
    }

    public List<BenefitResponseDto> toDtoList(List<BenefitView> views) {
        List<BenefitResponseDto> result = new ArrayList<>();
        for (BenefitView v : views) result.add(toDto(v));
        return result;
    }

    public BenefitRedemptionResponseDto toDto(BenefitRedemptionView view) {
        return new BenefitRedemptionResponseDto(
                view.id(), view.userId(), view.benefitId(), view.pointsSpent(), view.redeemedAt());
    }

    public List<BenefitRedemptionResponseDto> toRedemptionDtoList(List<BenefitRedemptionView> views) {
        List<BenefitRedemptionResponseDto> result = new ArrayList<>();
        for (BenefitRedemptionView v : views) result.add(toDto(v));
        return result;
    }
}
