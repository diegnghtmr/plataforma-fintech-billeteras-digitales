package com.proyectofinal.fintech;

import com.proyectofinal.fintech.application.usecase.*;
import com.proyectofinal.fintech.domain.port.TransactionIdGenerator;
import com.proyectofinal.fintech.domain.port.TransactionRepository;
import com.proyectofinal.fintech.infrastructure.mapper.TransactionMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * T05-B23 — Integration test: all SDD 05 beans load without conflicts.
 */
@SpringBootTest
class OperationsWiringIntegrationTest {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private TransactionIdGenerator transactionIdGenerator;

    @Autowired
    private RechargeWalletUseCase rechargeWalletUseCase;

    @Autowired
    private WithdrawWalletUseCase withdrawWalletUseCase;

    @Autowired
    private InternalTransferUseCase internalTransferUseCase;

    @Autowired
    private ExternalTransferUseCase externalTransferUseCase;

    @Autowired
    private TransactionMapper transactionMapper;

    @Test
    void contextLoads_allSDD05BeansPresent() {
        assertThat(transactionRepository).isNotNull();
        assertThat(transactionIdGenerator).isNotNull();
        assertThat(rechargeWalletUseCase).isNotNull();
        assertThat(withdrawWalletUseCase).isNotNull();
        assertThat(internalTransferUseCase).isNotNull();
        assertThat(externalTransferUseCase).isNotNull();
        assertThat(transactionMapper).isNotNull();
    }
}
