package com.proyectofinal.fintech.application.usecase;

import com.proyectofinal.fintech.domain.exception.BusinessRuleException;
import com.proyectofinal.fintech.domain.model.*;
import com.proyectofinal.fintech.domain.port.TransactionRepository;
import com.proyectofinal.fintech.infrastructure.input.rest.dto.RangeTotalResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * T11-I05 (RED→GREEN) — GetTotalMovedInRangeUseCase tests.
 */
@ExtendWith(MockitoExtension.class)
class GetTotalMovedInRangeUseCaseTest {

    @Mock
    private TransactionRepository transactionRepository;

    private GetTotalMovedInRangeUseCase useCase;

    private static final Instant FROM = Instant.parse("2026-01-01T00:00:00Z");
    private static final Instant TO   = Instant.parse("2026-12-31T23:59:59Z");
    private static final Instant MID  = Instant.parse("2026-06-01T00:00:00Z");
    private static final Instant OUTSIDE = Instant.parse("2025-01-01T00:00:00Z");

    @BeforeEach
    void setUp() {
        useCase = new GetTotalMovedInRangeUseCase(transactionRepository);
    }

    private Transaccion makeTx(String id, double amount, Instant ts,
                                TransactionType type, TransactionStatus status) {
        return new Transaccion(id, ts, type, amount,
                "W001", null, "USR001", null, status, 1.0, null, true);
    }

    @Test
    void execute_twoInRangeOneOutside_returnsCorrectSumAndCount() {
        when(transactionRepository.findAll()).thenReturn(List.of(
                makeTx("TX-1", 500.0, MID, TransactionType.RECHARGE, TransactionStatus.SUCCESSFUL),
                makeTx("TX-2", 1000.0, MID, TransactionType.WITHDRAWAL, TransactionStatus.SUCCESSFUL),
                makeTx("TX-3", 200.0, OUTSIDE, TransactionType.RECHARGE, TransactionStatus.SUCCESSFUL)
        ));

        RangeTotalResponseDto result = useCase.execute(FROM, TO);

        assertThat(result.count()).isEqualTo(2);
        assertThat(result.totalAmount()).isEqualTo(1500.0);
        assertThat(result.from()).isEqualTo(FROM.toString());
        assertThat(result.to()).isEqualTo(TO.toString());
    }

    @Test
    void execute_receivedTypeExcluded() {
        when(transactionRepository.findAll()).thenReturn(List.of(
                makeTx("TX-1", 500.0, MID, TransactionType.RECHARGE, TransactionStatus.SUCCESSFUL),
                makeTx("TX-2", 999.0, MID, TransactionType.EXTERNAL_TRANSFER_RECEIVED, TransactionStatus.SUCCESSFUL)
        ));

        RangeTotalResponseDto result = useCase.execute(FROM, TO);

        assertThat(result.count()).isEqualTo(1);
        assertThat(result.totalAmount()).isEqualTo(500.0);
    }

    @Test
    void execute_fromAfterTo_throwsBusinessRuleException() {
        assertThatThrownBy(() -> useCase.execute(TO, FROM))
                .isInstanceOf(BusinessRuleException.class);
    }

    @Test
    void execute_empty_returnsZero() {
        when(transactionRepository.findAll()).thenReturn(List.of());

        RangeTotalResponseDto result = useCase.execute(FROM, TO);

        assertThat(result.count()).isEqualTo(0);
        assertThat(result.totalAmount()).isEqualTo(0.0);
    }
}
