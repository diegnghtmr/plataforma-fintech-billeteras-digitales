package com.proyectofinal.fintech.infrastructure.output.memory;

import com.proyectofinal.fintech.domain.model.Transaccion;
import com.proyectofinal.fintech.domain.model.TransactionType;
import com.proyectofinal.fintech.domain.port.ReversibleOperationsStack;
import com.proyectofinal.fintech.domain.structures.Pila;
import com.proyectofinal.fintech.domain.structures.TablaHash;

import java.util.Optional;

/**
 * In-memory implementation of {@link ReversibleOperationsStack}.
 * Uses {@code TablaHash<String, Pila<Transaccion>>} keyed by userId.
 *
 * <p>Constraint C2: Pila MUST be the underlying structure — NOT java.util.Stack or ArrayDeque.
 * No Spring annotations — infrastructure adapter.
 */
public class InMemoryReversibleOperationsStack implements ReversibleOperationsStack {

    private final TablaHash<String, Pila<Transaccion>> table = new TablaHash<>();

    /**
     * Pushes a transaction onto the user's stack.
     * Silently skips {@code EXTERNAL_TRANSFER_RECEIVED} transactions.
     */
    @Override
    public void push(Transaccion tx) {
        if (tx.getType() == TransactionType.EXTERNAL_TRANSFER_RECEIVED) {
            return; // Spec: skip RECEIVED silently
        }
        Pila<Transaccion> pila = table.get(tx.getSourceUserId())
                .orElseGet(() -> {
                    Pila<Transaccion> newPila = new Pila<>();
                    table.put(tx.getSourceUserId(), newPila);
                    return newPila;
                });
        pila.push(tx);
    }

    /**
     * Removes the transaction with the given id from the user's stack.
     * Uses a temporary Pila to pop-then-reinsert, preserving order of remaining entries.
     *
     * @return {@code true} if removed; {@code false} if not found
     */
    @Override
    public boolean removeById(String userId, String transactionId) {
        Optional<Pila<Transaccion>> pilaOpt = table.get(userId);
        if (pilaOpt.isEmpty()) {
            return false;
        }

        Pila<Transaccion> pila = pilaOpt.get();
        Pila<Transaccion> temp = new Pila<>();
        boolean found = false;

        // Pop all elements into temp; skip the matching one
        while (!pila.isEmpty()) {
            Transaccion top = pila.pop().orElseThrow();
            if (!found && top.getId().equals(transactionId)) {
                found = true;
                // Don't add to temp — this is the one to remove
            } else {
                temp.push(top);
            }
        }

        // Re-push from temp back into pila (reverses temp order back to original)
        while (!temp.isEmpty()) {
            pila.push(temp.pop().orElseThrow());
        }

        return found;
    }

    /**
     * Non-destructive peek at the most recently pushed transaction for the user.
     */
    @Override
    public Optional<Transaccion> peekLatestFor(String userId) {
        return table.get(userId).flatMap(Pila::peek);
    }
}
