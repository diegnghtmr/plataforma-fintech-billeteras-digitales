package com.proyectofinal.fintech.application.result;

/**
 * Represents a single analytics metric entry.
 * Plain record — ZERO Spring/Jakarta imports.
 *
 * @param id    identifier (e.g. userId or userId/walletCode)
 * @param label human-readable label
 * @param value numeric metric value
 */
public record MetricItem(String id, String label, double value) {}
