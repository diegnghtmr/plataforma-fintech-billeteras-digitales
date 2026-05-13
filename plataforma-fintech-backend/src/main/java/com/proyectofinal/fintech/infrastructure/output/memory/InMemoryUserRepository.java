package com.proyectofinal.fintech.infrastructure.output.memory;

import com.proyectofinal.fintech.domain.model.Usuario;
import com.proyectofinal.fintech.domain.port.UserRepository;
import com.proyectofinal.fintech.domain.structures.TablaHash;

import java.util.Optional;

/**
 * In-memory adapter for UserRepository.
 * Uses TablaHash<String, Usuario> as its ONLY internal storage.
 * ZERO java.util.Map / java.util.HashMap imports.
 */
public class InMemoryUserRepository implements UserRepository {

    private final TablaHash<String, Usuario> store = new TablaHash<>();

    @Override
    public void save(Usuario usuario) {
        store.put(usuario.getId(), usuario);
    }

    @Override
    public Optional<Usuario> findById(String id) {
        return store.get(id);
    }

    @Override
    public boolean existsById(String id) {
        return store.containsKey(id);
    }

    @Override
    public Iterable<Usuario> findAll() {
        return store.values();
    }

    @Override
    public void deleteById(String id) {
        store.remove(id);
    }
}
