package com.proyectofinal.fintech.application.usecase;

import com.proyectofinal.fintech.domain.model.Transaccion;
import com.proyectofinal.fintech.domain.model.TransactionStatus;
import com.proyectofinal.fintech.domain.port.TransactionRepository;
import com.proyectofinal.fintech.domain.structures.ArbolBST;

import java.util.ArrayList;
import java.util.List;

/**
 * Use case: returns the top N transactions by amount (SUCCESSFUL only).
 * Uses ArbolBST<TxValueNode> ordered descending by amount (id ASC tie-break).
 * Plain class — ZERO Spring/Jakarta imports.
 */
public class GetTopTransactionsUseCase {

    /**
     * Comparable node for the BST — descends by amount, ties broken by id ascending.
     */
    public static class TxValueNode implements Comparable<TxValueNode> {
        private final double amount;
        private final String id;
        private final Transaccion tx;

        public TxValueNode(Transaccion tx) {
            this.amount = tx.getAmount();
            this.id = tx.getId();
            this.tx = tx;
        }

        public Transaccion getTx() { return tx; }

        @Override
        public int compareTo(TxValueNode other) {
            // Descending by amount → flip sign
            int cmp = Double.compare(other.amount, this.amount);
            if (cmp != 0) return cmp;
            // Ascending by id for tie-break
            return this.id.compareTo(other.id);
        }
    }

    private final TransactionRepository transactionRepository;

    public GetTopTransactionsUseCase(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    /**
     * Returns the top {@code limit} SUCCESSFUL transactions by amount, descending.
     *
     * @param limit maximum number to return (≥1)
     * @return list of transactions
     */
    public List<Transaccion> execute(int limit) {
        ArbolBST<TxValueNode> bst = new ArbolBST<>();

        for (Transaccion tx : transactionRepository.findAll()) {
            if (tx.getStatus() == TransactionStatus.SUCCESSFUL) {
                bst.insert(new TxValueNode(tx));
            }
        }

        List<Transaccion> result = new ArrayList<>();
        for (TxValueNode node : bst.inOrder()) {
            if (result.size() >= limit) break;
            result.add(node.getTx());
        }
        return result;
    }
}
