package com.github.alanzplus.codebox.cache;

import com.google.common.annotations.VisibleForTesting;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import lombok.NonNull;

/**
 * An O(1) algorithm for implementing the LFU cache eviction scheme
 *
 * @see <a href="http://dhruvbird.com/lfu.pdf">reference</a>
 */
public class FastLFU<K, V> implements Cache<K, V> {
    private final int capacity;
    private final Map<K, CacheNode<K, V>> cache = new HashMap<>();
    private final FreqNode<K, V> head = new FreqNode<>(-1);

    public FastLFU(final int capacity) {
        this.capacity = capacity;
    }

    @VisibleForTesting
    static <K, V> void promoteCacheNode(final CacheNode<K, V> cacheNode) {
        final FreqNode<K, V> freqNode = cacheNode.freqNode;
        final FreqNode<K, V> prev = freqNode.prev;
        FreqNode<K, V> next = freqNode.next;
        cacheNode.unregister();
        if (Long.MAX_VALUE == freqNode.freq) {
            throw new IllegalStateException("frequency counter overflow");
        }
        if ((null == next) || (next.freq != (freqNode.freq + 1L))) {
            next = new FreqNode<>(freqNode.freq + 1L);
            FreqNode.insertAfter(freqNode.set.isEmpty() ? prev : freqNode, next);
        }
        cacheNode.register(next);
    }

    @Override
    public void put(final K key, final V value) {
        if (cache.size() == capacity) {
            evict();
        }
        FreqNode<K, V> nextFreqNode = head.next;
        if (null == nextFreqNode || 0L != nextFreqNode.freq) {
            nextFreqNode = new FreqNode<>(0L);
            FreqNode.insertAfter(head, nextFreqNode);
        }
        cache.put(key, new CacheNode<>(new FastLFUEntry<>(key, value)).register(nextFreqNode));
    }

    @Override
    public V get(final K key) {
        final CacheNode<K, V> cacheNode = cache.get(key);
        if (null == cacheNode) {
            return null;
        }
        promoteCacheNode(cacheNode);
        return cacheNode.entry.value();
    }

    @Override
    public Entry<K, V> victim() {
        return null == head.next ? null : head.next.set.iterator().next().entry;
    }

    @Override
    public int size() {
        return cache.size();
    }

    @Override
    public int capacity() {
        return capacity;
    }

    private Entry<K, V> evict() {
        return cache.remove(victim().key()).unregister().entry;
    }

    @VisibleForTesting
    Map<K, CacheNode<K, V>> cache() {
        return cache;
    }

    @VisibleForTesting
    static class FreqNode<K, V> {
        final Set<CacheNode<K, V>> set = new LinkedHashSet<>();
        long freq;
        FreqNode<K, V> prev;
        FreqNode<K, V> next;

        FreqNode(final long freq) {
            this.freq = freq;
        }

        /** Double-Linked List Utilities */
        static <K, V> FreqNode<K, V> insertAfter(@NonNull final FreqNode<K, V> curr, @NonNull final FreqNode<K, V> newNode) {
            newNode.next = curr.next;
            curr.next = newNode;
            newNode.prev = curr;
            if (null != newNode.next) {
                newNode.next.prev = newNode;
            }
            return curr;
        }

        static <K, V> FreqNode<K, V> remove(@NonNull final FreqNode<K, V> target) {
            if (null == target) {
                return null;
            }
            if (null != target.prev) {
                final FreqNode<K, V> prev = target.prev;
                prev.next = target.next;
                if (null != target.next) {
                    target.next.prev = prev;
                }
            }
            target.prev = null;
            target.next = null;
            return target;
        }

        FreqNode<K, V> addCacheNode(final CacheNode<K, V> cacheNode) {
            set.add(cacheNode);
            return this;
        }

        FreqNode<K, V> removeCacheNode(final CacheNode<K, V> cacheNode) {
            set.remove(cacheNode);
            if (set.isEmpty()) {
                remove(this);
            }
            return this;
        }
    }

    @VisibleForTesting
    static class CacheNode<K, V> {
        final Entry<K, V> entry;
        FreqNode<K, V> freqNode;

        CacheNode(final Entry<K, V> entry) {
            this.entry = entry;
        }

        CacheNode<K, V> register(@NonNull final FreqNode<K, V> freqNode) {
            this.freqNode = freqNode.addCacheNode(this);
            return this;
        }

        CacheNode<K, V> unregister() {
            if (null != freqNode) {
                freqNode.removeCacheNode(this);
            }
            return this;
        }
    }

    static class FastLFUEntry<K, V> implements Entry<K, V> {
        private final K key;
        private final V value;

        FastLFUEntry(final K key, final V value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public K key() {
            return key;
        }

        @Override
        public V value() {
            return value;
        }
    }
}
