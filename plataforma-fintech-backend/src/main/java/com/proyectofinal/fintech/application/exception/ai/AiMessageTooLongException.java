package com.proyectofinal.fintech.application.exception.ai;

/**
 * Thrown when the user message exceeds the configured max length.
 * Maps to HTTP 400 in GlobalExceptionHandler.
 * Zero Spring imports.
 */
public class AiMessageTooLongException extends RuntimeException {

    private final int maxAllowed;
    private final int actual;

    public AiMessageTooLongException(int maxAllowed, int actual) {
        super("Message too long: " + actual + " chars (max " + maxAllowed + ")");
        this.maxAllowed = maxAllowed;
        this.actual = actual;
    }

    public int getMaxAllowed() {
        return maxAllowed;
    }

    public int getActual() {
        return actual;
    }
}
