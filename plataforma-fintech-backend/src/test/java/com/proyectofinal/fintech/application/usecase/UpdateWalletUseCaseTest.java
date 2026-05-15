package com.proyectofinal.fintech.application.usecase;

import com.proyectofinal.fintech.domain.exception.BusinessRuleException;
import com.proyectofinal.fintech.domain.exception.ErrorCode;
import com.proyectofinal.fintech.domain.exception.NotFoundException;
import com.proyectofinal.fintech.domain.model.Billetera;
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

@ExtendWith(MockitoExtension.class)
class UpdateWalletUseCaseTest {

    @Mock private UserRepository userRepository;
    @Mock private WalletRepository walletRepository;

    private UpdateWalletUseCase useCase;
    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

    @BeforeEach
    void setUp() {
        useCase = new UpdateWalletUseCase(userRepository, walletRepository);
    }

    private Usuario user(String id) {
        return new Usuario(id, "Name", id + "@x.com", NOW, 0.0, LoyaltyLevel.BRONZE);
    }

    private Billetera wallet(double balance, boolean active) {
        return new Billetera("W001", "OldName", "SAVINGS", "USR001", balance, active, NOW, 3);
    }

    @Test
    void execute_renameOnly_persistsNewName() {
        when(userRepository.findById("USR001")).thenReturn(Optional.of(user("USR001")));
        when(walletRepository.findByOwnerIdAndCode("USR001", "W001"))
                .thenReturn(Optional.of(wallet(500.0, true)));

        Billetera result = useCase.execute("USR001", "W001", Optional.of("NewName"), Optional.empty());

        assertThat(result.getName()).isEqualTo("NewName");
        assertThat(result.isActive()).isTrue();
        ArgumentCaptor<Billetera> captor = ArgumentCaptor.forClass(Billetera.class);
        verify(walletRepository).save(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("NewName");
        assertThat(captor.getValue().getBalance()).isEqualTo(500.0);
    }

    @Test
    void execute_closeWithZeroBalance_setsActiveFalse() {
        when(userRepository.findById("USR001")).thenReturn(Optional.of(user("USR001")));
        when(walletRepository.findByOwnerIdAndCode("USR001", "W001"))
                .thenReturn(Optional.of(wallet(0.0, true)));

        Billetera result = useCase.execute("USR001", "W001", Optional.empty(), Optional.of(false));

        assertThat(result.isActive()).isFalse();
    }

    @Test
    void execute_closeWithNonZeroBalance_throwsBusinessRule() {
        when(userRepository.findById("USR001")).thenReturn(Optional.of(user("USR001")));
        when(walletRepository.findByOwnerIdAndCode("USR001", "W001"))
                .thenReturn(Optional.of(wallet(100.0, true)));

        assertThatThrownBy(() ->
                useCase.execute("USR001", "W001", Optional.empty(), Optional.of(false)))
                .isInstanceOf(BusinessRuleException.class)
                .satisfies(ex -> assertThat(((BusinessRuleException) ex).code())
                        .isEqualTo(ErrorCode.VALIDATION_ERROR));

        verify(walletRepository, never()).save(any());
    }

    @Test
    void execute_reopen_alwaysAllowed() {
        when(userRepository.findById("USR001")).thenReturn(Optional.of(user("USR001")));
        when(walletRepository.findByOwnerIdAndCode("USR001", "W001"))
                .thenReturn(Optional.of(wallet(0.0, false)));

        Billetera result = useCase.execute("USR001", "W001", Optional.empty(), Optional.of(true));

        assertThat(result.isActive()).isTrue();
    }

    @Test
    void execute_blankName_throwsBusinessRule() {
        when(userRepository.findById("USR001")).thenReturn(Optional.of(user("USR001")));
        when(walletRepository.findByOwnerIdAndCode("USR001", "W001"))
                .thenReturn(Optional.of(wallet(0.0, true)));

        assertThatThrownBy(() ->
                useCase.execute("USR001", "W001", Optional.of("   "), Optional.empty()))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void execute_unknownUser_throwsNotFound() {
        when(userRepository.findById("GHOST")).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                useCase.execute("GHOST", "W001", Optional.of("X"), Optional.empty()))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void execute_unknownWallet_throwsNotFound() {
        when(userRepository.findById("USR001")).thenReturn(Optional.of(user("USR001")));
        when(walletRepository.findByOwnerIdAndCode("USR001", "GHOST")).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                useCase.execute("USR001", "GHOST", Optional.of("X"), Optional.empty()))
                .isInstanceOf(NotFoundException.class)
                .satisfies(ex -> assertThat(((NotFoundException) ex).code())
                        .isEqualTo(ErrorCode.WALLET_NOT_FOUND));
    }

    @Test
    void execute_preservesTransactionCountAndCreatedAt() {
        when(userRepository.findById("USR001")).thenReturn(Optional.of(user("USR001")));
        when(walletRepository.findByOwnerIdAndCode("USR001", "W001"))
                .thenReturn(Optional.of(wallet(0.0, true)));

        useCase.execute("USR001", "W001", Optional.of("Z"), Optional.of(false));

        ArgumentCaptor<Billetera> captor = ArgumentCaptor.forClass(Billetera.class);
        verify(walletRepository).save(captor.capture());
        assertThat(captor.getValue().getTransactionCount()).isEqualTo(3);
        assertThat(captor.getValue().getCreatedAt()).isEqualTo(NOW);
    }
}
