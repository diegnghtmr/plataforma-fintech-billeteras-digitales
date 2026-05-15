package com.proyectofinal.fintech.infrastructure.input.rest;

import com.proyectofinal.fintech.application.result.BenefitRedemptionView;
import com.proyectofinal.fintech.application.result.BenefitView;
import com.proyectofinal.fintech.infrastructure.input.rest.dto.BenefitRedemptionResponseDto;
import com.proyectofinal.fintech.application.usecase.ListBenefitsUseCase;
import com.proyectofinal.fintech.application.usecase.ListUserRedemptionsUseCase;
import com.proyectofinal.fintech.application.usecase.RedeemBenefitUseCase;
import com.proyectofinal.fintech.domain.exception.BusinessRuleException;
import com.proyectofinal.fintech.domain.exception.ErrorCode;
import com.proyectofinal.fintech.domain.exception.NotFoundException;
import com.proyectofinal.fintech.infrastructure.input.rest.exception.GlobalExceptionHandler;
import com.proyectofinal.fintech.infrastructure.mapper.BenefitMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * BenefitController @WebMvcTest.
 * REQ-F1.1, REQ-F1.2, REQ-F1.3.
 *
 * Endpoints under test:
 *   GET  /api/v1/benefits
 *   POST /api/v1/users/{userId}/benefits/{benefitId}/redeem
 *   GET  /api/v1/users/{userId}/benefit-redemptions
 */
@WebMvcTest(BenefitController.class)
@Import(GlobalExceptionHandler.class)
class BenefitControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ListBenefitsUseCase listBenefitsUseCase;
    @MockBean
    private RedeemBenefitUseCase redeemBenefitUseCase;
    @MockBean
    private ListUserRedemptionsUseCase listUserRedemptionsUseCase;
    @MockBean
    private BenefitMapper benefitMapper;

    @Test
    void getActiveBenefits_returns200WithJsonArray() throws Exception {
        BenefitView view = new BenefitView("BEN-001", "Cashback 1%", "desc", 100, true);
        com.proyectofinal.fintech.infrastructure.input.rest.dto.BenefitResponseDto dto =
                new com.proyectofinal.fintech.infrastructure.input.rest.dto.BenefitResponseDto(
                        "BEN-001", "Cashback 1%", "desc", 100, true);
        when(listBenefitsUseCase.execute()).thenReturn(List.of(view));
        when(benefitMapper.toDtoList(List.of(view))).thenReturn(List.of(dto));

        mockMvc.perform(get("/benefits"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("BEN-001"))
                .andExpect(jsonPath("$[0].pointsCost").value(100));
    }

    @Test
    void redeemBenefit_happyPath_returns200WithRedemptionDto() throws Exception {
        BenefitRedemptionView view = new BenefitRedemptionView(
                "RED-001", "USR001", "BEN-001", 100, "2026-01-01T00:00:00Z");
        BenefitRedemptionResponseDto dto = new com.proyectofinal.fintech.infrastructure.input.rest.dto.BenefitRedemptionResponseDto(
                "RED-001", "USR001", "BEN-001", 100, "2026-01-01T00:00:00Z");
        when(redeemBenefitUseCase.execute("USR001", "BEN-001")).thenReturn(view);
        when(benefitMapper.toDto(view)).thenReturn(dto);

        mockMvc.perform(post("/users/USR001/benefits/BEN-001/redeem"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("RED-001"))
                .andExpect(jsonPath("$.pointsSpent").value(100));
    }

    @Test
    void redeemBenefit_insufficientPoints_returns400() throws Exception {
        when(redeemBenefitUseCase.execute("USR001", "BEN-001"))
                .thenThrow(new BusinessRuleException(ErrorCode.INSUFFICIENT_POINTS, "Not enough points"));

        mockMvc.perform(post("/users/USR001/benefits/BEN-001/redeem"))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void redeemBenefit_benefitNotFound_returns404() throws Exception {
        when(redeemBenefitUseCase.execute("USR001", "BEN-999"))
                .thenThrow(new NotFoundException(ErrorCode.BENEFIT_NOT_FOUND, "Not found"));

        mockMvc.perform(post("/users/USR001/benefits/BEN-999/redeem"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getUserRedemptions_returns200WithJsonArray() throws Exception {
        BenefitRedemptionView view = new BenefitRedemptionView(
                "RED-001", "USR001", "BEN-001", 100, "2026-01-01T00:00:00Z");
        BenefitRedemptionResponseDto dto = new BenefitRedemptionResponseDto(
                "RED-001", "USR001", "BEN-001", 100, "2026-01-01T00:00:00Z");
        when(listUserRedemptionsUseCase.execute("USR001")).thenReturn(List.of(view));
        when(benefitMapper.toRedemptionDtoList(List.of(view))).thenReturn(List.of(dto));

        mockMvc.perform(get("/users/USR001/benefit-redemptions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("RED-001"));
    }
}
