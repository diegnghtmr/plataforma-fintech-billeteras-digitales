package com.proyectofinal.fintech.domain.exception;

/**
 * Base sealed exception for all domain-level errors.
 * Zero Spring/Jakarta imports — this class is pure domain.
 */
public abstract sealed class DomainException extends RuntimeException
        permits NotFoundException, DuplicatedResourceException, BusinessRuleException {

    protected DomainException(String message) {
        super(message);
    }

    public abstract ErrorCode code();
}
