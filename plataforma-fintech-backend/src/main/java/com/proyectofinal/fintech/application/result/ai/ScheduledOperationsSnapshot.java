package com.proyectofinal.fintech.application.result.ai;

import com.proyectofinal.fintech.domain.structures.MiLista;

/**
 * Snapshot of scheduled operations for AI context.
 * Zero Spring imports.
 */
public record ScheduledOperationsSnapshot(
        int pendingCount,
        MiLista<String> pendingOperationIds
) {
}
