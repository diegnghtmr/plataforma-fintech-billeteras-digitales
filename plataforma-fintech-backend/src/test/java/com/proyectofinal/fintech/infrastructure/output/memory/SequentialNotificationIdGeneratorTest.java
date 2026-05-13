package com.proyectofinal.fintech.infrastructure.output.memory;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * T08-B08 (RED) — SequentialNotificationIdGenerator tests.
 */
class SequentialNotificationIdGeneratorTest {

    @Test
    void firstCall_returnsNTF000001() {
        SequentialNotificationIdGenerator gen = new SequentialNotificationIdGenerator();
        assertEquals("NTF-000001", gen.next());
    }

    @Test
    void secondCall_returnsNTF000002() {
        SequentialNotificationIdGenerator gen = new SequentialNotificationIdGenerator();
        gen.next();
        assertEquals("NTF-000002", gen.next());
    }

    @Test
    void multipleCallsIncrement() {
        SequentialNotificationIdGenerator gen = new SequentialNotificationIdGenerator();
        for (int i = 1; i <= 10; i++) {
            assertEquals(String.format("NTF-%06d", i), gen.next());
        }
    }
}
