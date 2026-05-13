package com.proyectofinal.fintech.application.usecase;

import com.proyectofinal.fintech.application.result.PointsView;
import com.proyectofinal.fintech.domain.exception.ErrorCode;
import com.proyectofinal.fintech.domain.exception.NotFoundException;
import com.proyectofinal.fintech.domain.model.LoyaltyLevel;
import com.proyectofinal.fintech.domain.model.Usuario;
import com.proyectofinal.fintech.domain.port.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * T07-B07 (RED) — GetUserPointsUseCase tests.
 */
@ExtendWith(MockitoExtension.class)
class GetUserPointsUseCaseTest {

    @Mock
    private UserRepository userRepository;

    private GetUserPointsUseCase useCase;

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

    @BeforeEach
    void setUp() {
        useCase = new GetUserPointsUseCase(userRepository);
    }

    @Test
    void execute_userExists_returnsPointsView() {
        Usuario user = new Usuario("USR001", "Ana", "ana@test.com", NOW, 1500.0, LoyaltyLevel.SILVER);
        when(userRepository.findById("USR001")).thenReturn(Optional.of(user));

        PointsView view = useCase.execute("USR001");

        assertThat(view.userId()).isEqualTo("USR001");
        assertThat(view.points()).isEqualTo(1500.0);
        assertThat(view.loyaltyLevel()).isEqualTo(LoyaltyLevel.SILVER);
    }

    @Test
    void execute_userNotFound_throwsNotFoundException() {
        when(userRepository.findById("GHOST")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute("GHOST"))
                .isInstanceOf(NotFoundException.class)
                .satisfies(ex -> assertThat(((NotFoundException) ex).code())
                        .isEqualTo(ErrorCode.USER_NOT_FOUND));
    }
}
