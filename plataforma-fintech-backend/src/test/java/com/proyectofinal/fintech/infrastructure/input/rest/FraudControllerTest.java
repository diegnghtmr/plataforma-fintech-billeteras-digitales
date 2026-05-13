package com.proyectofinal.fintech.infrastructure.input.rest;

import com.proyectofinal.fintech.application.usecase.ListFraudEventsUseCase;
import com.proyectofinal.fintech.domain.model.FraudEvent;
import com.proyectofinal.fintech.domain.model.FraudSeverity;
import com.proyectofinal.fintech.infrastructure.input.rest.exception.GlobalExceptionHandler;
import com.proyectofinal.fintech.infrastructure.mapper.FraudMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * T09-B27 (RED) — FraudController @WebMvcTest.
 */
@WebMvcTest(FraudController.class)
@Import(GlobalExceptionHandler.class)
class FraudControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ListFraudEventsUseCase listFraudEventsUseCase;
    @MockBean
    private FraudMapper fraudMapper;

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

    private FraudEvent makeEvent(String id, String userId, FraudSeverity severity) {
        return new FraudEvent(id, userId, "TX-" + id, "LARGE_TRANSACTION", severity, "desc", NOW);
    }

    @Test
    void getEvents_noParams_returnsAll() throws Exception {
        FraudEvent e = makeEvent("1", "USR_A", FraudSeverity.HIGH);
        when(listFraudEventsUseCase.execute(null, null)).thenReturn(List.of(e));
        when(fraudMapper.toDto(e)).thenCallRealMethod();

        mockMvc.perform(get("/fraud/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("1"));
    }

    @Test
    void getEvents_severityParam_filtered() throws Exception {
        FraudEvent e = makeEvent("2", "USR_A", FraudSeverity.HIGH);
        when(listFraudEventsUseCase.execute(null, FraudSeverity.HIGH)).thenReturn(List.of(e));
        when(fraudMapper.toDto(e)).thenCallRealMethod();

        mockMvc.perform(get("/fraud/events?severity=HIGH"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].severity").value("HIGH"));
    }

    @Test
    void getEvents_userIdParam_filtered() throws Exception {
        when(listFraudEventsUseCase.execute("USR_X", null)).thenReturn(List.of());

        mockMvc.perform(get("/fraud/events?userId=USR_X"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}
