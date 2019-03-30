package com.github.alanzplus.codebox.cache;

import com.google.common.annotations.VisibleForTesting;
import java.util.HashMap;

public class SimpleLRU<K, V> implements Cache<K, V> {
    private final int cap;
    private final HashMap<K, CacheEntry<K, V>> cache = new HashMap<>();
    private final CacheEntry<K, V> head = new CacheEntry<>(null, null);
    private final CacheEntry<K, V> tail = new CacheEntry<>(null, null);

    {
        head.next = tail;
        tail.prev = head;
    }

    public SimpleLRU(final int cap) {
        this.cap = cap;
    }

    @Override
    public void put(final K key, final V value) {
        CacheEntry<K, V> entry = cache.get(key);
        if (null == entry) {
            if (size() == capacity()) {
                evict();
            }
        } else {
            unlink(entry);
        }
        entry = new CacheEntry<>(key, value);
        cache.put(key, entry);
        setToHead(entry);
    }

    @VisibleForTesting
    CacheEntry<K, V> head() {
        return head;
    }

    @VisibleForTesting
    CacheEntry<K, V> tail() {
        return tail;
    }

    private void evict() {
        final Entry<K, V> victim = victim();
        if (null == victim) {
            return;
        }
        final CacheEntry<K, V> v = ((CacheEntry<K, V>) victim);
        link(v.prev, v.next);
        v.reset();
        cache.remove(v.key());
    }

    private void setToHead(final CacheEntry<K, V> entry) {
        link(entry, head.next);
        link(head, entry);
    }

    @Override
    public V get(final K key) {
        final CacheEntry<K, V> entry = cache.get(key);
        if (null == entry) {
            return null;
        }
        moveToHead(entry);
        return entry.value();
    }

    private void moveToHead(final CacheEntry<K, V> entry) {
        link(entry.prev, entry.next);
        link(entry, head.next);
        link(head, entry);
    }

    private void link(final CacheEntry<K, V> entry1, final CacheEntry<K, V> entry2) {
        entry1.next = entry2;
        entry2.prev = entry1;
    }

    private void unlink(final CacheEntry<K, V> entry) {
        final CacheEntry<K, V> prev = entry.prev;
        final CacheEntry<K, V> next = entry.next;
        entry.reset();
        link(prev, next);
    }

    @Override
    public Entry<K, V> victim() {
        final CacheEntry<K, V> prev = tail.prev;
        return head == prev ? null : prev;
    }

    @Override
    public int size() {
        return cache.size();
    }

    @Override
    public int capacity() {
        return cap;
    }

    static class CacheEntry<K, V> implements Entry<K, V> {
        CacheEntry<K, V> prev;
        CacheEntry<K, V> next;

        K key;
        V value;

        CacheEntry(final K key, final V value) {
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

        private void reset() {
            prev = null;
            next = null;
        }

        @Override
        public String toString() {
            return String.format("%s:%s", key, value);
        }
    }
}
