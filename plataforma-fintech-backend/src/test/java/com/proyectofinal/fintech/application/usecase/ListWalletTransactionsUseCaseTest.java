package com.proyectofinal.fintech.application.usecase;

import com.proyectofinal.fintech.domain.exception.ErrorCode;
import com.proyectofinal.fintech.domain.exception.NotFoundException;
import com.proyectofinal.fintech.domain.model.*;
import com.proyectofinal.fintech.domain.port.TransactionRepository;
import com.proyectofinal.fintech.domain.port.UserRepository;
import com.proyectofinal.fintech.domain.port.WalletRepository;
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
 * T06-B04 (RED) — ListWalletTransactionsUseCase tests.
 */
@ExtendWith(MockitoExtension.class)
class ListWalletTransactionsUseCaseTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private WalletRepository walletRepository;
    @Mock
    private TransactionRepository transactionRepository;

    private ListWalletTransactionsUseCase useCase;

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

    @BeforeEach
    void setUp() {
        useCase = new ListWalletTransactionsUseCase(userRepository, walletRepository, transactionRepository);
    }

    private Transaccion makeTx(String id, String walletId) {
        return new Transaccion(id, NOW, TransactionType.RECHARGE, 100.0,
                walletId, null, "USR001", null,
                TransactionStatus.SUCCESSFUL, 1.0, null, true);
    }

    // happy path — returns 2 transactions
    @Test
    void execute_happyPath_returnsTwoTransactions() {
        Usuario user = new Usuario("USR001", "Ana", "ana@test.com", NOW, 0.0, LoyaltyLevel.BRONZE);
        Billetera wallet = new Billetera("W001", "Ahorros", "SAVINGS", "USR001", 500.0, true, NOW, 0);
        List<Transaccion> txs = List.of(makeTx("TX-001", "W001"), makeTx("TX-002", "W001"));

        when(userRepository.findById("USR001")).thenReturn(Optional.of(user));
        when(walletRepository.findByOwnerIdAndCode("USR001", "W001")).thenReturn(Optional.of(wallet));
        when(transactionRepository.findByWalletId("W001")).thenReturn(txs);

        Iterable<Transaccion> result = useCase.execute("USR001", "W001");
        List<Transaccion> list = StreamSupport.stream(result.spliterator(), false).toList();

        assertThat(list).hasSize(2);
    }

    // user not found → USER_NOT_FOUND
    @Test
    void execute_userNotFound_throwsNotFound() {
        when(userRepository.findById("GHOST")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute("GHOST", "W001"))
                .isInstanceOf(NotFoundException.class)
                .satisfies(ex -> assertThat(((NotFoundException) ex).code())
                        .isEqualTo(ErrorCode.USER_NOT_FOUND));
        verifyNoInteractions(walletRepository, transactionRepository);
    }

    // wallet not found → WALLET_NOT_FOUND
    @Test
    void execute_walletNotFound_throwsNotFound() {
        Usuario user = new Usuario("USR001", "Ana", "ana@test.com", NOW, 0.0, LoyaltyLevel.BRONZE);

        when(userRepository.findById("USR001")).thenReturn(Optional.of(user));
        when(walletRepository.findByOwnerIdAndCode("USR001", "GHOST")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase.execute("USR001", "GHOST"))
                .isInstanceOf(NotFoundException.class)
                .satisfies(ex -> assertThat(((NotFoundException) ex).code())
                        .isEqualTo(ErrorCode.WALLET_NOT_FOUND));
        verifyNoInteractions(transactionRepository);
    }
}
