package com.proyectofinal.fintech.infrastructure.mapper;

import com.proyectofinal.fintech.application.result.AnalyticsSummaryView;
import com.proyectofinal.fintech.application.result.MetricItem;
import com.proyectofinal.fintech.application.result.RouteMetric;
import com.proyectofinal.fintech.infrastructure.input.rest.dto.AnalyticsSummaryResponseDto;
import com.proyectofinal.fintech.infrastructure.input.rest.dto.MetricItemResponseDto;
import com.proyectofinal.fintech.infrastructure.input.rest.dto.RouteMetricResponseDto;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * T09-B23 (RED) — AnalyticsMapper tests.
 */
class AnalyticsMapperTest {

    private final AnalyticsMapper mapper = new AnalyticsMapper();

    @Test
    void toDto_analyticsSummaryView_mapsAllFields() {
        AnalyticsSummaryView view = new AnalyticsSummaryView(3, 5, 10, 800.0, 2, 4);
        AnalyticsSummaryResponseDto dto = mapper.toDto(view);

        assertThat(dto.totalUsers()).isEqualTo(3);
        assertThat(dto.totalWallets()).isEqualTo(5);
        assertThat(dto.totalTransactions()).isEqualTo(10);
        assertThat(dto.totalMovedAmount()).isEqualTo(800.0);
        assertThat(dto.fraudEventCount()).isEqualTo(2);
        assertThat(dto.unreadNotificationCount()).isEqualTo(4);
    }

    @Test
    void toDto_metricItem_mapsAllFields() {
        MetricItem item = new MetricItem("USR_A", "Ana", 5.0);
        MetricItemResponseDto dto = mapper.toDto(item);

        assertThat(dto.id()).isEqualTo("USR_A");
        assertThat(dto.label()).isEqualTo("Ana");
        assertThat(dto.value()).isEqualTo(5.0);
    }

    @Test
    void toDto_routeMetric_mapsAllFields() {
        RouteMetric route = new RouteMetric("USR_A", "USR_B", 3, 900.0);
        RouteMetricResponseDto dto = mapper.toDto(route);

        assertThat(dto.sourceUserId()).isEqualTo("USR_A");
        assertThat(dto.targetUserId()).isEqualTo("USR_B");
        assertThat(dto.transferCount()).isEqualTo(3);
        assertThat(dto.totalAmount()).isEqualTo(900.0);
    }
}
