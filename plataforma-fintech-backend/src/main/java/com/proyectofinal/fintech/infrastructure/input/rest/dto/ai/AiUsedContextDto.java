package com.proyectofinal.fintech.infrastructure.input.rest.dto.ai;

import java.util.List;

/**
 * DTO for AiUsedContext in REST responses.
 * Exposes JDK List at the REST boundary (allowed per convention).
 */
public record AiUsedContextDto(
        List<String> sources,
        String scope,
        String rangeDescription
) {
}
