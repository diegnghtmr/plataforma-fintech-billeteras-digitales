package com.proyectofinal.fintech.domain.structures;

import org.junit.jupiter.api.Test;

import java.util.Comparator;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for ColaPrioridad (array-based binary min-heap). TDD RED → GREEN order.
 */
class ColaPrioridadTest {

    @Test
    void naturalOrderPollYieldsAscendingSequence() {
        ColaPrioridad<Integer> pq = new ColaPrioridad<>(Comparator.naturalOrder());
        pq.add(5);
        pq.add(3);
        pq.add(8);
        pq.add(1);
        assertThat(pq.poll()).isEqualTo(Optional.of(1));
        assertThat(pq.poll()).isEqualTo(Optional.of(3));
        assertThat(pq.poll()).isEqualTo(Optional.of(5));
        assertThat(pq.poll()).isEqualTo(Optional.of(8));
    }

    @Test
    void peekDoesNotChangeSizeOrOrder() {
        ColaPrioridad<Integer> pq = new ColaPrioridad<>(Comparator.naturalOrder());
        pq.add(10);
        pq.add(5);
        assertThat(pq.peek()).isEqualTo(Optional.of(5));
        assertThat(pq.peek()).isEqualTo(Optional.of(5));
        assertThat(pq.size()).isEqualTo(2);
    }

    @Test
    void pollOnEmptyReturnsEmpty() {
        ColaPrioridad<Integer> pq = new ColaPrioridad<>(Comparator.naturalOrder());
        assertThat(pq.poll()).isEqualTo(Optional.empty());
        assertThat(pq.size()).isEqualTo(0);
    }

    @Test
    void peekOnEmptyReturnsEmpty() {
        ColaPrioridad<Integer> pq = new ColaPrioridad<>(Comparator.naturalOrder());
        assertThat(pq.peek()).isEqualTo(Optional.empty());
    }

    @Test
    void nullComparatorThrowsIAE() {
        assertThatThrownBy(() -> new ColaPrioridad<Integer>(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void reverseComparatorProducesMaxHeapOrder() {
        ColaPrioridad<Integer> pq = new ColaPrioridad<>(
                (a, b) -> b.compareTo(a));
        pq.add(5);
        pq.add(3);
        pq.add(8);
        pq.add(1);
        assertThat(pq.poll()).isEqualTo(Optional.of(8));
        assertThat(pq.poll()).isEqualTo(Optional.of(5));
        assertThat(pq.poll()).isEqualTo(Optional.of(3));
        assertThat(pq.poll()).isEqualTo(Optional.of(1));
    }

    @Test
    void isEmptyAndSize() {
        ColaPrioridad<String> pq = new ColaPrioridad<>(Comparator.naturalOrder());
        assertThat(pq.isEmpty()).isTrue();
        pq.add("hello");
        assertThat(pq.isEmpty()).isFalse();
        assertThat(pq.size()).isEqualTo(1);
    }
}
