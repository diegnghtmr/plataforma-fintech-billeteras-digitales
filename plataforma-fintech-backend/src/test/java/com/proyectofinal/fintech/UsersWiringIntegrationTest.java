package com.proyectofinal.fintech;

import com.proyectofinal.fintech.application.usecase.CreateUserUseCase;
import com.proyectofinal.fintech.application.usecase.CreateWalletUseCase;
import com.proyectofinal.fintech.application.usecase.GetUserUseCase;
import com.proyectofinal.fintech.application.usecase.ListWalletsUseCase;
import com.proyectofinal.fintech.domain.port.UserRepository;
import com.proyectofinal.fintech.domain.port.WalletRepository;
import com.proyectofinal.fintech.infrastructure.mapper.UserMapper;
import com.proyectofinal.fintech.infrastructure.mapper.WalletMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration test: Spring context loads without bean conflicts.
 * All SDD 04 beans must be wired correctly.
 */
@SpringBootTest
class UsersWiringIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private CreateUserUseCase createUserUseCase;

    @Autowired
    private GetUserUseCase getUserUseCase;

    @Autowired
    private CreateWalletUseCase createWalletUseCase;

    @Autowired
    private ListWalletsUseCase listWalletsUseCase;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private WalletMapper walletMapper;

    @Test
    void contextLoads_noBeanConflicts() {
        assertThat(userRepository).isNotNull();
        assertThat(walletRepository).isNotNull();
        assertThat(createUserUseCase).isNotNull();
        assertThat(getUserUseCase).isNotNull();
        assertThat(createWalletUseCase).isNotNull();
        assertThat(listWalletsUseCase).isNotNull();
        assertThat(userMapper).isNotNull();
        assertThat(walletMapper).isNotNull();
    }
}
