package com.proyectofinal.fintech.infrastructure.output.ai.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Wire DTO for deserializing the LLM's JSON content for a chat answer.
 * Kept in infrastructure — application result type AiChatAnswer is Jackson-free.
 * Unknown fields are ignored for resilience against LLM output variation.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AiChatAnswerWire(
        @JsonProperty("answer") String answer,
        @JsonProperty("intent") String intent,
        @JsonProperty("confidence") Double confidence,
        @JsonProperty("usedMetrics") List<String> usedMetrics,
        @JsonProperty("suggestedActions") List<SuggestedActionWire> suggestedActions,
        @JsonProperty("requiresConfirmation") Boolean requiresConfirmation,
        @JsonProperty("conversationId") String conversationId,
        @JsonProperty("usedContext") String usedContext
) {

    /**
     * Wire DTO for a suggested action within a chat answer.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record SuggestedActionWire(
            @JsonProperty("type") String type,
            @JsonProperty("label") String label
    ) {}
}
