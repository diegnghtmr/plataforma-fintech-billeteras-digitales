package com.proyectofinal.fintech.infrastructure.output.memory;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * T05-B05 (RED) — SequentialTransactionIdGenerator tests.
 * Spec: first call → "TX-000001", second → "TX-000002", concurrent increments are atomic.
 * ADR-5.4: format TX-000001 (zero-padded for sort stability).
 */
class SequentialTransactionIdGeneratorTest {

    @Test
    void firstCall_returnsTX000001() {
        SequentialTransactionIdGenerator gen = new SequentialTransactionIdGenerator();
        assertThat(gen.next()).isEqualTo("TX-000001");
    }

    @Test
    void secondCall_returnsTX000002() {
        SequentialTransactionIdGenerator gen = new SequentialTransactionIdGenerator();
        gen.next(); // TX-000001
        assertThat(gen.next()).isEqualTo("TX-000002");
    }

    @Test
    void tenthCall_returnsTX000010() {
        SequentialTransactionIdGenerator gen = new SequentialTransactionIdGenerator();
        for (int i = 0; i < 9; i++) {
            gen.next();
        }
        assertThat(gen.next()).isEqualTo("TX-000010");
    }

    @Test
    void concurrentIncrements_produceUniqueIds() throws InterruptedException {
        SequentialTransactionIdGenerator gen = new SequentialTransactionIdGenerator();
        int threads = 20;
        Set<String> ids = Collections.synchronizedSet(new HashSet<>());
        CountDownLatch latch = new CountDownLatch(threads);
        ExecutorService executor = Executors.newFixedThreadPool(threads);

        for (int i = 0; i < threads; i++) {
            executor.submit(() -> {
                ids.add(gen.next());
                latch.countDown();
            });
        }

        latch.await();
        executor.shutdown();

        // All IDs must be unique — AtomicLong guarantees no duplicates
        assertThat(ids).hasSize(threads);
    }
}
