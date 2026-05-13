package com.proyectofinal.fintech.infrastructure.input.rest;

import com.proyectofinal.fintech.application.usecase.ListUserTransactionsUseCase;
import com.proyectofinal.fintech.application.usecase.ListWalletTransactionsUseCase;
import com.proyectofinal.fintech.application.usecase.ReverseTransactionUseCase;
import com.proyectofinal.fintech.domain.exception.BusinessRuleException;
import com.proyectofinal.fintech.domain.exception.ErrorCode;
import com.proyectofinal.fintech.domain.exception.NotFoundException;
import com.proyectofinal.fintech.domain.model.Transaccion;
import com.proyectofinal.fintech.domain.model.TransactionStatus;
import com.proyectofinal.fintech.domain.model.TransactionType;
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
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * T06-B07 (RED) — TransactionController @WebMvcTest.
 * Covers GET user txs (filtered/unfiltered), GET wallet txs, POST reverse, error cases.
 */
@WebMvcTest({TransactionController.class})
@Import(GlobalExceptionHandler.class)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ListUserTransactionsUseCase listUserTransactionsUseCase;

    @MockBean
    private ListWalletTransactionsUseCase listWalletTransactionsUseCase;

    @MockBean
    private ReverseTransactionUseCase reverseTransactionUseCase;

    @MockBean
    private TransactionMapper transactionMapper;

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

    private Transaccion makeTx(String id, TransactionType type) {
        return new Transaccion(id, NOW, type, 100.0,
                "W001", null, "USR001", null,
                TransactionStatus.SUCCESSFUL, 1.0, null, true);
    }

    private TransactionResponseDto makeDto(String id, String type, String status) {
        return new TransactionResponseDto(id, NOW.toString(), type, 100.0,
                "W001", null, "USR001", null,
                status, 1.0, null, true, "LOW");
    }

    // S1: GET /users/USR001/transactions → 200 with list
    @Test
    void getUserTransactions_noFilters_returns200() throws Exception {
        Transaccion tx = makeTx("TX-000001", TransactionType.RECHARGE);
        TransactionResponseDto dto = makeDto("TX-000001", "RECHARGE", "SUCCESSFUL");

        when(listUserTransactionsUseCase.execute(eq("USR001"), any(), any()))
                .thenReturn(List.of(tx));
        when(transactionMapper.toDto(tx)).thenReturn(dto);

        mockMvc.perform(get("/users/USR001/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("TX-000001"))
                .andExpect(jsonPath("$[0].type").value("RECHARGE"));
    }

    // S1 with type filter
    @Test
    void getUserTransactions_withTypeFilter_returns200() throws Exception {
        Transaccion tx = makeTx("TX-000001", TransactionType.RECHARGE);
        TransactionResponseDto dto = makeDto("TX-000001", "RECHARGE", "SUCCESSFUL");

        when(listUserTransactionsUseCase.execute(eq("USR001"), any(), any()))
                .thenReturn(List.of(tx));
        when(transactionMapper.toDto(tx)).thenReturn(dto);

        mockMvc.perform(get("/users/USR001/transactions?type=RECHARGE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].type").value("RECHARGE"));
    }

    // S1 user not found → 404
    @Test
    void getUserTransactions_userNotFound_returns404() throws Exception {
        when(listUserTransactionsUseCase.execute(eq("GHOST"), any(), any()))
                .thenThrow(new NotFoundException(ErrorCode.USER_NOT_FOUND, "User not found"));

        mockMvc.perform(get("/users/GHOST/transactions"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("USER_NOT_FOUND"));
    }

    // S2: GET /users/USR001/wallets/W001/transactions → 200
    @Test
    void getWalletTransactions_happyPath_returns200() throws Exception {
        Transaccion tx = makeTx("TX-000001", TransactionType.RECHARGE);
        TransactionResponseDto dto = makeDto("TX-000001", "RECHARGE", "SUCCESSFUL");

        when(listWalletTransactionsUseCase.execute("USR001", "W001"))
                .thenReturn(List.of(tx));
        when(transactionMapper.toDto(tx)).thenReturn(dto);

        mockMvc.perform(get("/users/USR001/wallets/W001/transactions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("TX-000001"));
    }

    // S2 wallet not found → 404
    @Test
    void getWalletTransactions_walletNotFound_returns404() throws Exception {
        when(listWalletTransactionsUseCase.execute("USR001", "GHOST"))
                .thenThrow(new NotFoundException(ErrorCode.WALLET_NOT_FOUND, "Wallet not found"));

        mockMvc.perform(get("/users/USR001/wallets/GHOST/transactions"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("WALLET_NOT_FOUND"));
    }

    // POST /transactions/TX-001/reverse → 200 REVERSED
    @Test
    void reverseTransaction_happyPath_returns200() throws Exception {
        Transaccion tx = new Transaccion("TX-001", NOW, TransactionType.RECHARGE, 100.0,
                "W001", null, "USR001", null, TransactionStatus.REVERSED, 1.0, null, true);
        TransactionResponseDto dto = makeDto("TX-001", "RECHARGE", "REVERSED");

        when(reverseTransactionUseCase.execute("TX-001")).thenReturn(tx);
        when(transactionMapper.toDto(tx)).thenReturn(dto);

        mockMvc.perform(post("/transactions/TX-001/reverse")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REVERSED"));
    }

    // POST reverse unknown id → 404 TRANSACTION_NOT_FOUND
    @Test
    void reverseTransaction_notFound_returns404() throws Exception {
        when(reverseTransactionUseCase.execute("TX-999999"))
                .thenThrow(new NotFoundException(ErrorCode.TRANSACTION_NOT_FOUND, "Not found"));

        mockMvc.perform(post("/transactions/TX-999999/reverse")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("TRANSACTION_NOT_FOUND"));
    }

    // POST reverse non-reversible → 422 TRANSACTION_NOT_REVERSIBLE
    @Test
    void reverseTransaction_notReversible_returns422() throws Exception {
        when(reverseTransactionUseCase.execute("TX-002"))
                .thenThrow(new BusinessRuleException(ErrorCode.TRANSACTION_NOT_REVERSIBLE, "Not reversible"));

        mockMvc.perform(post("/transactions/TX-002/reverse")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.code").value("TRANSACTION_NOT_REVERSIBLE"));
    }
}
