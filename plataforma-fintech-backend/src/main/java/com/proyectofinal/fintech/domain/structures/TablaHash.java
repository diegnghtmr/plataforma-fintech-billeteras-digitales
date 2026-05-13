package com.proyectofinal.fintech.domain.structures;

import java.util.ArrayList;
import java.util.Optional;

/**
 * Generic hash table with separate chaining for collision resolution.
 *
 * <p>Default capacity is 16 with a load-factor threshold of 0.75. When the threshold
 * is exceeded the table doubles its capacity and rehashes all entries. A null key is
 * rejected with {@link IllegalArgumentException}; null values are permitted.
 *
 * <p>{@code java.util.ArrayList} is used ONLY inside boundary helpers
 * {@link #keys()} and {@link #values()}. Not thread-safe.
 *
 * @param <K> type of keys
 * @param <V> type of values
 */
public class TablaHash<K, V> {

    private static final int DEFAULT_CAPACITY = 16;
    private static final double LOAD_FACTOR = 0.75;

    /** Package-private singly-linked chain node. */
    static class Node<K, V> {
        final K key;
        V value;
        Node<K, V> next;

        Node(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }

    @SuppressWarnings("unchecked")
    private Node<K, V>[] buckets = new Node[DEFAULT_CAPACITY];
    private int capacity;
    private int size;

    /** Creates a TablaHash with the default capacity (16). */
    public TablaHash() {
        this(DEFAULT_CAPACITY);
    }

    /**
     * Creates a TablaHash with the given initial capacity.
     *
     * @param initialCapacity must be &gt; 0
     * @throws IllegalArgumentException if {@code initialCapacity <= 0}
     */
    @SuppressWarnings("unchecked")
    public TablaHash(int initialCapacity) {
        if (initialCapacity <= 0) {
            throw new IllegalArgumentException(
                    "initialCapacity must be > 0, got: " + initialCapacity);
        }
        this.capacity = initialCapacity;
        this.buckets = new Node[capacity];
        this.size = 0;
    }

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Associates {@code value} with {@code key}. Returns the previous value, or {@code null}
     * if no previous mapping existed. Resizes the table if load-factor threshold is exceeded.
     *
     * @param key   non-null key
     * @param value value to store (may be null)
     * @return previous value or {@code null}
     * @throws IllegalArgumentException if {@code key} is null
     */
    public V put(K key, V value) {
        if (key == null) {
            throw new IllegalArgumentException("Key must not be null");
        }
        if ((double) size / capacity > LOAD_FACTOR) {
            resize();
        }
        int idx = bucketIndex(key);
        Node<K, V> cur = buckets[idx];
        while (cur != null) {
            if (cur.key.equals(key)) {
                V old = cur.value;
                cur.value = value;
                return old;
            }
            cur = cur.next;
        }
        // Prepend new node
        Node<K, V> node = new Node<>(key, value);
        node.next = buckets[idx];
        buckets[idx] = node;
        size++;
        return null;
    }

    /**
     * Returns the value mapped to {@code key}, or {@link Optional#empty()} if absent.
     * When the stored value is {@code null} this method also returns {@link Optional#empty()}.
     *
     * @param key key to look up (may be null — returns empty)
     * @return optional value
     */
    public Optional<V> get(K key) {
        if (key == null) {
            return Optional.empty();
        }
        Node<K, V> node = findNode(key);
        return (node != null) ? Optional.ofNullable(node.value) : Optional.empty();
    }

    /**
     * Removes the mapping for {@code key} and returns its value, or {@link Optional#empty()}.
     *
     * @param key key to remove (may be null — returns empty)
     * @return optional previous value
     */
    public Optional<V> remove(K key) {
        if (key == null) {
            return Optional.empty();
        }
        int idx = bucketIndex(key);
        Node<K, V> cur = buckets[idx];
        Node<K, V> prev = null;
        while (cur != null) {
            if (cur.key.equals(key)) {
                if (prev == null) {
                    buckets[idx] = cur.next;
                } else {
                    prev.next = cur.next;
                }
                size--;
                return Optional.ofNullable(cur.value);
            }
            prev = cur;
            cur = cur.next;
        }
        return Optional.empty();
    }

    /**
     * Returns {@code true} if this table contains a mapping for {@code key}.
     *
     * @param key key to test
     * @return {@code true} if present
     */
    public boolean containsKey(K key) {
        if (key == null) {
            return false;
        }
        return findNode(key) != null;
    }

    /**
     * Returns the number of key-value mappings. O(1).
     *
     * @return size
     */
    public int size() {
        return size;
    }

    /**
     * Returns {@code true} if this table contains no mappings. O(1).
     *
     * @return {@code true} if empty
     */
    public boolean isEmpty() {
        return size == 0;
    }

    // ── Boundary helpers (ArrayList allowed here only) ────────────────────────

    /**
     * Returns an {@link Iterable} over all keys in this table. O(capacity + size).
     *
     * @return keys iterable
     */
    public Iterable<K> keys() {
        ArrayList<K> result = new ArrayList<>(size);
        for (int i = 0; i < capacity; i++) {
            Node<K, V> cur = buckets[i];
            while (cur != null) {
                result.add(cur.key);
                cur = cur.next;
            }
        }
        return result;
    }

    /**
     * Returns an {@link Iterable} over all values in this table. O(capacity + size).
     *
     * @return values iterable
     */
    public Iterable<V> values() {
        ArrayList<V> result = new ArrayList<>(size);
        for (int i = 0; i < capacity; i++) {
            Node<K, V> cur = buckets[i];
            while (cur != null) {
                result.add(cur.value);
                cur = cur.next;
            }
        }
        return result;
    }

    // ── Internal helpers ──────────────────────────────────────────────────────

    private int bucketIndex(K key) {
        return Math.floorMod(key.hashCode(), capacity);
    }

    private Node<K, V> findNode(K key) {
        int idx = bucketIndex(key);
        Node<K, V> cur = buckets[idx];
        while (cur != null) {
            if (cur.key.equals(key)) {
                return cur;
            }
            cur = cur.next;
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private void resize() {
        int newCapacity = capacity * 2;
        Node<K, V>[] newBuckets = new Node[newCapacity];
        for (int i = 0; i < capacity; i++) {
            Node<K, V> cur = buckets[i];
            while (cur != null) {
                Node<K, V> next = cur.next;
                int newIdx = Math.floorMod(cur.key.hashCode(), newCapacity);
                cur.next = newBuckets[newIdx];
                newBuckets[newIdx] = cur;
                cur = next;
            }
        }
        buckets = newBuckets;
        capacity = newCapacity;
    }
}
