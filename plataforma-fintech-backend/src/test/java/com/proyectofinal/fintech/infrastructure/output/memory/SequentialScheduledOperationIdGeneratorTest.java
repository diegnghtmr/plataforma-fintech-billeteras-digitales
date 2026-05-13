package com.proyectofinal.fintech.infrastructure.output.memory;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * T08-B06 (RED) — SequentialScheduledOperationIdGenerator tests.
 */
class SequentialScheduledOperationIdGeneratorTest {

    @Test
    void firstCall_returnsSOP000001() {
        SequentialScheduledOperationIdGenerator gen = new SequentialScheduledOperationIdGenerator();
        assertEquals("SOP-000001", gen.next());
    }

    @Test
    void secondCall_returnsSOP000002() {
        SequentialScheduledOperationIdGenerator gen = new SequentialScheduledOperationIdGenerator();
        gen.next();
        assertEquals("SOP-000002", gen.next());
    }

    @Test
    void multipleCallsIncrement() {
        SequentialScheduledOperationIdGenerator gen = new SequentialScheduledOperationIdGenerator();
        for (int i = 1; i <= 10; i++) {
            assertEquals(String.format("SOP-%06d", i), gen.next());
        }
    }
}
