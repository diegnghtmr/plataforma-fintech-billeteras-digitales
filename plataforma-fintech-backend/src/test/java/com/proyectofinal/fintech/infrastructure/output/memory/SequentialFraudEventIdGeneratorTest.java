package com.proyectofinal.fintech.infrastructure.output.memory;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * T09-B03 (RED) — SequentialFraudEventIdGenerator tests.
 */
class SequentialFraudEventIdGeneratorTest {

    @Test
    void firstCall_returnsFRD000001() {
        SequentialFraudEventIdGenerator gen = new SequentialFraudEventIdGenerator();
        assertThat(gen.next()).isEqualTo("FRD-000001");
    }

    @Test
    void thirdCall_returnsFRD000003() {
        SequentialFraudEventIdGenerator gen = new SequentialFraudEventIdGenerator();
        gen.next();
        gen.next();
        assertThat(gen.next()).isEqualTo("FRD-000003");
    }

    @Test
    void threadSafety_via_atomicLong_noDuplicates() throws InterruptedException {
        SequentialFraudEventIdGenerator gen = new SequentialFraudEventIdGenerator();
        int threads = 10;
        int callsPerThread = 100;
        List<String> results = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(threads);
        ExecutorService pool = Executors.newFixedThreadPool(threads);

        for (int i = 0; i < threads; i++) {
            pool.submit(() -> {
                List<String> local = new ArrayList<>();
                for (int j = 0; j < callsPerThread; j++) {
                    local.add(gen.next());
                }
                synchronized (results) {
                    results.addAll(local);
                }
                latch.countDown();
            });
        }

        latch.await(5, TimeUnit.SECONDS);
        pool.shutdown();

        assertThat(results).hasSize(threads * callsPerThread);
        assertThat(results).doesNotHaveDuplicates();
    }
}
