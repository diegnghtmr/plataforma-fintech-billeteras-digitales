package com.proyectofinal.fintech.infrastructure.config;

import com.proyectofinal.fintech.application.usecase.CreateUserUseCase;
import com.proyectofinal.fintech.application.usecase.DeleteUserUseCase;
import com.proyectofinal.fintech.application.usecase.GetUserUseCase;
import com.proyectofinal.fintech.application.usecase.ListUsersUseCase;
import com.proyectofinal.fintech.application.usecase.UpdateUserUseCase;
import com.proyectofinal.fintech.domain.port.*;
import com.proyectofinal.fintech.infrastructure.mapper.UserMapper;
import com.proyectofinal.fintech.infrastructure.output.memory.InMemoryUserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Wires user-related beans.
 * Use cases are plain classes — Spring-free application layer.
 * SDD-11: adds UpdateUserUseCase and DeleteUserUseCase.
 */
@Configuration
public class UserBeansConfig {

    @Bean
    public InMemoryUserRepository inMemoryUserRepository() {
        return new InMemoryUserRepository();
    }

    @Bean
    public CreateUserUseCase createUserUseCase(UserRepository userRepository, Clock clock) {
        return new CreateUserUseCase(userRepository, clock);
    }

    @Bean
    public GetUserUseCase getUserUseCase(UserRepository userRepository, WalletRepository walletRepository) {
        return new GetUserUseCase(userRepository, walletRepository);
    }

    @Bean
    public ListUsersUseCase listUsersUseCase(UserRepository userRepository, WalletRepository walletRepository) {
        return new ListUsersUseCase(userRepository, walletRepository);
    }

    @Bean
    public UpdateUserUseCase updateUserUseCase(UserRepository userRepository, WalletRepository walletRepository) {
        return new UpdateUserUseCase(userRepository, walletRepository);
    }

    @Bean
    public DeleteUserUseCase deleteUserUseCase(UserRepository userRepository,
                                                WalletRepository walletRepository,
                                                TransactionRepository transactionRepository,
                                                ScheduledOperationRepository scheduledOperationRepository,
                                                NotificationRepository notificationRepository,
                                                FraudEventRepository fraudEventRepository) {
        return new DeleteUserUseCase(userRepository, walletRepository, transactionRepository,
                scheduledOperationRepository, notificationRepository, fraudEventRepository);
    }

    @Bean
    public UserMapper userMapper() {
        return new UserMapper();
    }
}
