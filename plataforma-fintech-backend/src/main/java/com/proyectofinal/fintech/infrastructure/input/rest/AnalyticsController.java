package com.proyectofinal.fintech.infrastructure.input.rest;

import com.proyectofinal.fintech.application.result.MetricItem;
import com.proyectofinal.fintech.application.result.RangeTotalView;
import com.proyectofinal.fintech.application.usecase.*;
import com.proyectofinal.fintech.domain.model.Transaccion;
import com.proyectofinal.fintech.infrastructure.input.rest.dto.*;
import com.proyectofinal.fintech.infrastructure.mapper.AnalyticsMapper;
import com.proyectofinal.fintech.infrastructure.mapper.TransactionMapper;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * REST adapter for analytics endpoints.
 * @Validated required on class for @Min/@Max on @RequestParam to fire.
 */
@RestController
@Validated
public class AnalyticsController {

    private final GetAnalyticsSummaryUseCase getAnalyticsSummaryUseCase;
    private final GetTopUsersUseCase getTopUsersUseCase;
    private final GetTopWalletsUseCase getTopWalletsUseCase;
    private final GetFrequentRoutesUseCase getFrequentRoutesUseCase;
    private final GetTopTransactionsUseCase getTopTransactionsUseCase;
    private final GetCyclesUseCase getCyclesUseCase;
    private final GetTopWalletCategoriesUseCase getTopWalletCategoriesUseCase;
    private final GetMovementByTypeUseCase getMovementByTypeUseCase;
    private final GetTotalMovedInRangeUseCase getTotalMovedInRangeUseCase;
    private final AnalyticsMapper analyticsMapper;
    private final TransactionMapper transactionMapper;

    public AnalyticsController(GetAnalyticsSummaryUseCase getAnalyticsSummaryUseCase,
                                GetTopUsersUseCase getTopUsersUseCase,
                                GetTopWalletsUseCase getTopWalletsUseCase,
                                GetFrequentRoutesUseCase getFrequentRoutesUseCase,
                                GetTopTransactionsUseCase getTopTransactionsUseCase,
                                GetCyclesUseCase getCyclesUseCase,
                                GetTopWalletCategoriesUseCase getTopWalletCategoriesUseCase,
                                GetMovementByTypeUseCase getMovementByTypeUseCase,
                                GetTotalMovedInRangeUseCase getTotalMovedInRangeUseCase,
                                AnalyticsMapper analyticsMapper,
                                TransactionMapper transactionMapper) {
        this.getAnalyticsSummaryUseCase = getAnalyticsSummaryUseCase;
        this.getTopUsersUseCase = getTopUsersUseCase;
        this.getTopWalletsUseCase = getTopWalletsUseCase;
        this.getFrequentRoutesUseCase = getFrequentRoutesUseCase;
        this.getTopTransactionsUseCase = getTopTransactionsUseCase;
        this.getCyclesUseCase = getCyclesUseCase;
        this.getTopWalletCategoriesUseCase = getTopWalletCategoriesUseCase;
        this.getMovementByTypeUseCase = getMovementByTypeUseCase;
        this.getTotalMovedInRangeUseCase = getTotalMovedInRangeUseCase;
        this.analyticsMapper = analyticsMapper;
        this.transactionMapper = transactionMapper;
    }

    /** GET /analytics/summary */
    @GetMapping("/analytics/summary")
    public ResponseEntity<AnalyticsSummaryResponseDto> getSummary() {
        return ResponseEntity.ok(analyticsMapper.toDto(getAnalyticsSummaryUseCase.execute()));
    }

    /** GET /analytics/top-users?limit=10 (1..100) */
    @GetMapping("/analytics/top-users")
    public ResponseEntity<List<MetricItemResponseDto>> getTopUsers(
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int limit) {
        List<MetricItemResponseDto> result = getTopUsersUseCase.execute(limit)
                .stream().map(analyticsMapper::toDto).toList();
        return ResponseEntity.ok(result);
    }

    /** GET /analytics/top-wallets?limit=10 (1..100) */
    @GetMapping("/analytics/top-wallets")
    public ResponseEntity<List<MetricItemResponseDto>> getTopWallets(
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int limit) {
        List<MetricItemResponseDto> result = getTopWalletsUseCase.execute(limit)
                .stream().map(analyticsMapper::toDto).toList();
        return ResponseEntity.ok(result);
    }

    /** GET /analytics/frequent-routes?minTransfers=1 (min 1) */
    @GetMapping("/analytics/frequent-routes")
    public ResponseEntity<List<RouteMetricResponseDto>> getFrequentRoutes(
            @RequestParam(defaultValue = "1") @Min(1) int minTransfers) {
        List<RouteMetricResponseDto> result = getFrequentRoutesUseCase.execute(minTransfers)
                .stream().map(analyticsMapper::toDto).toList();
        return ResponseEntity.ok(result);
    }

    /** GET /analytics/top-transactions?limit=10 (1..100) */
    @GetMapping("/analytics/top-transactions")
    public ResponseEntity<List<TransactionResponseDto>> getTopTransactions(
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int limit) {
        List<TransactionResponseDto> result = new ArrayList<>();
        for (Transaccion tx : getTopTransactionsUseCase.execute(limit)) {
            result.add(transactionMapper.toDto(tx));
        }
        return ResponseEntity.ok(result);
    }

    /** GET /analytics/cycles */
    @GetMapping("/analytics/cycles")
    public ResponseEntity<List<List<String>>> getCycles() {
        // C-13: boundary conversion MiLista<MiLista<String>> → List<List<String>> for Jackson
        List<List<String>> result = new ArrayList<>();
        for (com.proyectofinal.fintech.domain.structures.MiLista<String> cycle : getCyclesUseCase.execute()) {
            List<String> innerList = new ArrayList<>();
            for (String node : cycle) {
                innerList.add(node);
            }
            result.add(innerList);
        }
        return ResponseEntity.ok(result);
    }

    /** GET /analytics/top-wallet-categories?limit=10 (1..100) */
    @GetMapping("/analytics/top-wallet-categories")
    public ResponseEntity<List<MetricItemResponseDto>> getTopWalletCategories(
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int limit) {
        List<MetricItemResponseDto> result = getTopWalletCategoriesUseCase.execute(limit)
                .stream().map(m -> analyticsMapper.toDto(new MetricItem(m.id(), m.label(), m.value())))
                .toList();
        return ResponseEntity.ok(result);
    }

    /** GET /analytics/movement-by-type */
    @GetMapping("/analytics/movement-by-type")
    public ResponseEntity<List<MetricItemResponseDto>> getMovementByType() {
        List<MetricItemResponseDto> result = getMovementByTypeUseCase.execute()
                .stream().map(analyticsMapper::toDto).toList();
        return ResponseEntity.ok(result);
    }

    /** GET /analytics/total-moved?from=&to= (ISO-8601 Instant params) */
    @GetMapping("/analytics/total-moved")
    public ResponseEntity<RangeTotalResponseDto> getTotalMoved(
            @RequestParam String from,
            @RequestParam String to) {
        RangeTotalView view = getTotalMovedInRangeUseCase.execute(
                Instant.parse(from), Instant.parse(to));
        return ResponseEntity.ok(new RangeTotalResponseDto(view.totalAmount(), view.count(), view.from(), view.to()));
    }
}
