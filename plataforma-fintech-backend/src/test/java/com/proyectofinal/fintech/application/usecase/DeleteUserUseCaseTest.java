package com.proyectofinal.fintech.application.usecase;

import com.proyectofinal.fintech.domain.exception.ErrorCode;
import com.proyectofinal.fintech.domain.exception.NotFoundException;
import com.proyectofinal.fintech.domain.model.LoyaltyLevel;
import com.proyectofinal.fintech.domain.model.Usuario;
import com.proyectofinal.fintech.domain.port.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * T11-E06 (RED) — DeleteUserUseCase tests.
 * Cascade deletion order: tx → wallet → scheduled → notification → fraud → user.
 */
@ExtendWith(MockitoExtension.class)
class DeleteUserUseCaseTest {

    @Mock private UserRepository userRepository;
    @Mock private WalletRepository walletRepository;
    @Mock private TransactionRepository transactionRepository;
    @Mock private ScheduledOperationRepository scheduledOperationRepository;
    @Mock private NotificationRepository notificationRepository;
    @Mock private FraudEventRepository fraudEventRepository;

    private DeleteUserUseCase useCase;

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

    @BeforeEach
    void setUp() {
        useCase = new DeleteUserUseCase(
                userRepository, walletRepository, transactionRepository,
                scheduledOperationRepository, notificationRepository, fraudEventRepository
        );
    }

    // S-E06-01: cascade deletes all 6 repos
    @Test
    void execute_existingUser_cascadeDeletesAllRepos() {
        Usuario user = new Usuario("USR001", "Ana", "ana@test.com", NOW, 0.0, LoyaltyLevel.BRONZE);
        when(userRepository.findById("USR001")).thenReturn(Optional.of(user));

        useCase.execute("USR001");

        verify(transactionRepository).deleteByUserId("USR001");
        verify(walletRepository).deleteByOwnerId("USR001");
        verify(scheduledOperationRepository).deleteBySourceUserId("USR001");
        verify(notificationRepository).deleteByUserId("USR001");
        verify(fraudEventRepository).deleteByUserId("USR001");
        verify(userRepository).deleteById("USR001");
    }

    // S-E06-02: cascade order is tx → wallet → scheduled → notification → fraud → user
    @Test
    void execute_existingUser_deletesInCorrectOrder() {
        Usuario user = new Usuario("USR001", "Ana", "ana@test.com", NOW, 0.0, LoyaltyLevel.BRONZE);
        when(userRepository.findById("USR001")).thenReturn(Optional.of(user));

        InOrder order = inOrder(
                transactionRepository, walletRepository, scheduledOperationRepository,
                notificationRepository, fraudEventRepository, userRepository
        );

        useCase.execute("USR001");

        order.verify(transactionRepository).deleteByUserId("USR001");
        order.verify(walletRepository).deleteByOwnerId("USR001");
        order.verify(scheduledOperationRepository).deleteBySourceUserId("USR001");
        order.verify(notificationRepository).deleteByUserId("USR001");
        order.verify(fraudEventRepository).deleteByUserId("USR001");
        order.verify(userRepository).deleteById("USR001");
    }

    // S-E06-03: unknown user → NotFoundException; no deletes called
    @Test
    void execute_unknownUser_throwsUserNotFound() {
        when(userRepository.findById("GHOST")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute("GHOST"))
                .isInstanceOf(NotFoundException.class)
                .satisfies(ex -> assertThat(((NotFoundException) ex).code())
                        .isEqualTo(ErrorCode.USER_NOT_FOUND));

        verify(transactionRepository, never()).deleteByUserId(any());
        verify(walletRepository, never()).deleteByOwnerId(any());
        verify(userRepository, never()).deleteById(any());
    }
}
