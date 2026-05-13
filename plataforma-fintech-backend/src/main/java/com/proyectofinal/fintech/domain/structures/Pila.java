package com.proyectofinal.fintech.domain.structures;

import java.util.Optional;

/**
 * Generic LIFO stack backed by a singly-linked list of package-private nodes.
 *
 * <p>All operations run in O(1) time. Not thread-safe.
 *
 * @param <E> type of elements held in this stack
 */
public class Pila<E> {

    /** Package-private singly-linked node. */
    static class Node<E> {
        E value;
        Node<E> next;

        Node(E value) {
            this.value = value;
        }
    }

    private Node<E> top;
    private int size;

    /** Creates an empty Pila. */
    public Pila() {
        top = null;
        size = 0;
    }

    /**
     * Pushes an element onto the top of this stack. O(1).
     *
     * @param e element to push
     */
    public void push(E e) {
        Node<E> node = new Node<>(e);
        node.next = top;
        top = node;
        size++;
    }

    /**
     * Removes and returns the top element. O(1).
     *
     * @return {@code Optional.of(top)} or {@code Optional.empty()} when empty
     */
    public Optional<E> pop() {
        if (top == null) {
            return Optional.empty();
        }
        E value = top.value;
        top = top.next;
        size--;
        return Optional.of(value);
    }

    /**
     * Returns the top element without removing it. O(1).
     *
     * @return {@code Optional.of(top)} or {@code Optional.empty()} when empty
     */
    public Optional<E> peek() {
        if (top == null) {
            return Optional.empty();
        }
        return Optional.of(top.value);
    }

    /**
     * Returns the number of elements in this stack. O(1).
     *
     * @return element count
     */
    public int size() {
        return size;
    }

    /**
     * Returns {@code true} if this stack contains no elements. O(1).
     *
     * @return {@code true} if empty
     */
    public boolean isEmpty() {
        return size == 0;
    }
}
