package com.proyectofinal.fintech.application.usecase.ai;

import com.proyectofinal.fintech.application.result.ai.AiIntent;
import com.proyectofinal.fintech.application.result.ai.AiScope;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * T02 — IntentClassifier parameterized tests.
 * Validates keyword-based intent classification for Spanish messages.
 */
class IntentClassifierTest {

    private final IntentClassifier classifier = new IntentClassifier();

    @ParameterizedTest(name = "scope={0}, message=''{1}'' -> {2}")
    @CsvSource({
        "USER,  '¿Por qué bajó mi saldo esta semana?',          EXPLAIN_BALANCE_CHANGE",
        "USER,  'mi balance cambió mucho',                       EXPLAIN_BALANCE_CHANGE",
        "USER,  '¿cuánto moví este mes? resumen de actividad',   SUMMARIZE_ACTIVITY",
        "USER,  'últimas transacciones',                         SUMMARIZE_ACTIVITY",
        "USER,  'alerta de fraude sospechoso',                   EXPLAIN_FRAUD_EVENT",
        "USER,  'notifico fraude',                               EXPLAIN_FRAUD_EVENT",
        "ADMIN, 'analytics de la plataforma',                    EXPLAIN_ANALYTICS",
        "ADMIN, 'métricas top usuarios',                         EXPLAIN_ANALYTICS",
        "USER,  'programar transferencia mañana',                DRAFT_SCHEDULED_OPERATION",
        "USER,  'agendar pago recurrente',                       DRAFT_SCHEDULED_OPERATION",
        "USER,  'quiero transferir dinero',                      DRAFT_TRANSFER",
        "USER,  'envíame fondos a mi cuenta',                    DRAFT_TRANSFER",
        "USER,  'notificaciones pendientes',                      SHOW_NOTIFICATIONS",
        "USER,  'sin pista de que trata este mensaje',            UNKNOWN",
    })
    void classify_variousMessages_returnsExpectedIntent(String scopeStr, String message, String expectedIntent) {
        AiScope scope = AiScope.valueOf(scopeStr.trim());
        AiIntent result = classifier.classify(scope, message.trim());
        assertEquals(AiIntent.valueOf(expectedIntent.trim()), result);
    }

    @Test
    void classify_nullMessage_returnsUnknown() {
        AiIntent result = classifier.classify(AiScope.USER, null);
        assertEquals(AiIntent.UNKNOWN, result);
    }

    @Test
    void classify_emptyMessage_returnsUnknown() {
        AiIntent result = classifier.classify(AiScope.USER, "");
        assertEquals(AiIntent.UNKNOWN, result);
    }

    @Test
    void classify_upperCaseKeyword_returnsCorrectIntent() {
        // Case-insensitive matching
        AiIntent result = classifier.classify(AiScope.USER, "SALDO BAJO");
        assertEquals(AiIntent.EXPLAIN_BALANCE_CHANGE, result);
    }
}
