package com.proyectofinal.fintech.application.usecase;

import com.proyectofinal.fintech.application.result.BenefitRedemptionView;
import com.proyectofinal.fintech.application.service.NotificationEmitter;
import com.proyectofinal.fintech.domain.exception.BusinessRuleException;
import com.proyectofinal.fintech.domain.exception.ErrorCode;
import com.proyectofinal.fintech.domain.exception.NotFoundException;
import com.proyectofinal.fintech.domain.model.*;
import com.proyectofinal.fintech.domain.port.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * F-39 (RED) — RedeemBenefitUseCase tests.
 * REQ-F1.2, REQ-F1.3, REQ-F1.4, REQ-F1.5.
 */
@ExtendWith(MockitoExtension.class)
class RedeemBenefitUseCaseTest {

    @Mock private UserRepository userRepository;
    @Mock private BeneficioRepository beneficioRepository;
    @Mock private BenefitRedemptionRepository redemptionRepository;
    @Mock private BenefitRedemptionIdGenerator redemptionIdGenerator;
    @Mock private NotificationEmitter notificationEmitter;

    private RedeemBenefitUseCase useCase;
    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

    @BeforeEach
    void setUp() {
        useCase = new RedeemBenefitUseCase(userRepository, beneficioRepository,
                redemptionRepository, redemptionIdGenerator, notificationEmitter);
    }

    private Usuario makeUser(String id, double points) {
        return new Usuario(id, "Ana", "ana@test.com", NOW, points, LoyaltyLevel.BRONZE);
    }

    private Beneficio makeBenefit(String id, int cost, boolean active) {
        return new Beneficio(id, "Test Benefit", "desc", cost, active);
    }

    @Test
    void redeem_happyPath_deductsPointsPersistsRedemptionEmitsNotification() {
        Usuario user = makeUser("USR001", 500.0);
        Beneficio benefit = makeBenefit("BEN-001", 200, true);
        when(userRepository.findById("USR001")).thenReturn(Optional.of(user));
        when(beneficioRepository.findById("BEN-001")).thenReturn(Optional.of(benefit));
        when(redemptionIdGenerator.next()).thenReturn("RED-000001");

        BenefitRedemptionView result = useCase.execute("USR001", "BEN-001");

        assertThat(result.userId()).isEqualTo("USR001");
        assertThat(result.benefitId()).isEqualTo("BEN-001");
        assertThat(result.pointsSpent()).isEqualTo(200);
        assertThat(user.getPoints()).isEqualTo(300.0);
        verify(redemptionRepository).save(any(BenefitRedemption.class));
        verify(userRepository).save(user);
        verify(notificationEmitter).emitBenefitRedeemed("USR001", "BEN-001", "Test Benefit", 200);
    }

    @Test
    void redeem_insufficientPoints_throwsBusinessRuleException() {
        Usuario user = makeUser("USR001", 50.0);
        Beneficio benefit = makeBenefit("BEN-001", 200, true);
        when(userRepository.findById("USR001")).thenReturn(Optional.of(user));
        when(beneficioRepository.findById("BEN-001")).thenReturn(Optional.of(benefit));

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> useCase.execute("USR001", "BEN-001"));
        assertThat(ex.code()).isEqualTo(ErrorCode.INSUFFICIENT_POINTS);
    }

    @Test
    void redeem_benefitNotFound_throwsNotFoundException() {
        when(userRepository.findById("USR001")).thenReturn(Optional.of(makeUser("USR001", 500.0)));
        when(beneficioRepository.findById("BEN-999")).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> useCase.execute("USR001", "BEN-999"));
        assertThat(ex.code()).isEqualTo(ErrorCode.BENEFIT_NOT_FOUND);
    }

    @Test
    void redeem_userNotFound_throwsNotFoundException() {
        when(userRepository.findById("GHOST")).thenReturn(Optional.empty());

        NotFoundException ex = assertThrows(NotFoundException.class,
                () -> useCase.execute("GHOST", "BEN-001"));
        assertThat(ex.code()).isEqualTo(ErrorCode.USER_NOT_FOUND);
    }

    @Test
    void redeem_inactiveBenefit_throwsBusinessRuleException() {
        Usuario user = makeUser("USR001", 500.0);
        Beneficio benefit = makeBenefit("BEN-001", 200, false);
        when(userRepository.findById("USR001")).thenReturn(Optional.of(user));
        when(beneficioRepository.findById("BEN-001")).thenReturn(Optional.of(benefit));

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> useCase.execute("USR001", "BEN-001"));
        assertThat(ex.code()).isEqualTo(ErrorCode.VALIDATION_ERROR);
    }

    @Test
    void redeem_emitsBenefitRedeemedExactlyOnce() {
        Usuario user = makeUser("USR001", 500.0);
        Beneficio benefit = makeBenefit("BEN-001", 100, true);
        when(userRepository.findById("USR001")).thenReturn(Optional.of(user));
        when(beneficioRepository.findById("BEN-001")).thenReturn(Optional.of(benefit));
        when(redemptionIdGenerator.next()).thenReturn("RED-000001");

        useCase.execute("USR001", "BEN-001");

        verify(notificationEmitter, times(1)).emitBenefitRedeemed("USR001", "BEN-001", "Test Benefit", 100);
    }
}
