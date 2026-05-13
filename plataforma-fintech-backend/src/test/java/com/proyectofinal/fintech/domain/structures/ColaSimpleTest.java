package com.proyectofinal.fintech.domain.structures;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for ColaSimple (FIFO queue). TDD RED → GREEN order.
 */
class ColaSimpleTest {

    private ColaSimple<String> cola;

    @BeforeEach
    void setUp() {
        cola = new ColaSimple<>();
    }

    @Test
    void newColaIsEmpty() {
        assertThat(cola.isEmpty()).isTrue();
        assertThat(cola.size()).isEqualTo(0);
    }

    @Test
    void enqueueIncreasesSize() {
        cola.enqueue("a");
        cola.enqueue("b");
        assertThat(cola.size()).isEqualTo(2);
        assertThat(cola.isEmpty()).isFalse();
    }

    @Test
    void dequeueReturnsFIFOOrder() {
        cola.enqueue("first");
        cola.enqueue("second");
        cola.enqueue("third");
        assertThat(cola.dequeue()).isEqualTo(Optional.of("first"));
        assertThat(cola.dequeue()).isEqualTo(Optional.of("second"));
        assertThat(cola.dequeue()).isEqualTo(Optional.of("third"));
    }

    @Test
    void dequeueOnEmptyReturnsEmpty() {
        assertThat(cola.dequeue()).isEqualTo(Optional.empty());
        assertThat(cola.size()).isEqualTo(0);
    }

    @Test
    void peekReturnsHeadWithoutRemoving() {
        cola.enqueue("head");
        cola.enqueue("next");
        assertThat(cola.peek()).isEqualTo(Optional.of("head"));
        assertThat(cola.peek()).isEqualTo(Optional.of("head"));
        assertThat(cola.size()).isEqualTo(2);
    }

    @Test
    void peekOnEmptyReturnsEmpty() {
        assertThat(cola.peek()).isEqualTo(Optional.empty());
    }

    @Test
    void dequeueDecreasesSize() {
        cola.enqueue("x");
        cola.enqueue("y");
        cola.dequeue();
        assertThat(cola.size()).isEqualTo(1);
    }
}
