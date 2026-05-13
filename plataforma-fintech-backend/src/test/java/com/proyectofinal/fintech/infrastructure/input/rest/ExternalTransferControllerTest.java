package com.proyectofinal.fintech.infrastructure.input.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyectofinal.fintech.application.result.ExternalTransferResult;
import com.proyectofinal.fintech.application.usecase.ExternalTransferUseCase;
import com.proyectofinal.fintech.domain.exception.BusinessRuleException;
import com.proyectofinal.fintech.domain.exception.ErrorCode;
import com.proyectofinal.fintech.domain.model.Transaccion;
import com.proyectofinal.fintech.domain.model.TransactionStatus;
import com.proyectofinal.fintech.domain.model.TransactionType;
import com.proyectofinal.fintech.infrastructure.input.rest.dto.ExternalTransferResponseDto;
import com.proyectofinal.fintech.infrastructure.input.rest.dto.TransactionResponseDto;
import com.proyectofinal.fintech.infrastructure.input.rest.exception.GlobalExceptionHandler;
import com.proyectofinal.fintech.infrastructure.mapper.TransactionMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * T05-B21 (RED) — ExternalTransferController @WebMvcTest.
 * Covers S-REST-05 (200 ExternalTransferResponse) and S-REST-06 (422 same user).
 */
@WebMvcTest({ExternalTransferController.class})
@Import(GlobalExceptionHandler.class)
class ExternalTransferControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ExternalTransferUseCase externalTransferUseCase;

    @MockBean
    private TransactionMapper transactionMapper;

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

    // S-REST-05: POST /transfers/external → 200 ExternalTransferResponse
    @Test
    void externalTransfer_happyPath_returns200WithBothTransactions() throws Exception {
        Transaccion outgoing = new Transaccion("TX-000001", NOW,
                TransactionType.EXTERNAL_TRANSFER_SENT, 500.0,
                "W_A", "W_B", "USR_A", "USR_B",
                TransactionStatus.SUCCESSFUL, 5.0, null, true);
        Transaccion incoming = new Transaccion("TX-000002", NOW,
                TransactionType.EXTERNAL_TRANSFER_RECEIVED, 500.0,
                "W_A", "W_B", "USR_A", "USR_B",
                TransactionStatus.SUCCESSFUL, 0.0, null, true);

        ExternalTransferResult result = new ExternalTransferResult(outgoing, incoming);

        TransactionResponseDto outDto = new TransactionResponseDto("TX-000001", NOW.toString(),
                "EXTERNAL_TRANSFER_SENT", 500.0, "W_A", "W_B", "USR_A", "USR_B",
                "SUCCESSFUL", 5.0, null, true, "LOW");
        TransactionResponseDto inDto = new TransactionResponseDto("TX-000002", NOW.toString(),
                "EXTERNAL_TRANSFER_RECEIVED", 500.0, "W_A", "W_B", "USR_A", "USR_B",
                "SUCCESSFUL", 0.0, null, true, "LOW");
        ExternalTransferResponseDto responseDto = new ExternalTransferResponseDto(outDto, inDto);

        when(externalTransferUseCase.execute(eq("USR_A"), eq("W_A"), eq("USR_B"), eq("W_B"),
                eq(500.0), isNull())).thenReturn(result);
        when(transactionMapper.toDto(result)).thenReturn(responseDto);

        mockMvc.perform(post("/transfers/external")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("sourceUserId", "USR_A",
                                       "sourceWalletId", "W_A",
                                       "targetUserId", "USR_B",
                                       "targetWalletId", "W_B",
                                       "amount", 500.0))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.outgoingTransaction.id").value("TX-000001"))
                .andExpect(jsonPath("$.incomingTransaction.id").value("TX-000002"))
                .andExpect(jsonPath("$.outgoingTransaction.type").value("EXTERNAL_TRANSFER_SENT"))
                .andExpect(jsonPath("$.incomingTransaction.type").value("EXTERNAL_TRANSFER_RECEIVED"));
    }

    // S-REST-06: POST /transfers/external with same user → 422
    @Test
    void externalTransfer_sameUser_returns422() throws Exception {
        when(externalTransferUseCase.execute(eq("USR_A"), anyString(), eq("USR_A"), anyString(),
                anyDouble(), isNull()))
                .thenThrow(new BusinessRuleException(ErrorCode.VALIDATION_ERROR,
                        "Source and target users must be different"));

        mockMvc.perform(post("/transfers/external")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("sourceUserId", "USR_A",
                                       "sourceWalletId", "W_A",
                                       "targetUserId", "USR_A",
                                       "targetWalletId", "W_B",
                                       "amount", 100.0))))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }
}
