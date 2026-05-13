package com.proyectofinal.fintech.application.usecase;

import com.proyectofinal.fintech.application.result.RankingItem;
import com.proyectofinal.fintech.domain.exception.BusinessRuleException;
import com.proyectofinal.fintech.domain.exception.ErrorCode;
import com.proyectofinal.fintech.domain.model.LoyaltyLevel;
import com.proyectofinal.fintech.domain.model.Usuario;
import com.proyectofinal.fintech.domain.port.UserRepository;
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
 * T07-B09 (RED) — GetPointsRankingUseCase tests.
 * Covers: descending order, limit, tie-break, invalid limits.
 */
@ExtendWith(MockitoExtension.class)
class GetPointsRankingUseCaseTest {

    @Mock
    private UserRepository userRepository;

    private GetPointsRankingUseCase useCase;

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

    @BeforeEach
    void setUp() {
        useCase = new GetPointsRankingUseCase(userRepository);
    }

    private Usuario makeUser(String id, double points) {
        return new Usuario(id, "User " + id, id + "@test.com", NOW, points, LoyaltyLevel.BRONZE);
    }

    // Scenario 1: 5 users with distinct points → descending order, positions 1..5
    @Test
    void execute_fiveDistinctUsers_returnsSortedDescending() {
        List<Usuario> users = List.of(
                makeUser("USR003", 300.0),
                makeUser("USR001", 100.0),
                makeUser("USR005", 500.0),
                makeUser("USR002", 200.0),
                makeUser("USR004", 400.0)
        );
        when(userRepository.findAll()).thenReturn(users);

        List<RankingItem> result = useCase.execute(10);

        assertThat(result).hasSize(5);
        assertThat(result.get(0).position()).isEqualTo(1);
        assertThat(result.get(0).userId()).isEqualTo("USR005");
        assertThat(result.get(0).points()).isEqualTo(500.0);
        assertThat(result.get(4).position()).isEqualTo(5);
        assertThat(result.get(4).userId()).isEqualTo("USR001");
        // Verify descending order
        for (int i = 0; i < result.size() - 1; i++) {
            assertThat(result.get(i).points()).isGreaterThanOrEqualTo(result.get(i + 1).points());
        }
    }

    // Scenario 2: limit=3 from 5 users → exactly 3 items
    @Test
    void execute_limitThreeFromFive_returnsExactlyThree() {
        List<Usuario> users = List.of(
                makeUser("USR001", 100.0),
                makeUser("USR002", 200.0),
                makeUser("USR003", 300.0),
                makeUser("USR004", 400.0),
                makeUser("USR005", 500.0)
        );
        when(userRepository.findAll()).thenReturn(users);

        List<RankingItem> result = useCase.execute(3);

        assertThat(result).hasSize(3);
        assertThat(result.get(0).userId()).isEqualTo("USR005");
        assertThat(result.get(2).userId()).isEqualTo("USR003");
    }

    // Scenario 3: tie-break — alice(500) before bob(500) (userId ASC)
    @Test
    void execute_tieBreakByUserIdAsc() {
        List<Usuario> users = List.of(
                new Usuario("bob", "Bob", "bob@test.com", NOW, 500.0, LoyaltyLevel.BRONZE),
                new Usuario("alice", "Alice", "alice@test.com", NOW, 500.0, LoyaltyLevel.BRONZE)
        );
        when(userRepository.findAll()).thenReturn(users);

        List<RankingItem> result = useCase.execute(10);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).userId()).isEqualTo("alice");
        assertThat(result.get(1).userId()).isEqualTo("bob");
    }

    // Scenario 4: limit=0 → BusinessRuleException
    @Test
    void execute_limitZero_throwsBusinessRuleException() {
        assertThatThrownBy(() -> useCase.execute(0))
                .isInstanceOf(BusinessRuleException.class)
                .satisfies(ex -> assertThat(((BusinessRuleException) ex).code())
                        .isEqualTo(ErrorCode.VALIDATION_ERROR));
    }

    // Scenario 5: limit=101 → BusinessRuleException
    @Test
    void execute_limitOneHundredOne_throwsBusinessRuleException() {
        assertThatThrownBy(() -> useCase.execute(101))
                .isInstanceOf(BusinessRuleException.class)
                .satisfies(ex -> assertThat(((BusinessRuleException) ex).code())
                        .isEqualTo(ErrorCode.VALIDATION_ERROR));
    }

    // Scenario 6: limit=100 → accepted
    @Test
    void execute_limitOneHundred_accepted() {
        List<Usuario> users = List.of(makeUser("USR001", 100.0));
        when(userRepository.findAll()).thenReturn(users);

        List<RankingItem> result = useCase.execute(100);

        assertThat(result).hasSize(1);
    }

    // Scenario 7: default limit=10, fewer than 10 users → return all
    @Test
    void execute_defaultLimitFewerUsers_returnsAll() {
        List<Usuario> users = List.of(
                makeUser("USR001", 100.0),
                makeUser("USR002", 200.0),
                makeUser("USR003", 300.0)
        );
        when(userRepository.findAll()).thenReturn(users);

        List<RankingItem> result = useCase.execute(10);

        assertThat(result).hasSize(3);
    }
}
