package com.proyectofinal.fintech.application.exception.ai;

/**
 * Thrown when the AI provider is unavailable or the NoOp adapter is active.
 * Maps to HTTP 503 in GlobalExceptionHandler.
 * Zero Spring imports.
 */
public class AiUnavailableException extends RuntimeException {

    public enum Reason {
        PROVIDER_DISABLED,
        UPSTREAM_5XX,
        UPSTREAM_AUTH,
        UPSTREAM_TIMEOUT,
        UPSTREAM_RATE_LIMIT,
        UPSTREAM_PARSE
    }

    private final Reason reason;

    public AiUnavailableException(Reason reason, String message) {
        super(message);
        this.reason = reason;
    }

    public AiUnavailableException(Reason reason, String message, Throwable cause) {
        super(message, cause);
        this.reason = reason;
    }

    public Reason getReason() {
        return reason;
    }
}
