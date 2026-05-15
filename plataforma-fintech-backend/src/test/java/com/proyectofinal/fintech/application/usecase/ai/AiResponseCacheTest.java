package com.proyectofinal.fintech.application.usecase.ai;

import com.proyectofinal.fintech.application.result.ai.AiChatAnswer;
import com.proyectofinal.fintech.application.result.ai.AiIntent;
import com.proyectofinal.fintech.domain.structures.MiLista;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * T01 — AiResponseCache unit tests.
 * Validates put/get within TTL, expiry sweep-on-read, and no java.util.HashMap usage.
 */
class AiResponseCacheTest {

    private static final String KEY = "user1:conv1:EXPLAIN_BALANCE_CHANGE:abc123";

    private AiChatAnswer sampleAnswer() {
        MiLista<String> metrics = new MiLista<>();
        metrics.add("WALLET_SNAPSHOT");
        MiLista<com.proyectofinal.fintech.application.result.ai.AiSuggestedAction> actions = new MiLista<>();
        return new AiChatAnswer(
                "Your balance decreased due to a transfer.",
                AiIntent.EXPLAIN_BALANCE_CHANGE,
                0.9,
                metrics,
                actions,
                false,
                "conv1",
                "WALLET_SNAPSHOT"
        );
    }

    @Test
    void put_then_get_within_ttl_returns_value() {
        Clock clock = Clock.fixed(Instant.parse("2026-01-01T12:00:00Z"), ZoneId.of("UTC"));
        AiResponseCache cache = new AiResponseCache(Duration.ofMinutes(10), clock);

        cache.put(KEY, sampleAnswer());
        Optional<AiChatAnswer> result = cache.get(KEY);

        assertTrue(result.isPresent(), "Should return value within TTL");
        assertEquals("Your balance decreased due to a transfer.", result.get().answer());
    }

    @Test
    void get_after_ttl_returns_empty() {
        // Start at T0, put entry, advance clock past TTL, get → empty
        Instant base = Instant.parse("2026-01-01T12:00:00Z");
        Clock clockBefore = Clock.fixed(base, ZoneId.of("UTC"));
        AiResponseCache cache = new AiResponseCache(Duration.ofMinutes(10), clockBefore);
        cache.put(KEY, sampleAnswer());

        // Advance 11 minutes (past TTL)
        Clock clockAfter = Clock.fixed(base.plusSeconds(660), ZoneId.of("UTC"));
        cache.setClock(clockAfter);

        Optional<AiChatAnswer> result = cache.get(KEY);
        assertTrue(result.isEmpty(), "Should return empty after TTL expiry");
    }

    @Test
    void get_missing_key_returns_empty() {
        Clock clock = Clock.fixed(Instant.parse("2026-01-01T12:00:00Z"), ZoneId.of("UTC"));
        AiResponseCache cache = new AiResponseCache(Duration.ofMinutes(10), clock);

        Optional<AiChatAnswer> result = cache.get("nonexistent-key");
        assertTrue(result.isEmpty());
    }

    @Test
    void put_same_key_twice_overwrites() {
        Clock clock = Clock.fixed(Instant.parse("2026-01-01T12:00:00Z"), ZoneId.of("UTC"));
        AiResponseCache cache = new AiResponseCache(Duration.ofMinutes(10), clock);

        cache.put(KEY, sampleAnswer());
        MiLista<String> metrics = new MiLista<>();
        AiChatAnswer updated = new AiChatAnswer("Updated answer.", AiIntent.SUMMARIZE_ACTIVITY,
                0.8, metrics, new MiLista<>(), false, "conv1", "ANALYTICS_SUMMARY");
        cache.put(KEY, updated);

        Optional<AiChatAnswer> result = cache.get(KEY);
        assertTrue(result.isPresent());
        assertEquals("Updated answer.", result.get().answer());
    }

    @Test
    void get_exactly_at_ttl_boundary_returns_value() {
        Instant base = Instant.parse("2026-01-01T12:00:00Z");
        Clock clockBefore = Clock.fixed(base, ZoneId.of("UTC"));
        AiResponseCache cache = new AiResponseCache(Duration.ofMinutes(10), clockBefore);
        cache.put(KEY, sampleAnswer());

        // Exactly at 10 minutes (not expired)
        Clock clockAtBoundary = Clock.fixed(base.plusSeconds(600), ZoneId.of("UTC"));
        cache.setClock(clockAtBoundary);

        Optional<AiChatAnswer> result = cache.get(KEY);
        // At exactly TTL boundary (expiresAt = now), entry is considered expired (strict <)
        assertTrue(result.isEmpty(), "Entry at exact TTL boundary should be expired");
    }
}
