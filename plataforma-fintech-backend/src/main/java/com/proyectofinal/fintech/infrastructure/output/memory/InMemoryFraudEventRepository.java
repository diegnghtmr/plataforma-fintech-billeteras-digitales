package com.proyectofinal.fintech.infrastructure.output.memory;

import com.proyectofinal.fintech.domain.model.FraudEvent;
import com.proyectofinal.fintech.domain.model.FraudSeverity;
import com.proyectofinal.fintech.domain.port.FraudEventRepository;
import com.proyectofinal.fintech.domain.structures.MiLista;
import com.proyectofinal.fintech.domain.structures.TablaHash;

import java.util.ArrayList;
import java.util.List;

/**
 * In-memory implementation of FraudEventRepository.
 * Uses MiLista<FraudEvent> as the global store and
 * TablaHash<String, MiLista<FraudEvent>> for per-user indexing.
 * No java.util collections as primary storage.
 */
public class InMemoryFraudEventRepository implements FraudEventRepository {

    private final MiLista<FraudEvent> global = new MiLista<>();
    private final TablaHash<String, MiLista<FraudEvent>> byUser = new TablaHash<>();

    @Override
    public void save(FraudEvent event) {
        global.add(event);
        MiLista<FraudEvent> userList = byUser.get(event.getUserId()).orElse(null);
        if (userList == null) {
            userList = new MiLista<>();
            byUser.put(event.getUserId(), userList);
        }
        userList.add(event);
    }

    @Override
    public Iterable<FraudEvent> findAll() {
        List<FraudEvent> result = new ArrayList<>();
        for (FraudEvent e : global) {
            result.add(e);
        }
        return result;
    }

    @Override
    public Iterable<FraudEvent> findByUserId(String userId) {
        List<FraudEvent> result = new ArrayList<>();
        MiLista<FraudEvent> userList = byUser.get(userId).orElse(null);
        if (userList != null) {
            for (FraudEvent e : userList) {
                result.add(e);
            }
        }
        return result;
    }

    @Override
    public Iterable<FraudEvent> findBySeverity(FraudSeverity severity) {
        List<FraudEvent> result = new ArrayList<>();
        for (FraudEvent e : global) {
            if (e.getSeverity() == severity) {
                result.add(e);
            }
        }
        return result;
    }

    @Override
    public Iterable<FraudEvent> findByUserIdAndSeverity(String userId, FraudSeverity severity) {
        List<FraudEvent> result = new ArrayList<>();
        MiLista<FraudEvent> userList = byUser.get(userId).orElse(null);
        if (userList != null) {
            for (FraudEvent e : userList) {
                if (e.getSeverity() == severity) {
                    result.add(e);
                }
            }
        }
        return result;
    }

    @Override
    public int count() {
        return global.size();
    }

    @Override
    public void deleteByUserId(String userId) {
        // Remove from global list by rebuilding it
        MiLista<FraudEvent> newGlobal = new MiLista<>();
        for (FraudEvent e : global) {
            if (!userId.equals(e.getUserId())) {
                newGlobal.add(e);
            }
        }
        while (!global.isEmpty()) global.removeFirst();
        for (FraudEvent e : newGlobal) global.add(e);

        // Remove from byUser index
        byUser.remove(userId);
    }
}
