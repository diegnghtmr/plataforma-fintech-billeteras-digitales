package com.proyectofinal.fintech.infrastructure.input.rest;

import com.proyectofinal.fintech.application.port.in.ai.AskFintechAiUseCase;
import com.proyectofinal.fintech.application.port.in.ai.DraftAiActionUseCase;
import com.proyectofinal.fintech.application.port.in.ai.ExplainFraudUseCase;
import com.proyectofinal.fintech.application.result.ai.AiActionDraft;
import com.proyectofinal.fintech.application.result.ai.AiChatAnswer;
import com.proyectofinal.fintech.application.result.ai.AiFraudExplanation;
import com.proyectofinal.fintech.application.result.ai.AiScope;
import com.proyectofinal.fintech.infrastructure.input.rest.dto.ai.AiActionDraftRequestDto;
import com.proyectofinal.fintech.infrastructure.input.rest.dto.ai.AiActionDraftResponseDto;
import com.proyectofinal.fintech.infrastructure.input.rest.dto.ai.AiChatRequestDto;
import com.proyectofinal.fintech.infrastructure.input.rest.dto.ai.AiChatResponseDto;
import com.proyectofinal.fintech.infrastructure.input.rest.dto.ai.AiFraudExplanationResponseDto;
import com.proyectofinal.fintech.infrastructure.input.rest.dto.ai.AiDtoMapper;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for AI chat endpoints.
 * Drives: AskFintechAiUseCase, ExplainFraudUseCase, DraftAiActionUseCase.
 * Exception handling delegated to GlobalExceptionHandler.
 */
@RestController
@RequestMapping("/ai")
@Validated
public class AiChatController {

    private final AskFintechAiUseCase askFintechAiUseCase;
    private final ExplainFraudUseCase explainFraudUseCase;
    private final DraftAiActionUseCase draftAiActionUseCase;
    private final AiDtoMapper aiDtoMapper;

    public AiChatController(
            AskFintechAiUseCase askFintechAiUseCase,
            ExplainFraudUseCase explainFraudUseCase,
            DraftAiActionUseCase draftAiActionUseCase,
            AiDtoMapper aiDtoMapper) {
        this.askFintechAiUseCase = askFintechAiUseCase;
        this.explainFraudUseCase = explainFraudUseCase;
        this.draftAiActionUseCase = draftAiActionUseCase;
        this.aiDtoMapper = aiDtoMapper;
    }

    /**
     * POST /ai/chat
     * Accepts a natural-language message and returns a structured AI answer.
     */
    @PostMapping("/chat")
    public ResponseEntity<AiChatResponseDto> chat(
            @RequestBody @Valid AiChatRequestDto request) {
        long start = System.currentTimeMillis();

        AiScope scope = parseScope(request.scope());

        AskFintechAiUseCase.AskFintechAiCommand command = new AskFintechAiUseCase.AskFintechAiCommand(
                request.actorUserId(),
                request.actorRole(),
                scope,
                request.conversationId(),
                request.message(),
                buildRange(request.from(), request.to()));

        AiChatAnswer answer = askFintechAiUseCase.ask(command);
        long latencyMs = System.currentTimeMillis() - start;

        return ResponseEntity.ok(aiDtoMapper.toDto(answer, latencyMs));
    }

    /**
     * GET /ai/fraud-events/{fraudEventId}/explain
     * Returns an AI-generated explanation of the specified fraud event.
     */
    @GetMapping("/fraud-events/{fraudEventId}/explain")
    public ResponseEntity<AiFraudExplanationResponseDto> explainFraud(
            @PathVariable String fraudEventId,
            @RequestParam @NotNull String actorRole,
            @RequestParam @NotBlank String actorUserId) {
        ExplainFraudUseCase.ExplainFraudCommand command = new ExplainFraudUseCase.ExplainFraudCommand(
                actorUserId,
                actorRole,
                fraudEventId);

        AiFraudExplanation explanation = explainFraudUseCase.explain(command);
        return ResponseEntity.ok(aiDtoMapper.toDto(explanation));
    }

    /**
     * POST /ai/action-draft
     * Returns a structured action draft based on the user's message.
     * requiresConfirmation is always true.
     */
    @PostMapping("/action-draft")
    public ResponseEntity<AiActionDraftResponseDto> actionDraft(
            @RequestBody @Valid AiActionDraftRequestDto request) {
        AiScope scope = parseScope(request.scope());

        DraftAiActionUseCase.DraftAiActionCommand command = new DraftAiActionUseCase.DraftAiActionCommand(
                request.actorUserId(),
                request.actorRole(),
                scope,
                request.message(),
                null,
                request.conversationId());

        AiActionDraft draft = draftAiActionUseCase.draft(command);
        return ResponseEntity.ok(aiDtoMapper.toDto(draft));
    }

    private AiScope parseScope(String scope) {
        try {
            return AiScope.valueOf(scope);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new IllegalArgumentException("Invalid scope: " + scope
                    + ". Must be one of: USER, ADMIN");
        }
    }

    private String buildRange(String from, String to) {
        if (from != null && to != null) {
            return from + " to " + to;
        }
        return null;
    }
}
