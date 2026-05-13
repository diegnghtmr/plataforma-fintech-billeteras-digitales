package com.proyectofinal.fintech.infrastructure.mapper;

import com.proyectofinal.fintech.application.result.UserView;
import com.proyectofinal.fintech.infrastructure.input.rest.dto.UserResponseDto;

/**
 * Hand-written mapper: UserView → UserResponseDto.
 * Zero Spring annotations — instantiated via @Bean in UserBeansConfig.
 */
public class UserMapper {

    public UserResponseDto toDto(UserView view) {
        return new UserResponseDto(
                view.id(),
                view.name(),
                view.email(),
                view.registeredAt().toString(),
                view.points(),
                view.loyaltyLevel().name(),
                view.walletCount(),
                view.totalBalance()
        );
    }
}
