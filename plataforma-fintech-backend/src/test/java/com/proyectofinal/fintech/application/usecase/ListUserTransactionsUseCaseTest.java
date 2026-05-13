package com.proyectofinal.fintech.application.usecase;

import com.proyectofinal.fintech.domain.exception.ErrorCode;
import com.proyectofinal.fintech.domain.exception.NotFoundException;
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
import java.util.Optional;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * T06-B03 (RED) — ListUserTransactionsUseCase tests.
 */
@ExtendWith(MockitoExtension.class)
class ListUserTransactionsUseCaseTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private TransactionRepository transactionRepository;

    private ListUserTransactionsUseCase useCase;

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

    @BeforeEach
    void setUp() {
        useCase = new ListUserTransactionsUseCase(userRepository, transactionRepository);
    }

    private Transaccion makeTx(String id, TransactionType type, TransactionStatus status) {
        return new Transaccion(id, NOW, type, 100.0,
                "W001", null, "USR001", null,
                status, 1.0, null, true);
    }

    // list all — no filters → returns all 3
    @Test
    void execute_noFilters_returnsAll() {
        Usuario user = new Usuario("USR001", "Ana", "ana@test.com", NOW, 0.0, LoyaltyLevel.BRONZE);
        List<Transaccion> txs = List.of(
                makeTx("TX-001", TransactionType.RECHARGE, TransactionStatus.SUCCESSFUL),
                makeTx("TX-002", TransactionType.WITHDRAWAL, TransactionStatus.SUCCESSFUL),
                makeTx("TX-003", TransactionType.RECHARGE, TransactionStatus.REVERSED)
        );

        when(userRepository.findById("USR001")).thenReturn(Optional.of(user));
        when(transactionRepository.findByUserId("USR001")).thenReturn(txs);

        Iterable<Transaccion> result = useCase.execute("USR001", Optional.empty(), Optional.empty());
        List<Transaccion> list = toList(result);

        assertThat(list).hasSize(3);
    }

    // filter by type=RECHARGE → returns 2
    @Test
    void execute_filterByType_returnsOnlyMatchingType() {
        Usuario user = new Usuario("USR001", "Ana", "ana@test.com", NOW, 0.0, LoyaltyLevel.BRONZE);
        List<Transaccion> txs = List.of(
                makeTx("TX-001", TransactionType.RECHARGE, TransactionStatus.SUCCESSFUL),
                makeTx("TX-002", TransactionType.WITHDRAWAL, TransactionStatus.SUCCESSFUL),
                makeTx("TX-003", TransactionType.RECHARGE, TransactionStatus.SUCCESSFUL)
        );

        when(userRepository.findById("USR001")).thenReturn(Optional.of(user));
        when(transactionRepository.findByUserId("USR001")).thenReturn(txs);

        Iterable<Transaccion> result = useCase.execute("USR001",
                Optional.of(TransactionType.RECHARGE), Optional.empty());
        List<Transaccion> list = toList(result);

        assertThat(list).hasSize(2);
        assertThat(list).allMatch(tx -> tx.getType() == TransactionType.RECHARGE);
    }

    // filter by status=REVERSED → returns 1
    @Test
    void execute_filterByStatus_returnsOnlyMatchingStatus() {
        Usuario user = new Usuario("USR001", "Ana", "ana@test.com", NOW, 0.0, LoyaltyLevel.BRONZE);
        List<Transaccion> txs = List.of(
                makeTx("TX-001", TransactionType.RECHARGE, TransactionStatus.SUCCESSFUL),
                makeTx("TX-002", TransactionType.WITHDRAWAL, TransactionStatus.REVERSED)
        );

        when(userRepository.findById("USR001")).thenReturn(Optional.of(user));
        when(transactionRepository.findByUserId("USR001")).thenReturn(txs);

        Iterable<Transaccion> result = useCase.execute("USR001",
                Optional.empty(), Optional.of(TransactionStatus.REVERSED));
        List<Transaccion> list = toList(result);

        assertThat(list).hasSize(1);
        assertThat(list.get(0).getStatus()).isEqualTo(TransactionStatus.REVERSED);
    }

    // filter by both type and status → returns only matching
    @Test
    void execute_filterByTypeAndStatus_returnsBothApplied() {
        Usuario user = new Usuario("USR001", "Ana", "ana@test.com", NOW, 0.0, LoyaltyLevel.BRONZE);
        List<Transaccion> txs = List.of(
                makeTx("TX-001", TransactionType.RECHARGE, TransactionStatus.SUCCESSFUL),
                makeTx("TX-002", TransactionType.RECHARGE, TransactionStatus.REVERSED),
                makeTx("TX-003", TransactionType.WITHDRAWAL, TransactionStatus.SUCCESSFUL)
        );

        when(userRepository.findById("USR001")).thenReturn(Optional.of(user));
        when(transactionRepository.findByUserId("USR001")).thenReturn(txs);

        Iterable<Transaccion> result = useCase.execute("USR001",
                Optional.of(TransactionType.RECHARGE), Optional.of(TransactionStatus.SUCCESSFUL));
        List<Transaccion> list = toList(result);

        assertThat(list).hasSize(1);
        assertThat(list.get(0).getId()).isEqualTo("TX-001");
    }

    // user not found → 404 USER_NOT_FOUND
    @Test
    void execute_userNotFound_throwsNotFound() {
        when(userRepository.findById("GHOST")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute("GHOST", Optional.empty(), Optional.empty()))
                .isInstanceOf(NotFoundException.class)
                .satisfies(ex -> assertThat(((NotFoundException) ex).code())
                        .isEqualTo(ErrorCode.USER_NOT_FOUND));
        verifyNoInteractions(transactionRepository);
    }

    private static List<Transaccion> toList(Iterable<Transaccion> it) {
        return StreamSupport.stream(it.spliterator(), false).toList();
    }
}
