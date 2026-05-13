package com.proyectofinal.fintech.domain.exception;

/**
 * Thrown when a requested resource is not found.
 * Maps to HTTP 404 in GlobalExceptionHandler.
 * Zero Spring/Jakarta imports.
 */
public non-sealed class NotFoundException extends DomainException {

    private final ErrorCode errorCode;

    public NotFoundException(ErrorCode code, String message) {
        super(message);
        this.errorCode = code;
    }

    @Override
    public ErrorCode code() {
        return errorCode;
    }
}
