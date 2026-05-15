package com.proyectofinal.fintech.infrastructure.output.ai.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Wire DTO for the OpenRouter /chat/completions response body.
 * Unknown fields are ignored for forward compatibility with API evolution.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record OpenRouterChatResponse(
        @JsonProperty("id") String id,
        @JsonProperty("model") String model,
        @JsonProperty("choices") List<Choice> choices
) {

    /**
     * One choice returned by the model.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Choice(
            @JsonProperty("message") Message message,
            @JsonProperty("finish_reason") String finishReason
    ) {}

    /**
     * The assistant message from the model.
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Message(
            @JsonProperty("role") String role,
            @JsonProperty("content") String content
    ) {}
}
