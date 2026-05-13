package com.proyectofinal.fintech.infrastructure.input.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyectofinal.fintech.application.usecase.CreateWalletUseCase;
import com.proyectofinal.fintech.application.usecase.ListWalletsUseCase;
import com.proyectofinal.fintech.domain.exception.ErrorCode;
import com.proyectofinal.fintech.domain.exception.NotFoundException;
import com.proyectofinal.fintech.domain.model.Billetera;
import com.proyectofinal.fintech.infrastructure.input.rest.dto.WalletResponseDto;
import com.proyectofinal.fintech.infrastructure.input.rest.exception.GlobalExceptionHandler;
import com.proyectofinal.fintech.infrastructure.mapper.WalletMapper;
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

@WebMvcTest(WalletController.class)
@Import(GlobalExceptionHandler.class)
class WalletControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CreateWalletUseCase createWalletUseCase;

    @MockBean
    private ListWalletsUseCase listWalletsUseCase;

    @MockBean
    private WalletMapper walletMapper;

    private static final Instant FIXED_NOW = Instant.parse("2026-01-01T00:00:00Z");

    @Test
    void listWallets_S5_unknownUser_returns404() throws Exception {
        when(listWalletsUseCase.execute("GHOST"))
                .thenThrow(new NotFoundException(ErrorCode.USER_NOT_FOUND, "not found"));

        mockMvc.perform(get("/users/GHOST/wallets"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("USER_NOT_FOUND"));
    }

    @Test
    void listWallets_S7_twoWallets_returns200WithList() throws Exception {
        List<Billetera> wallets = List.of(
                new Billetera("WAL001", "Ahorros", "SAVINGS", "USR001", 500.0, true, FIXED_NOW, 0),
                new Billetera("WAL002", "Gastos", "DAILY", "USR001", 300.0, true, FIXED_NOW, 0)
        );
        List<WalletResponseDto> dtos = List.of(
                new WalletResponseDto("WAL001", "Ahorros", "SAVINGS", "USR001", 500.0, true, FIXED_NOW.toString(), 0),
                new WalletResponseDto("WAL002", "Gastos", "DAILY", "USR001", 300.0, true, FIXED_NOW.toString(), 0)
        );

        when(listWalletsUseCase.execute("USR001")).thenReturn(wallets);
        when(walletMapper.toDtoList(wallets)).thenReturn(dtos);

        mockMvc.perform(get("/users/USR001/wallets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void createWallet_happyPath_returns201WithGeneratedCode() throws Exception {
        Billetera billetera = new Billetera("WAL001", "Ahorros", "SAVINGS", "USR001", 0.0, true, FIXED_NOW, 0);
        WalletResponseDto dto = new WalletResponseDto("WAL001", "Ahorros", "SAVINGS", "USR001", 0.0, true, FIXED_NOW.toString(), 0);

        when(createWalletUseCase.execute("USR001", "Ahorros", "SAVINGS")).thenReturn(billetera);
        when(walletMapper.toDto(billetera)).thenReturn(dto);

        mockMvc.perform(post("/users/USR001/wallets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("name", "Ahorros", "type", "SAVINGS"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("WAL001"));
    }

    @Test
    void createWallet_missingName_returns400() throws Exception {
        mockMvc.perform(post("/users/USR001/wallets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("type", "SAVINGS"))))
                .andExpect(status().isBadRequest());
    }
}
