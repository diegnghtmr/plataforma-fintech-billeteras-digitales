package com.proyectofinal.fintech.application.exception.ai;

/**
 * Thrown when the AI provider returns an intent value that is not in the allowlist.
 * Maps to HTTP 502 in GlobalExceptionHandler.
 * Zero Spring imports.
 */
public class AiInvalidIntentException extends RuntimeException {

    public AiInvalidIntentException(String message) {
        super(message);
    }

    public AiInvalidIntentException(String message, Throwable cause) {
        super(message, cause);
    }
}
