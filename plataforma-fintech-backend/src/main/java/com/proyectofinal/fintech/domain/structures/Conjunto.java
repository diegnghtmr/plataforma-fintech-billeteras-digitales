package com.proyectofinal.fintech.domain.structures;

import java.util.Iterator;

/**
 * Generic unordered set backed by {@link TablaHash}{@code <T, Boolean>}.
 *
 * <p>All membership operations are O(1) average, O(n) worst case — same as the
 * underlying hash table. Iteration is O(capacity + size).
 *
 * <p>Replaces {@code java.util.HashSet} / {@code Set.of()} in domain and application
 * layers, closing the last gap in hexagonal purity (ADR-9.1).
 *
 * @param <T> element type (must implement {@link Object#hashCode()} and
 *            {@link Object#equals(Object)} correctly)
 */
public class Conjunto<T> implements Iterable<T> {

    private final TablaHash<T, Boolean> table = new TablaHash<>();

    /**
     * Adds {@code value} to this set. If {@code value} is already present the
     * set is unchanged (size does not grow).
     *
     * @param value element to add (must not be null)
     */
    public void add(T value) {
        table.put(value, Boolean.TRUE);
    }

    /**
     * Returns {@code true} if this set contains {@code value}.
     *
     * @param value element to test
     * @return {@code true} if present
     */
    public boolean contains(T value) {
        return table.containsKey(value);
    }

    /**
     * Returns the number of distinct elements. O(1).
     *
     * @return size
     */
    public int size() {
        return table.size();
    }

    /**
     * Returns {@code true} if this set contains no elements. O(1).
     *
     * @return {@code true} if empty
     */
    public boolean isEmpty() {
        return table.isEmpty();
    }

    /**
     * Returns an iterator over the elements in this set (order undefined).
     * O(capacity + size) per full traversal.
     *
     * @return iterator
     */
    @Override
    public Iterator<T> iterator() {
        return table.keys().iterator();
    }

    // ── Varargs factory — replaces Set.of(...) ────────────────────────────────

    /**
     * Creates a {@code Conjunto} pre-populated with the given values.
     * Duplicates are silently ignored.
     *
     * @param values elements to add
     * @param <T>    element type
     * @return new set containing all distinct values
     */
    @SafeVarargs
    public static <T> Conjunto<T> of(T... values) {
        Conjunto<T> s = new Conjunto<>();
        for (T v : values) {
            s.add(v);
        }
        return s;
    }
}
