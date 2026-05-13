package com.proyectofinal.fintech.domain.exception;

/**
 * Thrown when a business rule is violated (e.g. insufficient funds).
 * Maps to HTTP 422 in GlobalExceptionHandler.
 * Zero Spring/Jakarta imports.
 */
public non-sealed class BusinessRuleException extends DomainException {

    private final ErrorCode errorCode;

    public BusinessRuleException(ErrorCode code, String message) {
        super(message);
        this.errorCode = code;
    }

    @Override
    public ErrorCode code() {
        return errorCode;
    }
}
