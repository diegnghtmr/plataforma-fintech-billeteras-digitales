package com.proyectofinal.fintech.domain.structures;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

/**
 * Generic doubly-linked list with indexed access and {@link Iterable} support.
 *
 * <p>Internal state is maintained solely via {@link Node} links (head + tail).
 * {@code java.util.ArrayList} is used ONLY inside boundary helper method {@link #toList()}.
 * Not thread-safe. Iterators are not fail-fast.
 *
 * @param <E> type of elements held in this list
 */
public class MiLista<E> implements Iterable<E> {

    /** Package-private doubly-linked node. */
    static class Node<E> {
        E value;
        Node<E> prev;
        Node<E> next;

        Node(E value) {
            this.value = value;
        }
    }

    private Node<E> head;
    private Node<E> tail;
    private int size;

    /** Creates an empty MiLista. */
    public MiLista() {
        head = null;
        tail = null;
        size = 0;
    }

    // ── Mutation ──────────────────────────────────────────────────────────────

    /**
     * Appends {@code e} to the tail of this list. O(1).
     *
     * @param e element to append
     */
    public void add(E e) {
        Node<E> node = new Node<>(e);
        if (tail == null) {
            head = node;
            tail = node;
        } else {
            node.prev = tail;
            tail.next = node;
            tail = node;
        }
        size++;
    }

    /**
     * Prepends {@code e} to the head of this list. O(1).
     *
     * @param e element to prepend
     */
    public void addFirst(E e) {
        Node<E> node = new Node<>(e);
        if (head == null) {
            head = node;
            tail = node;
        } else {
            node.next = head;
            head.prev = node;
            head = node;
        }
        size++;
    }

    // ── Access ────────────────────────────────────────────────────────────────

    /**
     * Returns the element at the given index. O(n).
     *
     * @param index zero-based index
     * @return element at {@code index}
     * @throws IndexOutOfBoundsException if {@code index < 0} or {@code index >= size()}
     */
    public E get(int index) {
        return nodeAt(index).value;
    }

    /**
     * Replaces the element at {@code index} with {@code e} and returns the previous value. O(n).
     *
     * @param index zero-based index
     * @param e     replacement value
     * @return the element previously at {@code index}
     * @throws IndexOutOfBoundsException if {@code index < 0} or {@code index >= size()}
     */
    public E set(int index, E e) {
        Node<E> node = nodeAt(index);
        E old = node.value;
        node.value = e;
        return old;
    }

    /**
     * Removes and returns the element at {@code index}. O(n).
     *
     * @param index zero-based index
     * @return element previously at {@code index}
     * @throws IndexOutOfBoundsException if {@code index < 0} or {@code index >= size()}
     */
    public E remove(int index) {
        Node<E> node = nodeAt(index);
        unlink(node);
        return node.value;
    }

    /**
     * Removes and returns the first element. O(1).
     *
     * @return {@code Optional.of(first)} or {@code Optional.empty()} when empty
     */
    public Optional<E> removeFirst() {
        if (head == null) {
            return Optional.empty();
        }
        E value = head.value;
        unlink(head);
        return Optional.of(value);
    }

    /**
     * Removes and returns the last element. O(1).
     *
     * @return {@code Optional.of(last)} or {@code Optional.empty()} when empty
     */
    public Optional<E> removeLast() {
        if (tail == null) {
            return Optional.empty();
        }
        E value = tail.value;
        unlink(tail);
        return Optional.of(value);
    }

    // ── Metadata ──────────────────────────────────────────────────────────────

    /**
     * Returns the number of elements in this list. O(1).
     *
     * @return element count
     */
    public int size() {
        return size;
    }

    /**
     * Returns {@code true} if this list contains no elements. O(1).
     *
     * @return {@code true} if empty
     */
    public boolean isEmpty() {
        return size == 0;
    }

    // ── Iteration ─────────────────────────────────────────────────────────────

    /**
     * Returns an iterator over the elements from head to tail. Not fail-fast.
     *
     * @return iterator
     */
    @Override
    public Iterator<E> iterator() {
        return new Iterator<>() {
            private Node<E> cursor = head;

            @Override
            public boolean hasNext() {
                return cursor != null;
            }

            @Override
            public E next() {
                if (cursor == null) {
                    throw new NoSuchElementException();
                }
                E value = cursor.value;
                cursor = cursor.next;
                return value;
            }
        };
    }

    // ── Boundary helper (ArrayList allowed here only) ─────────────────────────

    /**
     * Returns an independent {@link java.util.List} snapshot of this list. O(n).
     * Mutations to the returned list do not affect this MiLista and vice-versa.
     *
     * @return independent {@code ArrayList} copy
     */
    public List<E> toList() {
        List<E> result = new ArrayList<>(size);
        Node<E> cur = head;
        while (cur != null) {
            result.add(cur.value);
            cur = cur.next;
        }
        return result;
    }

    // ── Internal helpers ──────────────────────────────────────────────────────

    private Node<E> nodeAt(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException(
                    "Index " + index + " out of bounds for size " + size);
        }
        Node<E> cur = head;
        for (int i = 0; i < index; i++) {
            cur = cur.next;
        }
        return cur;
    }

    private void unlink(Node<E> node) {
        if (node.prev != null) {
            node.prev.next = node.next;
        } else {
            head = node.next;
        }
        if (node.next != null) {
            node.next.prev = node.prev;
        } else {
            tail = node.prev;
        }
        node.prev = null;
        node.next = null;
        size--;
    }
}
