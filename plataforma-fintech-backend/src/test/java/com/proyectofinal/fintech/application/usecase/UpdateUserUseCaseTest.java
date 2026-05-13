package com.proyectofinal.fintech.application.usecase;

import com.proyectofinal.fintech.application.result.UserView;
import com.proyectofinal.fintech.domain.exception.ErrorCode;
import com.proyectofinal.fintech.domain.exception.NotFoundException;
import com.proyectofinal.fintech.domain.model.LoyaltyLevel;
import com.proyectofinal.fintech.domain.model.Usuario;
import com.proyectofinal.fintech.domain.port.UserRepository;
import com.proyectofinal.fintech.domain.port.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * T11-E01 (RED) — UpdateUserUseCase tests.
 */
@ExtendWith(MockitoExtension.class)
class UpdateUserUseCaseTest {

    @Mock private UserRepository userRepository;
    @Mock private WalletRepository walletRepository;

    private UpdateUserUseCase useCase;

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

    @BeforeEach
    void setUp() {
        useCase = new UpdateUserUseCase(userRepository, walletRepository);
    }

    // S-E01: update name only
    @Test
    void execute_updateNameOnly_persistsNewName() {
        Usuario existing = new Usuario("USR001", "Ana", "ana@test.com", NOW, 100.0, LoyaltyLevel.BRONZE);
        when(userRepository.findById("USR001")).thenReturn(Optional.of(existing));
        when(walletRepository.countByOwnerId("USR001")).thenReturn(0);
        when(walletRepository.sumBalanceByOwnerId("USR001")).thenReturn(0.0);

        UserView result = useCase.execute("USR001", Optional.of("AnaUpdated"), Optional.empty());

        assertThat(result.name()).isEqualTo("AnaUpdated");
        assertThat(result.email()).isEqualTo("ana@test.com");

        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("AnaUpdated");
        assertThat(captor.getValue().getEmail()).isEqualTo("ana@test.com");
    }

    // S-E02: update email only
    @Test
    void execute_updateEmailOnly_persistsNewEmail() {
        Usuario existing = new Usuario("USR001", "Ana", "ana@test.com", NOW, 0.0, LoyaltyLevel.BRONZE);
        when(userRepository.findById("USR001")).thenReturn(Optional.of(existing));
        when(walletRepository.countByOwnerId("USR001")).thenReturn(0);
        when(walletRepository.sumBalanceByOwnerId("USR001")).thenReturn(0.0);

        UserView result = useCase.execute("USR001", Optional.empty(), Optional.of("new@email.com"));

        assertThat(result.email()).isEqualTo("new@email.com");
        assertThat(result.name()).isEqualTo("Ana");
    }

    // S-E03: update both
    @Test
    void execute_updateBoth_persistsBoth() {
        Usuario existing = new Usuario("USR001", "Ana", "ana@test.com", NOW, 0.0, LoyaltyLevel.BRONZE);
        when(userRepository.findById("USR001")).thenReturn(Optional.of(existing));
        when(walletRepository.countByOwnerId("USR001")).thenReturn(0);
        when(walletRepository.sumBalanceByOwnerId("USR001")).thenReturn(0.0);

        UserView result = useCase.execute("USR001", Optional.of("Bob"), Optional.of("bob@test.com"));

        assertThat(result.name()).isEqualTo("Bob");
        assertThat(result.email()).isEqualTo("bob@test.com");
    }

    // S-E04: unknown user → NotFoundException
    @Test
    void execute_unknownUser_throwsUserNotFound() {
        when(userRepository.findById("GHOST")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute("GHOST", Optional.of("X"), Optional.empty()))
                .isInstanceOf(NotFoundException.class)
                .satisfies(ex -> assertThat(((NotFoundException) ex).code())
                        .isEqualTo(ErrorCode.USER_NOT_FOUND));

        verify(userRepository, never()).save(any());
    }

    // S-E05: both empty → still saves (no-op update)
    @Test
    void execute_bothEmpty_noChangeButSavesCalled() {
        Usuario existing = new Usuario("USR001", "Ana", "ana@test.com", NOW, 0.0, LoyaltyLevel.BRONZE);
        when(userRepository.findById("USR001")).thenReturn(Optional.of(existing));
        when(walletRepository.countByOwnerId("USR001")).thenReturn(0);
        when(walletRepository.sumBalanceByOwnerId("USR001")).thenReturn(0.0);

        UserView result = useCase.execute("USR001", Optional.empty(), Optional.empty());

        assertThat(result.name()).isEqualTo("Ana");
        assertThat(result.email()).isEqualTo("ana@test.com");
        verify(userRepository).save(any());
    }
}
