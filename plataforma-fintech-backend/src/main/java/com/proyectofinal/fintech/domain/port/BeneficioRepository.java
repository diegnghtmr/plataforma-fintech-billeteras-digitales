package com.proyectofinal.fintech.domain.port;

import com.proyectofinal.fintech.domain.model.Beneficio;
import com.proyectofinal.fintech.domain.structures.MiLista;

import java.util.Optional;

/**
 * Output port for Beneficio persistence.
 * ZERO Spring/Jakarta imports — pure domain interface.
 */
public interface BeneficioRepository {

    void save(Beneficio beneficio);

    Optional<Beneficio> findById(String id);

    MiLista<Beneficio> findAllActive();
}
