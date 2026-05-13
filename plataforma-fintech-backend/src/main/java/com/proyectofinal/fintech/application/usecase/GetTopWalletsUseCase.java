package com.proyectofinal.fintech.application.usecase;

import com.proyectofinal.fintech.application.result.MetricItem;
import com.proyectofinal.fintech.domain.model.Billetera;
import com.proyectofinal.fintech.domain.model.Usuario;
import com.proyectofinal.fintech.domain.port.UserRepository;
import com.proyectofinal.fintech.domain.port.WalletRepository;
import com.proyectofinal.fintech.domain.structures.ArbolBST;

import java.util.ArrayList;
import java.util.List;

/**
 * Returns top N wallets ranked by transactionCount descending.
 * Uses ArbolBST for ordering.
 * Plain class — ZERO Spring/Jakarta imports.
 */
public class GetTopWalletsUseCase {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;

    public GetTopWalletsUseCase(UserRepository userRepository, WalletRepository walletRepository) {
        this.userRepository = userRepository;
        this.walletRepository = walletRepository;
    }

    /**
     * @param limit max results (1..100)
     * @return ordered list of MetricItem (desc by transactionCount)
     */
    public List<MetricItem> execute(int limit) {
        ArbolBST<WalletNode> bst = new ArbolBST<>();

        for (Usuario u : userRepository.findAll()) {
            for (Billetera w : walletRepository.findByOwnerId(u.getId())) {
                String id = u.getId() + "/" + w.getCode();
                bst.insert(new WalletNode(id, w.getName(), w.getTransactionCount()));
            }
        }

        List<MetricItem> result = new ArrayList<>();
        int added = 0;
        for (WalletNode node : bst.inOrder()) {
            if (added >= limit) break;
            result.add(new MetricItem(node.id(), node.label(), node.value()));
            added++;
        }
        return result;
    }

    /** Internal node — Comparable desc by transactionCount, then asc by id */
    record WalletNode(String id, String label, double value) implements Comparable<WalletNode> {
        @Override
        public int compareTo(WalletNode other) {
            int cmp = Double.compare(-this.value, -other.value);
            if (cmp != 0) return cmp;
            return this.id.compareTo(other.id);
        }
    }
}
