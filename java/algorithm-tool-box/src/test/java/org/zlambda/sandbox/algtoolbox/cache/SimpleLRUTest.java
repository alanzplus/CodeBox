package org.zlambda.sandbox.algtoolbox.cache;

import junit.framework.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class SimpleLRUTest {
    @Test
    public void testPutSetToHead() throws Exception {
        int cap = 3;
        SimpleLRU<Integer, Integer> lru = new SimpleLRU<>(cap);
        Assert.assertEquals(cap, lru.capacity());
        Assert.assertEquals(0, lru.size());
        Assert.assertNull(lru.get(1));
        lru.put(1, 1);
        Assert.assertEquals(1, (int) lru.head().next.key());

        lru.put(2, 2);
        Assert.assertEquals(2, (int) lru.head().next.key());

        lru.put(3, 3);
        Assert.assertEquals(3, (int) lru.head().next.key());
        Assert.assertEquals(cap, lru.size());

        lru.put(1, 10);
        Assert.assertEquals(1, (int) lru.head().next.key());
        Assert.assertEquals(cap, lru.size());

        assertEquals(lru.head(), lru.tail(), 1, 3, 2);
    }

    @Test
    public void testGetMoveToHead() throws Exception {
        int cap = 3;
        SimpleLRU<Integer, Integer> lru = new SimpleLRU<>(cap);
        lru.put(1, 1);
        lru.put(2, 2);
        lru.put(3, 3);
        assertEquals(lru.head(), lru.tail(), 3, 2, 1);

        Assert.assertEquals(1, (int) lru.get(1));
        assertEquals(lru.head(), lru.tail(), 1, 3, 2);

        Assert.assertEquals(2, (int) lru.get(2));
        assertEquals(lru.head(), lru.tail(), 2, 1, 3);

        Assert.assertEquals(1, (int) lru.get(1));
        assertEquals(lru.head(), lru.tail(), 1, 2, 3);
    }

    @Test
    public void testAll() {
        int cap = 10;
        SimpleLRU<Integer, Integer> lru = new SimpleLRU<>(cap);
        List<Integer> data = new ArrayList<>();
        for (int i = 0; i < cap * 2; ++i) {
            int key = i;
            if (i >= cap) {
                Assert.assertEquals(data.get(data.size() - cap), lru.tail().prev.value());
                Assert.assertEquals(data.get(data.size() - cap), lru.victim().key());
            }
            lru.put(key, key);
            Assert.assertEquals(key, (int) lru.head().next.value());
            Assert.assertEquals(key, (int) lru.get(key));
            data.add(key);
        }
        assertEquals(lru.head(), lru.tail(), 19, 18, 17, 16, 15, 14, 13, 12, 11, 10);
    }


    private void assertEquals(SimpleLRU.CacheEntry<Integer, Integer> head,
                              SimpleLRU.CacheEntry<Integer, Integer> tail,
                              int... eles) {
        head = head.next;
        for (int ele : eles) {
            Assert.assertEquals(ele, (int) head.key());
            head = head.next;
        }
        Assert.assertEquals(head, tail);
    }
}
