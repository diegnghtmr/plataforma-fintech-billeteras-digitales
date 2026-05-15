package com.proyectofinal.fintech.infrastructure.output.ai.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Wire DTO for deserializing the LLM's JSON content for a fraud explanation.
 * Kept in infrastructure — application result type AiFraudExplanation is Jackson-free.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AiFraudExplanationWire(
        @JsonProperty("fraudEventId") String fraudEventId,
        @JsonProperty("summary") String summary,
        @JsonProperty("severityExplanation") String severityExplanation,
        @JsonProperty("evidence") List<String> evidence,
        @JsonProperty("recommendation") String recommendation
) {}
