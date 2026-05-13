package com.proyectofinal.fintech.domain.model;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class BilleteraTest {

    @Test
    void defaultValues_balance_zero_active_true_transactionCount_zero() {
        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        Billetera billetera = new Billetera("W001", "Ahorros", "SAVINGS", "USR001", 0.0, true, now, 0);

        assertEquals(0.0, billetera.getBalance());
        assertTrue(billetera.isActive());
        assertEquals(0, billetera.getTransactionCount());
    }

    @Test
    void constructor_setsAllFields() {
        Instant now = Instant.parse("2026-01-01T00:00:00Z");
        Billetera billetera = new Billetera("W001", "Ahorros", "SAVINGS", "USR001", 1500.0, true, now, 5);

        assertEquals("W001", billetera.getCode());
        assertEquals("Ahorros", billetera.getName());
        assertEquals("SAVINGS", billetera.getType());
        assertEquals("USR001", billetera.getOwnerId());
        assertEquals(1500.0, billetera.getBalance());
        assertTrue(billetera.isActive());
        assertEquals(now, billetera.getCreatedAt());
        assertEquals(5, billetera.getTransactionCount());
    }

    @Test
    void setBalance_updatesBalance() {
        Instant now = Instant.now();
        Billetera billetera = new Billetera("W001", "Test", "TYPE", "USR001", 0.0, true, now, 0);
        billetera.setBalance(2000.0);
        assertEquals(2000.0, billetera.getBalance());
    }

    @Test
    void setTransactionCount_updatesCount() {
        Instant now = Instant.now();
        Billetera billetera = new Billetera("W001", "Test", "TYPE", "USR001", 0.0, true, now, 0);
        billetera.setTransactionCount(10);
        assertEquals(10, billetera.getTransactionCount());
    }
}
