package com.proyectofinal.fintech.domain.structures;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ConjuntoTest {

    @Test
    void add_andContains_happyPath() {
        Conjunto<String> s = new Conjunto<>();
        s.add("alpha");
        assertThat(s.contains("alpha")).isTrue();
    }

    @Test
    void contains_absentValue_returnsFalse() {
        Conjunto<String> s = new Conjunto<>();
        s.add("alpha");
        assertThat(s.contains("beta")).isFalse();
    }

    @Test
    void add_duplicate_doesNotIncrementSize() {
        Conjunto<Integer> s = new Conjunto<>();
        s.add(42);
        s.add(42);
        assertThat(s.size()).isEqualTo(1);
    }

    @Test
    void size_emptyConjunto_isZero() {
        assertThat(new Conjunto<>().size()).isZero();
    }

    @Test
    void isEmpty_emptyConjunto_isTrue() {
        assertThat(new Conjunto<>().isEmpty()).isTrue();
    }

    @Test
    void isEmpty_nonEmpty_isFalse() {
        Conjunto<String> s = new Conjunto<>();
        s.add("x");
        assertThat(s.isEmpty()).isFalse();
    }

    @Test
    void of_varargs_constructsCorrectly() {
        Conjunto<Integer> s = Conjunto.of(1, 2, 3);
        assertThat(s.size()).isEqualTo(3);
        assertThat(s.contains(1)).isTrue();
        assertThat(s.contains(2)).isTrue();
        assertThat(s.contains(3)).isTrue();
    }

    @Test
    void of_varargs_doesNotContainAbsentValue() {
        Conjunto<Integer> s = Conjunto.of(1, 2, 3);
        assertThat(s.contains(99)).isFalse();
    }

    @Test
    void iterator_traversesAllElements() {
        Conjunto<String> s = Conjunto.of("a", "b", "c");
        List<String> collected = new ArrayList<>();
        for (String v : s) {
            collected.add(v);
        }
        assertThat(collected).containsExactlyInAnyOrder("a", "b", "c");
    }

    @Test
    void iterator_emptyConjunto_noElements() {
        Conjunto<String> s = new Conjunto<>();
        List<String> collected = new ArrayList<>();
        for (String v : s) {
            collected.add(v);
        }
        assertThat(collected).isEmpty();
    }
}
