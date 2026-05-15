package com.proyectofinal.fintech.application.usecase.ai;

import com.proyectofinal.fintech.application.result.ai.AiIntent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Observability helper wrapping slf4j Logger.
 * Never logs the user's message body or API key.
 * slf4j is an API jar with no Spring dependency — allowed in application layer.
 * Zero Spring imports.
 */
public class AiObservability {

    private static final Logger log = LoggerFactory.getLogger(AiObservability.class);

    public void recordAnswered(String actorUserId, AiIntent intent, double confidence) {
        log.info("[AI] answered actorUserId={} intent={} confidence={}",
                hashId(actorUserId), intent, confidence);
    }

    public void recordCacheHit(String actorUserId, AiIntent intent) {
        log.info("[AI] cache=hit actorUserId={} intent={}", hashId(actorUserId), intent);
    }

    public void recordUnavailable(String actorUserId, String reason) {
        log.warn("[AI] unavailable actorUserId={} reason={}", hashId(actorUserId), reason);
    }

    public void recordRejectedIntent(String actorUserId, String rawIntent) {
        log.warn("[AI] rejected-intent actorUserId={} rawIntent=[redacted]", hashId(actorUserId));
    }

    /** Returns a hash of the id to avoid logging PII. */
    private String hashId(String id) {
        if (id == null) return "null";
        return Integer.toHexString(id.hashCode());
    }
}
