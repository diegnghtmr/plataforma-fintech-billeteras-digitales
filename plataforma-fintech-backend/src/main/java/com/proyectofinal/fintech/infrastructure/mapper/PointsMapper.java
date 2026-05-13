package com.proyectofinal.fintech.infrastructure.mapper;

import com.proyectofinal.fintech.application.result.PointsView;
import com.proyectofinal.fintech.application.result.RankingItem;
import com.proyectofinal.fintech.infrastructure.input.rest.dto.PointsResponseDto;
import com.proyectofinal.fintech.infrastructure.input.rest.dto.RankingItemResponseDto;

/**
 * Hand-written mapper for points domain objects → HTTP DTOs.
 * Instantiated via @Bean in PointsBeansConfig.
 */
public class PointsMapper {

    public PointsResponseDto toDto(PointsView view) {
        return new PointsResponseDto(
                view.userId(),
                view.points(),
                view.loyaltyLevel().name()
        );
    }

    public RankingItemResponseDto toDto(RankingItem item) {
        return new RankingItemResponseDto(
                item.position(),
                item.userId(),
                item.userName(),
                item.points(),
                item.loyaltyLevel().name()
        );
    }
}
