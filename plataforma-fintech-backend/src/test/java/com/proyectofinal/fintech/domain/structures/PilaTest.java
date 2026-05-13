package com.proyectofinal.fintech.domain.structures;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for Pila (LIFO stack). TDD RED → GREEN order.
 */
class PilaTest {

    private Pila<Integer> pila;

    @BeforeEach
    void setUp() {
        pila = new Pila<>();
    }

    @Test
    void newPilaIsEmpty() {
        assertThat(pila.isEmpty()).isTrue();
        assertThat(pila.size()).isEqualTo(0);
    }

    @Test
    void pushIncreasesSize() {
        pila.push(1);
        pila.push(2);
        assertThat(pila.size()).isEqualTo(2);
        assertThat(pila.isEmpty()).isFalse();
    }

    @Test
    void popReturnsLIFOOrder() {
        pila.push(1);
        pila.push(2);
        pila.push(3);
        assertThat(pila.pop()).isEqualTo(Optional.of(3));
        assertThat(pila.pop()).isEqualTo(Optional.of(2));
        assertThat(pila.pop()).isEqualTo(Optional.of(1));
    }

    @Test
    void popOnEmptyReturnsEmpty() {
        assertThat(pila.pop()).isEqualTo(Optional.empty());
        assertThat(pila.size()).isEqualTo(0);
    }

    @Test
    void peekDoesNotMutate() {
        pila.push(42);
        assertThat(pila.peek()).isEqualTo(Optional.of(42));
        assertThat(pila.peek()).isEqualTo(Optional.of(42));
        assertThat(pila.size()).isEqualTo(1);
    }

    @Test
    void peekOnEmptyReturnsEmpty() {
        assertThat(pila.peek()).isEqualTo(Optional.empty());
    }

    @Test
    void sizeTracksAllPushes() {
        for (int i = 0; i < 10; i++) {
            pila.push(i);
        }
        assertThat(pila.size()).isEqualTo(10);
    }
}
