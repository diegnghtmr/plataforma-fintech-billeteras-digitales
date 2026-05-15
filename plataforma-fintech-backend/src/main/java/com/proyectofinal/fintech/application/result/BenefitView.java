package com.proyectofinal.fintech.application.result;

/**
 * Read model for a benefit in list context.
 * REQ-F1.1: includes id, name, description, pointsCost, active.
 */
public record BenefitView(String id, String name, String description, int pointsCost, boolean active) {}
