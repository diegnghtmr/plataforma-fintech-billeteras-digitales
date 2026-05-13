package com.proyectofinal.fintech.infrastructure.output.memory;

import com.proyectofinal.fintech.domain.model.Transaccion;
import com.proyectofinal.fintech.domain.port.TransactionRepository;
import com.proyectofinal.fintech.domain.structures.MiLista;
import com.proyectofinal.fintech.domain.structures.TablaHash;

import java.util.Optional;

/**
 * In-memory adapter for TransactionRepository.
 * Primary storage uses MiLista and TablaHash — ZERO java.util.Map/List as primary storage.
 *
 * <ul>
 *   <li>{@code all} — global MiLista for findAll / findById iteration</li>
 *   <li>{@code byUser} — TablaHash keyed by userId; dual-indexed for external transfers</li>
 *   <li>{@code byWallet} — TablaHash keyed by walletId; dual-indexed when source != target</li>
 * </ul>
 *
 * Dedup rule: when sourceUserId == targetUserId (internal/recharge/withdraw), the transaction
 * is added to the user's list exactly once. Same for wallets.
 * ADR-5.2: External transfers are over-indexed; consumers filter by type.
 */
public class InMemoryTransactionRepository implements TransactionRepository {

    private final MiLista<Transaccion> all = new MiLista<>();
    private final TablaHash<String, MiLista<Transaccion>> byUser = new TablaHash<>();
    private final TablaHash<String, MiLista<Transaccion>> byWallet = new TablaHash<>();

    @Override
    public Transaccion save(Transaccion transaccion) {
        all.add(transaccion);

        // Index by sourceUserId
        appendToIndex(byUser, transaccion.getSourceUserId(), transaccion);

        // Index by targetUserId only if distinct from source (avoids duplicate for internal tx)
        String targetUserId = transaccion.getTargetUserId();
        if (targetUserId != null && !targetUserId.equals(transaccion.getSourceUserId())) {
            appendToIndex(byUser, targetUserId, transaccion);
        }

        // Index by sourceWalletId
        appendToIndex(byWallet, transaccion.getSourceWalletId(), transaccion);

        // Index by targetWalletId only if distinct from source
        String targetWalletId = transaccion.getTargetWalletId();
        if (targetWalletId != null && !targetWalletId.equals(transaccion.getSourceWalletId())) {
            appendToIndex(byWallet, targetWalletId, transaccion);
        }

        return transaccion;
    }

    @Override
    public Optional<Transaccion> findById(String id) {
        for (Transaccion tx : all) {
            if (tx.getId().equals(id)) {
                return Optional.of(tx);
            }
        }
        return Optional.empty();
    }

    @Override
    public Iterable<Transaccion> findByUserId(String userId) {
        return byUser.get(userId).orElseGet(MiLista::new);
    }

    @Override
    public Iterable<Transaccion> findByWalletId(String walletId) {
        return byWallet.get(walletId).orElseGet(MiLista::new);
    }

    @Override
    public Iterable<Transaccion> findAll() {
        return all;
    }

    @Override
    public void deleteByUserId(String userId) {
        // Rebuild all, byUser, byWallet excluding transactions belonging to userId
        MiLista<Transaccion> newAll = new MiLista<>();
        TablaHash<String, MiLista<Transaccion>> newByUser = new TablaHash<>();
        TablaHash<String, MiLista<Transaccion>> newByWallet = new TablaHash<>();

        for (Transaccion tx : all) {
            boolean ownedByUser = userId.equals(tx.getSourceUserId())
                    || userId.equals(tx.getTargetUserId());
            if (!ownedByUser) {
                newAll.add(tx);
                appendToIndex(newByUser, tx.getSourceUserId(), tx);
                String tgt = tx.getTargetUserId();
                if (tgt != null && !tgt.equals(tx.getSourceUserId())) {
                    appendToIndex(newByUser, tgt, tx);
                }
                appendToIndex(newByWallet, tx.getSourceWalletId(), tx);
                String tgtW = tx.getTargetWalletId();
                if (tgtW != null && !tgtW.equals(tx.getSourceWalletId())) {
                    appendToIndex(newByWallet, tgtW, tx);
                }
            }
        }

        // Swap references — rebuild in-place
        // MiLista doesn't expose clear, so we remove all elements one by one
        while (!all.isEmpty()) all.removeFirst();
        for (Transaccion tx : newAll) all.add(tx);

        // Replace index maps
        for (String key : byUser.keys()) byUser.remove(key);
        for (String key : newByUser.keys()) {
            newByUser.get(key).ifPresent(list -> byUser.put(key, list));
        }

        for (String key : byWallet.keys()) byWallet.remove(key);
        for (String key : newByWallet.keys()) {
            newByWallet.get(key).ifPresent(list -> byWallet.put(key, list));
        }
    }

    // ── Internal helper ───────────────────────────────────────────────────────

    private void appendToIndex(TablaHash<String, MiLista<Transaccion>> index,
                                String key, Transaccion tx) {
        MiLista<Transaccion> list = index.get(key).orElseGet(() -> {
            MiLista<Transaccion> newList = new MiLista<>();
            index.put(key, newList);
            return newList;
        });
        list.add(tx);
    }
}
