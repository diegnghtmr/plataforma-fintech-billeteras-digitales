package com.proyectofinal.fintech.domain.structures;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Tests for TablaHash (separate-chaining hash table). TDD RED → GREEN order.
 *
 * <p>Uses a local {@code FixedHashKey} record whose hashCode always returns 1
 * to force collision scenarios without relying on implementation internals.
 */
class TablaHashTest {

    /** Key class with a fixed hashCode to force bucket collisions. */
    record FixedHashKey(int id) {
        @Override
        public int hashCode() {
            return 1;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof FixedHashKey other)) return false;
            return id == other.id;
        }
    }

    private TablaHash<String, Integer> map;

    @BeforeEach
    void setUp() {
        map = new TablaHash<>();
    }

    @Test
    void newTableIsEmpty() {
        assertThat(map.isEmpty()).isTrue();
        assertThat(map.size()).isEqualTo(0);
    }

    @Test
    void putAndGet() {
        map.put("alice", 100);
        assertThat(map.get("alice")).isEqualTo(Optional.of(100));
    }

    @Test
    void putSameKeyReturnsOldValue() {
        map.put("alice", 100);
        Integer old = map.put("alice", 200);
        assertThat(old).isEqualTo(100);
        assertThat(map.get("alice")).isEqualTo(Optional.of(200));
        assertThat(map.size()).isEqualTo(1);
    }

    @Test
    void putNewKeyReturnsNull() {
        Integer old = map.put("bob", 42);
        assertThat(old).isNull();
    }

    @Test
    void getMissingKeyReturnsEmpty() {
        assertThat(map.get("missing")).isEqualTo(Optional.empty());
    }

    @Test
    void removeExistingKeyReturnsValue() {
        map.put("alice", 1);
        Optional<Integer> removed = map.remove("alice");
        assertThat(removed).isEqualTo(Optional.of(1));
        assertThat(map.size()).isEqualTo(0);
        assertThat(map.get("alice")).isEqualTo(Optional.empty());
    }

    @Test
    void removeMissingKeyReturnsEmpty() {
        assertThat(map.remove("none")).isEqualTo(Optional.empty());
    }

    @Test
    void nullKeyThrowsIAE() {
        assertThatThrownBy(() -> map.put(null, 1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void nullValueStoredAndRetrievedCorrectly() {
        map.put("key", null);
        assertThat(map.containsKey("key")).isTrue();
        assertThat(map.get("key")).isEqualTo(Optional.empty()); // null value → empty Optional
    }

    @Test
    void collisionBothKeysRetrievableAndRemovable() {
        TablaHash<FixedHashKey, String> collisionMap = new TablaHash<>();
        FixedHashKey k1 = new FixedHashKey(1);
        FixedHashKey k2 = new FixedHashKey(2);
        // Both map to same bucket (hashCode == 1)
        collisionMap.put(k1, "valueOne");
        collisionMap.put(k2, "valueTwo");

        assertThat(collisionMap.get(k1)).isEqualTo(Optional.of("valueOne"));
        assertThat(collisionMap.get(k2)).isEqualTo(Optional.of("valueTwo"));

        collisionMap.remove(k1);
        assertThat(collisionMap.get(k1)).isEqualTo(Optional.empty());
        assertThat(collisionMap.get(k2)).isEqualTo(Optional.of("valueTwo"));
    }

    @Test
    void resizePreservesAllEntries() {
        // Capacity 4, load factor 0.75 → resize after 3 inserts (size/cap > 0.75)
        TablaHash<String, Integer> small = new TablaHash<>(4);
        small.put("a", 1);
        small.put("b", 2);
        small.put("c", 3);
        small.put("d", 4);  // triggers resize
        assertThat(small.size()).isEqualTo(4);
        assertThat(small.get("a")).isEqualTo(Optional.of(1));
        assertThat(small.get("b")).isEqualTo(Optional.of(2));
        assertThat(small.get("c")).isEqualTo(Optional.of(3));
        assertThat(small.get("d")).isEqualTo(Optional.of(4));
    }

    @Test
    void invalidInitialCapacityThrowsIAE() {
        assertThatThrownBy(() -> new TablaHash<String, Integer>(0))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new TablaHash<String, Integer>(-1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void keysAndValuesIterationYieldsAllEntries() {
        map.put("x", 10);
        map.put("y", 20);
        map.put("z", 30);

        List<String> keys = new ArrayList<>();
        map.keys().forEach(keys::add);
        assertThat(keys).containsExactlyInAnyOrder("x", "y", "z");

        List<Integer> values = new ArrayList<>();
        map.values().forEach(values::add);
        assertThat(values).containsExactlyInAnyOrder(10, 20, 30);
    }

    @Test
    void containsKeyReturnsTrueForPresentKey() {
        map.put("present", 1);
        assertThat(map.containsKey("present")).isTrue();
        assertThat(map.containsKey("absent")).isFalse();
    }
}
