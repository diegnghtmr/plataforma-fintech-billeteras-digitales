package com.proyectofinal.fintech.application.usecase;

import com.proyectofinal.fintech.application.result.AnalyticsSummaryView;
import com.proyectofinal.fintech.domain.model.*;
import com.proyectofinal.fintech.domain.port.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import com.proyectofinal.fintech.domain.model.OperacionProgramada;
import com.proyectofinal.fintech.domain.model.ScheduledOperationStatus;
import com.proyectofinal.fintech.domain.model.ScheduledOperationType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * T09-B19 (RED) — GetAnalyticsSummaryUseCase tests.
 */
@ExtendWith(MockitoExtension.class)
class GetAnalyticsSummaryUseCaseTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private WalletRepository walletRepository;
    @Mock
    private TransactionRepository transactionRepository;
    @Mock
    private FraudEventRepository fraudEventRepository;
    @Mock
    private NotificationRepository notificationRepository;
    @Mock
    private ScheduledOperationRepository scheduledOperationRepository;

    private GetAnalyticsSummaryUseCase useCase;
    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

    @BeforeEach
    void setUp() {
        useCase = new GetAnalyticsSummaryUseCase(userRepository, walletRepository,
                transactionRepository, fraudEventRepository, notificationRepository,
                scheduledOperationRepository);
    }

    private Usuario makeUser(String id) {
        return new Usuario(id, id, id + "@t.com", NOW, 0.0, LoyaltyLevel.BRONZE);
    }

    private Transaccion makeTx(String id, TransactionType type, double amount, TransactionStatus status) {
        return new Transaccion(id, NOW, type, amount, "W1", null, "USR_A", null, status, 0.0, null, false);
    }

    private Notificacion makeNotif(String id, String userId, boolean read) {
        return new Notificacion(id, userId, NotificationType.TRANSACTION,
                NotificationSeverity.INFO, "Title", "MSG", read, NOW);
    }

    @Test
    void execute_summaryReflectsSeededData() {
        Usuario u1 = makeUser("USR_A");
        Usuario u2 = makeUser("USR_B");
        Usuario u3 = makeUser("USR_C");

        when(userRepository.findAll()).thenReturn(List.of(u1, u2, u3));
        when(walletRepository.countByOwnerId("USR_A")).thenReturn(2);
        when(walletRepository.countByOwnerId("USR_B")).thenReturn(2);
        when(walletRepository.countByOwnerId("USR_C")).thenReturn(1);

        when(transactionRepository.findAll()).thenReturn(List.of(
                makeTx("T1", TransactionType.RECHARGE, 100.0, TransactionStatus.SUCCESSFUL),
                makeTx("T2", TransactionType.WITHDRAWAL, 50.0, TransactionStatus.SUCCESSFUL),
                makeTx("T3", TransactionType.INTERNAL_TRANSFER, 200.0, TransactionStatus.SUCCESSFUL),
                makeTx("T4", TransactionType.EXTERNAL_TRANSFER_SENT, 300.0, TransactionStatus.SUCCESSFUL),
                makeTx("T5", TransactionType.EXTERNAL_TRANSFER_RECEIVED, 300.0, TransactionStatus.SUCCESSFUL),
                makeTx("T6", TransactionType.RECHARGE, 400.0, TransactionStatus.REVERSED),
                makeTx("T7", TransactionType.RECHARGE, 150.0, TransactionStatus.SUCCESSFUL)
        ));

        when(fraudEventRepository.count()).thenReturn(2);

        // Unread notifications
        when(notificationRepository.findByUserId("USR_A", true)).thenReturn(List.of(
                makeNotif("N1", "USR_A", false),
                makeNotif("N2", "USR_A", false)
        ));
        when(notificationRepository.findByUserId("USR_B", true)).thenReturn(List.of(
                makeNotif("N3", "USR_B", false)
        ));
        when(notificationRepository.findByUserId("USR_C", true)).thenReturn(List.of());

        // Pending scheduled operations (2 PENDING, 1 EXECUTED — only PENDING counts)
        when(scheduledOperationRepository.findAll()).thenReturn(List.of(
                makeScheduledOp("SO1", ScheduledOperationStatus.PENDING),
                makeScheduledOp("SO2", ScheduledOperationStatus.PENDING),
                makeScheduledOp("SO3", ScheduledOperationStatus.EXECUTED)
        ));

        AnalyticsSummaryView summary = useCase.execute();

        assertThat(summary.totalUsers()).isEqualTo(3);
        assertThat(summary.totalWallets()).isEqualTo(5); // 2+2+1
        assertThat(summary.totalTransactions()).isEqualTo(7);
        // totalMovedAmount: T1(100)+T2(50)+T3(200)+T4(300)+T7(150) = 800; T5 RECEIVED excluded; T6 REVERSED excluded
        assertThat(summary.totalMovedAmount()).isEqualTo(800.0);
        assertThat(summary.fraudEventCount()).isEqualTo(2);
        assertThat(summary.unreadNotificationCount()).isEqualTo(3);
        assertThat(summary.pendingScheduledOperations()).isEqualTo(2);
    }

    @Test
    void execute_totalMovedAmount_excludesExternalTransferReceived() {
        when(userRepository.findAll()).thenReturn(List.of(makeUser("USR_A")));
        when(walletRepository.countByOwnerId("USR_A")).thenReturn(1);
        when(notificationRepository.findByUserId("USR_A", true)).thenReturn(List.of());
        when(fraudEventRepository.count()).thenReturn(0);
        when(scheduledOperationRepository.findAll()).thenReturn(List.of());

        when(transactionRepository.findAll()).thenReturn(List.of(
                makeTx("T1", TransactionType.EXTERNAL_TRANSFER_SENT, 500.0, TransactionStatus.SUCCESSFUL),
                makeTx("T2", TransactionType.EXTERNAL_TRANSFER_RECEIVED, 500.0, TransactionStatus.SUCCESSFUL)
        ));

        AnalyticsSummaryView summary = useCase.execute();
        // Only SENT counts, not RECEIVED
        assertThat(summary.totalMovedAmount()).isEqualTo(500.0);
    }

    @Test
    void execute_pendingScheduledOperations_excludesExecutedAndCancelled() {
        when(userRepository.findAll()).thenReturn(List.of());
        when(transactionRepository.findAll()).thenReturn(List.of());
        when(fraudEventRepository.count()).thenReturn(0);

        when(scheduledOperationRepository.findAll()).thenReturn(List.of(
                makeScheduledOp("SO1", ScheduledOperationStatus.PENDING),
                makeScheduledOp("SO2", ScheduledOperationStatus.EXECUTED),
                makeScheduledOp("SO3", ScheduledOperationStatus.CANCELLED),
                makeScheduledOp("SO4", ScheduledOperationStatus.FAILED),
                makeScheduledOp("SO5", ScheduledOperationStatus.PENDING)
        ));

        AnalyticsSummaryView summary = useCase.execute();
        assertThat(summary.pendingScheduledOperations()).isEqualTo(2);
    }

    private OperacionProgramada makeScheduledOp(String id, ScheduledOperationStatus status) {
        return new OperacionProgramada(id, ScheduledOperationType.RECHARGE, status,
                "USR_A", "W1", null, null, 100.0, NOW, null);
    }
}
