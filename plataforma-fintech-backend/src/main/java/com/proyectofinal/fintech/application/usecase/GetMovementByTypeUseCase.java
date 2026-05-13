package com.proyectofinal.fintech.application.usecase;

import com.proyectofinal.fintech.application.result.MetricItem;
import com.proyectofinal.fintech.domain.model.Transaccion;
import com.proyectofinal.fintech.domain.model.TransactionStatus;
import com.proyectofinal.fintech.domain.model.TransactionType;
import com.proyectofinal.fintech.domain.port.TransactionRepository;
import com.proyectofinal.fintech.domain.structures.TablaHash;

import java.util.ArrayList;
import java.util.List;

/**
 * Use case: returns transaction count by type, excluding REVERSED status and EXTERNAL_TRANSFER_RECEIVED type.
 * Uses TablaHash<type, count>.
 * Plain class — ZERO Spring/Jakarta imports.
 */
public class GetMovementByTypeUseCase {

    private final TransactionRepository transactionRepository;

    public GetMovementByTypeUseCase(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    /**
     * Returns a list of MetricItem where id=transactionType name, value=count.
     * Excludes REVERSED and EXTERNAL_TRANSFER_RECEIVED types.
     *
     * @return unordered list of metric items per transaction type
     */
    public List<MetricItem> execute() {
        TablaHash<String, Integer> counts = new TablaHash<>();

        for (Transaccion tx : transactionRepository.findAll()) {
            if (tx.getStatus() == TransactionStatus.REVERSED) continue;
            if (tx.getType() == TransactionType.EXTERNAL_TRANSFER_RECEIVED) continue;
            String key = tx.getType().name();
            int current = counts.get(key).orElse(0);
            counts.put(key, current + 1);
        }

        List<MetricItem> result = new ArrayList<>();
        for (String type : counts.keys()) {
            int count = counts.get(type).orElse(0);
            result.add(new MetricItem(type, type, count));
        }
        return result;
    }
}
