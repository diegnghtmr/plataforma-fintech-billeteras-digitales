package com.proyectofinal.fintech.infrastructure.input.rest.exception;

import com.proyectofinal.fintech.domain.exception.BusinessRuleException;
import com.proyectofinal.fintech.domain.exception.DuplicatedResourceException;
import com.proyectofinal.fintech.domain.exception.ErrorCode;
import com.proyectofinal.fintech.domain.exception.NotFoundException;
import com.proyectofinal.fintech.infrastructure.input.rest.dto.ValidatedBean;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

/**
 * Test-only controller — lives in src/test/java.
 * Used exclusively by GlobalExceptionHandlerTest via @WebMvcTest.
 * DO NOT move to src/main/java.
 */
@RestController
@RequestMapping("/test-errors")
public class TestErrorTriggerController {

    @GetMapping("/{type}")
    public String trigger(@PathVariable String type) {
        return switch (type) {
            case "not-found" -> throw new NotFoundException(
                    ErrorCode.USER_NOT_FOUND, "Usuario no encontrado");
            case "duplicated" -> throw new DuplicatedResourceException(
                    ErrorCode.DUPLICATED_RESOURCE, "Recurso duplicado");
            case "business" -> throw new BusinessRuleException(
                    ErrorCode.INSUFFICIENT_FUNDS, "Saldo insuficiente");
            case "internal" -> throw new RuntimeException("Error interno inesperado");
            case "illegal-argument" -> throw new IllegalArgumentException(
                    "No enum constant TransactionType.NOT_REAL");
            case "date-parse" -> {
                Instant.parse("not-a-date");
                yield "unreachable";
            }
            default -> "ok";
        };
    }

    @PostMapping("/validation")
    public String validation(@Valid @RequestBody ValidatedBean bean) {
        return "ok";
    }
}
