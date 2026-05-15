package com.proyectofinal.fintech.domain.model;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * E-19 (RED) — RecurrenceType enum sanity test.
 * Verifies exactly {NONE, DAILY, WEEKLY, MONTHLY} values.
 */
class RecurrenceTypeTest {

    @Test
    void values_containsNoneDailyWeeklyMonthly() {
        Set<String> names = Arrays.stream(RecurrenceType.values())
                .map(Enum::name)
                .collect(Collectors.toSet());

        assertThat(names).containsExactlyInAnyOrder("NONE", "DAILY", "WEEKLY", "MONTHLY");
    }
}
