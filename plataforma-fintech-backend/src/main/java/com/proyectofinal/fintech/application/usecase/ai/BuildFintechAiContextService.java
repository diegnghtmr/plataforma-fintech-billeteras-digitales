package com.proyectofinal.fintech.application.usecase.ai;

import com.proyectofinal.fintech.application.port.in.ai.AskFintechAiUseCase.AskFintechAiCommand;
import com.proyectofinal.fintech.application.result.PointsView;
import com.proyectofinal.fintech.application.result.ai.*;
import com.proyectofinal.fintech.application.usecase.GetUserPointsUseCase;
import com.proyectofinal.fintech.domain.model.*;
import com.proyectofinal.fintech.domain.port.*;
import com.proyectofinal.fintech.domain.structures.MiLista;

import java.util.Objects;
import java.util.function.Function;

/**
 * Application service that builds FintechAiContext from the data stores.
 * <p>
 * Dispatches on command scope to build USER or ADMIN context.
 * Inputs from repositories may return JDK Iterables — wrapped to MiLista at the boundary
 * (single wrap site per field, compliant with ADR-9.1).
 * <p>
 * jsonSerializer and hasher injected as Function SPIs so the application layer stays Spring-free
 * (Jackson ObjectMapper wired in AiConfig, not here).
 * <p>
 * Zero Spring imports. Constructor-injected.
 */
public class BuildFintechAiContextService {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final FraudEventRepository fraudEventRepository;
    private final ScheduledOperationRepository scheduledOperationRepository;
    private final NotificationRepository notificationRepository;
    private final GetUserPointsUseCase getUserPointsUseCase;
    private final Function<Object, String> jsonSerializer;
    private final Function<String, String> hasher;

    public BuildFintechAiContextService(
            UserRepository userRepository,
            WalletRepository walletRepository,
            TransactionRepository transactionRepository,
            FraudEventRepository fraudEventRepository,
            ScheduledOperationRepository scheduledOperationRepository,
            NotificationRepository notificationRepository,
            GetUserPointsUseCase getUserPointsUseCase,
            Function<Object, String> jsonSerializer,
            Function<String, String> hasher) {
        this.userRepository = Objects.requireNonNull(userRepository);
        this.walletRepository = Objects.requireNonNull(walletRepository);
        this.transactionRepository = Objects.requireNonNull(transactionRepository);
        this.fraudEventRepository = Objects.requireNonNull(fraudEventRepository);
        this.scheduledOperationRepository = Objects.requireNonNull(scheduledOperationRepository);
        this.notificationRepository = Objects.requireNonNull(notificationRepository);
        this.getUserPointsUseCase = Objects.requireNonNull(getUserPointsUseCase);
        this.jsonSerializer = Objects.requireNonNull(jsonSerializer);
        this.hasher = Objects.requireNonNull(hasher);
    }

    /**
     * Dispatches by scope to build the appropriate context.
     */
    public FintechAiContext build(AskFintechAiCommand command) {
        Objects.requireNonNull(command, "command must not be null");
        String range = command.range() != null ? command.range() : "last 7 days";

        if (command.scope() == AiScope.ADMIN) {
            return buildForAdmin(command.actorRole(), range);
        }
        return buildForUser(command.actorUserId(), command.actorRole(), range);
    }

    /**
     * Builds a USER-scoped context: user financial snapshot + user-specific fraud/notifications.
     */
    public FintechAiContext buildForUser(String userId, String actorRole, String range) {
        // ── User financial snapshot ─────────────────────────────────────────────
        MiLista<String> walletIds = new MiLista<>();
        for (Billetera w : walletRepository.findByOwnerId(userId)) {
            walletIds.add(w.getCode());
        }
        double totalBalance = walletRepository.sumBalanceByOwnerId(userId);

        int txCount = 0;
        for (Transaccion ignored : transactionRepository.findByUserId(userId)) {
            txCount++;
        }

        // Loyalty info — retrieve via GetUserPointsUseCase (RF-AI-05)
        int loyaltyPoints = 0;
        String loyaltyLevel = "BRONZE";
        try {
            PointsView pv = getUserPointsUseCase.execute(userId);
            loyaltyPoints = (int) pv.points();
            loyaltyLevel = pv.loyaltyLevel() != null ? pv.loyaltyLevel().name() : "BRONZE";
        } catch (Exception ignored) {
            // If loyalty data is unavailable, keep defaults (RF-AI-05 degradation clause)
        }

        UserFinancialSnapshot userFinancials = new UserFinancialSnapshot(
                userId, totalBalance, walletIds, txCount, loyaltyPoints, loyaltyLevel);

        // ── Fraud snapshot (user-scoped) ────────────────────────────────────────
        MiLista<String> recentFraudIds = new MiLista<>();
        int fraudCount = 0;
        for (FraudEvent fe : fraudEventRepository.findByUserId(userId)) {
            fraudCount++;
            if (fraudCount <= 5) {
                recentFraudIds.add(fe.getId());
            }
        }
        FraudSnapshot fraudSnapshot = new FraudSnapshot(fraudCount, recentFraudIds);

        // ── Scheduled operations (all, pending, user-relevant) ─────────────────
        MiLista<String> pendingOpIds = new MiLista<>();
        int pendingCount = 0;
        for (OperacionProgramada op : scheduledOperationRepository.findAll()) {
            if (op.getStatus() == ScheduledOperationStatus.PENDING
                    && userId.equals(op.getSourceUserId())) {
                pendingCount++;
                if (pendingCount <= 5) {
                    pendingOpIds.add(op.getId());
                }
            }
        }
        ScheduledOperationsSnapshot scheduledSnapshot =
                new ScheduledOperationsSnapshot(pendingCount, pendingOpIds);

        // ── Notifications (unread) ──────────────────────────────────────────────
        MiLista<String> recentNotifIds = new MiLista<>();
        int unreadCount = 0;
        for (Notificacion n : notificationRepository.findByUserId(userId, true)) {
            unreadCount++;
            if (unreadCount <= 5) {
                recentNotifIds.add(n.getId());
            }
        }
        NotificationSnapshot notificationSnapshot = new NotificationSnapshot(unreadCount, recentNotifIds);

        // ── Analytics snapshot (minimal for USER scope) ─────────────────────────
        AnalyticsSnapshot analyticsSnapshot = new AnalyticsSnapshot(
                new MiLista<>(), new MiLista<>(), new MiLista<>(), txCount);

        DataQualityNotes notes = new DataQualityNotes("user-scope");

        // ── Context hash ──────────────────────────────────────────────────────
        String contextHash = computeHash(userId, totalBalance, fraudCount, unreadCount, pendingCount);

        return new FintechAiContext(
                actorRole, AiScope.USER, range,
                analyticsSnapshot, userFinancials,
                fraudSnapshot, scheduledSnapshot,
                notificationSnapshot, notes, contextHash);
    }

    /**
     * Builds an ADMIN-scoped context: global analytics, no user-specific data.
     */
    public FintechAiContext buildForAdmin(String actorRole, String range) {
        // ── Global analytics ────────────────────────────────────────────────────
        int totalTxCount = 0;
        for (Transaccion ignored : transactionRepository.findAll()) {
            totalTxCount++;
        }

        // Top users by activity (first 5 user IDs found in transactions)
        MiLista<String> topUserIds = new MiLista<>();
        com.proyectofinal.fintech.domain.structures.TablaHash<String, Integer> userCounts =
                new com.proyectofinal.fintech.domain.structures.TablaHash<>();
        for (Transaccion tx : transactionRepository.findAll()) {
            if (tx.getSourceUserId() != null) {
                int current = userCounts.get(tx.getSourceUserId()).orElse(0);
                userCounts.put(tx.getSourceUserId(), current + 1);
            }
        }
        int added = 0;
        for (String uid : userCounts.keys()) {
            if (added < 5) {
                topUserIds.add(uid);
                added++;
            }
        }

        // Movement by type labels
        MiLista<String> movementByType = new MiLista<>();
        com.proyectofinal.fintech.domain.structures.TablaHash<String, Integer> typeCounts =
                new com.proyectofinal.fintech.domain.structures.TablaHash<>();
        for (Transaccion tx : transactionRepository.findAll()) {
            if (tx.getType() != null) {
                String key = tx.getType().name();
                int current = typeCounts.get(key).orElse(0);
                typeCounts.put(key, current + 1);
            }
        }
        for (String type : typeCounts.keys()) {
            int count = typeCounts.get(type).orElse(0);
            movementByType.add(type + ":" + count);
        }

        AnalyticsSnapshot analyticsSnapshot = new AnalyticsSnapshot(
                topUserIds, movementByType, new MiLista<>(), totalTxCount);

        // ── Global fraud snapshot ───────────────────────────────────────────────
        MiLista<String> recentFraudIds = new MiLista<>();
        int fraudCount = 0;
        for (FraudEvent fe : fraudEventRepository.findAll()) {
            fraudCount++;
            if (fraudCount <= 5) {
                recentFraudIds.add(fe.getId());
            }
        }
        FraudSnapshot fraudSnapshot = new FraudSnapshot(fraudCount, recentFraudIds);

        // ── Scheduled ops (all pending) ────────────────────────────────────────
        MiLista<String> pendingOpIds = new MiLista<>();
        int pendingCount = 0;
        for (OperacionProgramada op : scheduledOperationRepository.findAll()) {
            if (op.getStatus() == ScheduledOperationStatus.PENDING) {
                pendingCount++;
                if (pendingCount <= 5) pendingOpIds.add(op.getId());
            }
        }
        ScheduledOperationsSnapshot scheduledSnapshot =
                new ScheduledOperationsSnapshot(pendingCount, pendingOpIds);

        DataQualityNotes notes = new DataQualityNotes("admin-scope");

        String contextHash = computeHash("admin", totalTxCount, fraudCount, 0, pendingCount);

        return new FintechAiContext(
                actorRole, AiScope.ADMIN, range,
                analyticsSnapshot, null, // no user-specific data for ADMIN
                fraudSnapshot, scheduledSnapshot,
                new NotificationSnapshot(0, new MiLista<>()),
                notes, contextHash);
    }

    // ── Internal helpers ──────────────────────────────────────────────────────

    private String computeHash(String userId, double totalBalance, int fraudCount,
                                int unreadCount, int pendingCount) {
        // Build a minimal serializable object for hashing
        String input = userId + "|" + totalBalance + "|" + fraudCount
                + "|" + unreadCount + "|" + pendingCount;
        try {
            String json = jsonSerializer.apply(input);
            return hasher.apply(json);
        } catch (Exception e) {
            // If serialization fails, fall back to a simple hash
            return "fallback:" + input.hashCode();
        }
    }
}
