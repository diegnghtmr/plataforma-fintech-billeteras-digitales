package com.proyectofinal.fintech.infrastructure.input.rest.exception;

import com.proyectofinal.fintech.domain.exception.BusinessRuleException;
import com.proyectofinal.fintech.domain.exception.DuplicatedResourceException;
import com.proyectofinal.fintech.domain.exception.ErrorCode;
import com.proyectofinal.fintech.domain.exception.NotFoundException;
import com.proyectofinal.fintech.infrastructure.input.rest.dto.ApiErrorDto;
import jakarta.validation.ConstraintViolationException;
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

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorDto> handleGeneric(Exception ex) {
        // Intentionally not leaking stack trace or internal message
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiErrorDto(
                        ErrorCode.INTERNAL_ERROR.name(),
                        "Internal server error",
                        null));
    }
}
