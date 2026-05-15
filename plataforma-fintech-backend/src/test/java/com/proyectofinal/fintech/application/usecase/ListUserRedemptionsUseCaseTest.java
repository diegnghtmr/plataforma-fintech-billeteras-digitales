package com.proyectofinal.fintech.application.usecase;

import com.proyectofinal.fintech.application.result.BenefitRedemptionView;
import com.proyectofinal.fintech.domain.exception.ErrorCode;
import com.proyectofinal.fintech.domain.exception.NotFoundException;
import com.proyectofinal.fintech.domain.model.BenefitRedemption;
import com.proyectofinal.fintech.domain.model.LoyaltyLevel;
import com.proyectofinal.fintech.domain.model.Usuario;
import com.proyectofinal.fintech.domain.port.BenefitRedemptionRepository;
import com.proyectofinal.fintech.domain.port.UserRepository;
import com.proyectofinal.fintech.domain.structures.MiLista;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

/**
 * F-41 (RED) — ListUserRedemptionsUseCase tests.
 * REQ-F1.2.
 */
@ExtendWith(MockitoExtension.class)
class ListUserRedemptionsUseCaseTest {

    @Mock private UserRepository userRepository;
    @Mock private BenefitRedemptionRepository redemptionRepository;

    private ListUserRedemptionsUseCase useCase;
    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

    @BeforeEach
    void setUp() {
        useCase = new ListUserRedemptionsUseCase(userRepository, redemptionRepository);
    }

    @Test
    void execute_happyPath_returnsRedemptions() {
        Usuario user = new Usuario("USR001", "Ana", "ana@test.com", NOW, 300.0, LoyaltyLevel.BRONZE);
        when(userRepository.findById("USR001")).thenReturn(Optional.of(user));

        MiLista<BenefitRedemption> redemptions = new MiLista<>();
        redemptions.add(new BenefitRedemption("RED-001", "USR001", "BEN-001", 200, NOW));
        when(redemptionRepository.findByUserId("USR001")).thenReturn(redemptions);

        List<BenefitRedemptionView> result = useCase.execute("USR001");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).id()).isEqualTo("RED-001");
        assertThat(result.get(0).pointsSpent()).isEqualTo(200);
    }

    @Test
    void execute_unknownUser_throwsNotFoundException() {
        when(userRepository.findById("GHOST")).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> useCase.execute("GHOST"));
        assertThat(ex.code()).isEqualTo(ErrorCode.USER_NOT_FOUND);
    }
}
