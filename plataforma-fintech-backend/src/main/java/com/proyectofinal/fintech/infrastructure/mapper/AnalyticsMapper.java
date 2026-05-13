package com.proyectofinal.fintech.infrastructure.mapper;

import com.proyectofinal.fintech.application.result.AnalyticsSummaryView;
import com.proyectofinal.fintech.application.result.MetricItem;
import com.proyectofinal.fintech.application.result.RouteMetric;
import com.proyectofinal.fintech.infrastructure.input.rest.dto.AnalyticsSummaryResponseDto;
import com.proyectofinal.fintech.infrastructure.input.rest.dto.MetricItemResponseDto;
import com.proyectofinal.fintech.infrastructure.input.rest.dto.RouteMetricResponseDto;

/**
 * Maps analytics domain results to response DTOs.
 */
public class AnalyticsMapper {

    public AnalyticsSummaryResponseDto toDto(AnalyticsSummaryView view) {
        return new AnalyticsSummaryResponseDto(
                view.totalUsers(),
                view.totalWallets(),
                view.totalTransactions(),
                view.totalMovedAmount(),
                view.fraudEventCount(),
                view.unreadNotificationCount()
        );
    }

    public MetricItemResponseDto toDto(MetricItem item) {
        return new MetricItemResponseDto(item.id(), item.label(), item.value());
    }

    public RouteMetricResponseDto toDto(RouteMetric route) {
        return new RouteMetricResponseDto(
                route.sourceUserId(),
                route.targetUserId(),
                route.transferCount(),
                route.totalAmount()
        );
    }
}
