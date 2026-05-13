package com.proyectofinal.fintech.infrastructure.mapper;

import com.proyectofinal.fintech.domain.model.Billetera;
import com.proyectofinal.fintech.infrastructure.input.rest.dto.WalletResponseDto;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class WalletMapperTest {

    private final WalletMapper mapper = new WalletMapper();

    @Test
    void toDto_mapsAllFields() {
        Instant createdAt = Instant.parse("2026-01-01T00:00:00Z");
        Billetera billetera = new Billetera("W001", "Ahorros", "SAVINGS", "USR001", 1500.0, true, createdAt, 5);

        WalletResponseDto dto = mapper.toDto(billetera);

        assertEquals("W001", dto.code());
        assertEquals("Ahorros", dto.name());
        assertEquals("SAVINGS", dto.type());
        assertEquals("USR001", dto.ownerId());
        assertEquals(1500.0, dto.balance(), 0.001);
        assertTrue(dto.active());
        assertEquals(createdAt.toString(), dto.createdAt());
        assertEquals(5, dto.transactionCount());
    }

    @Test
    void toDtoList_mapsAllItems() {
        Instant createdAt = Instant.parse("2026-01-01T00:00:00Z");
        List<Billetera> billeteras = List.of(
                new Billetera("W001", "Ahorros", "SAVINGS", "USR001", 500.0, true, createdAt, 0),
                new Billetera("W002", "Gastos", "DAILY", "USR001", 300.0, true, createdAt, 2)
        );

        List<WalletResponseDto> dtos = mapper.toDtoList(billeteras);

        assertEquals(2, dtos.size());
        assertEquals("W001", dtos.get(0).code());
        assertEquals("W002", dtos.get(1).code());
    }
}
