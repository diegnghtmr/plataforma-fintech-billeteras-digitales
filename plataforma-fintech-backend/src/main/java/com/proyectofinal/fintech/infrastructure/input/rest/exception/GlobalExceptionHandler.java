package com.proyectofinal.fintech.infrastructure.input.rest.exception;

import com.proyectofinal.fintech.application.exception.ai.AiInvalidIntentException;
import com.proyectofinal.fintech.application.exception.ai.AiMessageTooLongException;
import com.proyectofinal.fintech.application.exception.ai.AiUnavailableException;
import com.proyectofinal.fintech.application.exception.ai.ForbiddenActorException;
import com.proyectofinal.fintech.domain.exception.BusinessRuleException;
import com.proyectofinal.fintech.domain.exception.DuplicatedResourceException;
import com.proyectofinal.fintech.domain.exception.ErrorCode;
import com.proyectofinal.fintech.domain.exception.NotFoundException;
import com.proyectofinal.fintech.infrastructure.input.rest.dto.ApiErrorDto;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.format.DateTimeParseException;
import java.util.List;

/**
 * Central HTTP exception mapper.
 * Translates domain exceptions and Spring validation exceptions into ApiErrorDto responses.
 * Lives in infrastructure — the only layer allowed to know both domain exceptions and Spring.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorDto> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex) {
        List<String> details = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .toList();
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ApiErrorDto(
                        ErrorCode.VALIDATION_ERROR.name(),
                        "Validation failed",
                        details));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiErrorDto> handleConstraintViolation(
            ConstraintViolationException ex) {
        List<String> details = ex.getConstraintViolations().stream()
                .map(cv -> cv.getPropertyPath() + ": " + cv.getMessage())
                .toList();
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ApiErrorDto(
                        ErrorCode.VALIDATION_ERROR.name(),
                        "Constraint violation",
                        details));
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiErrorDto> handleNotFound(NotFoundException ex) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ApiErrorDto(
                        ex.code().name(),
                        ex.getMessage(),
                        null));
    }

    @ExceptionHandler(DuplicatedResourceException.class)
    public ResponseEntity<ApiErrorDto> handleDuplicatedResource(
            DuplicatedResourceException ex) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ApiErrorDto(
                        ex.code().name(),
                        ex.getMessage(),
                        null));
    }

    @ExceptionHandler(BusinessRuleException.class)
    public ResponseEntity<ApiErrorDto> handleBusinessRule(BusinessRuleException ex) {
        return ResponseEntity
                .status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(new ApiErrorDto(
                        ex.code().name(),
                        ex.getMessage(),
                        null));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiErrorDto> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ApiErrorDto(
                        ErrorCode.VALIDATION_ERROR.name(),
                        ex.getMessage(),
                        null));
    }

    @ExceptionHandler(DateTimeParseException.class)
    public ResponseEntity<ApiErrorDto> handleDateTimeParse(DateTimeParseException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ApiErrorDto(
                        ErrorCode.VALIDATION_ERROR.name(),
                        "Invalid date-time format: " + ex.getParsedString(),
                        null));
    }

    // ── AI exception handlers ─────────────────────────────────────────────────

    @ExceptionHandler(AiUnavailableException.class)
    public ResponseEntity<ApiErrorDto> handleAiUnavailable(AiUnavailableException ex) {
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new ApiErrorDto(
                        "AI_UNAVAILABLE",
                        ex.getMessage(),
                        ex.getReason() != null ? List.of(ex.getReason().name()) : null));
    }

    @ExceptionHandler(AiInvalidIntentException.class)
    public ResponseEntity<ApiErrorDto> handleAiInvalidIntent(AiInvalidIntentException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_GATEWAY)
                .body(new ApiErrorDto(
                        "AI_INVALID_INTENT",
                        ex.getMessage(),
                        null));
    }

    @ExceptionHandler(AiMessageTooLongException.class)
    public ResponseEntity<ApiErrorDto> handleAiMessageTooLong(AiMessageTooLongException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ApiErrorDto(
                        "AI_MESSAGE_TOO_LONG",
                        ex.getMessage(),
                        List.of("maxAllowed=" + ex.getMaxAllowed())));
    }

    @ExceptionHandler(ForbiddenActorException.class)
    public ResponseEntity<ApiErrorDto> handleForbiddenActor(ForbiddenActorException ex) {
        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(new ApiErrorDto(
                        "AI_FORBIDDEN",
                        ex.getMessage(),
                        null));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorDto> handleGeneric(Exception ex) {
        // Log server-side for diagnostics; never leak details to the client
        log.error("Unhandled exception reached GlobalExceptionHandler", ex);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiErrorDto(
                        ErrorCode.INTERNAL_ERROR.name(),
                        "Internal server error",
                        null));
    }
}
