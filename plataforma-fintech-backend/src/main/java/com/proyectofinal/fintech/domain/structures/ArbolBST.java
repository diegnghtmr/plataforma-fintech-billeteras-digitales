package com.proyectofinal.fintech.domain.structures;

import java.util.ArrayList;

/**
 * Generic unbalancing recursive Binary Search Tree (BST).
 *
 * <p>Duplicate inserts are a no-op. Removal of a node with two children uses the
 * in-order successor strategy (smallest value in the right subtree). Null elements
 * are rejected with {@link IllegalArgumentException}. Not thread-safe.
 *
 * <p>{@code java.util.ArrayList} is used ONLY inside boundary helpers
 * {@link #inOrder()} and {@link #inOrderDescending()}.
 *
 * @param <E> comparable element type
 */
public class ArbolBST<E extends Comparable<E>> {

    /** Package-private tree node. */
    static class Node<E> {
        E value;
        Node<E> left;
        Node<E> right;

        Node(E value) {
            this.value = value;
        }
    }

    private Node<E> root;
    private int size;

    /** Creates an empty ArbolBST. */
    public ArbolBST() {
        root = null;
        size = 0;
    }

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Inserts {@code e} into the tree. No-op if already present. O(h).
     *
     * @param e element to insert — must not be null
     * @throws IllegalArgumentException if {@code e} is null
     */
    public void insert(E e) {
        if (e == null) throw new IllegalArgumentException("Element must not be null");
        int before = size;
        root = insertRec(root, e);
        // size is incremented inside insertRec only when a new node is created
    }

    /**
     * Returns {@code true} if {@code e} is present in the tree. O(h).
     *
     * @param e element to test — must not be null
     * @throws IllegalArgumentException if {@code e} is null
     */
    public boolean contains(E e) {
        if (e == null) throw new IllegalArgumentException("Element must not be null");
        return containsRec(root, e);
    }

    /**
     * Removes {@code e} from the tree. Returns {@code true} if removed, {@code false}
     * if not found. Uses in-order successor for two-child removal. O(h).
     *
     * @param e element to remove — must not be null
     * @throws IllegalArgumentException if {@code e} is null
     */
    public boolean remove(E e) {
        if (e == null) throw new IllegalArgumentException("Element must not be null");
        int before = size;
        root = removeRec(root, e);
        return size < before;
    }

    /**
     * Returns an {@link Iterable} of elements in ascending (in-order) sequence. O(n).
     *
     * @return ascending iterable (backed by ArrayList — boundary helper)
     */
    public Iterable<E> inOrder() {
        ArrayList<E> result = new ArrayList<>(size);
        inOrderRec(root, result);
        return result;
    }

    /**
     * Returns an {@link Iterable} of elements in descending (reverse in-order) sequence. O(n).
     *
     * @return descending iterable (backed by ArrayList — boundary helper)
     */
    public Iterable<E> inOrderDescending() {
        ArrayList<E> result = new ArrayList<>(size);
        inOrderDescRec(root, result);
        return result;
    }

    /**
     * Returns the number of elements in this tree. O(1).
     *
     * @return element count
     */
    public int size() {
        return size;
    }

    /**
     * Returns {@code true} if this tree contains no elements. O(1).
     *
     * @return {@code true} if empty
     */
    public boolean isEmpty() {
        return size == 0;
    }

    // ── Internal recursive helpers ────────────────────────────────────────────

    private Node<E> insertRec(Node<E> node, E e) {
        if (node == null) {
            size++;
            return new Node<>(e);
        }
        int cmp = e.compareTo(node.value);
        if (cmp < 0) {
            node.left = insertRec(node.left, e);
        } else if (cmp > 0) {
            node.right = insertRec(node.right, e);
        }
        // cmp == 0 → duplicate, no-op
        return node;
    }

    private boolean containsRec(Node<E> node, E e) {
        if (node == null) return false;
        int cmp = e.compareTo(node.value);
        if (cmp < 0) return containsRec(node.left, e);
        if (cmp > 0) return containsRec(node.right, e);
        return true;
    }

    private Node<E> removeRec(Node<E> node, E e) {
        if (node == null) return null;
        int cmp = e.compareTo(node.value);
        if (cmp < 0) {
            node.left = removeRec(node.left, e);
        } else if (cmp > 0) {
            node.right = removeRec(node.right, e);
        } else {
            // Found: handle the three removal cases
            size--;
            if (node.left == null) return node.right;
            if (node.right == null) return node.left;
            // Two children: replace with in-order successor (min of right subtree)
            Node<E> successor = minNode(node.right);
            node.value = successor.value;
            // Remove the successor from the right subtree (it has no left child)
            size++; // counteract the decrement that will happen inside the recursive call
            node.right = removeRec(node.right, successor.value);
        }
        return node;
    }

    private Node<E> minNode(Node<E> node) {
        while (node.left != null) {
            node = node.left;
        }
        return node;
    }

    private void inOrderRec(Node<E> node, ArrayList<E> acc) {
        if (node == null) return;
        inOrderRec(node.left, acc);
        acc.add(node.value);
        inOrderRec(node.right, acc);
    }

    private void inOrderDescRec(Node<E> node, ArrayList<E> acc) {
        if (node == null) return;
        inOrderDescRec(node.right, acc);
        acc.add(node.value);
        inOrderDescRec(node.left, acc);
    }
}
