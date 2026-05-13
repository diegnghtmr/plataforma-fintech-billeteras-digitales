package com.proyectofinal.fintech.application.usecase;

import com.proyectofinal.fintech.application.result.MetricItem;
import com.proyectofinal.fintech.domain.model.TransactionStatus;
import com.proyectofinal.fintech.domain.model.TransactionType;
import com.proyectofinal.fintech.domain.model.Transaccion;
import com.proyectofinal.fintech.domain.model.Usuario;
import com.proyectofinal.fintech.domain.port.TransactionRepository;
import com.proyectofinal.fintech.domain.port.UserRepository;
import com.proyectofinal.fintech.domain.structures.ArbolBST;
import com.proyectofinal.fintech.domain.structures.TablaHash;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Returns top N users ranked by number of OUTGOING SUCCESSFUL transactions.
 * Uses TablaHash for counting and ArbolBST for ordering.
 * Plain class — ZERO Spring/Jakarta imports.
 * ADR-9.3: TopUsers metric = count of OUTGOING SUCCESSFUL transactions.
 * ADR-9.5: ArbolBST Comparable by (-value, id) for desc order via in-order traversal.
 */
public class GetTopUsersUseCase {

    private static final Set<TransactionType> OUTGOING_TYPES = Set.of(
            TransactionType.RECHARGE,
            TransactionType.WITHDRAWAL,
            TransactionType.INTERNAL_TRANSFER,
            TransactionType.EXTERNAL_TRANSFER_SENT
    );

    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;

    public GetTopUsersUseCase(UserRepository userRepository,
                               TransactionRepository transactionRepository) {
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
    }

    /**
     * @param limit max results (1..100)
     * @return ordered list of MetricItem (desc by count, tie-break asc by userId)
     */
    public List<MetricItem> execute(int limit) {
        // Build user map for label lookup
        TablaHash<String, String> userNames = new TablaHash<>();
        for (Usuario u : userRepository.findAll()) {
            userNames.put(u.getId(), u.getName());
        }

        // Count qualifying transactions per user
        TablaHash<String, Integer> counts = new TablaHash<>();
        for (Transaccion tx : transactionRepository.findAll()) {
            if (tx.getStatus() == TransactionStatus.SUCCESSFUL
                    && OUTGOING_TYPES.contains(tx.getType())) {
                String uid = tx.getSourceUserId();
                if (uid != null && userNames.containsKey(uid)) {
                    int current = counts.get(uid).orElse(0);
                    counts.put(uid, current + 1);
                }
            }
        }

        // Sort via ArbolBST — MetricNode Comparable: desc by value, asc by id
        ArbolBST<MetricNode> bst = new ArbolBST<>();
        for (String uid : counts.keys()) {
            int count = counts.get(uid).orElse(0);
            String label = userNames.get(uid).orElse(uid);
            bst.insert(new MetricNode(uid, label, count));
        }

        // inOrder() yields ascending by MetricNode compareTo — since (-value, id) the order is
        // smallest-first for negated value i.e. highest value first
        List<MetricItem> result = new ArrayList<>();
        int added = 0;
        for (MetricNode node : bst.inOrder()) {
            if (added >= limit) break;
            result.add(new MetricItem(node.id(), node.label(), node.value()));
            added++;
        }
        return result;
    }

    /**
     * Internal node for ArbolBST sorting.
     * Comparable: ascending by (-value, id) → inOrder() yields desc value, asc id on tie.
     */
    record MetricNode(String id, String label, double value) implements Comparable<MetricNode> {
        @Override
        public int compareTo(MetricNode other) {
            // Sort by descending value (negate), then ascending id
            int cmp = Double.compare(-this.value, -other.value);
            if (cmp != 0) return cmp;
            return this.id.compareTo(other.id);
        }
    }
}
