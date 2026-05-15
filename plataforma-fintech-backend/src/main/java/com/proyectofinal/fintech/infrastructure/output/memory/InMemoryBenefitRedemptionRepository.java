package com.proyectofinal.fintech.infrastructure.output.memory;

import com.proyectofinal.fintech.domain.model.BenefitRedemption;
import com.proyectofinal.fintech.domain.port.BenefitRedemptionRepository;
import com.proyectofinal.fintech.domain.structures.MiLista;
import com.proyectofinal.fintech.domain.structures.TablaHash;

/**
 * In-memory adapter for BenefitRedemptionRepository.
 * Uses TablaHash<String, MiLista<BenefitRedemption>> keyed by userId.
 * ZERO java.util.Map imports — propias data structures only.
 */
public class InMemoryBenefitRedemptionRepository implements BenefitRedemptionRepository {

    private final TablaHash<String, MiLista<BenefitRedemption>> store = new TablaHash<>();

    @Override
    public void save(BenefitRedemption redemption) {
        MiLista<BenefitRedemption> userList = store.get(redemption.userId())
                .orElseGet(() -> {
                    MiLista<BenefitRedemption> list = new MiLista<>();
                    store.put(redemption.userId(), list);
                    return list;
                });
        userList.add(redemption);
    }

    @Override
    public MiLista<BenefitRedemption> findByUserId(String userId) {
        return store.get(userId).orElse(new MiLista<>());
    }
}
