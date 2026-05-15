package com.proyectofinal.fintech.application.usecase;

import com.proyectofinal.fintech.application.result.MetricItem;
import com.proyectofinal.fintech.domain.model.Billetera;
import com.proyectofinal.fintech.domain.model.Transaccion;
import com.proyectofinal.fintech.domain.port.TransactionRepository;
import com.proyectofinal.fintech.domain.port.WalletRepository;
import com.proyectofinal.fintech.domain.structures.ArbolBST;
import com.proyectofinal.fintech.domain.structures.TablaHash;

import java.util.ArrayList;
import java.util.List;

/**
 * Use case: returns top N wallet categories ranked by transaction activity.
 * Uses TablaHash<walletId, type> + TablaHash<type, count> + ArbolBST<MetricNode>.
 * Plain class — ZERO Spring/Jakarta imports.
 */
public class GetTopWalletCategoriesUseCase {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;

    public GetTopWalletCategoriesUseCase(WalletRepository walletRepository,
                                          TransactionRepository transactionRepository) {
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
    }

    /**
     * Returns the top {@code limit} wallet categories by transaction count, descending.
     * Ties broken alphabetically ascending by type name.
     *
     * @param limit maximum number to return (1..100)
     * @return ordered list of MetricItem (id=type, label=type, value=txCount)
     */
    public List<MetricItem> execute(int limit) {
        // Step 1: build walletId → walletType lookup
        TablaHash<String, String> walletIdToType = new TablaHash<>();
        for (Billetera b : walletRepository.findAll()) {
            if (b.getCode() != null && b.getType() != null) {
                walletIdToType.put(b.getCode(), b.getType());
            }
        }

        // Step 2: count transactions per wallet type via sourceWalletId AND targetWalletId
        TablaHash<String, Integer> countsByType = new TablaHash<>();
        for (Transaccion tx : transactionRepository.findAll()) {
            String src = tx.getSourceWalletId();
            if (src != null) walletIdToType.get(src).ifPresent(type -> incrementCount(countsByType, type));
            String tgt = tx.getTargetWalletId();
            if (tgt != null) walletIdToType.get(tgt).ifPresent(type -> incrementCount(countsByType, type));
        }

        // Step 3: sort via ArbolBST (desc by count, asc by type on tie)
        ArbolBST<MetricNode> bst = new ArbolBST<>();
        for (String type : countsByType.keys()) {
            int count = countsByType.get(type).orElse(0);
            bst.insert(new MetricNode(type, count));
        }

        List<MetricItem> result = new ArrayList<>();
        for (MetricNode node : bst.inOrder()) {
            if (result.size() >= limit) break;
            result.add(new MetricItem(node.type(), node.type(), node.count()));
        }
        return result;
    }

    /** Increments the count for the given wallet type in the counts map. */
    private void incrementCount(TablaHash<String, Integer> counts, String type) {
        int current = counts.get(type).orElse(0);
        counts.put(type, current + 1);
    }

    /** Internal BST node: desc by count, asc by type on tie. */
    record MetricNode(String type, double count) implements Comparable<MetricNode> {
        @Override
        public int compareTo(MetricNode other) {
            int cmp = Double.compare(-this.count, -other.count);
            if (cmp != 0) return cmp;
            return this.type.compareTo(other.type);
        }
    }
}
