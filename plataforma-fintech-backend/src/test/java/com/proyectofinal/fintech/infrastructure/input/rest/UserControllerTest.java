package com.proyectofinal.fintech.infrastructure.input.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyectofinal.fintech.application.result.UserView;
import com.proyectofinal.fintech.application.usecase.CreateUserUseCase;
import com.proyectofinal.fintech.application.usecase.DeleteUserUseCase;
import com.proyectofinal.fintech.application.usecase.GetUserUseCase;
import com.proyectofinal.fintech.application.usecase.ListUsersUseCase;
import com.proyectofinal.fintech.application.usecase.UpdateUserUseCase;
import com.proyectofinal.fintech.domain.exception.DuplicatedResourceException;
import com.proyectofinal.fintech.domain.exception.ErrorCode;
import com.proyectofinal.fintech.domain.exception.NotFoundException;
import com.proyectofinal.fintech.domain.model.LoyaltyLevel;
import com.proyectofinal.fintech.domain.model.Usuario;
import com.proyectofinal.fintech.infrastructure.input.rest.exception.GlobalExceptionHandler;
import com.proyectofinal.fintech.infrastructure.mapper.UserMapper;
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

@WebMvcTest(UserController.class)
@Import(GlobalExceptionHandler.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CreateUserUseCase createUserUseCase;

    @MockBean
    private GetUserUseCase getUserUseCase;

    @MockBean
    private ListUsersUseCase listUsersUseCase;

    @MockBean
    private UpdateUserUseCase updateUserUseCase;

    @MockBean
    private DeleteUserUseCase deleteUserUseCase;

    @MockBean
    private UserMapper userMapper;

    private static final Instant FIXED_NOW = Instant.parse("2026-01-01T00:00:00Z");

    @Test
    void createUser_S1_returns201() throws Exception {
        Usuario usuario = new Usuario("USR001", "Juan Pérez", "juan@example.com", FIXED_NOW, 0.0, LoyaltyLevel.BRONZE);
        UserView userView = new UserView("USR001", "Juan Pérez", "juan@example.com", FIXED_NOW, 0.0, LoyaltyLevel.BRONZE, 0, 0.0);

        when(createUserUseCase.execute("USR001", "Juan Pérez", "juan@example.com")).thenReturn(usuario);
        when(getUserUseCase.execute("USR001")).thenReturn(userView);
        when(userMapper.toDto(userView)).thenReturn(
                new com.proyectofinal.fintech.infrastructure.input.rest.dto.UserResponseDto(
                        "USR001", "Juan Pérez", "juan@example.com",
                        FIXED_NOW.toString(), 0.0, "BRONZE", 0, 0.0)
        );

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("id", "USR001", "name", "Juan Pérez", "email", "juan@example.com"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("USR001"))
                .andExpect(jsonPath("$.loyaltyLevel").value("BRONZE"));
    }

    @Test
    void createUser_S2_duplicateId_returns409() throws Exception {
        when(createUserUseCase.execute(anyString(), anyString(), anyString()))
                .thenThrow(new DuplicatedResourceException(ErrorCode.DUPLICATED_RESOURCE, "already exists"));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("id", "USR001", "name", "Another", "email", "other@example.com"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DUPLICATED_RESOURCE"));
    }

    @Test
    void createUser_S3_invalidEmail_returns400() throws Exception {
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("id", "USR002", "name", "Valid", "email", "not-an-email"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.details").isArray());
    }

    @Test
    void getUser_S4_unknownId_returns404() throws Exception {
        when(getUserUseCase.execute("UNKNOWN"))
                .thenThrow(new NotFoundException(ErrorCode.USER_NOT_FOUND, "not found"));

        mockMvc.perform(get("/users/UNKNOWN"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("USER_NOT_FOUND"));
    }

    @Test
    void listUsers_S5_returns200WithList() throws Exception {
        UserView view1 = new UserView("USR001", "Juan Pérez", "juan@example.com", FIXED_NOW, 0.0, LoyaltyLevel.BRONZE, 1, 500.0);
        UserView view2 = new UserView("USR002", "Ana López", "ana@example.com", FIXED_NOW, 50.0, LoyaltyLevel.SILVER, 2, 1200.0);

        when(listUsersUseCase.execute()).thenReturn(List.of(view1, view2));
        when(userMapper.toDto(view1)).thenReturn(
                new com.proyectofinal.fintech.infrastructure.input.rest.dto.UserResponseDto(
                        "USR001", "Juan Pérez", "juan@example.com", FIXED_NOW.toString(), 0.0, "BRONZE", 1, 500.0));
        when(userMapper.toDto(view2)).thenReturn(
                new com.proyectofinal.fintech.infrastructure.input.rest.dto.UserResponseDto(
                        "USR002", "Ana López", "ana@example.com", FIXED_NOW.toString(), 50.0, "SILVER", 2, 1200.0));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value("USR001"))
                .andExpect(jsonPath("$[1].id").value("USR002"))
                .andExpect(jsonPath("$[1].loyaltyLevel").value("SILVER"));
    }

    @Test
    void listUsers_S6_emptyRepository_returns200WithEmptyArray() throws Exception {
        when(listUsersUseCase.execute()).thenReturn(List.of());

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }
}
