package com.proyectofinal.fintech.application.exception.ai;

/**
 * Thrown when an actor attempts to perform an action not permitted for their role/scope.
 * Maps to HTTP 403 in GlobalExceptionHandler.
 * Zero Spring imports.
 */
public class ForbiddenActorException extends RuntimeException {

    public ForbiddenActorException(String message) {
        super(message);
    }
}
