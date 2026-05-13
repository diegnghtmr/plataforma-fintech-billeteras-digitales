package com.proyectofinal.fintech.infrastructure.input.rest;

import com.proyectofinal.fintech.application.result.PointsView;
import com.proyectofinal.fintech.application.result.RankingItem;
import com.proyectofinal.fintech.application.usecase.GetPointsRankingUseCase;
import com.proyectofinal.fintech.application.usecase.GetUserPointsUseCase;
import com.proyectofinal.fintech.domain.exception.ErrorCode;
import com.proyectofinal.fintech.domain.exception.NotFoundException;
import com.proyectofinal.fintech.domain.model.LoyaltyLevel;
import com.proyectofinal.fintech.infrastructure.input.rest.exception.GlobalExceptionHandler;
import com.proyectofinal.fintech.infrastructure.mapper.PointsMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * T07-B11 (RED) — PointsController HTTP layer tests.
 */
@WebMvcTest(PointsController.class)
@Import({GlobalExceptionHandler.class, PointsMapper.class})
class PointsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GetUserPointsUseCase getUserPointsUseCase;

    @MockBean
    private GetPointsRankingUseCase getPointsRankingUseCase;

    // Scenario 1: GET /users/u1/points → 200 with PointsResponseDto fields
    @Test
    void getUserPoints_S1_returns200() throws Exception {
        when(getUserPointsUseCase.execute("u1"))
                .thenReturn(new PointsView("u1", 1500.0, LoyaltyLevel.SILVER));

        mockMvc.perform(get("/users/u1/points"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value("u1"))
                .andExpect(jsonPath("$.points").value(1500.0))
                .andExpect(jsonPath("$.loyaltyLevel").value("SILVER"));
    }

    // Scenario 2: GET /users/unknown/points → 404 USER_NOT_FOUND
    @Test
    void getUserPoints_S2_unknownUser_returns404() throws Exception {
        when(getUserPointsUseCase.execute("unknown"))
                .thenThrow(new NotFoundException(ErrorCode.USER_NOT_FOUND, "not found"));

        mockMvc.perform(get("/users/unknown/points"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("USER_NOT_FOUND"));
    }

    // Scenario 3: GET /points/ranking (no param) → 200 list with default
    @Test
    void getRanking_S3_noParam_returns200() throws Exception {
        List<RankingItem> items = List.of(
                new RankingItem(1, "USR001", "Alice", 500.0, LoyaltyLevel.BRONZE),
                new RankingItem(2, "USR002", "Bob", 300.0, LoyaltyLevel.BRONZE)
        );
        when(getPointsRankingUseCase.execute(10)).thenReturn(items);

        mockMvc.perform(get("/points/ranking"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].position").value(1))
                .andExpect(jsonPath("$[0].userId").value("USR001"))
                .andExpect(jsonPath("$[0].loyaltyLevel").value("BRONZE"));
    }

    // Scenario 4: GET /points/ranking?limit=3 → 200 list of 3
    @Test
    void getRanking_S4_limitThree_returns200() throws Exception {
        List<RankingItem> items = List.of(
                new RankingItem(1, "USR001", "Alice", 500.0, LoyaltyLevel.BRONZE),
                new RankingItem(2, "USR002", "Bob", 300.0, LoyaltyLevel.BRONZE),
                new RankingItem(3, "USR003", "Carol", 100.0, LoyaltyLevel.BRONZE)
        );
        when(getPointsRankingUseCase.execute(3)).thenReturn(items);

        mockMvc.perform(get("/points/ranking").param("limit", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3));
    }

    // Scenario 5: GET /points/ranking?limit=0 → 400
    @Test
    void getRanking_S5_limitZero_returns400() throws Exception {
        mockMvc.perform(get("/points/ranking").param("limit", "0"))
                .andExpect(status().isBadRequest());
    }

    // Scenario 6: GET /points/ranking?limit=101 → 400
    @Test
    void getRanking_S6_limitOneHundredOne_returns400() throws Exception {
        mockMvc.perform(get("/points/ranking").param("limit", "101"))
                .andExpect(status().isBadRequest());
    }
}
