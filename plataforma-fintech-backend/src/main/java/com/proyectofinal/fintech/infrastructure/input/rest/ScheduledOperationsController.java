package com.proyectofinal.fintech.infrastructure.input.rest;

import com.proyectofinal.fintech.application.result.ExecutionReport;
import com.proyectofinal.fintech.application.usecase.CancelScheduledOperationUseCase;
import com.proyectofinal.fintech.application.usecase.CreateScheduledOperationUseCase;
import com.proyectofinal.fintech.application.usecase.ExecuteDueScheduledOperationsUseCase;
import com.proyectofinal.fintech.application.usecase.ListScheduledOperationsUseCase;
import com.proyectofinal.fintech.domain.model.OperacionProgramada;
import com.proyectofinal.fintech.domain.model.ScheduledOperationType;
import com.proyectofinal.fintech.infrastructure.input.rest.dto.CreateScheduledOperationRequestDto;
import com.proyectofinal.fintech.infrastructure.input.rest.dto.ExecutionReportResponseDto;
import com.proyectofinal.fintech.infrastructure.input.rest.dto.ScheduledOperationResponseDto;
import com.proyectofinal.fintech.infrastructure.mapper.ScheduledOperationMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * REST adapter for scheduled operations.
 * Context-path /api/v1 set globally (SDD 1).
 */
@RestController
@RequestMapping("/scheduled-operations")
public class ScheduledOperationsController {

    private final CreateScheduledOperationUseCase createUseCase;
    private final ListScheduledOperationsUseCase listUseCase;
    private final CancelScheduledOperationUseCase cancelUseCase;
    private final ExecuteDueScheduledOperationsUseCase executeUseCase;
    private final ScheduledOperationMapper mapper;

    public ScheduledOperationsController(CreateScheduledOperationUseCase createUseCase,
                                          ListScheduledOperationsUseCase listUseCase,
                                          CancelScheduledOperationUseCase cancelUseCase,
                                          ExecuteDueScheduledOperationsUseCase executeUseCase,
                                          ScheduledOperationMapper mapper) {
        this.createUseCase = createUseCase;
        this.listUseCase = listUseCase;
        this.cancelUseCase = cancelUseCase;
        this.executeUseCase = executeUseCase;
        this.mapper = mapper;
    }

    @GetMapping
    public ResponseEntity<List<ScheduledOperationResponseDto>> list() {
        List<ScheduledOperationResponseDto> result = new ArrayList<>();
        for (OperacionProgramada op : listUseCase.execute()) {
            result.add(mapper.toDto(op));
        }
        return ResponseEntity.ok(result);
    }

    @PostMapping
    public ResponseEntity<ScheduledOperationResponseDto> create(
            @Valid @RequestBody CreateScheduledOperationRequestDto request) {
        OperacionProgramada op = createUseCase.execute(
                ScheduledOperationType.valueOf(request.type()),
                request.sourceUserId(),
                request.sourceWalletId(),
                request.targetUserId(),
                request.targetWalletId(),
                request.amount(),
                Instant.parse(request.scheduledAt()),
                request.description());
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toDto(op));
    }

    @PostMapping("/{operationId}/cancel")
    public ResponseEntity<ScheduledOperationResponseDto> cancel(
            @PathVariable String operationId) {
        OperacionProgramada op = cancelUseCase.execute(operationId);
        return ResponseEntity.ok(mapper.toDto(op));
    }

    @PostMapping("/run")
    public ResponseEntity<ExecutionReportResponseDto> run() {
        ExecutionReport report = executeUseCase.execute();
        return ResponseEntity.ok(new ExecutionReportResponseDto(
                report.executed(), report.failed(), report.executedIds(), report.failedIds()));
    }
}
