package com.proyectofinal.fintech.application.usecase.ai;

import com.proyectofinal.fintech.application.exception.ai.ForbiddenActorException;
import com.proyectofinal.fintech.application.result.ai.AiIntent;
import com.proyectofinal.fintech.application.result.ai.AiScope;

/**
 * Role-scope-intent authorization service.
 * Enforces the access rules from design §3.6.
 * Plain class — not a Spring bean in the application layer (wired via AiConfig).
 * Zero Spring imports.
 *
 * Authorization rules:
 * - USER + USER scope → allowed intents: EXPLAIN_BALANCE_CHANGE, SUMMARIZE_ACTIVITY,
 *                        EXPLAIN_FRAUD_EVENT, DRAFT_SCHEDULED_OPERATION, DRAFT_TRANSFER,
 *                        SHOW_NOTIFICATIONS, UNKNOWN
 * - USER + ADMIN scope → forbidden
 * - ADMIN + USER scope → forbidden
 * - ADMIN + ADMIN scope → allowed intents: EXPLAIN_ANALYTICS, SUMMARIZE_ACTIVITY,
 *                          EXPLAIN_FRAUD_EVENT, UNKNOWN
 * - Any other role → forbidden
 * - USER cannot use EXPLAIN_ANALYTICS even with USER scope
 */
public class AuthorizationService {

    // Allowed intents for USER role (USER scope)
    private static final AiIntent[] USER_ALLOWED = {
            AiIntent.EXPLAIN_BALANCE_CHANGE,
            AiIntent.SUMMARIZE_ACTIVITY,
            AiIntent.EXPLAIN_FRAUD_EVENT,
            AiIntent.DRAFT_SCHEDULED_OPERATION,
            AiIntent.DRAFT_TRANSFER,
            AiIntent.SHOW_NOTIFICATIONS,
            AiIntent.UNKNOWN
    };

    // Allowed intents for ADMIN role (ADMIN scope)
    private static final AiIntent[] ADMIN_ALLOWED = {
            AiIntent.EXPLAIN_ANALYTICS,
            AiIntent.SUMMARIZE_ACTIVITY,
            AiIntent.EXPLAIN_FRAUD_EVENT,
            AiIntent.UNKNOWN
    };

    /**
     * Authorizes the given actor role, scope and intent combination.
     *
     * @param actorRole the actor's role string ("USER" or "ADMIN")
     * @param scope     the requested scope
     * @param intent    the classified intent
     * @throws ForbiddenActorException if the combination is not permitted
     */
    public void authorize(String actorRole, AiScope scope, AiIntent intent) {
        if (actorRole == null) {
            throw new ForbiddenActorException("AI_FORBIDDEN: actorRole must not be null");
        }

        switch (actorRole) {
            case "USER" -> {
                if (scope != AiScope.USER) {
                    throw new ForbiddenActorException(
                            "AI_FORBIDDEN: USER actors may not use ADMIN scope");
                }
                if (!isAllowed(intent, USER_ALLOWED)) {
                    throw new ForbiddenActorException(
                            "AI_FORBIDDEN: USER actor may not use intent " + intent);
                }
            }
            case "ADMIN" -> {
                if (scope != AiScope.ADMIN) {
                    throw new ForbiddenActorException(
                            "AI_FORBIDDEN: ADMIN actors must use ADMIN scope");
                }
                if (!isAllowed(intent, ADMIN_ALLOWED)) {
                    throw new ForbiddenActorException(
                            "AI_FORBIDDEN: ADMIN actor may not use intent " + intent);
                }
            }
            default -> throw new ForbiddenActorException(
                    "AI_FORBIDDEN: unknown actorRole '" + actorRole + "'");
        }
    }

    private boolean isAllowed(AiIntent intent, AiIntent[] allowed) {
        for (AiIntent a : allowed) {
            if (a == intent) {
                return true;
            }
        }
        return false;
    }
}
