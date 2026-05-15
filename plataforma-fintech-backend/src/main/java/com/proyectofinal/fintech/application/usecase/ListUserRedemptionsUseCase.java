package com.proyectofinal.fintech.application.usecase;

import com.proyectofinal.fintech.application.result.BenefitRedemptionView;
import com.proyectofinal.fintech.domain.exception.ErrorCode;
import com.proyectofinal.fintech.domain.exception.NotFoundException;
import com.proyectofinal.fintech.domain.model.BenefitRedemption;
import com.proyectofinal.fintech.domain.port.BenefitRedemptionRepository;
import com.proyectofinal.fintech.domain.port.UserRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * Use case: list all benefit redemptions for a user.
 * Plain class — ZERO Spring/Jakarta imports.
 * REQ-F1.2.
 */
public class ListUserRedemptionsUseCase {

    private final UserRepository userRepository;
    private final BenefitRedemptionRepository redemptionRepository;

    public ListUserRedemptionsUseCase(UserRepository userRepository,
                                       BenefitRedemptionRepository redemptionRepository) {
        this.userRepository = userRepository;
        this.redemptionRepository = redemptionRepository;
    }

    /**
     * Returns all redemptions for the given user.
     *
     * @param userId the user whose redemptions to list
     * @return list of redemption views (may be empty)
     * @throws NotFoundException if user does not exist
     */
    public List<BenefitRedemptionView> execute(String userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(ErrorCode.USER_NOT_FOUND,
                        "User not found: " + userId));

        List<BenefitRedemptionView> result = new ArrayList<>();
        for (BenefitRedemption r : redemptionRepository.findByUserId(userId)) {
            result.add(new BenefitRedemptionView(
                    r.id(), r.userId(), r.benefitId(), r.pointsSpent(), r.redeemedAt().toString()));
        }
        return result;
    }
}
