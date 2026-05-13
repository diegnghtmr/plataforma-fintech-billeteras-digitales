package com.proyectofinal.fintech.domain.structures;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for MiLista (doubly-linked list). TDD RED → GREEN order.
 */
class MiListaTest {

    private MiLista<String> lista;

    @BeforeEach
    void setUp() {
        lista = new MiLista<>();
    }

    @Test
    void newListaIsEmpty() {
        assertThat(lista.isEmpty()).isTrue();
        assertThat(lista.size()).isEqualTo(0);
    }

    @Test
    void addPreservesInsertionOrder() {
        lista.add("a");
        lista.add("b");
        lista.add("c");
        Iterator<String> it = lista.iterator();
        assertThat(it.next()).isEqualTo("a");
        assertThat(it.next()).isEqualTo("b");
        assertThat(it.next()).isEqualTo("c");
        assertThat(it.hasNext()).isFalse();
    }

    @Test
    void addFirstPrependsElements() {
        lista.add("b");
        lista.addFirst("a");
        assertThat(lista.get(0)).isEqualTo("a");
        assertThat(lista.get(1)).isEqualTo("b");
    }

    @Test
    void getFirstAndLastElement() {
        lista.add("x");
        lista.add("y");
        assertThat(lista.get(0)).isEqualTo("x");
        assertThat(lista.get(1)).isEqualTo("y");
    }

    @Test
    void getOutOfBoundsNegativeThrows() {
        lista.add("a");
        lista.add("b");
        assertThatThrownBy(() -> lista.get(-1))
                .isInstanceOf(IndexOutOfBoundsException.class);
    }

    @Test
    void getOutOfBoundsEqualSizeThrows() {
        lista.add("a");
        lista.add("b");
        assertThatThrownBy(() -> lista.get(2))
                .isInstanceOf(IndexOutOfBoundsException.class);
    }

    @Test
    void setReturnsOldValueAndInstallsNew() {
        lista.add("old");
        String prev = lista.set(0, "new");
        assertThat(prev).isEqualTo("old");
        assertThat(lista.get(0)).isEqualTo("new");
    }

    @Test
    void setOutOfBoundsThrows() {
        assertThatThrownBy(() -> lista.set(0, "x"))
                .isInstanceOf(IndexOutOfBoundsException.class);
    }

    @Test
    void removeByIndexShiftsElements() {
        lista.add("a");
        lista.add("b");
        lista.add("c");
        String removed = lista.remove(1);
        assertThat(removed).isEqualTo("b");
        assertThat(lista.size()).isEqualTo(2);
        assertThat(lista.get(0)).isEqualTo("a");
        assertThat(lista.get(1)).isEqualTo("c");
    }

    @Test
    void removeFirstOnNonEmptyReturnsHead() {
        lista.add("first");
        lista.add("second");
        Optional<String> removed = lista.removeFirst();
        assertThat(removed).isEqualTo(Optional.of("first"));
        assertThat(lista.size()).isEqualTo(1);
        assertThat(lista.get(0)).isEqualTo("second");
    }

    @Test
    void removeLastOnNonEmptyReturnsTail() {
        lista.add("a");
        lista.add("b");
        lista.add("c");
        Optional<String> removed = lista.removeLast();
        assertThat(removed).isEqualTo(Optional.of("c"));
        assertThat(lista.size()).isEqualTo(2);
        assertThat(lista.get(1)).isEqualTo("b");
    }

    @Test
    void removeFirstOnEmptyReturnsEmpty() {
        assertThat(lista.removeFirst()).isEqualTo(Optional.empty());
    }

    @Test
    void removeLastOnEmptyReturnsEmpty() {
        assertThat(lista.removeLast()).isEqualTo(Optional.empty());
    }

    @Test
    void toListReturnsIndependentCopy() {
        lista.add("a");
        lista.add("b");
        List<String> snapshot = lista.toList();
        lista.add("c");
        assertThat(snapshot).hasSize(2);
        assertThat(snapshot).containsExactly("a", "b");
    }
}
