package com.proyectofinal.fintech.infrastructure.input.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyectofinal.fintech.application.usecase.CancelScheduledOperationUseCase;
import com.proyectofinal.fintech.application.usecase.CreateScheduledOperationUseCase;
import com.proyectofinal.fintech.application.usecase.ExecuteDueScheduledOperationsUseCase;
import com.proyectofinal.fintech.application.usecase.ListScheduledOperationsUseCase;
import com.proyectofinal.fintech.domain.exception.BusinessRuleException;
import com.proyectofinal.fintech.domain.exception.ErrorCode;
import com.proyectofinal.fintech.domain.exception.NotFoundException;
import com.proyectofinal.fintech.domain.model.*;
import com.proyectofinal.fintech.infrastructure.input.rest.dto.ScheduledOperationResponseDto;
import com.proyectofinal.fintech.infrastructure.input.rest.exception.GlobalExceptionHandler;
import com.proyectofinal.fintech.infrastructure.mapper.ScheduledOperationMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * T08-B28 (RED) — ScheduledOperationsController @WebMvcTest.
 * Covers S1-S7.
 */
@WebMvcTest({ScheduledOperationsController.class})
@Import(GlobalExceptionHandler.class)
class ScheduledOperationsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CreateScheduledOperationUseCase createUseCase;
    @MockBean
    private ListScheduledOperationsUseCase listUseCase;
    @MockBean
    private CancelScheduledOperationUseCase cancelUseCase;
    @MockBean
    private ExecuteDueScheduledOperationsUseCase executeUseCase;
    @MockBean
    private ScheduledOperationMapper mapper;

    private static final Instant SCHED = Instant.parse("2027-01-01T00:00:00Z");

    private OperacionProgramada makeOp(String id, ScheduledOperationStatus status) {
        return new OperacionProgramada(id, ScheduledOperationType.RECHARGE, status,
                "USR001", "W001", null, null, 100.0, SCHED, null);
    }

    private ScheduledOperationResponseDto makeDto(OperacionProgramada op) {
        return new ScheduledOperationResponseDto(op.getId(), op.getType().name(),
                op.getStatus().name(), op.getSourceUserId(), op.getSourceWalletId(),
                op.getTargetUserId(), op.getTargetWalletId(), op.getAmount(),
                op.getScheduledAt().toString(), op.getDescription(), op.getRecurrence().name());
    }

    // S1: POST create → 201
    @Test
    void create_happyPath_returns201() throws Exception {
        OperacionProgramada op = makeOp("SOP-000001", ScheduledOperationStatus.PENDING);
        ScheduledOperationResponseDto dto = makeDto(op);

        when(createUseCase.execute(any(), anyString(), anyString(), any(), any(),
                anyDouble(), any(), any(), any())).thenReturn(op);
        when(mapper.toDto(op)).thenReturn(dto);

        mockMvc.perform(post("/scheduled-operations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "type", "RECHARGE",
                                "sourceUserId", "USR001",
                                "sourceWalletId", "W001",
                                "amount", 100.0,
                                "scheduledAt", "2027-01-01T00:00:00Z"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("SOP-000001"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    // S4: GET list → 200
    @Test
    void list_returns200() throws Exception {
        OperacionProgramada op = makeOp("SOP-000001", ScheduledOperationStatus.PENDING);
        ScheduledOperationResponseDto dto = makeDto(op);

        when(listUseCase.execute()).thenReturn(List.of(op));
        when(mapper.toDto(op)).thenReturn(dto);

        mockMvc.perform(get("/scheduled-operations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("SOP-000001"));
    }

    // S5: POST cancel happy → 200
    @Test
    void cancel_happyPath_returns200() throws Exception {
        OperacionProgramada op = makeOp("SOP-000001", ScheduledOperationStatus.CANCELLED);
        ScheduledOperationResponseDto dto = makeDto(op);

        when(cancelUseCase.execute("SOP-000001")).thenReturn(op);
        when(mapper.toDto(op)).thenReturn(dto);

        mockMvc.perform(post("/scheduled-operations/SOP-000001/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    // S2: POST create with unknown user → 404
    @Test
    void create_userNotFound_returns404() throws Exception {
        when(createUseCase.execute(any(), anyString(), anyString(), any(), any(),
                anyDouble(), any(), any(), any()))
                .thenThrow(new NotFoundException(ErrorCode.USER_NOT_FOUND, "User not found"));

        mockMvc.perform(post("/scheduled-operations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "type", "RECHARGE",
                                "sourceUserId", "GHOST",
                                "sourceWalletId", "W001",
                                "amount", 100.0,
                                "scheduledAt", "2027-01-01T00:00:00Z"))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("USER_NOT_FOUND"));
    }

    // S3: POST create missing target for EXTERNAL_TRANSFER → 400
    @Test
    void create_missingTarget_returns400() throws Exception {
        when(createUseCase.execute(any(), anyString(), anyString(), any(), any(),
                anyDouble(), any(), any(), any()))
                .thenThrow(new BusinessRuleException(ErrorCode.VALIDATION_ERROR, "Missing target"));

        mockMvc.perform(post("/scheduled-operations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "type", "EXTERNAL_TRANSFER",
                                "sourceUserId", "USR001",
                                "sourceWalletId", "W001",
                                "amount", 100.0,
                                "scheduledAt", "2027-01-01T00:00:00Z"))))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    // S6: POST cancel with missing op → 404
    @Test
    void cancel_notFound_returns404() throws Exception {
        when(cancelUseCase.execute("GHOST"))
                .thenThrow(new NotFoundException(ErrorCode.OPERATION_NOT_FOUND, "Not found"));

        mockMvc.perform(post("/scheduled-operations/GHOST/cancel"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("OPERATION_NOT_FOUND"));
    }

    // S7: POST cancel non-PENDING → 422
    @Test
    void cancel_nonPending_returns422() throws Exception {
        when(cancelUseCase.execute("SOP-000001"))
                .thenThrow(new BusinessRuleException(ErrorCode.VALIDATION_ERROR, "Non-pending"));

        mockMvc.perform(post("/scheduled-operations/SOP-000001/cancel"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    // E-26 (RED) — REQ-F4.3: recurrence in POST and response

    @Test
    void create_withDailyRecurrence_returns201WithRecurrence() throws Exception {
        OperacionProgramada op = new OperacionProgramada("SOP-000002", ScheduledOperationType.RECHARGE,
                ScheduledOperationStatus.PENDING, "USR001", "W001", null, null, 100.0, SCHED, null,
                com.proyectofinal.fintech.domain.model.RecurrenceType.DAILY);
        ScheduledOperationResponseDto dto = new ScheduledOperationResponseDto(
                op.getId(), op.getType().name(), op.getStatus().name(),
                op.getSourceUserId(), op.getSourceWalletId(),
                op.getTargetUserId(), op.getTargetWalletId(), op.getAmount(),
                op.getScheduledAt().toString(), op.getDescription(), "DAILY");

        when(createUseCase.execute(any(), anyString(), anyString(), any(), any(),
                anyDouble(), any(), any(), any())).thenReturn(op);
        when(mapper.toDto(op)).thenReturn(dto);

        mockMvc.perform(post("/scheduled-operations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "type", "RECHARGE",
                                "sourceUserId", "USR001",
                                "sourceWalletId", "W001",
                                "amount", 100.0,
                                "scheduledAt", "2027-01-01T00:00:00Z",
                                "recurrence", "DAILY"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.recurrence").value("DAILY"));
    }

    @Test
    void create_withInvalidRecurrence_returns400() throws Exception {
        when(createUseCase.execute(any(), anyString(), anyString(), any(), any(),
                anyDouble(), any(), any(), any()))
                .thenThrow(new IllegalArgumentException("invalid_recurrence"));

        mockMvc.perform(post("/scheduled-operations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "type", "RECHARGE",
                                "sourceUserId", "USR001",
                                "sourceWalletId", "W001",
                                "amount", 100.0,
                                "scheduledAt", "2027-01-01T00:00:00Z",
                                "recurrence", "HOURLY"))))
                .andExpect(status().isBadRequest());
    }
}
