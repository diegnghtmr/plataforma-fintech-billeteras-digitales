package com.proyectofinal.fintech.infrastructure.input.rest;

import com.proyectofinal.fintech.application.usecase.GetPointsRankingUseCase;
import com.proyectofinal.fintech.application.usecase.GetUserPointsUseCase;
import com.proyectofinal.fintech.infrastructure.input.rest.dto.PointsResponseDto;
import com.proyectofinal.fintech.infrastructure.input.rest.dto.RankingItemResponseDto;
import com.proyectofinal.fintech.infrastructure.mapper.PointsMapper;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST adapter for points and ranking endpoints.
 * @Validated is required on the class for @Min/@Max on @RequestParam to fire (ADR-7.4).
 */
@RestController
@Validated
public class PointsController {

    private final GetUserPointsUseCase getUserPointsUseCase;
    private final GetPointsRankingUseCase getPointsRankingUseCase;
    private final PointsMapper pointsMapper;

    public PointsController(GetUserPointsUseCase getUserPointsUseCase,
                             GetPointsRankingUseCase getPointsRankingUseCase,
                             PointsMapper pointsMapper) {
        this.getUserPointsUseCase = getUserPointsUseCase;
        this.getPointsRankingUseCase = getPointsRankingUseCase;
        this.pointsMapper = pointsMapper;
    }

    /**
     * GET /users/{userId}/points
     * Returns current points and loyalty level for a user.
     */
    @GetMapping("/users/{userId}/points")
    public ResponseEntity<PointsResponseDto> getUserPoints(@PathVariable String userId) {
        return ResponseEntity.ok(pointsMapper.toDto(getUserPointsUseCase.execute(userId)));
    }

    /**
     * GET /points/ranking?limit={limit}
     * Returns the top-N users by points in descending order.
     * limit defaults to 10; must be in [1, 100].
     */
    @GetMapping("/points/ranking")
    public ResponseEntity<List<RankingItemResponseDto>> getPointsRanking(
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int limit) {
        List<RankingItemResponseDto> result = getPointsRankingUseCase.execute(limit)
                .stream()
                .map(pointsMapper::toDto)
                .toList();
        return ResponseEntity.ok(result);
    }
}
