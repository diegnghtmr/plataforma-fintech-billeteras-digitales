package com.proyectofinal.fintech.domain.port;

import com.proyectofinal.fintech.domain.model.BenefitRedemption;
import com.proyectofinal.fintech.domain.structures.MiLista;

/**
 * Output port for BenefitRedemption persistence.
 * ZERO Spring/Jakarta imports — pure domain interface.
 */
public interface BenefitRedemptionRepository {

    void save(BenefitRedemption redemption);

    MiLista<BenefitRedemption> findByUserId(String userId);
}
