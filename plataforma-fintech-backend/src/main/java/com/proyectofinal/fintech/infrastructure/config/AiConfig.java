package com.proyectofinal.fintech.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.proyectofinal.fintech.application.port.in.ai.AskFintechAiUseCase;
import com.proyectofinal.fintech.application.port.in.ai.DraftAiActionUseCase;
import com.proyectofinal.fintech.application.port.in.ai.ExplainFraudUseCase;
import com.proyectofinal.fintech.application.port.out.ai.AiAuditPort;
import com.proyectofinal.fintech.application.port.out.ai.FintechAiAssistantPort;
import com.proyectofinal.fintech.application.usecase.ai.AiIntentValidator;
import com.proyectofinal.fintech.application.usecase.ai.AiObservability;
import com.proyectofinal.fintech.application.usecase.ai.AiResponseCache;
import com.proyectofinal.fintech.application.usecase.ai.AskFintechAiService;
import com.proyectofinal.fintech.application.usecase.ai.AuthorizationService;
import com.proyectofinal.fintech.application.usecase.ai.BuildFintechAiContextService;
import com.proyectofinal.fintech.application.usecase.ai.DraftAiActionService;
import com.proyectofinal.fintech.application.usecase.ai.ExplainFraudService;
import com.proyectofinal.fintech.application.usecase.ai.IntentClassifier;
import com.proyectofinal.fintech.domain.port.FraudEventRepository;
import com.proyectofinal.fintech.domain.port.NotificationRepository;
import com.proyectofinal.fintech.domain.port.ScheduledOperationRepository;
import com.proyectofinal.fintech.domain.port.TransactionRepository;
import com.proyectofinal.fintech.domain.port.UserRepository;
import com.proyectofinal.fintech.domain.port.WalletRepository;
import com.proyectofinal.fintech.application.usecase.GetUserPointsUseCase;
import com.proyectofinal.fintech.infrastructure.input.rest.dto.ai.AiDtoMapper;
import com.proyectofinal.fintech.infrastructure.output.ai.NoOpAiAuditAdapter;
import com.proyectofinal.fintech.infrastructure.output.ai.NoOpFintechAiAssistantAdapter;
import com.proyectofinal.fintech.infrastructure.output.ai.PromptResources;
import com.proyectofinal.fintech.infrastructure.output.ai.RestClientFintechAssistantAdapter;
import com.proyectofinal.fintech.infrastructure.output.ai.Slf4jAiAuditAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Clock;
import java.time.Duration;
import java.util.HexFormat;

/**
 * Spring configuration for AI feature.
 * Wires all AI beans: ports, use cases, adapters, cache, mappers.
 * NoOp adapters active in slice 1; OpenRouter adapters added in slice 2.
 */
@Configuration
@EnableConfigurationProperties({AiConfig.AiProperties.class, AiConfig.OpenRouterProperties.class})
public class AiConfig {

    private static final Logger log = LoggerFactory.getLogger(AiConfig.class);

    // ── Config properties ──────────────────────────────────────────────────────

    @ConfigurationProperties("app.ai")
    public record AiProperties(
            String provider,
            int maxUserMessageLength,
            int requestTimeoutSeconds,
            int cacheTtlMinutes
    ) {
    }

    @ConfigurationProperties("openrouter")
    public record OpenRouterProperties(
            String baseUrl,
            String apiKey,
            String model,
            String httpReferer,
            String appTitle
    ) {
    }

    // ── RestClient ─────────────────────────────────────────────────────────────

    @Bean
    public RestClient openRouterRestClient(AiProperties aiProperties,
                                            OpenRouterProperties openRouterProperties) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        int timeoutMs = aiProperties.requestTimeoutSeconds() * 1000;
        factory.setConnectTimeout(timeoutMs);
        factory.setReadTimeout(timeoutMs);

        RestClient.Builder builder = RestClient.builder()
                .baseUrl(openRouterProperties.baseUrl())
                .requestFactory(factory)
                .defaultHeader("Content-Type", "application/json");

        String apiKey = openRouterProperties.apiKey();
        if (apiKey != null && !apiKey.isBlank()) {
            builder.defaultHeader("Authorization", "Bearer " + apiKey);
        }
        if (openRouterProperties.httpReferer() != null && !openRouterProperties.httpReferer().isBlank()) {
            builder.defaultHeader("HTTP-Referer", openRouterProperties.httpReferer());
        }
        if (openRouterProperties.appTitle() != null && !openRouterProperties.appTitle().isBlank()) {
            builder.defaultHeader("X-Title", openRouterProperties.appTitle());
        }
        return builder.build();
    }

    // ── Prompt resources ───────────────────────────────────────────────────────

    @Bean
    public PromptResources promptResources() {
        return new PromptResources();
    }

    // ── Adapters ───────────────────────────────────────────────────────────────

    @Bean
    public FintechAiAssistantPort fintechAiAssistantPort(AiProperties aiProperties,
                                                          OpenRouterProperties openRouterProperties,
                                                          RestClient openRouterRestClient,
                                                          ObjectMapper objectMapper,
                                                          PromptResources promptResources) {
        String provider = aiProperties.provider();
        if ("openrouter".equals(provider)) {
            String apiKey = openRouterProperties.apiKey();
            if (apiKey == null || apiKey.isBlank()) {
                log.warn("[AI] provider=openrouter but OPENROUTER_API_KEY is blank — falling back to NoOp adapter");
                return new NoOpFintechAiAssistantAdapter();
            }
            log.info("[AI] Wiring RestClientFintechAssistantAdapter with model={}", openRouterProperties.model());
            return new RestClientFintechAssistantAdapter(
                    openRouterRestClient,
                    openRouterProperties.model(),
                    objectMapper,
                    promptResources);
        }
        log.info("[AI] provider={} — using NoOpFintechAiAssistantAdapter", provider);
        return new NoOpFintechAiAssistantAdapter();
    }

    @Bean
    public AiAuditPort aiAuditPort() {
        log.info("[AI] Wiring Slf4jAiAuditAdapter for structured audit logging");
        return new Slf4jAiAuditAdapter();
    }

    // ── BuildFintechAiContextService ───────────────────────────────────────────

    @Bean
    public BuildFintechAiContextService buildFintechAiContextService(
            UserRepository userRepository,
            WalletRepository walletRepository,
            TransactionRepository transactionRepository,
            FraudEventRepository fraudEventRepository,
            ScheduledOperationRepository scheduledOperationRepository,
            NotificationRepository notificationRepository,
            GetUserPointsUseCase getUserPointsUseCase,
            ObjectMapper objectMapper) {
        // jsonSerializer: serialize to JSON string using Jackson (infra concern — allowed here)
        java.util.function.Function<Object, String> jsonSerializer = obj -> {
            try {
                return objectMapper.writeValueAsString(obj);
            } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
                return "{}";
            }
        };
        // hasher: SHA-256 hex of the input string
        java.util.function.Function<String, String> hasher = input -> {
            try {
                byte[] bytes = MessageDigest.getInstance("SHA-256")
                        .digest(input.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                return HexFormat.of().formatHex(bytes);
            } catch (NoSuchAlgorithmException e) {
                return Integer.toHexString(input.hashCode());
            }
        };
        return new BuildFintechAiContextService(
                userRepository, walletRepository, transactionRepository,
                fraudEventRepository, scheduledOperationRepository, notificationRepository,
                getUserPointsUseCase, jsonSerializer, hasher);
    }

    // ── Cache ──────────────────────────────────────────────────────────────────

    @Bean
    public AiResponseCache aiResponseCache(AiProperties aiProperties) {
        return new AiResponseCache(
                Duration.ofMinutes(aiProperties.cacheTtlMinutes()),
                Clock.systemUTC());
    }

    // ── Application layer helpers ──────────────────────────────────────────────

    @Bean
    public IntentClassifier intentClassifier() {
        return new IntentClassifier();
    }

    @Bean
    public AiIntentValidator aiIntentValidator() {
        return new AiIntentValidator();
    }

    @Bean
    public AuthorizationService authorizationService() {
        return new AuthorizationService();
    }

    @Bean
    public AiObservability aiObservability() {
        return new AiObservability();
    }

    // ── Use cases ──────────────────────────────────────────────────────────────

    @Bean
    public AskFintechAiUseCase askFintechAiUseCase(
            AuthorizationService authorizationService,
            IntentClassifier intentClassifier,
            AiResponseCache aiResponseCache,
            FintechAiAssistantPort fintechAiAssistantPort,
            AiIntentValidator aiIntentValidator,
            AiObservability aiObservability,
            AiAuditPort aiAuditPort,
            BuildFintechAiContextService buildFintechAiContextService,
            AiProperties aiProperties) {
        return new AskFintechAiService(
                authorizationService,
                intentClassifier,
                aiResponseCache,
                fintechAiAssistantPort,
                aiIntentValidator,
                aiObservability,
                aiAuditPort,
                buildFintechAiContextService,
                aiProperties.maxUserMessageLength());
    }

    @Bean
    public ExplainFraudUseCase explainFraudUseCase(
            FraudEventRepository fraudEventRepository,
            FintechAiAssistantPort fintechAiAssistantPort,
            AiResponseCache aiResponseCache,
            AiObservability aiObservability,
            AiAuditPort aiAuditPort) {
        return new ExplainFraudService(
                fraudEventRepository,
                fintechAiAssistantPort,
                aiResponseCache,
                aiObservability,
                aiAuditPort);
    }

    @Bean
    public DraftAiActionUseCase draftAiActionUseCase(
            AuthorizationService authorizationService,
            IntentClassifier intentClassifier,
            FintechAiAssistantPort fintechAiAssistantPort,
            AiObservability aiObservability,
            AiAuditPort aiAuditPort,
            BuildFintechAiContextService buildFintechAiContextService,
            AiProperties aiProperties) {
        return new DraftAiActionService(
                authorizationService,
                intentClassifier,
                fintechAiAssistantPort,
                aiObservability,
                aiAuditPort,
                buildFintechAiContextService,
                aiProperties.maxUserMessageLength());
    }

    // ── REST mapper ────────────────────────────────────────────────────────────

    @Bean
    public AiDtoMapper aiDtoMapper() {
        return new AiDtoMapper();
    }
}
