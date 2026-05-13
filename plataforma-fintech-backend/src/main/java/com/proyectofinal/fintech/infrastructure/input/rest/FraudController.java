package com.proyectofinal.fintech.infrastructure.input.rest;

import com.proyectofinal.fintech.application.usecase.ListFraudEventsUseCase;
import com.proyectofinal.fintech.domain.model.FraudSeverity;
import com.proyectofinal.fintech.infrastructure.input.rest.dto.FraudEventResponseDto;
import com.proyectofinal.fintech.infrastructure.mapper.FraudMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * REST adapter for fraud event endpoints.
 */
@RestController
@Validated
public class FraudController {

    private final ListFraudEventsUseCase listFraudEventsUseCase;
    private final FraudMapper fraudMapper;

    public FraudController(ListFraudEventsUseCase listFraudEventsUseCase,
                            FraudMapper fraudMapper) {
        this.listFraudEventsUseCase = listFraudEventsUseCase;
        this.fraudMapper = fraudMapper;
    }

    /**
     * GET /fraud/events
     * Optional query params: userId, severity
     */
    @GetMapping("/fraud/events")
    public ResponseEntity<List<FraudEventResponseDto>> getEvents(
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) FraudSeverity severity) {
        List<FraudEventResponseDto> result = new ArrayList<>();
        listFraudEventsUseCase.execute(userId, severity)
                .forEach(event -> result.add(fraudMapper.toDto(event)));
        return ResponseEntity.ok(result);
    }
}
