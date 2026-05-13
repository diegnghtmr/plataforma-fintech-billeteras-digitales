package com.proyectofinal.fintech.infrastructure.input.rest;

import com.proyectofinal.fintech.application.result.AnalyticsSummaryView;
import com.proyectofinal.fintech.application.result.MetricItem;
import com.proyectofinal.fintech.application.result.RouteMetric;
import com.proyectofinal.fintech.application.usecase.*;
import com.proyectofinal.fintech.domain.model.*;
import com.proyectofinal.fintech.infrastructure.input.rest.dto.TransactionResponseDto;
import com.proyectofinal.fintech.infrastructure.input.rest.exception.GlobalExceptionHandler;
import com.proyectofinal.fintech.infrastructure.mapper.AnalyticsMapper;
import com.proyectofinal.fintech.infrastructure.mapper.TransactionMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * T09-B25 (RED) — AnalyticsController @WebMvcTest.
 */
@WebMvcTest(AnalyticsController.class)
@Import(GlobalExceptionHandler.class)
class AnalyticsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GetAnalyticsSummaryUseCase getAnalyticsSummaryUseCase;
    @MockBean
    private GetTopUsersUseCase getTopUsersUseCase;
    @MockBean
    private GetTopWalletsUseCase getTopWalletsUseCase;
    @MockBean
    private GetFrequentRoutesUseCase getFrequentRoutesUseCase;
    @MockBean
    private GetTopTransactionsUseCase getTopTransactionsUseCase;
    @MockBean
    private GetCyclesUseCase getCyclesUseCase;
    @MockBean
    private GetTopWalletCategoriesUseCase getTopWalletCategoriesUseCase;
    @MockBean
    private GetMovementByTypeUseCase getMovementByTypeUseCase;
    @MockBean
    private GetTotalMovedInRangeUseCase getTotalMovedInRangeUseCase;
    @MockBean
    private AnalyticsMapper analyticsMapper;
    @MockBean
    private TransactionMapper transactionMapper;

    @Test
    void getSummary_returns200WithAllFields() throws Exception {
        AnalyticsSummaryView view = new AnalyticsSummaryView(3, 5, 10, 800.0, 2, 4, 7);
        when(getAnalyticsSummaryUseCase.execute()).thenReturn(view);
        when(analyticsMapper.toDto(view)).thenCallRealMethod();

        mockMvc.perform(get("/analytics/summary"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalUsers").value(3))
                .andExpect(jsonPath("$.totalWallets").value(5))
                .andExpect(jsonPath("$.totalTransactions").value(10))
                .andExpect(jsonPath("$.totalMovedAmount").value(800.0))
                .andExpect(jsonPath("$.fraudEventCount").value(2))
                .andExpect(jsonPath("$.unreadNotificationCount").value(4))
                .andExpect(jsonPath("$.pendingScheduledOperations").value(7));
    }

    @Test
    void getTopUsers_defaultLimit10_returns200() throws Exception {
        when(getTopUsersUseCase.execute(10)).thenReturn(List.of(
                new MetricItem("USR_A", "Ana", 5.0)
        ));
        when(analyticsMapper.toDto(new MetricItem("USR_A", "Ana", 5.0))).thenCallRealMethod();

        mockMvc.perform(get("/analytics/top-users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("USR_A"));
    }

    @Test
    void getTopUsers_limitZero_returns400() throws Exception {
        mockMvc.perform(get("/analytics/top-users?limit=0"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getTopUsers_limit101_returns400() throws Exception {
        mockMvc.perform(get("/analytics/top-users?limit=101"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getTopWallets_defaultLimit10_returns200() throws Exception {
        when(getTopWalletsUseCase.execute(10)).thenReturn(List.of());

        mockMvc.perform(get("/analytics/top-wallets"))
                .andExpect(status().isOk());
    }

    @Test
    void getFrequentRoutes_defaultMinTransfers1_returns200() throws Exception {
        when(getFrequentRoutesUseCase.execute(1)).thenReturn(List.of(
                new RouteMetric("A", "B", 3, 900.0)
        ));
        when(analyticsMapper.toDto(new RouteMetric("A", "B", 3, 900.0))).thenCallRealMethod();

        mockMvc.perform(get("/analytics/frequent-routes"))
                .andExpect(status().isOk());
    }

    @Test
    void getFrequentRoutes_minTransfers0_returns400() throws Exception {
        mockMvc.perform(get("/analytics/frequent-routes?minTransfers=0"))
                .andExpect(status().isBadRequest());
    }

    // T11-G03: GET /analytics/top-transactions
    @Test
    void getTopTransactions_default10_returns200() throws Exception {
        Transaccion tx = new Transaccion("TX-001", Instant.parse("2026-01-01T00:00:00Z"),
                TransactionType.RECHARGE, 500.0, "W001", null, "USR001", null,
                TransactionStatus.SUCCESSFUL, 5.0, null, true);
        TransactionResponseDto dto = new TransactionResponseDto("TX-001",
                "2026-01-01T00:00:00Z", "RECHARGE", 500.0, "W001", null,
                "USR001", null, "SUCCESSFUL", 5.0, null, true, "LOW");

        when(getTopTransactionsUseCase.execute(10)).thenReturn(List.of(tx));
        when(transactionMapper.toDto(tx)).thenReturn(dto);

        mockMvc.perform(get("/analytics/top-transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("TX-001"));
    }

    @Test
    void getTopTransactions_limit0_returns400() throws Exception {
        mockMvc.perform(get("/analytics/top-transactions?limit=0"))
                .andExpect(status().isBadRequest());
    }

    // T11-H04: GET /analytics/cycles
    @Test
    void getCycles_returns200() throws Exception {
        when(getCyclesUseCase.execute()).thenReturn(List.of(List.of("A", "B")));

        mockMvc.perform(get("/analytics/cycles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0][0]").value("A"));
    }

    // T11-I02: GET /analytics/top-wallet-categories
    @Test
    void getTopWalletCategories_returns200() throws Exception {
        when(getTopWalletCategoriesUseCase.execute(10)).thenReturn(List.of(
                new MetricItem("SAVINGS", "SAVINGS", 5.0)
        ));
        when(analyticsMapper.toDto(new MetricItem("SAVINGS", "SAVINGS", 5.0))).thenCallRealMethod();

        mockMvc.perform(get("/analytics/top-wallet-categories"))
                .andExpect(status().isOk());
    }

    // T11-I04: GET /analytics/movement-by-type
    @Test
    void getMovementByType_returns200() throws Exception {
        when(getMovementByTypeUseCase.execute()).thenReturn(List.of(
                new MetricItem("RECHARGE", "RECHARGE", 3.0)
        ));
        when(analyticsMapper.toDto(new MetricItem("RECHARGE", "RECHARGE", 3.0))).thenCallRealMethod();

        mockMvc.perform(get("/analytics/movement-by-type"))
                .andExpect(status().isOk());
    }

    // T11-I06: GET /analytics/total-moved
    @Test
    void getTotalMoved_validRange_returns200() throws Exception {
        com.proyectofinal.fintech.infrastructure.input.rest.dto.RangeTotalResponseDto dto =
                new com.proyectofinal.fintech.infrastructure.input.rest.dto.RangeTotalResponseDto(
                        1500.0, 3, "2026-01-01T00:00:00Z", "2026-12-31T00:00:00Z");

        when(getTotalMovedInRangeUseCase.execute(
                Instant.parse("2026-01-01T00:00:00Z"),
                Instant.parse("2026-12-31T00:00:00Z"))).thenReturn(dto);

        mockMvc.perform(get("/analytics/total-moved?from=2026-01-01T00:00:00Z&to=2026-12-31T00:00:00Z"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAmount").value(1500.0))
                .andExpect(jsonPath("$.count").value(3));
    }
}
