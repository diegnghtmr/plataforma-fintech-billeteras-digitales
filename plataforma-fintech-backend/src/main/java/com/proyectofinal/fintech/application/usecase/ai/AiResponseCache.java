package com.proyectofinal.fintech.application.usecase.ai;

import com.proyectofinal.fintech.application.result.ai.AiChatAnswer;
import com.proyectofinal.fintech.domain.structures.TablaHash;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

/**
 * In-memory TTL cache for AiChatAnswer responses.
 * Backed by TablaHash (project custom structure — no java.util.HashMap).
 * Thread-safe via synchronization. Sweep-on-read for expired entries.
 * Zero Spring imports.
 */
public class AiResponseCache {

    record CacheEntry(AiChatAnswer answer, Instant expiresAt) {
    }

    private final TablaHash<String, CacheEntry> table;
    private final Duration ttl;
    private Clock clock;

    public AiResponseCache(Duration ttl, Clock clock) {
        this.ttl = ttl;
        this.clock = clock;
        this.table = new TablaHash<>();
    }

    /**
     * Retrieves a cached answer for the given key, returning empty if absent or expired.
     * Expired entries are removed on access (sweep-on-read).
     *
     * @param key composite cache key
     * @return optional answer
     */
    public synchronized Optional<AiChatAnswer> get(String key) {
        Optional<CacheEntry> entryOpt = table.get(key);
        if (entryOpt.isEmpty()) {
            return Optional.empty();
        }
        CacheEntry entry = entryOpt.get();
        Instant now = clock.instant();
        if (!now.isBefore(entry.expiresAt())) {
            // Entry expired — remove and return empty
            table.remove(key);
            return Optional.empty();
        }
        return Optional.of(entry.answer());
    }

    /**
     * Stores an answer in the cache with TTL from now.
     *
     * @param key    composite cache key
     * @param answer the answer to cache
     */
    public synchronized void put(String key, AiChatAnswer answer) {
        Instant expiresAt = clock.instant().plus(ttl);
        table.put(key, new CacheEntry(answer, expiresAt));
    }

    /**
     * Allows the clock to be replaced for testing (e.g. simulating TTL expiry).
     *
     * @param clock new clock
     */
    public synchronized void setClock(Clock clock) {
        this.clock = clock;
    }
}
