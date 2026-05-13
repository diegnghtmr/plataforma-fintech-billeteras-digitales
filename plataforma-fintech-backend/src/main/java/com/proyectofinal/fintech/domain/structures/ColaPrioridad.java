package com.proyectofinal.fintech.domain.structures;

import java.util.Comparator;
import java.util.Optional;

/**
 * Generic priority queue backed by a hand-rolled array binary min-heap.
 *
 * <p>The ordering is determined by the {@link Comparator} supplied at construction
 * time. To obtain max-heap behaviour, pass a reversed comparator
 * (e.g., {@code (a, b) -> b.compareTo(a)}).
 *
 * <p>Heap invariant: element at index {@code i} is &le; both children at
 * {@code 2i+1} and {@code 2i+2} (per the supplied comparator). The root sits at
 * index 0. When the internal array fills, it doubles in capacity. Not thread-safe.
 *
 * @param <E> type of elements
 */
public class ColaPrioridad<E> {

    private static final int DEFAULT_CAPACITY = 16;

    @SuppressWarnings("unchecked")
    private E[] heap = (E[]) new Object[DEFAULT_CAPACITY];
    private int size;
    private final Comparator<? super E> cmp;

    /**
     * Creates a ColaPrioridad ordered by the given comparator.
     *
     * @param comparator must not be null
     * @throws IllegalArgumentException if {@code comparator} is null
     */
    public ColaPrioridad(Comparator<? super E> comparator) {
        if (comparator == null) {
            throw new IllegalArgumentException("Comparator must not be null");
        }
        this.cmp = comparator;
    }

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Inserts {@code e} into this priority queue. O(log n).
     *
     * @param e element to add
     */
    public void add(E e) {
        ensureCapacity();
        heap[size] = e;
        siftUp(size);
        size++;
    }

    /**
     * Removes and returns the minimum element per comparator. O(log n).
     *
     * @return {@code Optional.of(min)} or {@code Optional.empty()} when empty
     */
    public Optional<E> poll() {
        if (size == 0) {
            return Optional.empty();
        }
        E min = heap[0];
        size--;
        heap[0] = heap[size];
        heap[size] = null;
        if (size > 0) {
            siftDown(0);
        }
        return Optional.of(min);
    }

    /**
     * Returns the minimum element without removing it. O(1).
     *
     * @return {@code Optional.of(min)} or {@code Optional.empty()} when empty
     */
    public Optional<E> peek() {
        if (size == 0) {
            return Optional.empty();
        }
        return Optional.of(heap[0]);
    }

    /**
     * Returns the number of elements in this priority queue. O(1).
     *
     * @return element count
     */
    public int size() {
        return size;
    }

    /**
     * Returns {@code true} if this priority queue contains no elements. O(1).
     *
     * @return {@code true} if empty
     */
    public boolean isEmpty() {
        return size == 0;
    }

    // ── Internal helpers ──────────────────────────────────────────────────────

    private void siftUp(int i) {
        while (i > 0) {
            int parent = (i - 1) / 2;
            if (cmp.compare(heap[i], heap[parent]) < 0) {
                swap(i, parent);
                i = parent;
            } else {
                break;
            }
        }
    }

    private void siftDown(int i) {
        while (true) {
            int left = 2 * i + 1;
            int right = 2 * i + 2;
            int smallest = i;

            if (left < size && cmp.compare(heap[left], heap[smallest]) < 0) {
                smallest = left;
            }
            if (right < size && cmp.compare(heap[right], heap[smallest]) < 0) {
                smallest = right;
            }
            if (smallest == i) {
                break;
            }
            swap(i, smallest);
            i = smallest;
        }
    }

    private void swap(int a, int b) {
        E tmp = heap[a];
        heap[a] = heap[b];
        heap[b] = tmp;
    }

    @SuppressWarnings("unchecked")
    private void ensureCapacity() {
        if (size == heap.length) {
            E[] newHeap = (E[]) new Object[heap.length * 2];
            System.arraycopy(heap, 0, newHeap, 0, size);
            heap = newHeap;
        }
    }
}
