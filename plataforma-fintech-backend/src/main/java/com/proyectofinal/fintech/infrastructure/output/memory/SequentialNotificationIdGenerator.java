package com.proyectofinal.fintech.infrastructure.output.memory;

import com.proyectofinal.fintech.domain.port.NotificationIdGenerator;

import java.util.concurrent.atomic.AtomicLong;

/**
 * In-memory adapter for NotificationIdGenerator.
 * Produces IDs in format "NTF-000001", "NTF-000002", etc.
 */
public class SequentialNotificationIdGenerator implements NotificationIdGenerator {

    private final AtomicLong counter = new AtomicLong(0);

    @Override
    public String next() {
        long id = counter.incrementAndGet();
        return String.format("NTF-%06d", id);
    }
}
