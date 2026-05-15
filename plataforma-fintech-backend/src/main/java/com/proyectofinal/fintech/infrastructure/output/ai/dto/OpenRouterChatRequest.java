package com.proyectofinal.fintech.infrastructure.output.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

/**
 * Wire DTO for the OpenRouter /chat/completions request body.
 * Kept in infrastructure layer — application layer is Jackson-free.
 */
public record OpenRouterChatRequest(
        @JsonProperty("model") String model,
        @JsonProperty("messages") List<Message> messages,
        @JsonProperty("temperature") double temperature,
        @JsonProperty("response_format") Map<String, String> responseFormat
) {

    /**
     * Individual message in the conversation.
     */
    public record Message(
            @JsonProperty("role") String role,
            @JsonProperty("content") String content
    ) {}
}
