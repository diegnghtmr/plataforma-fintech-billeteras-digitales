package com.proyectofinal.fintech.application.usecase;

import com.proyectofinal.fintech.domain.model.*;
import com.proyectofinal.fintech.domain.port.TransactionRepository;
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
 * T11-G01 (RED→GREEN) — GetTopTransactionsUseCase tests.
 */
@ExtendWith(MockitoExtension.class)
class GetTopTransactionsUseCaseTest {

    @Mock private TransactionRepository transactionRepository;

    private GetTopTransactionsUseCase useCase;

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

    @BeforeEach
    void setUp() {
        useCase = new GetTopTransactionsUseCase(transactionRepository);
    }

    private Transaccion makeTx(String id, double amount, TransactionStatus status) {
        return new Transaccion(id, NOW, TransactionType.RECHARGE, amount,
                "W001", null, "USR001", null, status, 1.0, null, true);
    }

    // S-G01: top 3 from 5 distinct amounts
    @Test
    void execute_fiveTransactions_returnsTop3Descending() {
        when(transactionRepository.findAll()).thenReturn(List.of(
                makeTx("TX-100", 100.0, TransactionStatus.SUCCESSFUL),
                makeTx("TX-500", 500.0, TransactionStatus.SUCCESSFUL),
                makeTx("TX-200", 200.0, TransactionStatus.SUCCESSFUL),
                makeTx("TX-800", 800.0, TransactionStatus.SUCCESSFUL),
                makeTx("TX-050", 50.0, TransactionStatus.SUCCESSFUL)
        ));

        List<Transaccion> result = useCase.execute(3);

        assertThat(result).hasSize(3);
        assertThat(result.get(0).getAmount()).isEqualTo(800.0);
        assertThat(result.get(1).getAmount()).isEqualTo(500.0);
        assertThat(result.get(2).getAmount()).isEqualTo(200.0);
    }

    // S-G02: only SUCCESSFUL included
    @Test
    void execute_includesOnlySuccessful() {
        when(transactionRepository.findAll()).thenReturn(List.of(
                makeTx("TX-100", 100.0, TransactionStatus.SUCCESSFUL),
                makeTx("TX-999", 999.0, TransactionStatus.REVERSED)
        ));

        List<Transaccion> result = useCase.execute(10);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo("TX-100");
    }

    // S-G03: tie-break by id asc
    @Test
    void execute_tieBreakByIdAsc() {
        when(transactionRepository.findAll()).thenReturn(List.of(
                makeTx("TX-B", 500.0, TransactionStatus.SUCCESSFUL),
                makeTx("TX-A", 500.0, TransactionStatus.SUCCESSFUL)
        ));

        List<Transaccion> result = useCase.execute(10);

        assertThat(result.get(0).getId()).isEqualTo("TX-A");
        assertThat(result.get(1).getId()).isEqualTo("TX-B");
    }

    // S-G04: empty → returns empty
    @Test
    void execute_emptyRepo_returnsEmpty() {
        when(transactionRepository.findAll()).thenReturn(List.of());

        List<Transaccion> result = useCase.execute(10);

        assertThat(result).isEmpty();
    }
}
