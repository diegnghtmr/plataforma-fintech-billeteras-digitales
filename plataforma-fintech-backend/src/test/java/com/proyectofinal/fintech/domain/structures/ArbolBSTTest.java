package com.proyectofinal.fintech.domain.structures;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for ArbolBST (recursive Binary Search Tree). TDD RED → GREEN order.
 */
class ArbolBSTTest {

    private ArbolBST<Integer> bst;

    @BeforeEach
    void setUp() {
        bst = new ArbolBST<>();
    }

    // ── Basic structure ───────────────────────────────────────────────────────

    @Test
    void newTreeIsEmpty() {
        assertThat(bst.size()).isEqualTo(0);
        List<Integer> inOrder = toList(bst.inOrder());
        assertThat(inOrder).isEmpty();
    }

    @Test
    void inOrderYieldsSortedAscending() {
        bst.insert(5);
        bst.insert(2);
        bst.insert(8);
        bst.insert(1);
        assertThat(toList(bst.inOrder())).containsExactly(1, 2, 5, 8);
    }

    @Test
    void inOrderDescendingYieldsSortedDescending() {
        bst.insert(5);
        bst.insert(2);
        bst.insert(8);
        bst.insert(1);
        assertThat(toList(bst.inOrderDescending())).containsExactly(8, 5, 2, 1);
    }

    @Test
    void containsReturnsTrueForPresentElement() {
        bst.insert(10);
        assertThat(bst.contains(10)).isTrue();
    }

    @Test
    void containsReturnsFalseForAbsentElement() {
        bst.insert(10);
        assertThat(bst.contains(99)).isFalse();
    }

    // ── Duplicate insert ──────────────────────────────────────────────────────

    @Test
    void duplicateInsertIsNoop() {
        bst.insert(10);
        bst.insert(10);
        assertThat(bst.size()).isEqualTo(1);
        assertThat(toList(bst.inOrder())).containsExactly(10);
    }

    // ── Remove scenarios ──────────────────────────────────────────────────────

    @Test
    void removeLeafNode() {
        bst.insert(5);
        bst.insert(3);
        bst.insert(7);
        boolean removed = bst.remove(3);
        assertThat(removed).isTrue();
        assertThat(bst.contains(3)).isFalse();
        assertThat(bst.size()).isEqualTo(2);
        assertThat(toList(bst.inOrder())).containsExactly(5, 7);
    }

    @Test
    void removeNodeWithOneChild() {
        bst.insert(5);
        bst.insert(3);
        bst.insert(1); // 3 has one child: 1
        boolean removed = bst.remove(3);
        assertThat(removed).isTrue();
        assertThat(bst.contains(3)).isFalse();
        assertThat(bst.size()).isEqualTo(2);
        assertThat(toList(bst.inOrder())).containsExactly(1, 5);
    }

    @Test
    void removeNodeWithTwoChildren() {
        bst.insert(5);
        bst.insert(3);
        bst.insert(7);
        bst.insert(6);
        bst.insert(8);
        // Remove 7 which has two children (6 and 8); in-order successor is 8
        boolean removed = bst.remove(7);
        assertThat(removed).isTrue();
        assertThat(bst.contains(7)).isFalse();
        assertThat(bst.size()).isEqualTo(4);
        assertThat(toList(bst.inOrder())).containsExactly(3, 5, 6, 8);
    }

    @Test
    void removeMissingElementReturnsFalse() {
        bst.insert(5);
        assertThat(bst.remove(99)).isFalse();
        assertThat(bst.size()).isEqualTo(1);
    }

    // ── Null guard ────────────────────────────────────────────────────────────

    @Test
    void insertNullThrowsIAE() {
        assertThatThrownBy(() -> bst.insert(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void containsNullThrowsIAE() {
        assertThatThrownBy(() -> bst.contains(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void removeNullThrowsIAE() {
        assertThatThrownBy(() -> bst.remove(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    // ── Helper ────────────────────────────────────────────────────────────────

    private static <T> List<T> toList(Iterable<T> it) {
        List<T> list = new ArrayList<>();
        it.forEach(list::add);
        return list;
    }
}
