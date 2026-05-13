package com.proyectofinal.fintech.infrastructure.config;

import com.proyectofinal.fintech.application.service.NotificationEmitter;
import com.proyectofinal.fintech.application.usecase.ListUserNotificationsUseCase;
import com.proyectofinal.fintech.application.usecase.MarkNotificationAsReadUseCase;
import com.proyectofinal.fintech.domain.port.NotificationIdGenerator;
import com.proyectofinal.fintech.domain.port.NotificationRepository;
import com.proyectofinal.fintech.domain.port.UserRepository;
import com.proyectofinal.fintech.infrastructure.mapper.NotificationMapper;
import com.proyectofinal.fintech.infrastructure.output.memory.InMemoryNotificationRepository;
import com.proyectofinal.fintech.infrastructure.output.memory.SequentialNotificationIdGenerator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;


/**
 * Wires all beans for SDD 08 notifications slice.
 * SDD-11: adds NotificationEmitter bean (service for domain-triggered notifications).
 */
@Configuration
public class NotificationsBeansConfig {

    @Bean
    public InMemoryNotificationRepository inMemoryNotificationRepository() {
        return new InMemoryNotificationRepository();
    }

    @Bean
    public SequentialNotificationIdGenerator sequentialNotificationIdGenerator() {
        return new SequentialNotificationIdGenerator();
    }

    @Bean
    public NotificationEmitter notificationEmitter(NotificationRepository notificationRepository,
                                                    NotificationIdGenerator notificationIdGenerator,
                                                    Clock clock) {
        return new NotificationEmitter(notificationRepository, notificationIdGenerator, clock);
    }

    @Bean
    public ListUserNotificationsUseCase listUserNotificationsUseCase(
            UserRepository userRepository,
            NotificationRepository notificationRepository) {
        return new ListUserNotificationsUseCase(userRepository, notificationRepository);
    }

    @Bean
    public MarkNotificationAsReadUseCase markNotificationAsReadUseCase(
            NotificationRepository notificationRepository) {
        return new MarkNotificationAsReadUseCase(notificationRepository);
    }

    @Bean
    public NotificationMapper notificationMapper() {
        return new NotificationMapper();
    }
}
