package com.proyectofinal.fintech.infrastructure.output.memory;

import com.proyectofinal.fintech.domain.model.Beneficio;
import com.proyectofinal.fintech.domain.port.BeneficioRepository;
import com.proyectofinal.fintech.domain.structures.MiLista;
import com.proyectofinal.fintech.domain.structures.TablaHash;

import java.util.Optional;

/**
 * In-memory adapter for BeneficioRepository.
 * Uses TablaHash<String, Beneficio> keyed by beneficio id.
 * ZERO java.util.Map imports — propias data structures only.
 */
public class InMemoryBeneficioRepository implements BeneficioRepository {

    private final TablaHash<String, Beneficio> store = new TablaHash<>();

    @Override
    public void save(Beneficio beneficio) {
        store.put(beneficio.getId(), beneficio);
    }

    @Override
    public Optional<Beneficio> findById(String id) {
        return store.get(id);
    }

    @Override
    public MiLista<Beneficio> findAllActive() {
        MiLista<Beneficio> result = new MiLista<>();
        for (String key : store.keys()) {
            store.get(key).ifPresent(b -> {
                if (b.isActive()) result.add(b);
            });
        }
        return result;
    }
}
