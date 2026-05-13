package com.proyectofinal.fintech.infrastructure.input.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyectofinal.fintech.application.usecase.InternalTransferUseCase;
import com.proyectofinal.fintech.application.usecase.RechargeWalletUseCase;
import com.proyectofinal.fintech.application.usecase.WithdrawWalletUseCase;
import com.proyectofinal.fintech.domain.exception.BusinessRuleException;
import com.proyectofinal.fintech.domain.exception.ErrorCode;
import com.proyectofinal.fintech.domain.exception.NotFoundException;
import com.proyectofinal.fintech.domain.model.Transaccion;
import com.proyectofinal.fintech.domain.model.TransactionStatus;
import com.proyectofinal.fintech.domain.model.TransactionType;
import com.proyectofinal.fintech.infrastructure.input.rest.exception.GlobalExceptionHandler;
import com.proyectofinal.fintech.infrastructure.mapper.TransactionMapper;
import com.proyectofinal.fintech.infrastructure.input.rest.dto.TransactionResponseDto;
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
 * T05-B19 (RED) — OperationsController @WebMvcTest.
 * Covers S-REST-01 to S-REST-04.
 */
@WebMvcTest({OperationsController.class})
@Import(GlobalExceptionHandler.class)
class OperationsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RechargeWalletUseCase rechargeWalletUseCase;

    @MockBean
    private WithdrawWalletUseCase withdrawWalletUseCase;

    @MockBean
    private InternalTransferUseCase internalTransferUseCase;

    @MockBean
    private TransactionMapper transactionMapper;

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

    private Transaccion makeTx(TransactionType type) {
        return new Transaccion("TX-000001", NOW, type, 500.0,
                "W001", null, "USR001", null,
                TransactionStatus.SUCCESSFUL, 5.0, null, true);
    }

    private TransactionResponseDto makeDto(Transaccion tx) {
        return new TransactionResponseDto(tx.getId(), tx.getTimestamp().toString(),
                tx.getType().name(), tx.getAmount(), tx.getSourceWalletId(), tx.getTargetWalletId(),
                tx.getSourceUserId(), tx.getTargetUserId(), tx.getStatus().name(),
                tx.getPointsGenerated(), tx.getDescription(), tx.isReversible(), "LOW");
    }

    // S-REST-01: POST recharge → 200 with TransactionResponse
    @Test
    void recharge_happyPath_returns200() throws Exception {
        Transaccion tx = makeTx(TransactionType.RECHARGE);
        TransactionResponseDto dto = makeDto(tx);

        when(rechargeWalletUseCase.execute(eq("USR001"), eq("W001"), eq(500.0), isNull()))
                .thenReturn(tx);
        when(transactionMapper.toDto(tx)).thenReturn(dto);

        mockMvc.perform(post("/users/USR001/wallets/W001/recharge")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("amount", 500.0))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("TX-000001"))
                .andExpect(jsonPath("$.type").value("RECHARGE"))
                .andExpect(jsonPath("$.status").value("SUCCESSFUL"));
    }

    // S-REST-02: POST recharge with amount=0 → 400
    @Test
    void recharge_zeroAmount_returns400() throws Exception {
        mockMvc.perform(post("/users/USR001/wallets/W001/recharge")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("amount", 0.0))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));

        verifyNoInteractions(rechargeWalletUseCase);
    }

    // S-REST-03: POST withdraw with insufficient funds → 422 INSUFFICIENT_FUNDS
    @Test
    void withdraw_insufficientFunds_returns422() throws Exception {
        when(withdrawWalletUseCase.execute(eq("USR001"), eq("W001"), eq(100.0), isNull()))
                .thenThrow(new BusinessRuleException(ErrorCode.INSUFFICIENT_FUNDS,
                        "Insufficient funds"));

        mockMvc.perform(post("/users/USR001/wallets/W001/withdraw")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("amount", 100.0))))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("INSUFFICIENT_FUNDS"));
    }

    // S-REST-04: POST internal transfer with unknown wallet → 404 WALLET_NOT_FOUND
    @Test
    void internalTransfer_unknownWallet_returns404() throws Exception {
        when(internalTransferUseCase.execute(eq("USR001"), anyString(), anyString(), anyDouble(), isNull()))
                .thenThrow(new NotFoundException(ErrorCode.WALLET_NOT_FOUND, "Wallet not found"));

        mockMvc.perform(post("/users/USR001/transfers/internal")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("sourceWalletId", "W001",
                                       "targetWalletId", "GHOST",
                                       "amount", 100.0))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("WALLET_NOT_FOUND"));
    }

    // Internal transfer happy path → 200
    @Test
    void internalTransfer_happyPath_returns200() throws Exception {
        Transaccion tx = new Transaccion("TX-000001", NOW, TransactionType.INTERNAL_TRANSFER, 200.0,
                "W001", "W002", "USR001", "USR001",
                TransactionStatus.SUCCESSFUL, 1.0, null, true);
        TransactionResponseDto dto = new TransactionResponseDto(tx.getId(), tx.getTimestamp().toString(),
                tx.getType().name(), tx.getAmount(), tx.getSourceWalletId(), tx.getTargetWalletId(),
                tx.getSourceUserId(), tx.getTargetUserId(), tx.getStatus().name(),
                tx.getPointsGenerated(), tx.getDescription(), tx.isReversible(), "LOW");

        when(internalTransferUseCase.execute(eq("USR001"), eq("W001"), eq("W002"), eq(200.0), isNull()))
                .thenReturn(tx);
        when(transactionMapper.toDto(tx)).thenReturn(dto);

        mockMvc.perform(post("/users/USR001/transfers/internal")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("sourceWalletId", "W001",
                                       "targetWalletId", "W002",
                                       "amount", 200.0))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type").value("INTERNAL_TRANSFER"));
    }
}
