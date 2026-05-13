package com.proyectofinal.fintech.infrastructure.config;

import com.proyectofinal.fintech.application.usecase.ListFraudEventsUseCase;
import com.proyectofinal.fintech.domain.port.FraudEventIdGenerator;
import com.proyectofinal.fintech.domain.port.FraudEventRepository;
import com.proyectofinal.fintech.domain.port.TransactionRepository;
import com.proyectofinal.fintech.domain.port.TransferGraphRepository;
import com.proyectofinal.fintech.domain.service.FraudDetector;
import com.proyectofinal.fintech.infrastructure.input.rest.FraudController;
import com.proyectofinal.fintech.infrastructure.mapper.FraudMapper;
import com.proyectofinal.fintech.infrastructure.output.memory.InMemoryFraudEventRepository;
import com.proyectofinal.fintech.infrastructure.output.memory.InMemoryTransferGraphRepository;
import com.proyectofinal.fintech.infrastructure.output.memory.SequentialFraudEventIdGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Wires all fraud-related beans.
 * Singletons: FraudEventRepository, FraudEventIdGenerator, TransferGraphRepository.
 * ADR-9.4: TransferGraphRepository is a singleton so graph state persists across requests.
 */
@Configuration
public class FraudBeansConfig {

    @Bean
    public FraudEventRepository fraudEventRepository() {
        return new InMemoryFraudEventRepository();
    }

    @Bean
    public FraudEventIdGenerator fraudEventIdGenerator() {
        return new SequentialFraudEventIdGenerator();
    }

    @Bean
    public TransferGraphRepository transferGraphRepository() {
        return new InMemoryTransferGraphRepository();
    }

    /**
     * ADR-11.4: FraudDetector refactored from static to injectable bean.
     * ADR-11.5: FraudEventIdGenerator moved into constructor — callers call detect(tx) only.
     * Needs TransactionRepository for velocity rule and Clock for timestamp comparison.
     */
    @Bean
    public FraudDetector fraudDetector(TransactionRepository transactionRepository,
                                        FraudEventIdGenerator fraudEventIdGenerator,
                                        Clock clock) {
        return new FraudDetector(transactionRepository, fraudEventIdGenerator, clock);
    }

    @Bean
    public ListFraudEventsUseCase listFraudEventsUseCase(FraudEventRepository fraudEventRepository) {
        return new ListFraudEventsUseCase(fraudEventRepository);
    }

    @Bean
    public FraudMapper fraudMapper() {
        return new FraudMapper();
    }
}
