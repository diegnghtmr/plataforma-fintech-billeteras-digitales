package com.proyectofinal.fintech.application.usecase;

import com.proyectofinal.fintech.application.result.BenefitView;
import com.proyectofinal.fintech.domain.model.Beneficio;
import com.proyectofinal.fintech.domain.port.BeneficioRepository;

import java.util.ArrayList;
import java.util.List;

/**
 * Use case: list all active benefits.
 * Plain class — ZERO Spring/Jakarta imports.
 * REQ-F1.1.
 */
public class ListBenefitsUseCase {

    private final BeneficioRepository beneficioRepository;

    public ListBenefitsUseCase(BeneficioRepository beneficioRepository) {
        this.beneficioRepository = beneficioRepository;
    }

    /**
     * Returns all active benefits as view models.
     */
    public List<BenefitView> execute() {
        List<BenefitView> result = new ArrayList<>();
        for (Beneficio b : beneficioRepository.findAllActive()) {
            result.add(new BenefitView(b.getId(), b.getName(), b.getDescription(), b.getPointsCost(), b.isActive()));
        }
        return result;
    }
}
