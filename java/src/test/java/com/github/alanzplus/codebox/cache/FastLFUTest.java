package com.github.alanzplus.codebox.cache;

import java.util.Map;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FastLFUTest {
    @Test
    void freqNode() throws Exception {
        final FastLFU.FreqNode<String, Integer> head = new FastLFU.FreqNode<>(0);

        assertThat(head.freq).isEqualTo(0);
        assertThat(head.set).isEmpty();

        final FastLFU.FreqNode<String, Integer> next = new FastLFU.FreqNode<>(1);

        assertThat(next.freq).isEqualTo(1);
        assertThat(next.set).isEmpty();

        final FastLFU.FreqNode<String, Integer> newHead = FastLFU.FreqNode.insertAfter(head, next);

        assertThat(head).isSameAs(newHead);
        assertThat(head.prev).isNull();
        assertThat(head.next).isSameAs(next);
        assertThat(next.prev).isSameAs(head);
        assertThat(next.next).isNull();

        final FastLFU.CacheNode<String, Integer> cacheNode = new FastLFU.CacheNode<>(new FastLFU.FastLFUEntry<>("hello", 1));
        cacheNode.register(next);
        next.removeCacheNode(cacheNode);

        assertThat(head.next).isNull();
    }

    @Test
    void promoteCacheNode() throws Exception {
        final FastLFU.FreqNode<String, Integer> head = new FastLFU.FreqNode<>(0);
        final FastLFU.FreqNode<String, Integer> next = new FastLFU.FreqNode<>(1);
        FastLFU.FreqNode.insertAfter(head, next);

        final String key = "hello";
        final int value = 1;
        final FastLFU.CacheNode<String, Integer> node1 = new FastLFU.CacheNode<>(new FastLFU.FastLFUEntry<>(key, value)).register(next);
        final FastLFU.CacheNode<String, Integer> node2 = new FastLFU.CacheNode<>(new FastLFU.FastLFUEntry<>("world", 2)).register(next);

        assertThat(next.set).hasSize(2);
        assertThat(head.next).isSameAs(next);
        assertThat(next.prev).isSameAs(head);
        assertThat(next.next).isNull();

        FastLFU.promoteCacheNode(node1);

        assertThat(next.set).hasSize(1);
        assertThat(next.next).isNotNull();
        assertThat(next.next.set).hasSize(1);
        assertThat(next.next.set.iterator().next()).matches(n -> n.entry.key().equals(key) && n.entry.value().equals(value));
    }

    @Test
    void fastLRU() throws Exception {
        final FastLFU<String, Integer> cache = new FastLFU<>(3);

        assertThat(cache.capacity()).isEqualTo(3);
        assertThat(cache.size()).isEqualTo(0);

        final Map<String, FastLFU.CacheNode<String, Integer>> internal = cache.cache();
        assertThat(cache.get("A")).isNull();

        cache.put("A", 1);
        assertThat(internal.get("A").entry.key()).isEqualTo("A");
        assertThat(internal.get("A").entry.value()).isEqualTo(1);
        assertThat(internal.get("A").freqNode.freq).isEqualTo(0);

        final FastLFU.FreqNode<String, Integer> head = internal.get("A").freqNode.prev;

        cache.put("B", 2);
        assertThat(internal.get("B").entry.key()).isEqualTo("B");
        assertThat(internal.get("B").entry.value()).isEqualTo(2);
        assertThat(internal.get("B").freqNode.freq).isEqualTo(0);

        cache.put("C", 3);
        assertThat(internal.get("C").entry.key()).isEqualTo("C");
        assertThat(internal.get("C").entry.value()).isEqualTo(3);
        assertThat(internal.get("C").freqNode.freq).isEqualTo(0);

        assertThat(internal.get("A").freqNode).isSameAs(internal.get("B").freqNode);
        assertThat(internal.get("A").freqNode).isSameAs(internal.get("C").freqNode);
        assertThat(internal.get("A").freqNode.next).isNull();

        assertThat(cache.get("A")).isEqualTo(1);
        assertThat(internal.get("A").freqNode.freq).isEqualTo(1);
        assertThat(internal.get("A").freqNode.next).isNull();
        assertThat(internal.get("B").freqNode.next).isSameAs(internal.get("A").freqNode);

        assertThat(cache.get("B")).isEqualTo(2);
        assertThat(internal.get("B").freqNode.freq).isEqualTo(1);
        assertThat(internal.get("A").freqNode).isSameAs(internal.get("B").freqNode);
        assertThat(internal.get("C").freqNode.next).isSameAs(internal.get("B").freqNode);

        assertThat(cache.get("C")).isEqualTo(3);
        assertThat(internal.get("C").freqNode.freq).isEqualTo(1);
        assertThat(internal.get("A").freqNode).isEqualTo(internal.get("C").freqNode);
        assertThat(internal.get("A").freqNode.set).hasSize(3);

        assertThat(internal.get("A").freqNode.prev).isSameAs(head);

        assertThat(cache.get("A")).isEqualTo(1);
        assertThat(cache.get("A")).isEqualTo(1);
        assertThat(cache.get("B")).isEqualTo(2);

        final Cache.Entry<String, Integer> victim = cache.victim();
        assertThat(victim.key()).isEqualTo("C");
        assertThat(victim.value()).isEqualTo(3);

        /* Test evict */
        assertThat(cache.size()).isEqualTo(3);

        cache.put("D", 4);

        assertThat(cache.size()).isEqualTo(3);
        assertThat(head.next).isSameAs(internal.get("D").freqNode);
        assertThat(internal.get("D").freqNode.freq).isEqualTo(0);
        assertThat(internal.get("B").freqNode).isSameAs(internal.get("D").freqNode.next);
    }
}
