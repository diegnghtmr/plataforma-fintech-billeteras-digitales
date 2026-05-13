package com.proyectofinal.fintech.application.usecase;

import com.proyectofinal.fintech.domain.model.FraudEvent;
import com.proyectofinal.fintech.domain.model.FraudSeverity;
import com.proyectofinal.fintech.domain.port.FraudEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * T09-B21 (RED) — ListFraudEventsUseCase tests.
 */
@ExtendWith(MockitoExtension.class)
class ListFraudEventsUseCaseTest {

    @Mock
    private FraudEventRepository fraudEventRepository;

    private ListFraudEventsUseCase useCase;
    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

    @BeforeEach
    void setUp() {
        useCase = new ListFraudEventsUseCase(fraudEventRepository);
    }

    private FraudEvent makeEvent(String id, String userId, FraudSeverity severity) {
        return new FraudEvent(id, userId, "TX-" + id, "LARGE_TRANSACTION", severity, "desc", NOW);
    }

    @Test
    void execute_noFilters_callsFindAll() {
        List<FraudEvent> events = List.of(makeEvent("1", "USR_A", FraudSeverity.HIGH));
        when(fraudEventRepository.findAll()).thenReturn(events);

        Iterable<FraudEvent> result = useCase.execute(null, null);

        verify(fraudEventRepository).findAll();
        assertThat(result).containsExactlyElementsOf(events);
    }

    @Test
    void execute_userIdOnly_callsFindByUserId() {
        when(fraudEventRepository.findByUserId("USR_A")).thenReturn(List.of());

        useCase.execute("USR_A", null);

        verify(fraudEventRepository).findByUserId("USR_A");
    }

    @Test
    void execute_severityOnly_callsFindBySeverity() {
        when(fraudEventRepository.findBySeverity(FraudSeverity.HIGH)).thenReturn(List.of());

        useCase.execute(null, FraudSeverity.HIGH);

        verify(fraudEventRepository).findBySeverity(FraudSeverity.HIGH);
    }

    @Test
    void execute_bothFilters_callsFindByUserIdAndSeverity() {
        when(fraudEventRepository.findByUserIdAndSeverity("USR_A", FraudSeverity.HIGH))
                .thenReturn(List.of());

        useCase.execute("USR_A", FraudSeverity.HIGH);

        verify(fraudEventRepository).findByUserIdAndSeverity("USR_A", FraudSeverity.HIGH);
    }
}
