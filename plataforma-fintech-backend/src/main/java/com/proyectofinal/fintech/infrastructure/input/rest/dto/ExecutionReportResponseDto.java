package com.proyectofinal.fintech.infrastructure.input.rest.dto;

import java.util.List;

/**
 * Response DTO for POST /scheduled-operations/run.
 */
public record ExecutionReportResponseDto(
        int executed,
        int failed,
        List<String> executedIds,
        List<String> failedIds
) {}
