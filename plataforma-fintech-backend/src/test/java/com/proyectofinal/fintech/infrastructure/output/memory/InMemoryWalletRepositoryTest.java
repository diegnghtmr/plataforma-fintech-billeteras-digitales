package com.proyectofinal.fintech.infrastructure.output.memory;

import com.proyectofinal.fintech.domain.model.Billetera;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryWalletRepositoryTest {

    private InMemoryWalletRepository repository;
    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

    @BeforeEach
    void setUp() {
        repository = new InMemoryWalletRepository();
    }

    private Billetera wallet(String code, String ownerId, double balance) {
        return new Billetera(code, "Wallet " + code, "SAVINGS", ownerId, balance, true, NOW, 0);
    }

    @Test
    void save_thenFindByOwnerId_returnsWallet() {
        repository.save(wallet("W001", "USR001", 0.0));

        Iterable<Billetera> result = repository.findByOwnerId("USR001");
        int count = 0;
        for (Billetera b : result) {
            count++;
            assertEquals("W001", b.getCode());
        }
        assertEquals(1, count);
    }

    @Test
    void findByOwnerId_unknownOwner_returnsEmpty() {
        Iterable<Billetera> result = repository.findByOwnerId("UNKNOWN");
        assertFalse(result.iterator().hasNext());
    }

    @Test
    void findByOwnerIdAndCode_returnsCorrectWallet() {
        repository.save(wallet("W001", "USR001", 500.0));
        repository.save(wallet("W002", "USR001", 300.0));

        Optional<Billetera> found = repository.findByOwnerIdAndCode("USR001", "W001");
        assertTrue(found.isPresent());
        assertEquals(500.0, found.get().getBalance());
    }

    @Test
    void findByOwnerIdAndCode_unknownCode_returnsEmpty() {
        repository.save(wallet("W001", "USR001", 500.0));
        Optional<Billetera> found = repository.findByOwnerIdAndCode("USR001", "UNKNOWN");
        assertTrue(found.isEmpty());
    }

    @Test
    void existsByOwnerIdAndCode_true_whenExists() {
        repository.save(wallet("W001", "USR001", 0.0));
        assertTrue(repository.existsByOwnerIdAndCode("USR001", "W001"));
    }

    @Test
    void existsByOwnerIdAndCode_false_whenNotExists() {
        assertFalse(repository.existsByOwnerIdAndCode("USR001", "W999"));
    }

    @Test
    void countByOwnerId_returnsCorrectCount() {
        repository.save(wallet("W001", "USR001", 0.0));
        repository.save(wallet("W002", "USR001", 0.0));
        repository.save(wallet("W001", "USR002", 0.0)); // different owner

        assertEquals(2, repository.countByOwnerId("USR001"));
        assertEquals(1, repository.countByOwnerId("USR002"));
    }

    @Test
    void sumBalanceByOwnerId_returnsTotalBalance() {
        repository.save(wallet("W001", "USR001", 1000.0));
        repository.save(wallet("W002", "USR001", 500.0));

        assertEquals(1500.0, repository.sumBalanceByOwnerId("USR001"), 0.001);
    }

    @Test
    void multiOwnerIsolation_walletsAreIsolated() {
        repository.save(wallet("W001", "USR001", 100.0));
        repository.save(wallet("W001", "USR002", 200.0)); // same code, different owner

        assertEquals(1, repository.countByOwnerId("USR001"));
        assertEquals(1, repository.countByOwnerId("USR002"));
        assertEquals(100.0, repository.sumBalanceByOwnerId("USR001"), 0.001);
        assertEquals(200.0, repository.sumBalanceByOwnerId("USR002"), 0.001);
    }

    @Test
    void countByOwnerId_unknownOwner_returnsZero() {
        assertEquals(0, repository.countByOwnerId("UNKNOWN"));
    }

    @Test
    void sumBalanceByOwnerId_unknownOwner_returnsZero() {
        assertEquals(0.0, repository.sumBalanceByOwnerId("UNKNOWN"), 0.001);
    }

    // C-9: findAll returns MiLista<Billetera>
    @Test
    void findAll_returnsMiLista() {
        repository.save(wallet("W001", "USR001", 100.0));
        repository.save(wallet("W002", "USR002", 200.0));
        repository.save(wallet("W003", "USR001", 300.0));

        com.proyectofinal.fintech.domain.structures.MiLista<Billetera> result = repository.findAll();

        assertNotNull(result);
        int count = 0;
        for (Billetera b : result) { count++; }
        assertEquals(3, count);
    }

    @Test
    void findAll_empty_returnsMiLista() {
        com.proyectofinal.fintech.domain.structures.MiLista<Billetera> result = repository.findAll();
        assertNotNull(result);
        assertFalse(result.iterator().hasNext());
    }
}
