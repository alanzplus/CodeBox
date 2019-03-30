package com.github.alanzplus.codebox.cache;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SimpleLRUTest {
    @Test
    void latestAddedElement_shouldInertIntoHead() throws Exception {
        final int cap = 3;
        final SimpleLRU<Integer, Integer> lru = new SimpleLRU<>(cap);

        assertThat(lru.capacity()).isEqualTo(cap);
        assertThat(lru.size()).isEqualTo(0);
        assertThat(lru.get(1)).isNull();

        lru.put(1, 1);
        assertThat(lru.head().next.key()).isEqualTo(1);

        lru.put(2, 2);
        assertThat(lru.head().next.key()).isEqualTo(2);

        lru.put(3, 3);
        assertThat(lru.head().next.key()).isEqualTo(3);

        assertThat(lru.size()).isEqualTo(3);

        lru.put(1, 10);
        assertThat(lru.head().next.key()).isEqualTo(1);
        assertThat(lru.size()).isEqualTo(3);
        assertThat(lru.size()).isEqualTo(3);
        assertThat(lru.tail());
        assertEquals(lru.head(), lru.tail(), 1, 3, 2);
    }

    @Test
    void get_shouldMoveElementToHead() throws Exception {
        final int cap = 3;
        final SimpleLRU<Integer, Integer> lru = new SimpleLRU<>(cap);
        lru.put(1, 1);
        lru.put(2, 2);
        lru.put(3, 3);
        assertEquals(lru.head(), lru.tail(), 3, 2, 1);

        assertThat(lru.get(1)).isEqualTo(1);
        assertEquals(lru.head(), lru.tail(), 1, 3, 2);

        assertThat(lru.get(2)).isEqualTo(2);
        assertEquals(lru.head(), lru.tail(), 2, 1, 3);

        assertThat(lru.get(1)).isEqualTo(1);
        assertEquals(lru.head(), lru.tail(), 1, 2, 3);
    }

    @Test
    void inAllTesting() {
        final int cap = 10;
        final SimpleLRU<Integer, Integer> lru = new SimpleLRU<>(cap);
        final List<Integer> data = new ArrayList<>();
        for (int i = 0; i < cap * 2; ++i) {
            final int key = i;
            if (i >= cap) {
                assertThat(data.get(data.size() - cap)).isEqualTo(lru.tail().prev.value());
                assertThat(data.get(data.size() - cap)).isEqualTo(lru.victim().key());
            }
            lru.put(key, key);
            assertThat(lru.head().next.value()).isEqualTo(key);
            assertThat(lru.get(key)).isEqualTo(key);
            data.add(key);
        }
        assertEquals(lru.head(), lru.tail(), 19, 18, 17, 16, 15, 14, 13, 12, 11, 10);
    }

    private static void assertEquals(
            SimpleLRU.CacheEntry<Integer, Integer> head, final SimpleLRU.CacheEntry<Integer, Integer> tail, final int... eles) {
        head = head.next;
        for (final int ele : eles) {
            assertThat(head.key()).isEqualTo(ele);
            head = head.next;
        }
        assertThat(head).isSameAs(tail);
    }
}
