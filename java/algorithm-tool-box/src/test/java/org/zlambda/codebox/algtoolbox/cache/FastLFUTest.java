package org.zlambda.codebox.algtoolbox.cache;

import org.junit.Assert;
import org.junit.Test;

import java.util.Map;

public class FastLFUTest {
    @Test
    public void freqNodeTest() throws Exception {
        FastLFU.FreqNode<String, Integer> head = new FastLFU.FreqNode<>(0);
        Assert.assertEquals(0, head.freq);
        Assert.assertTrue(head.set.isEmpty());
        FastLFU.FreqNode<String, Integer> next = new FastLFU.FreqNode<>(1);
        Assert.assertEquals(1, next.freq);
        Assert.assertTrue(next.set.isEmpty());
        FastLFU.FreqNode<String, Integer> newHead = FastLFU.FreqNode.insertAfter(head, next);
        Assert.assertEquals(head, newHead);
        Assert.assertNull(head.prev);
        Assert.assertEquals(head.next, next);
        Assert.assertEquals(next.prev, head);
        Assert.assertNull(next.next);

        FastLFU.CacheNode<String, Integer> cacheNode = new FastLFU.CacheNode<>(new FastLFU.FastLFUEntry<>("hello", 1));
        cacheNode.register(next);
        next.removeCacheNode(cacheNode);
        Assert.assertNull(head.next);
    }

    @Test
    public void testPromoteCacheNode() throws Exception {
        FastLFU.FreqNode<String, Integer> head = new FastLFU.FreqNode<>(0);
        FastLFU.FreqNode<String, Integer> next = new FastLFU.FreqNode<>(1);
        FastLFU.FreqNode.insertAfter(head, next);
        FastLFU.CacheNode<String, Integer> cacheNode1 = new FastLFU.CacheNode<>(new FastLFU.FastLFUEntry<>("hello", 1)).register(next);
        FastLFU.CacheNode<String, Integer> cacheNode2 = new FastLFU.CacheNode<>(new FastLFU.FastLFUEntry<>("world", 2)).register(next);
        Assert.assertEquals(2, next.set.size());
        Assert.assertEquals(head.next, next);
        Assert.assertEquals(next.prev, head);
        Assert.assertNull(next.next);
        FastLFU.promoteCacheNode(cacheNode1);
        Assert.assertEquals(1, next.set.size());
        Assert.assertNotNull(next.next);
        Assert.assertEquals(1, next.next.set.size());
        Assert.assertEquals("hello", next.next.set.iterator().next().entry.key());
        Assert.assertEquals(1, (int) next.next.set.iterator().next().entry.value());
    }

    @Test
    public void testFastLRU() throws Exception {
        Cache<String, Integer> cache = new FastLFU<>(3);
        Assert.assertEquals(3, cache.capacity());
        Assert.assertEquals(0, cache.size());
        Map<String, FastLFU.CacheNode<String, Integer>> internal = ((FastLFU<String, Integer>) cache).cache();
        Assert.assertNull(cache.get("A"));

        cache.put("A", 1);
        Assert.assertEquals("A", internal.get("A").entry.key());
        Assert.assertEquals(1, (int) internal.get("A").entry.value());
        Assert.assertEquals(0, internal.get("A").freqNode.freq);

        FastLFU.FreqNode<String, Integer> head = internal.get("A").freqNode.prev;

        cache.put("B", 2);
        Assert.assertEquals("B", internal.get("B").entry.key());
        Assert.assertEquals(2, (int) internal.get("B").entry.value());
        Assert.assertEquals(0, internal.get("B").freqNode.freq);

        cache.put("C", 3);
        Assert.assertEquals("C", internal.get("C").entry.key());
        Assert.assertEquals(3, (int) internal.get("C").entry.value());
        Assert.assertEquals(0, internal.get("C").freqNode.freq);

        Assert.assertEquals(internal.get("A").freqNode, internal.get("B").freqNode);
        Assert.assertEquals(internal.get("C").freqNode, internal.get("B").freqNode);
        Assert.assertNull(internal.get("A").freqNode.next);

        Assert.assertEquals(1, (int) cache.get("A"));
        Assert.assertEquals(1, internal.get("A").freqNode.freq);
        Assert.assertNull(internal.get("A").freqNode.next);
        Assert.assertEquals(internal.get("B").freqNode.next, internal.get("A").freqNode);

        Assert.assertEquals(2, (int) cache.get("B"));
        Assert.assertEquals(1, internal.get("B").freqNode.freq);
        Assert.assertEquals(internal.get("A").freqNode, internal.get("B").freqNode);
        Assert.assertEquals(internal.get("C").freqNode.next, internal.get("B").freqNode);

        Assert.assertEquals(3, (int) cache.get("C"));
        Assert.assertEquals(1, internal.get("C").freqNode.freq);
        Assert.assertEquals(internal.get("A").freqNode, internal.get("C").freqNode);
        Assert.assertEquals(3, internal.get("A").freqNode.set.size());

        Assert.assertEquals(head, internal.get("A").freqNode.prev);

        Assert.assertEquals(1, (int) cache.get("A"));
        Assert.assertEquals(1, (int) cache.get("A"));
        Assert.assertEquals(2, (int) cache.get("B"));

        Cache.Entry<String, Integer> victim = cache.victim();
        Assert.assertEquals("C", victim.key());
        Assert.assertEquals(3, (int) victim.value());

        /**
         * Test evict
         */
        Assert.assertEquals(3, cache.size());
        cache.put("D", 4);
        Assert.assertEquals(3, cache.size());
        Assert.assertEquals(head.next, internal.get("D").freqNode);
        Assert.assertEquals(0, internal.get("D").freqNode.freq);
        Assert.assertEquals(internal.get("B").freqNode, internal.get("D").freqNode.next);
    }
}
