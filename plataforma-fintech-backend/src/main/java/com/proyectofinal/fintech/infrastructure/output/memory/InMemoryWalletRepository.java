package com.proyectofinal.fintech.infrastructure.output.memory;

import com.proyectofinal.fintech.domain.model.Billetera;
import com.proyectofinal.fintech.domain.port.WalletRepository;
import com.proyectofinal.fintech.domain.structures.TablaHash;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * In-memory adapter for WalletRepository.
 * Uses TablaHash<String, TablaHash<String, Billetera>> as ONLY internal storage.
 * Outer key = ownerId, inner key = walletCode.
 * ZERO java.util.Map / java.util.HashMap imports.
 * ArrayList used ONLY as boundary translation (return value).
 */
public class InMemoryWalletRepository implements WalletRepository {

    private final TablaHash<String, TablaHash<String, Billetera>> store = new TablaHash<>();

    @Override
    public void save(Billetera billetera) {
        TablaHash<String, Billetera> ownerWallets = store.get(billetera.getOwnerId())
                .orElseGet(() -> {
                    TablaHash<String, Billetera> inner = new TablaHash<>();
                    store.put(billetera.getOwnerId(), inner);
                    return inner;
                });
        ownerWallets.put(billetera.getCode(), billetera);
    }

    @Override
    public Iterable<Billetera> findByOwnerId(String ownerId) {
        return store.get(ownerId)
                .map(inner -> {
                    ArrayList<Billetera> list = new ArrayList<>();
                    for (Billetera b : inner.values()) {
                        list.add(b);
                    }
                    return (Iterable<Billetera>) list;
                })
                .orElse(new ArrayList<>());
    }

    @Override
    public Optional<Billetera> findByOwnerIdAndCode(String ownerId, String code) {
        return store.get(ownerId)
                .flatMap(inner -> inner.get(code));
    }

    @Override
    public boolean existsByOwnerIdAndCode(String ownerId, String code) {
        return store.get(ownerId)
                .map(inner -> inner.containsKey(code))
                .orElse(false);
    }

    @Override
    public int countByOwnerId(String ownerId) {
        return store.get(ownerId)
                .map(TablaHash::size)
                .orElse(0);
    }

    @Override
    public double sumBalanceByOwnerId(String ownerId) {
        return store.get(ownerId)
                .map(inner -> {
                    double sum = 0.0;
                    for (Billetera b : inner.values()) {
                        sum += b.getBalance();
                    }
                    return sum;
                })
                .orElse(0.0);
    }

    @Override
    public List<Billetera> findAll() {
        ArrayList<Billetera> list = new ArrayList<>();
        for (String ownerId : store.keys()) {
            store.get(ownerId).ifPresent(inner -> {
                for (Billetera b : inner.values()) {
                    list.add(b);
                }
            });
        }
        return list;
    }

    @Override
    public void deleteByOwnerId(String ownerId) {
        store.remove(ownerId);
    }
}
