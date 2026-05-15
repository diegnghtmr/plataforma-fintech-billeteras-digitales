package com.proyectofinal.fintech.infrastructure.input.rest.ai;

import com.proyectofinal.fintech.application.port.in.ai.AskFintechAiUseCase;
import com.proyectofinal.fintech.application.port.in.ai.DraftAiActionUseCase;
import com.proyectofinal.fintech.application.port.in.ai.ExplainFraudUseCase;
import com.proyectofinal.fintech.application.result.ai.*;
import com.proyectofinal.fintech.domain.structures.MiLista;
import com.proyectofinal.fintech.infrastructure.input.rest.AiChatController;
import com.proyectofinal.fintech.infrastructure.input.rest.dto.ai.AiDtoMapper;
import com.proyectofinal.fintech.infrastructure.input.rest.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * T32 — AiChatController with APP_AI_PROVIDER=openrouter and mocked use cases.
 * When use cases return valid answers (simulating openrouter adapter), controller returns 200.
 */
@WebMvcTest(AiChatController.class)
@Import({GlobalExceptionHandler.class, AiDtoMapper.class})
@TestPropertySource(properties = {
        "app.ai.provider=openrouter",
        "openrouter.api-key=test-key-12345"
})
class AiChatControllerOpenRouterTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AskFintechAiUseCase askFintechAiUseCase;

    @MockBean
    private ExplainFraudUseCase explainFraudUseCase;

    @MockBean
    private DraftAiActionUseCase draftAiActionUseCase;

    @Test
    void postChat_withOpenRouterProvider_returns200() throws Exception {
        AiChatAnswer answer = buildChatAnswer();
        when(askFintechAiUseCase.ask(any())).thenReturn(answer);

        mockMvc.perform(post("/ai/chat")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "message": "mi saldo cambio ayer",
                                  "actorRole": "USER",
                                  "actorUserId": "u1",
                                  "scope": "USER"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.intent").value("EXPLAIN_BALANCE_CHANGE"))
                .andExpect(jsonPath("$.answer").value("Your balance changed."));
    }

    @Test
    void postActionDraft_withOpenRouterProvider_returns200() throws Exception {
        AiActionDraft draft = buildActionDraft();
        when(draftAiActionUseCase.draft(any())).thenReturn(draft);

        mockMvc.perform(post("/ai/action-draft")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "message": "quiero programar una transferencia",
                                  "actorRole": "USER",
                                  "actorUserId": "u1",
                                  "scope": "USER"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.intent").value("DRAFT_TRANSFER"))
                .andExpect(jsonPath("$.requiresConfirmation").value(true));
    }

    @Test
    void getFraudExplain_withOpenRouterProvider_returns200() throws Exception {
        AiFraudExplanation explanation = buildFraudExplanation();
        when(explainFraudUseCase.explain(any())).thenReturn(explanation);

        mockMvc.perform(get("/ai/fraud-events/fe-1/explain")
                        .param("actorRole", "USER")
                        .param("actorUserId", "u1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fraudEventId").value("fe-1"))
                .andExpect(jsonPath("$.summary").value("A large transaction was flagged."));
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private AiChatAnswer buildChatAnswer() {
        MiLista<String> metrics = new MiLista<>();
        metrics.add("WALLET_SNAPSHOT");
        MiLista<AiSuggestedAction> actions = new MiLista<>();
        return new AiChatAnswer(
                "Your balance changed.",
                AiIntent.EXPLAIN_BALANCE_CHANGE,
                0.92,
                metrics, actions, false, "conv1", "USER_CONTEXT");
    }

    private AiActionDraft buildActionDraft() {
        com.proyectofinal.fintech.domain.structures.TablaHash<String, String> draft =
                new com.proyectofinal.fintech.domain.structures.TablaHash<>();
        draft.put("targetUserId", "u2");
        MiLista<String> missing = new MiLista<>();
        missing.add("amount");
        return new AiActionDraft(AiIntent.DRAFT_TRANSFER, true, draft, missing,
                "I will help you set up a transfer.");
    }

    private AiFraudExplanation buildFraudExplanation() {
        MiLista<String> evidence = new MiLista<>();
        evidence.add("Amount 10000");
        return new AiFraudExplanation(
                "fe-1",
                "A large transaction was flagged.",
                "HIGH severity because amount exceeded threshold.",
                evidence,
                "Contact your bank immediately.");
    }
}
