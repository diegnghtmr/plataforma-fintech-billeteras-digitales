package com.proyectofinal.fintech.domain.structures;

import java.util.Optional;

/**
 * Generic FIFO queue backed by a singly-linked list with head and tail pointers.
 *
 * <p>All operations run in O(1) time. Not thread-safe.
 *
 * @param <E> type of elements held in this queue
 */
public class ColaSimple<E> {

    /** Package-private singly-linked node. */
    static class Node<E> {
        E value;
        Node<E> next;

        Node(E value) {
            this.value = value;
        }
    }

    private Node<E> head;
    private Node<E> tail;
    private int size;

    /** Creates an empty ColaSimple. */
    public ColaSimple() {
        head = null;
        tail = null;
        size = 0;
    }

    /**
     * Adds an element to the tail of this queue. O(1).
     *
     * @param e element to enqueue
     */
    public void enqueue(E e) {
        Node<E> node = new Node<>(e);
        if (tail == null) {
            head = node;
            tail = node;
        } else {
            tail.next = node;
            tail = node;
        }
        size++;
    }

    /**
     * Removes and returns the head element. O(1).
     *
     * @return {@code Optional.of(head)} or {@code Optional.empty()} when empty
     */
    public Optional<E> dequeue() {
        if (head == null) {
            return Optional.empty();
        }
        E value = head.value;
        head = head.next;
        if (head == null) {
            tail = null;
        }
        size--;
        return Optional.of(value);
    }

    /**
     * Returns the head element without removing it. O(1).
     *
     * @return {@code Optional.of(head)} or {@code Optional.empty()} when empty
     */
    public Optional<E> peek() {
        if (head == null) {
            return Optional.empty();
        }
        return Optional.of(head.value);
    }

    /**
     * Returns the number of elements in this queue. O(1).
     *
     * @return element count
     */
    public int size() {
        return size;
    }

    /**
     * Returns {@code true} if this queue contains no elements. O(1).
     *
     * @return {@code true} if empty
     */
    public boolean isEmpty() {
        return size == 0;
    }
}
