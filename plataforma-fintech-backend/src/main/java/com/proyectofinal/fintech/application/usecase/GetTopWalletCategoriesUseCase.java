package com.proyectofinal.fintech.application.usecase;

import com.proyectofinal.fintech.application.result.MetricItem;
import com.proyectofinal.fintech.domain.model.Billetera;
import com.proyectofinal.fintech.domain.port.WalletRepository;
import com.proyectofinal.fintech.domain.structures.ArbolBST;
import com.proyectofinal.fintech.domain.structures.TablaHash;

import java.util.ArrayList;
import java.util.List;

/**
 * Use case: returns top N wallet categories by count.
 * Uses TablaHash<type, count> + ArbolBST<MetricNode> for descending order.
 * Plain class — ZERO Spring/Jakarta imports.
 */
public class GetTopWalletCategoriesUseCase {

    private final WalletRepository walletRepository;

    public GetTopWalletCategoriesUseCase(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    /**
     * Returns the top {@code limit} wallet categories by count, descending.
     *
     * @param limit maximum number to return (1..100)
     * @return ordered list of MetricItem (id=type, label=type, value=count)
     */
    public List<MetricItem> execute(int limit) {
        TablaHash<String, Integer> counts = new TablaHash<>();

        for (Billetera b : walletRepository.findAll()) {
            String type = b.getType();
            if (type == null) continue;
            int current = counts.get(type).orElse(0);
            counts.put(type, current + 1);
        }

        ArbolBST<MetricNode> bst = new ArbolBST<>();
        for (String type : counts.keys()) {
            int count = counts.get(type).orElse(0);
            bst.insert(new MetricNode(type, count));
        }

        List<MetricItem> result = new ArrayList<>();
        for (MetricNode node : bst.inOrder()) {
            if (result.size() >= limit) break;
            result.add(new MetricItem(node.type(), node.type(), node.count()));
        }
        return result;
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
