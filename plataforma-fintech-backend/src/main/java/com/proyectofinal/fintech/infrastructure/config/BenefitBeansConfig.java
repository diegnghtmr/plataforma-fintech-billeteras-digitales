package com.proyectofinal.fintech.infrastructure.config;

import com.proyectofinal.fintech.application.service.NotificationEmitter;
import com.proyectofinal.fintech.application.usecase.ListBenefitsUseCase;
import com.proyectofinal.fintech.application.usecase.ListUserRedemptionsUseCase;
import com.proyectofinal.fintech.application.usecase.RedeemBenefitUseCase;
import com.proyectofinal.fintech.domain.port.*;
import com.proyectofinal.fintech.infrastructure.mapper.BenefitMapper;
import com.proyectofinal.fintech.infrastructure.output.memory.InMemoryBeneficioRepository;
import com.proyectofinal.fintech.infrastructure.output.memory.InMemoryBenefitRedemptionRepository;
import com.proyectofinal.fintech.infrastructure.output.memory.SequentialBeneficioIdGenerator;
import com.proyectofinal.fintech.infrastructure.output.memory.SequentialBenefitRedemptionIdGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Wires all beans for the benefit redemption flow (Slice F).
 * REQ-F1.6.
 */
@Configuration
public class BenefitBeansConfig {

    @Bean
    public InMemoryBeneficioRepository inMemoryBeneficioRepository() {
        return new InMemoryBeneficioRepository();
    }

    @Bean
    public InMemoryBenefitRedemptionRepository inMemoryBenefitRedemptionRepository() {
        return new InMemoryBenefitRedemptionRepository();
    }

    @Bean
    public SequentialBeneficioIdGenerator sequentialBeneficioIdGenerator() {
        return new SequentialBeneficioIdGenerator();
    }

    @Bean
    public SequentialBenefitRedemptionIdGenerator sequentialBenefitRedemptionIdGenerator() {
        return new SequentialBenefitRedemptionIdGenerator();
    }

    @Bean
    public ListBenefitsUseCase listBenefitsUseCase(BeneficioRepository beneficioRepository) {
        return new ListBenefitsUseCase(beneficioRepository);
    }

    @Bean
    public RedeemBenefitUseCase redeemBenefitUseCase(UserRepository userRepository,
                                                       BeneficioRepository beneficioRepository,
                                                       BenefitRedemptionRepository redemptionRepository,
                                                       BenefitRedemptionIdGenerator redemptionIdGenerator,
                                                       NotificationEmitter notificationEmitter) {
        return new RedeemBenefitUseCase(userRepository, beneficioRepository,
                redemptionRepository, redemptionIdGenerator, notificationEmitter);
    }

    @Bean
    public ListUserRedemptionsUseCase listUserRedemptionsUseCase(UserRepository userRepository,
                                                                   BenefitRedemptionRepository redemptionRepository) {
        return new ListUserRedemptionsUseCase(userRepository, redemptionRepository);
    }

    @Bean
    public BenefitMapper benefitMapper() {
        return new BenefitMapper();
    }
}
