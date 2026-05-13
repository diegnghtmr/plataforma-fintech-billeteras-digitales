package com.proyectofinal.fintech.infrastructure.config;

import com.proyectofinal.fintech.application.usecase.CreateWalletUseCase;
import com.proyectofinal.fintech.application.usecase.ListWalletsUseCase;
import com.proyectofinal.fintech.domain.port.UserRepository;
import com.proyectofinal.fintech.domain.port.WalletRepository;
import com.proyectofinal.fintech.infrastructure.mapper.WalletMapper;
import com.proyectofinal.fintech.infrastructure.output.memory.InMemoryWalletRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Wires wallet-related beans.
 * Use cases are plain classes — Spring-free application layer.
 */
@Configuration
public class WalletBeansConfig {

    @Bean
    public InMemoryWalletRepository inMemoryWalletRepository() {
        return new InMemoryWalletRepository();
    }

    @Bean
    public CreateWalletUseCase createWalletUseCase(UserRepository userRepository,
                                                    WalletRepository walletRepository,
                                                    Clock clock) {
        return new CreateWalletUseCase(userRepository, walletRepository, clock);
    }

    @Bean
    public ListWalletsUseCase listWalletsUseCase(UserRepository userRepository,
                                                  WalletRepository walletRepository) {
        return new ListWalletsUseCase(userRepository, walletRepository);
    }

    @Bean
    public WalletMapper walletMapper() {
        return new WalletMapper();
    }
}
