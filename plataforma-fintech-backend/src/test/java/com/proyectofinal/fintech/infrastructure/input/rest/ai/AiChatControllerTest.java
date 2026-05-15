package com.proyectofinal.fintech.infrastructure.input.rest.ai;

import com.proyectofinal.fintech.application.exception.ai.AiUnavailableException;
import com.proyectofinal.fintech.application.exception.ai.ForbiddenActorException;
import com.proyectofinal.fintech.application.port.in.ai.AskFintechAiUseCase;
import com.proyectofinal.fintech.application.port.in.ai.DraftAiActionUseCase;
import com.proyectofinal.fintech.application.port.in.ai.ExplainFraudUseCase;
import com.proyectofinal.fintech.domain.exception.NotFoundException;
import com.proyectofinal.fintech.domain.exception.ErrorCode;
import com.proyectofinal.fintech.infrastructure.input.rest.AiChatController;
import com.proyectofinal.fintech.infrastructure.input.rest.dto.ai.AiDtoMapper;
import com.proyectofinal.fintech.infrastructure.input.rest.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * T06 + T07 — AiChatController @WebMvcTest.
 * Tests: 503 on all endpoints when adapter is NoOp,
 * 400 on message > 1000 chars, 400 on empty message,
 * 403 on role/scope mismatch, 404 on unknown fraud id.
 */
@WebMvcTest(AiChatController.class)
@Import({GlobalExceptionHandler.class, AiDtoMapper.class})
class AiChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AskFintechAiUseCase askFintechAiUseCase;

    @MockBean
    private ExplainFraudUseCase explainFraudUseCase;

    @MockBean
    private DraftAiActionUseCase draftAiActionUseCase;

    // ── T06: 503 on all endpoints when NoOp adapter ───────────────────────────

    @Test
    void postChat_noopAdapter_returns503() throws Exception {
        when(askFintechAiUseCase.ask(any()))
                .thenThrow(new AiUnavailableException(AiUnavailableException.Reason.PROVIDER_DISABLED,
                        "AI is disabled"));

        mockMvc.perform(post("/ai/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "message": "test message",
                                  "actorRole": "USER",
                                  "actorUserId": "u1",
                                  "scope": "USER"
                                }
                                """))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.code").value("AI_UNAVAILABLE"));
    }

    @Test
    void getFraudExplain_noopAdapter_returns503() throws Exception {
        when(explainFraudUseCase.explain(any()))
                .thenThrow(new AiUnavailableException(AiUnavailableException.Reason.PROVIDER_DISABLED,
                        "AI is disabled"));

        mockMvc.perform(get("/ai/fraud-events/fe-1/explain")
                        .param("actorRole", "USER")
                        .param("actorUserId", "u1"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.code").value("AI_UNAVAILABLE"));
    }

    @Test
    void postActionDraft_noopAdapter_returns503() throws Exception {
        when(draftAiActionUseCase.draft(any()))
                .thenThrow(new AiUnavailableException(AiUnavailableException.Reason.PROVIDER_DISABLED,
                        "AI is disabled"));

        mockMvc.perform(post("/ai/action-draft")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "message": "program a transfer",
                                  "actorRole": "USER",
                                  "actorUserId": "u1",
                                  "scope": "USER"
                                }
                                """))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.code").value("AI_UNAVAILABLE"));
    }

    // ── T07: 400 when message exceeds 1000 chars ──────────────────────────────

    @Test
    void postChat_messageTooLong_returns400() throws Exception {
        String longMessage = "a".repeat(1001);
        String body = "{\"message\":\"" + longMessage + "\",\"actorRole\":\"USER\","
                + "\"actorUserId\":\"u1\",\"scope\":\"USER\"}";

        mockMvc.perform(post("/ai/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void postChat_emptyMessage_returns400() throws Exception {
        mockMvc.perform(post("/ai/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "message": "",
                                  "actorRole": "USER",
                                  "actorUserId": "u1",
                                  "scope": "USER"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void postChat_missingMessage_returns400() throws Exception {
        mockMvc.perform(post("/ai/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "actorRole": "USER",
                                  "actorUserId": "u1",
                                  "scope": "USER"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void postChat_missingActorUserId_returns400() throws Exception {
        mockMvc.perform(post("/ai/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "message": "test",
                                  "actorRole": "USER",
                                  "scope": "USER"
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    // ── 403 when role/scope mismatch ──────────────────────────────────────────

    @Test
    void postChat_forbiddenRole_returns403() throws Exception {
        when(askFintechAiUseCase.ask(any()))
                .thenThrow(new ForbiddenActorException("AI_FORBIDDEN: USER cannot use ADMIN scope"));

        mockMvc.perform(post("/ai/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "message": "analytics platform",
                                  "actorRole": "USER",
                                  "actorUserId": "u1",
                                  "scope": "ADMIN"
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("AI_FORBIDDEN"));
    }

    // ── 404 when fraud id unknown ─────────────────────────────────────────────

    @Test
    void getFraudExplain_unknownFraudId_returns404() throws Exception {
        when(explainFraudUseCase.explain(any()))
                .thenThrow(new NotFoundException(ErrorCode.FRAUD_EVENT_NOT_FOUND,
                        "Fraud event fe-999 not found"));

        mockMvc.perform(get("/ai/fraud-events/fe-999/explain")
                        .param("actorRole", "USER")
                        .param("actorUserId", "u1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void postChat_exactlyMaxLength_passesValidation_returns503FromNoOp() throws Exception {
        // 1000 chars should pass bean validation and hit NoOp
        String maxMessage = "a".repeat(1000);
        String body = "{\"message\":\"" + maxMessage + "\",\"actorRole\":\"USER\","
                + "\"actorUserId\":\"u1\",\"scope\":\"USER\"}";

        when(askFintechAiUseCase.ask(any()))
                .thenThrow(new AiUnavailableException(AiUnavailableException.Reason.PROVIDER_DISABLED,
                        "AI is disabled"));

        mockMvc.perform(post("/ai/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isServiceUnavailable()); // hits NoOp, not validation error
    }
}
