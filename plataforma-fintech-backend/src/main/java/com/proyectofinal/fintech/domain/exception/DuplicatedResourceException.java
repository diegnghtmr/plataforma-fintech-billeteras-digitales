package com.proyectofinal.fintech.domain.exception;

/**
 * Thrown when an attempt is made to create a resource that already exists.
 * Maps to HTTP 409 in GlobalExceptionHandler.
 * Zero Spring/Jakarta imports.
 */
public non-sealed class DuplicatedResourceException extends DomainException {

    private final ErrorCode errorCode;

    public DuplicatedResourceException(ErrorCode code, String message) {
        super(message);
        this.errorCode = code;
    }

    @Override
    public ErrorCode code() {
        return errorCode;
    }
}
