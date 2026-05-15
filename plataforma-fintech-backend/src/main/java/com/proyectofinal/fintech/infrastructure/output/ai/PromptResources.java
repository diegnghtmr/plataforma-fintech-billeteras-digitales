package com.proyectofinal.fintech.infrastructure.output.ai;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Loads and caches prompt classpath resources for AI endpoints.
 * Resources are loaded eagerly at construction — missing files fail fast at startup (not at first request).
 * No Spring dependencies; Spring can construct this via AiConfig @Bean.
 */
public class PromptResources {

    private final String chatSystem;
    private final String chatUser;
    private final String fraudSystem;
    private final String fraudUser;
    private final String actionSystem;
    private final String actionUser;

    /**
     * Loads all six prompt files from classpath at construction.
     * Throws IllegalStateException if any file is missing.
     */
    public PromptResources() {
        this.chatSystem = load("/prompts/fintech-chat.system.txt");
        this.chatUser = load("/prompts/fintech-chat.user.st");
        this.fraudSystem = load("/prompts/fintech-fraud.system.txt");
        this.fraudUser = load("/prompts/fintech-fraud.user.st");
        this.actionSystem = load("/prompts/fintech-action.system.txt");
        this.actionUser = load("/prompts/fintech-action.user.st");
    }

    /**
     * Returns the system prompt for the given key.
     *
     * @param key prompt key: "fintech-chat", "fintech-fraud", "fintech-action"
     * @return the system prompt content
     */
    public String system(String key) {
        return switch (key) {
            case "fintech-chat" -> chatSystem;
            case "fintech-fraud" -> fraudSystem;
            case "fintech-action" -> actionSystem;
            default -> throw new IllegalArgumentException("Unknown prompt key: " + key);
        };
    }

    /**
     * Returns the user-turn template for the given key.
     *
     * @param key prompt key: "fintech-chat", "fintech-fraud", "fintech-action"
     * @return the user template content (with {{placeholder}} markers)
     */
    public String userTemplate(String key) {
        return switch (key) {
            case "fintech-chat" -> chatUser;
            case "fintech-fraud" -> fraudUser;
            case "fintech-action" -> actionUser;
            default -> throw new IllegalArgumentException("Unknown prompt key: " + key);
        };
    }

    private String load(String classpathResource) {
        try (InputStream is = PromptResources.class.getResourceAsStream(classpathResource)) {
            Objects.requireNonNull(is,
                    "Prompt resource not found on classpath: " + classpathResource
                    + ". Cannot start AI feature without all prompt files.");
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Failed to load prompt resource: " + classpathResource, e);
        }
    }
}
