package com.proyectofinal.fintech.application.usecase;

import com.proyectofinal.fintech.application.result.MetricItem;
import com.proyectofinal.fintech.domain.model.*;
import com.proyectofinal.fintech.domain.port.TransactionRepository;
import com.proyectofinal.fintech.domain.port.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * T09-B13 (RED) — GetTopUsersUseCase tests.
 */
@ExtendWith(MockitoExtension.class)
class GetTopUsersUseCaseTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private TransactionRepository transactionRepository;

    private GetTopUsersUseCase useCase;
    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

    @BeforeEach
    void setUp() {
        useCase = new GetTopUsersUseCase(userRepository, transactionRepository);
    }

    private Usuario makeUser(String id, String name) {
        return new Usuario(id, name, id + "@test.com", NOW, 0.0, LoyaltyLevel.BRONZE);
    }

    private Transaccion makeTx(String sourceUserId, TransactionType type) {
        return new Transaccion("TX-" + Math.random(), NOW, type, 100.0,
                "W1", null, sourceUserId, null,
                TransactionStatus.SUCCESSFUL, 0.0, null, false);
    }

    @Test
    void execute_returnsUsersDescendingByTransactionCount() {
        Usuario userA = makeUser("USR_A", "Ana");
        Usuario userB = makeUser("USR_B", "Bob");

        when(userRepository.findAll()).thenReturn(List.of(userA, userB));
        when(transactionRepository.findAll()).thenReturn(List.of(
                makeTx("USR_A", TransactionType.RECHARGE),
                makeTx("USR_A", TransactionType.WITHDRAWAL),
                makeTx("USR_A", TransactionType.RECHARGE),
                makeTx("USR_A", TransactionType.INTERNAL_TRANSFER),
                makeTx("USR_A", TransactionType.EXTERNAL_TRANSFER_SENT),
                makeTx("USR_B", TransactionType.RECHARGE),
                makeTx("USR_B", TransactionType.WITHDRAWAL),
                makeTx("USR_B", TransactionType.RECHARGE)
        ));

        List<MetricItem> result = useCase.execute(10);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).id()).isEqualTo("USR_A");
        assertThat(result.get(0).value()).isEqualTo(5.0);
        assertThat(result.get(1).id()).isEqualTo("USR_B");
        assertThat(result.get(1).value()).isEqualTo(3.0);
    }

    @Test
    void execute_limitCapsResultSize() {
        List<Usuario> users = List.of(
                makeUser("USR_1", "User1"), makeUser("USR_2", "User2"),
                makeUser("USR_3", "User3"), makeUser("USR_4", "User4"),
                makeUser("USR_5", "User5")
        );
        when(userRepository.findAll()).thenReturn(users);
        when(transactionRepository.findAll()).thenReturn(List.of(
                makeTx("USR_1", TransactionType.RECHARGE),
                makeTx("USR_2", TransactionType.RECHARGE),
                makeTx("USR_3", TransactionType.RECHARGE),
                makeTx("USR_4", TransactionType.RECHARGE),
                makeTx("USR_5", TransactionType.RECHARGE)
        ));

        List<MetricItem> result = useCase.execute(3);
        assertThat(result).hasSize(3);
    }

    @Test
    void execute_tieBrokenByUserIdAscending() {
        Usuario userX = makeUser("USR_X", "X");
        Usuario userY = makeUser("USR_Y", "Y");

        when(userRepository.findAll()).thenReturn(List.of(userX, userY));
        when(transactionRepository.findAll()).thenReturn(List.of(
                makeTx("USR_X", TransactionType.RECHARGE),
                makeTx("USR_X", TransactionType.WITHDRAWAL),
                makeTx("USR_Y", TransactionType.RECHARGE),
                makeTx("USR_Y", TransactionType.WITHDRAWAL)
        ));

        List<MetricItem> result = useCase.execute(10);

        // Both have 2 transactions; tie-break by userId ascending
        assertThat(result.get(0).id()).isEqualTo("USR_X");
        assertThat(result.get(1).id()).isEqualTo("USR_Y");
    }

    @Test
    void execute_excludesExternalTransferReceived() {
        Usuario userA = makeUser("USR_A", "Ana");
        Usuario userB = makeUser("USR_B", "Bob");

        when(userRepository.findAll()).thenReturn(List.of(userA, userB));
        // USR_B has EXTERNAL_TRANSFER_RECEIVED (should not count) + 1 RECHARGE
        when(transactionRepository.findAll()).thenReturn(List.of(
                makeTx("USR_A", TransactionType.RECHARGE),
                makeTx("USR_A", TransactionType.RECHARGE),
                makeTx("USR_B", TransactionType.RECHARGE),
                new Transaccion("TX-R", NOW, TransactionType.EXTERNAL_TRANSFER_RECEIVED, 200.0,
                        "W_A", "W_B", "USR_A", "USR_B",
                        TransactionStatus.SUCCESSFUL, 0.0, null, false)
        ));

        List<MetricItem> result = useCase.execute(10);

        assertThat(result.get(0).id()).isEqualTo("USR_A");
        assertThat(result.get(0).value()).isEqualTo(2.0);
        assertThat(result.get(1).id()).isEqualTo("USR_B");
        assertThat(result.get(1).value()).isEqualTo(1.0);
    }
}
