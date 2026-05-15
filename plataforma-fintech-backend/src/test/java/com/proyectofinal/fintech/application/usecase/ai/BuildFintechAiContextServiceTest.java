package com.proyectofinal.fintech.application.usecase.ai;

import com.proyectofinal.fintech.application.port.in.ai.AskFintechAiUseCase.AskFintechAiCommand;
import com.proyectofinal.fintech.application.result.PointsView;
import com.proyectofinal.fintech.application.result.ai.AiScope;
import com.proyectofinal.fintech.application.result.ai.FintechAiContext;
import com.proyectofinal.fintech.application.usecase.GetUserPointsUseCase;
import com.proyectofinal.fintech.domain.model.*;
import com.proyectofinal.fintech.domain.port.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * T26 — BuildFintechAiContextService unit tests.
 * Validates that context is correctly assembled for USER and ADMIN scopes.
 * Uses Mockito to stub repositories; does NOT use Spring context.
 */
@ExtendWith(MockitoExtension.class)
class BuildFintechAiContextServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private WalletRepository walletRepository;
    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private FraudEventRepository fraudEventRepository;
    @Mock
    private ScheduledOperationRepository scheduledOperationRepository;
    @Mock
    private NotificationRepository notificationRepository;
    @Mock
    private GetUserPointsUseCase getUserPointsUseCase;

    private BuildFintechAiContextService service;

    @BeforeEach
    void setUp() {
        // jsonSerializer: just return a stable string so we can verify contextHash is set
        Function<Object, String> jsonSerializer = obj -> "{\"stub\":\"json\"}";
        // hasher: SHA-256 of "{\"stub\":\"json\"}" — precomputed or just identity for test
        Function<String, String> hasher = s -> "sha256:" + s.length();
        // Default stub: return empty points (tests that need real values stub individually)
        lenient().when(getUserPointsUseCase.execute(any()))
                .thenReturn(new PointsView("any", 0.0, LoyaltyLevel.BRONZE));
        service = new BuildFintechAiContextService(
                userRepository, walletRepository, transactionRepository,
                fraudEventRepository, scheduledOperationRepository,
                notificationRepository, getUserPointsUseCase, jsonSerializer, hasher);
    }

    @Test
    void buildForUser_returnsContextWithUserScope() {
        String userId = "u1";
        setupEmptyReposForUser(userId);

        AskFintechAiCommand command = new AskFintechAiCommand(
                userId, "USER", AiScope.USER, "conv1", "my balance", "last 7 days");

        FintechAiContext ctx = service.build(command);

        assertNotNull(ctx);
        assertEquals("USER", ctx.actorRole());
        assertEquals(AiScope.USER, ctx.scope());
        assertNotNull(ctx.userFinancials());
        assertEquals(userId, ctx.userFinancials().userId());
        assertNotNull(ctx.contextHash());
        assertFalse(ctx.contextHash().isBlank());
    }

    @Test
    void buildForUser_populatesWalletIds() {
        String userId = "u1";
        com.proyectofinal.fintech.domain.structures.MiLista<Billetera> wallets =
                new com.proyectofinal.fintech.domain.structures.MiLista<>();
        Billetera w1 = new Billetera("w-1", "Main", "savings", userId, 1000.0, true,
                Instant.now(), 5);
        wallets.add(w1);
        when(walletRepository.findByOwnerId(userId)).thenReturn(wallets);
        when(walletRepository.sumBalanceByOwnerId(userId)).thenReturn(1000.0);
        when(transactionRepository.findByUserId(userId)).thenReturn(List.of());
        when(fraudEventRepository.findByUserId(userId)).thenReturn(List.of());
        when(scheduledOperationRepository.findAll()).thenReturn(List.of());
        when(notificationRepository.findByUserId(userId, true)).thenReturn(List.of());

        AskFintechAiCommand command = new AskFintechAiCommand(
                userId, "USER", AiScope.USER, "conv1", "my wallets", "last 7 days");
        FintechAiContext ctx = service.build(command);

        assertNotNull(ctx.userFinancials());
        assertFalse(ctx.userFinancials().walletIds().isEmpty());
    }

    @Test
    void buildForAdmin_doesNotPopulateUserFinancials() {
        // ADMIN scope has no specific user context
        when(transactionRepository.findAll()).thenReturn(List.of());
        when(fraudEventRepository.findAll()).thenReturn(List.of());
        when(scheduledOperationRepository.findAll()).thenReturn(List.of());

        AskFintechAiCommand command = new AskFintechAiCommand(
                "admin1", "ADMIN", AiScope.ADMIN, "conv1", "show analytics", "last 7 days");
        FintechAiContext ctx = service.build(command);

        assertNotNull(ctx);
        assertEquals("ADMIN", ctx.actorRole());
        assertEquals(AiScope.ADMIN, ctx.scope());
        assertNull(ctx.userFinancials(), "ADMIN scope must not have userFinancials");
    }

    @Test
    void build_contextHashIsNotNull() {
        String userId = "u1";
        setupEmptyReposForUser(userId);

        AskFintechAiCommand command = new AskFintechAiCommand(
                userId, "USER", AiScope.USER, null, "test", null);
        FintechAiContext ctx = service.build(command);

        assertNotNull(ctx.contextHash());
    }

    @Test
    void build_fraudSnapshotIncludesFraudEventCount() {
        String userId = "u1";
        FraudEvent fe = new FraudEvent("fe-1", userId, null, "LARGE_TRANSACTION",
                FraudSeverity.HIGH, "suspicious", Instant.now());
        when(walletRepository.findByOwnerId(userId)).thenReturn(
                new com.proyectofinal.fintech.domain.structures.MiLista<>());
        when(walletRepository.sumBalanceByOwnerId(userId)).thenReturn(0.0);
        when(transactionRepository.findByUserId(userId)).thenReturn(List.of());
        when(fraudEventRepository.findByUserId(userId)).thenReturn(List.of(fe));
        when(scheduledOperationRepository.findAll()).thenReturn(List.of());
        when(notificationRepository.findByUserId(userId, true)).thenReturn(List.of());

        AskFintechAiCommand command = new AskFintechAiCommand(
                userId, "USER", AiScope.USER, null, "fraud", null);
        FintechAiContext ctx = service.build(command);

        assertEquals(1, ctx.fraud().totalFraudEvents());
        assertFalse(ctx.fraud().recentFraudEventIds().isEmpty());
    }

    /**
     * T26-loyalty — RF-AI-05: when a user has 840 points and SILVER level,
     * the context snapshot reflects those values.
     * This test is RED until GetUserPointsUseCase is injected into BuildFintechAiContextService.
     */
    @Test
    void buildForUser_populatesLoyaltyPointsAndLevel() {
        String userId = "u-silver";
        // Stub loyalty use case to return 840 points at SILVER
        when(getUserPointsUseCase.execute(userId))
                .thenReturn(new PointsView(userId, 840.0, LoyaltyLevel.SILVER));
        setupEmptyReposForUser(userId);

        AskFintechAiCommand command = new AskFintechAiCommand(
                userId, "USER", AiScope.USER, "conv-loyalty", "mis puntos", "last 7 days");
        FintechAiContext ctx = service.build(command);

        assertNotNull(ctx.userFinancials());
        assertEquals(840, ctx.userFinancials().loyaltyPoints(),
                "loyaltyPoints should reflect the 840 points from GetUserPointsUseCase");
        assertEquals("SILVER", ctx.userFinancials().loyaltyLevel(),
                "loyaltyLevel should reflect SILVER from GetUserPointsUseCase");
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private void setupEmptyReposForUser(String userId) {
        when(walletRepository.findByOwnerId(userId)).thenReturn(
                new com.proyectofinal.fintech.domain.structures.MiLista<>());
        when(walletRepository.sumBalanceByOwnerId(userId)).thenReturn(0.0);
        when(transactionRepository.findByUserId(userId)).thenReturn(List.of());
        when(fraudEventRepository.findByUserId(userId)).thenReturn(List.of());
        when(scheduledOperationRepository.findAll()).thenReturn(List.of());
        when(notificationRepository.findByUserId(userId, true)).thenReturn(List.of());
    }
}
