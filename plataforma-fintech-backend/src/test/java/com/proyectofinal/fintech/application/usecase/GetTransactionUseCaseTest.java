package com.proyectofinal.fintech.application.usecase;

import com.proyectofinal.fintech.domain.exception.ErrorCode;
import com.proyectofinal.fintech.domain.exception.NotFoundException;
import com.proyectofinal.fintech.domain.model.Transaccion;
import com.proyectofinal.fintech.domain.model.TransactionStatus;
import com.proyectofinal.fintech.domain.model.TransactionType;
import com.proyectofinal.fintech.domain.port.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetTransactionUseCaseTest {

    @Mock private TransactionRepository transactionRepository;

    private GetTransactionUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new GetTransactionUseCase(transactionRepository);
    }

    @Test
    void execute_existingTransaction_returnsIt() {
        Transaccion tx = new Transaccion("TX-1", Instant.parse("2026-01-01T00:00:00Z"),
                TransactionType.RECHARGE, 50.0, "W001", null, "USR001", null,
                TransactionStatus.SUCCESSFUL, 1.0, null, true);
        when(transactionRepository.findById("TX-1")).thenReturn(Optional.of(tx));

        Transaccion result = useCase.execute("TX-1");

        assertThat(result).isSameAs(tx);
    }

    @Test
    void execute_unknownId_throwsNotFound() {
        when(transactionRepository.findById("GHOST")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute("GHOST"))
                .isInstanceOf(NotFoundException.class)
                .satisfies(ex -> assertThat(((NotFoundException) ex).code())
                        .isEqualTo(ErrorCode.TRANSACTION_NOT_FOUND));
    }
}
