package com.proyectofinal.fintech.infrastructure.mapper;

import com.proyectofinal.fintech.application.result.UserView;
import com.proyectofinal.fintech.domain.model.LoyaltyLevel;
import com.proyectofinal.fintech.infrastructure.input.rest.dto.UserResponseDto;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class UserMapperTest {

    private final UserMapper mapper = new UserMapper();

    @Test
    void toDto_mapsAllFields() {
        Instant registeredAt = Instant.parse("2026-01-01T00:00:00Z");
        UserView view = new UserView(
                "USR001",
                "Juan Pérez",
                "juan@example.com",
                registeredAt,
                100.0,
                LoyaltyLevel.SILVER,
                3,
                2500.0
        );

        UserResponseDto dto = mapper.toDto(view);

        assertEquals("USR001", dto.id());
        assertEquals("Juan Pérez", dto.name());
        assertEquals("juan@example.com", dto.email());
        assertEquals(registeredAt.toString(), dto.registeredAt());
        assertEquals(100.0, dto.points());
        assertEquals("SILVER", dto.loyaltyLevel());
        assertEquals(3, dto.walletCount());
        assertEquals(2500.0, dto.totalBalance(), 0.001);
    }
}
