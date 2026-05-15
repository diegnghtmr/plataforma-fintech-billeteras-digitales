package com.proyectofinal.fintech.infrastructure.input.rest;

import com.proyectofinal.fintech.application.usecase.CreateWalletUseCase;
import com.proyectofinal.fintech.application.usecase.ListWalletsUseCase;
import com.proyectofinal.fintech.application.usecase.UpdateWalletUseCase;
import com.proyectofinal.fintech.domain.model.Billetera;
import com.proyectofinal.fintech.infrastructure.input.rest.dto.CreateWalletRequestDto;
import com.proyectofinal.fintech.infrastructure.input.rest.dto.UpdateWalletRequestDto;
import com.proyectofinal.fintech.infrastructure.input.rest.dto.WalletResponseDto;
import com.proyectofinal.fintech.infrastructure.mapper.WalletMapper;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * REST adapter for wallet operations.
 * Context-path /api/v1 set globally (SDD 1).
 */
@RestController
@RequestMapping("/users/{userId}/wallets")
public class WalletController {

    private final CreateWalletUseCase createWalletUseCase;
    private final ListWalletsUseCase listWalletsUseCase;
    private final UpdateWalletUseCase updateWalletUseCase;
    private final WalletMapper walletMapper;

    public WalletController(CreateWalletUseCase createWalletUseCase,
                             ListWalletsUseCase listWalletsUseCase,
                             UpdateWalletUseCase updateWalletUseCase,
                             WalletMapper walletMapper) {
        this.createWalletUseCase = createWalletUseCase;
        this.listWalletsUseCase = listWalletsUseCase;
        this.updateWalletUseCase = updateWalletUseCase;
        this.walletMapper = walletMapper;
    }

    @GetMapping
    public ResponseEntity<List<WalletResponseDto>> listWallets(@PathVariable String userId) {
        Iterable<Billetera> wallets = listWalletsUseCase.execute(userId);
        return ResponseEntity.ok(walletMapper.toDtoList(wallets));
    }

    @PostMapping
    public ResponseEntity<WalletResponseDto> createWallet(@PathVariable String userId,
                                                           @Valid @RequestBody CreateWalletRequestDto request) {
        Billetera billetera = createWalletUseCase.execute(userId, request.name(), request.type());
        return ResponseEntity.status(HttpStatus.CREATED).body(walletMapper.toDto(billetera));
    }

    @PatchMapping("/{walletCode}")
    public ResponseEntity<WalletResponseDto> updateWallet(@PathVariable String userId,
                                                           @PathVariable String walletCode,
                                                           @Valid @RequestBody UpdateWalletRequestDto request) {
        Billetera billetera = updateWalletUseCase.execute(
                userId,
                walletCode,
                Optional.ofNullable(request.name()),
                Optional.ofNullable(request.active())
        );
        return ResponseEntity.ok(walletMapper.toDto(billetera));
    }
}
