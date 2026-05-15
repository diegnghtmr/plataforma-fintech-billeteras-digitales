package com.proyectofinal.fintech.application.usecase;

import com.proyectofinal.fintech.application.result.BenefitRedemptionView;
import com.proyectofinal.fintech.application.service.NotificationEmitter;
import com.proyectofinal.fintech.domain.exception.BusinessRuleException;
import com.proyectofinal.fintech.domain.exception.ErrorCode;
import com.proyectofinal.fintech.domain.exception.NotFoundException;
import com.proyectofinal.fintech.domain.model.Beneficio;
import com.proyectofinal.fintech.domain.model.BenefitRedemption;
import com.proyectofinal.fintech.domain.model.Usuario;
import com.proyectofinal.fintech.domain.port.BeneficioRepository;
import com.proyectofinal.fintech.domain.port.BenefitRedemptionIdGenerator;
import com.proyectofinal.fintech.domain.port.BenefitRedemptionRepository;
import com.proyectofinal.fintech.domain.port.UserRepository;

import java.time.Instant;

/**
 * Use case: redeem a benefit, deducting points from the user.
 * Plain class — ZERO Spring/Jakarta imports.
 * REQ-F1.2, REQ-F1.3, REQ-F1.4, REQ-F1.5.
 *
 * Steps:
 * 1. Find user — throw USER_NOT_FOUND if absent.
 * 2. Find benefit — throw BENEFIT_NOT_FOUND if absent.
 * 3. Validate benefit is active — throw VALIDATION_ERROR if inactive.
 * 4. Call user.spendPoints(cost) — propagates insufficient_points as BusinessRuleException.
 * 5. Build and persist BenefitRedemption.
 * 6. Persist updated user.
 * 7. Emit BENEFIT_REDEEMED notification.
 */
public class RedeemBenefitUseCase {

    private final UserRepository userRepository;
    private final BeneficioRepository beneficioRepository;
    private final BenefitRedemptionRepository redemptionRepository;
    private final BenefitRedemptionIdGenerator redemptionIdGenerator;
    private final NotificationEmitter notificationEmitter;

    public RedeemBenefitUseCase(UserRepository userRepository,
                                  BeneficioRepository beneficioRepository,
                                  BenefitRedemptionRepository redemptionRepository,
                                  BenefitRedemptionIdGenerator redemptionIdGenerator,
                                  NotificationEmitter notificationEmitter) {
        this.userRepository = userRepository;
        this.beneficioRepository = beneficioRepository;
        this.redemptionRepository = redemptionRepository;
        this.redemptionIdGenerator = redemptionIdGenerator;
        this.notificationEmitter = notificationEmitter;
    }

    /**
     * Executes benefit redemption for the given user and benefit.
     *
     * @param userId    the user redeeming the benefit
     * @param benefitId the benefit to redeem
     * @return a view of the created redemption record
     */
    public BenefitRedemptionView execute(String userId, String benefitId) {
        // Step 1: find user
        Usuario user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND,
                        "User not found: " + userId));

        // Step 2: find benefit
        Beneficio benefit = beneficioRepository.findById(benefitId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.BENEFIT_NOT_FOUND,
                        "Benefit not found: " + benefitId));

        // Step 3: validate active
        if (!benefit.isActive()) {
            throw new BusinessRuleException(ErrorCode.VALIDATION_ERROR,
                    "Benefit is inactive: " + benefitId);
        }

        // Step 4: deduct points (may throw insufficient_points)
        try {
            user.spendPoints(benefit.getPointsCost());
        } catch (IllegalArgumentException e) {
            if ("insufficient_points".equals(e.getMessage())) {
                throw new BusinessRuleException(ErrorCode.INSUFFICIENT_POINTS,
                        "Insufficient points to redeem benefit: " + benefitId);
            }
            throw e;
        }

        // Step 5: persist redemption
        BenefitRedemption redemption = new BenefitRedemption(
                redemptionIdGenerator.next(), userId, benefitId,
                benefit.getPointsCost(), Instant.now());
        redemptionRepository.save(redemption);

        // Step 6: persist updated user
        userRepository.save(user);

        // Step 7: emit notification
        notificationEmitter.emitBenefitRedeemed(userId, benefitId, benefit.getName(), benefit.getPointsCost());

        return new BenefitRedemptionView(
                redemption.id(), redemption.userId(), redemption.benefitId(),
                redemption.pointsSpent(), redemption.redeemedAt().toString());
    }
}
