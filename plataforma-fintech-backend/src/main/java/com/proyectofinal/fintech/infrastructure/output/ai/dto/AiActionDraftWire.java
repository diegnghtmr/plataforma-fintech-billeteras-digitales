package com.proyectofinal.fintech.infrastructure.output.ai.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

/**
 * Wire DTO for deserializing the LLM's JSON content for an action draft.
 * Kept in infrastructure — application result type AiActionDraft is Jackson-free.
 * draft fields arrive as a Map<String,String> from the LLM; converted to TablaHash at mapping layer.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AiActionDraftWire(
        @JsonProperty("intent") String intent,
        @JsonProperty("requiresConfirmation") Boolean requiresConfirmation,
        @JsonProperty("draft") Map<String, String> draft,
        @JsonProperty("missingFields") List<String> missingFields,
        @JsonProperty("explanation") String explanation
) {}
