package com.proyectofinal.fintech.application.usecase;

import com.proyectofinal.fintech.application.result.MetricItem;
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
 * T11-I03 (RED→GREEN) — GetMovementByTypeUseCase tests.
 */
@ExtendWith(MockitoExtension.class)
class GetMovementByTypeUseCaseTest {

    @Mock
    private TransactionRepository transactionRepository;

    private GetMovementByTypeUseCase useCase;

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

    @BeforeEach
    void setUp() {
        useCase = new GetMovementByTypeUseCase(transactionRepository);
    }

    private Transaccion makeTx(TransactionType type, TransactionStatus status) {
        return new Transaccion("TX-" + type.name(), NOW, type, 100.0,
                "W001", null, "USR001", null, status, 1.0, null, true);
    }

    @Test
    void execute_mixedTypes_excludesReversedAndReceived() {
        when(transactionRepository.findAll()).thenReturn(List.of(
                makeTx(TransactionType.RECHARGE, TransactionStatus.SUCCESSFUL),
                makeTx(TransactionType.RECHARGE, TransactionStatus.SUCCESSFUL),
                makeTx(TransactionType.RECHARGE, TransactionStatus.SUCCESSFUL),
                makeTx(TransactionType.WITHDRAWAL, TransactionStatus.SUCCESSFUL),
                makeTx(TransactionType.WITHDRAWAL, TransactionStatus.SUCCESSFUL),
                makeTx(TransactionType.RECHARGE, TransactionStatus.REVERSED),
                makeTx(TransactionType.EXTERNAL_TRANSFER_RECEIVED, TransactionStatus.SUCCESSFUL)
        ));

        List<MetricItem> result = useCase.execute();

        assertThat(result).extracting(MetricItem::id)
                .doesNotContain("EXTERNAL_TRANSFER_RECEIVED");
        MetricItem recharge = result.stream().filter(m -> m.id().equals("RECHARGE")).findFirst().orElseThrow();
        assertThat(recharge.value()).isEqualTo(3.0);
        MetricItem withdrawal = result.stream().filter(m -> m.id().equals("WITHDRAWAL")).findFirst().orElseThrow();
        assertThat(withdrawal.value()).isEqualTo(2.0);
    }

    @Test
    void execute_empty_returnsEmpty() {
        when(transactionRepository.findAll()).thenReturn(List.of());

        List<MetricItem> result = useCase.execute();

        assertThat(result).isEmpty();
    }
}
