package com.proyectofinal.fintech.application.usecase.ai;

import com.proyectofinal.fintech.application.exception.ai.ForbiddenActorException;
import com.proyectofinal.fintech.application.result.ai.AiIntent;
import com.proyectofinal.fintech.application.result.ai.AiScope;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * T04 — AuthorizationService tests.
 * Validates role+scope+intent access rules.
 */
class AuthorizationServiceTest {

    private final AuthorizationService authService = new AuthorizationService();

    @ParameterizedTest(name = "USER+USER+{0} -> allowed")
    @CsvSource({
        "EXPLAIN_BALANCE_CHANGE",
        "SUMMARIZE_ACTIVITY",
        "EXPLAIN_FRAUD_EVENT",
        "DRAFT_SCHEDULED_OPERATION",
        "DRAFT_TRANSFER",
        "SHOW_NOTIFICATIONS",
        "UNKNOWN"
    })
    void authorize_userScopeUser_allowedIntents_passes(String intentStr) {
        AiIntent intent = AiIntent.valueOf(intentStr);
        assertDoesNotThrow(() -> authService.authorize("USER", AiScope.USER, intent));
    }

    @Test
    void authorize_userScopeAdmin_throwsForbidden() {
        assertThrows(ForbiddenActorException.class, () ->
                authService.authorize("USER", AiScope.ADMIN, AiIntent.EXPLAIN_ANALYTICS));
    }

    @Test
    void authorize_adminScopeUser_throwsForbidden() {
        assertThrows(ForbiddenActorException.class, () ->
                authService.authorize("ADMIN", AiScope.USER, AiIntent.EXPLAIN_ANALYTICS));
    }

    @ParameterizedTest(name = "ADMIN+ADMIN+{0} -> allowed")
    @CsvSource({
        "EXPLAIN_ANALYTICS",
        "SUMMARIZE_ACTIVITY",
        "EXPLAIN_FRAUD_EVENT",
        "UNKNOWN"
    })
    void authorize_adminScopeAdmin_allowedIntents_passes(String intentStr) {
        AiIntent intent = AiIntent.valueOf(intentStr);
        assertDoesNotThrow(() -> authService.authorize("ADMIN", AiScope.ADMIN, intent));
    }

    @Test
    void authorize_invalidRole_throwsForbidden() {
        assertThrows(ForbiddenActorException.class, () ->
                authService.authorize("HACKER", AiScope.USER, AiIntent.SUMMARIZE_ACTIVITY));
    }

    @Test
    void authorize_nullRole_throwsForbidden() {
        assertThrows(ForbiddenActorException.class, () ->
                authService.authorize(null, AiScope.USER, AiIntent.SUMMARIZE_ACTIVITY));
    }

    @Test
    void authorize_userExplainAnalytics_throwsForbidden() {
        // USER cannot use EXPLAIN_ANALYTICS even with USER scope
        assertThrows(ForbiddenActorException.class, () ->
                authService.authorize("USER", AiScope.USER, AiIntent.EXPLAIN_ANALYTICS));
    }
}
