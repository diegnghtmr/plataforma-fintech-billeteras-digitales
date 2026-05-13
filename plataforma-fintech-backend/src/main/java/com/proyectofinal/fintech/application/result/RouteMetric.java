package com.proyectofinal.fintech.application.result;

/**
 * Represents a frequent transfer route metric.
 * Plain record — ZERO Spring/Jakarta imports.
 */
public record RouteMetric(
        String sourceUserId,
        String targetUserId,
        int transferCount,
        double totalAmount
) {}
