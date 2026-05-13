package com.proyectofinal.fintech.infrastructure.config;

import com.proyectofinal.fintech.application.usecase.GetPointsRankingUseCase;
import com.proyectofinal.fintech.application.usecase.GetUserPointsUseCase;
import com.proyectofinal.fintech.domain.port.UserRepository;
import com.proyectofinal.fintech.infrastructure.mapper.PointsMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Wires SDD 07 points and ranking beans.
 * Use cases are plain classes — Spring-free application layer.
 */
@Configuration
public class PointsBeansConfig {

    @Bean
    public GetUserPointsUseCase getUserPointsUseCase(UserRepository userRepository) {
        return new GetUserPointsUseCase(userRepository);
    }

    @Bean
    public GetPointsRankingUseCase getPointsRankingUseCase(UserRepository userRepository) {
        return new GetPointsRankingUseCase(userRepository);
    }

    @Bean
    public PointsMapper pointsMapper() {
        return new PointsMapper();
    }
}
