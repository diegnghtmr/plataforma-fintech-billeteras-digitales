package com.proyectofinal.fintech.infrastructure.mapper;

import com.proyectofinal.fintech.domain.model.Billetera;
import com.proyectofinal.fintech.infrastructure.input.rest.dto.WalletResponseDto;

import java.util.ArrayList;
import java.util.List;

/**
 * Hand-written mapper: Billetera → WalletResponseDto.
 * Zero Spring annotations — instantiated via @Bean in WalletBeansConfig.
 */
public class WalletMapper {

    public WalletResponseDto toDto(Billetera billetera) {
        return new WalletResponseDto(
                billetera.getCode(),
                billetera.getName(),
                billetera.getType(),
                billetera.getOwnerId(),
                billetera.getBalance(),
                billetera.isActive(),
                billetera.getCreatedAt().toString(),
                billetera.getTransactionCount()
        );
    }

    public List<WalletResponseDto> toDtoList(Iterable<Billetera> billeteras) {
        List<WalletResponseDto> list = new ArrayList<>();
        for (Billetera b : billeteras) {
            list.add(toDto(b));
        }
        return list;
    }
}
