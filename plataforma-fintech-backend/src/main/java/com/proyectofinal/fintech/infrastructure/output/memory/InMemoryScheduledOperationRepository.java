package com.proyectofinal.fintech.infrastructure.output.memory;

import com.proyectofinal.fintech.domain.model.OperacionProgramada;
import com.proyectofinal.fintech.domain.model.ScheduledOperationStatus;
import com.proyectofinal.fintech.domain.port.ScheduledOperationRepository;
import com.proyectofinal.fintech.domain.structures.ColaPrioridad;
import com.proyectofinal.fintech.domain.structures.TablaHash;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * In-memory adapter for ScheduledOperationRepository.
 *
 * <p>ADR-8.1: ColaPrioridad is rebuilt per findPendingInPriorityOrder call (not maintained
 * statelessly) to avoid dual-source-of-truth with TablaHash and to keep save() simple.
 *
 * <p>Primary index: {@code TablaHash<String, OperacionProgramada>} byId.
 * findPendingInPriorityOrder: filters PENDING from byId.values(), builds a temporary
 * {@code ColaPrioridad<OperacionProgramada>} (Comparator.naturalOrder()), then polls into ArrayList.
 */
public class InMemoryScheduledOperationRepository implements ScheduledOperationRepository {

    private final TablaHash<String, OperacionProgramada> byId = new TablaHash<>();

    @Override
    public void save(OperacionProgramada op) {
        byId.put(op.getId(), op);
    }

    @Override
    public Optional<OperacionProgramada> findById(String id) {
        return byId.get(id);
    }

    @Override
    public Iterable<OperacionProgramada> findAll() {
        return byId.values();
    }

    @Override
    public Iterable<OperacionProgramada> findPendingInPriorityOrder() {
        // ADR-8.1: rebuild ColaPrioridad per call — non-destructive to primary index
        ColaPrioridad<OperacionProgramada> queue =
                new ColaPrioridad<>(Comparator.naturalOrder());

        for (OperacionProgramada op : byId.values()) {
            if (op.getStatus() == ScheduledOperationStatus.PENDING) {
                queue.add(op);
            }
        }

        List<OperacionProgramada> result = new ArrayList<>();
        while (!queue.isEmpty()) {
            queue.poll().ifPresent(result::add);
        }
        return result;
    }

    @Override
    public void deleteBySourceUserId(String userId) {
        List<String> toDelete = new ArrayList<>();
        for (OperacionProgramada op : byId.values()) {
            if (userId.equals(op.getSourceUserId())) {
                toDelete.add(op.getId());
            }
        }
        for (String id : toDelete) {
            byId.remove(id);
        }
    }
}
