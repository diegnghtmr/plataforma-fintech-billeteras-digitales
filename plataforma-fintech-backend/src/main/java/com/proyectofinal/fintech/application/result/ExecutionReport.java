package com.proyectofinal.fintech.application.result;

import com.proyectofinal.fintech.domain.structures.MiLista;

/**
 * Immutable result record produced by ExecuteDueScheduledOperationsUseCase.
 * ZERO Spring/Jakarta imports.
 * W3: fields use MiLista&lt;String&gt; — no java.util.List in domain/application layer.
 */
public record ExecutionReport(
        int executed,
        int failed,
        MiLista<String> executedIds,
        MiLista<String> failedIds
) {}
