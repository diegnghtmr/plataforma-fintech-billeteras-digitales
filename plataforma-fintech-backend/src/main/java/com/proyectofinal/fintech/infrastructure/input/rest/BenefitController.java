package com.proyectofinal.fintech.infrastructure.input.rest;

import com.proyectofinal.fintech.application.usecase.ListBenefitsUseCase;
import com.proyectofinal.fintech.application.usecase.ListUserRedemptionsUseCase;
import com.proyectofinal.fintech.application.usecase.RedeemBenefitUseCase;
import com.proyectofinal.fintech.infrastructure.input.rest.dto.BenefitRedemptionResponseDto;
import com.proyectofinal.fintech.infrastructure.input.rest.dto.BenefitResponseDto;
import com.proyectofinal.fintech.infrastructure.mapper.BenefitMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST adapter for benefit management endpoints.
 * REQ-F1.1, REQ-F1.2, REQ-F1.3.
 *
 * Endpoints (all relative to servlet context-path /api/v1):
 *   GET  /benefits                                           — list active benefits
 *   POST /users/{userId}/benefits/{benefitId}/redeem         — redeem a benefit
 *   GET  /users/{userId}/benefit-redemptions                 — list redemptions for a user
 */
@RestController
public class BenefitController {

    private final ListBenefitsUseCase listBenefitsUseCase;
    private final RedeemBenefitUseCase redeemBenefitUseCase;
    private final ListUserRedemptionsUseCase listUserRedemptionsUseCase;
    private final BenefitMapper benefitMapper;

    public BenefitController(ListBenefitsUseCase listBenefitsUseCase,
                              RedeemBenefitUseCase redeemBenefitUseCase,
                              ListUserRedemptionsUseCase listUserRedemptionsUseCase,
                              BenefitMapper benefitMapper) {
        this.listBenefitsUseCase = listBenefitsUseCase;
        this.redeemBenefitUseCase = redeemBenefitUseCase;
        this.listUserRedemptionsUseCase = listUserRedemptionsUseCase;
        this.benefitMapper = benefitMapper;
    }

    /** GET /benefits — returns all active benefits */
    @GetMapping("/benefits")
    public ResponseEntity<List<BenefitResponseDto>> getActiveBenefits() {
        List<BenefitResponseDto> result = benefitMapper.toDtoList(listBenefitsUseCase.execute());
        return ResponseEntity.ok(result);
    }

    /** POST /users/{userId}/benefits/{benefitId}/redeem — redeem a benefit */
    @PostMapping("/users/{userId}/benefits/{benefitId}/redeem")
    public ResponseEntity<BenefitRedemptionResponseDto> redeemBenefit(
            @PathVariable String userId,
            @PathVariable String benefitId) {
        BenefitRedemptionResponseDto dto = benefitMapper.toDto(redeemBenefitUseCase.execute(userId, benefitId));
        return ResponseEntity.ok(dto);
    }

    /** GET /users/{userId}/benefit-redemptions — list redemptions for a user */
    @GetMapping("/users/{userId}/benefit-redemptions")
    public ResponseEntity<List<BenefitRedemptionResponseDto>> getUserRedemptions(
            @PathVariable String userId) {
        List<BenefitRedemptionResponseDto> result = benefitMapper.toRedemptionDtoList(
                listUserRedemptionsUseCase.execute(userId));
        return ResponseEntity.ok(result);
    }
}
