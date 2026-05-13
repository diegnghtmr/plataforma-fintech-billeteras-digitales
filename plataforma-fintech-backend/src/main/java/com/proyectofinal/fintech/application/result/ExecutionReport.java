package com.proyectofinal.fintech.application.result;

import java.util.List;

/**
 * Immutable result record produced by ExecuteDueScheduledOperationsUseCase.
 * ZERO Spring/Jakarta imports.
 */
public record ExecutionReport(
        int executed,
        int failed,
        List<String> executedIds,
        List<String> failedIds
) {}
