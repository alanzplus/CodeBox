package org.alanzplus.codebox.algtoolbox.cache;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import org.alanzplus.codebox.commons.Preconditions;
import org.alanzplus.codebox.commons.annotations.NotNull;
import org.alanzplus.codebox.commons.annotations.Nullable;
import org.alanzplus.codebox.commons.annotations.VisibleForTesting;

/**
 * An O(1) algorithm for implementing the LFU cache eviction scheme
 *
 * @see <a href="http://dhruvbird.com/lfu.pdf">reference</a>
 */
public class FastLFU<K, V> implements Cache<K, V> {
  private final int capacity;
  private final Map<K, CacheNode<K, V>> cache = new HashMap<>();
  private final FreqNode<K, V> head = new FreqNode<>(-1);

  public FastLFU(int capacity) {
    this.capacity = capacity;
  }

  @VisibleForTesting
  static <K, V> void promoteCacheNode(CacheNode<K, V> cacheNode) {
    FreqNode<K, V> freqNode = cacheNode.freqNode;
    FreqNode<K, V> prev = freqNode.prev;
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
  public void put(K key, V value) {
    if (cache.size() == capacity) {
      evict();
    }
    FreqNode<K, V> nextFreqNode = head.next;
    if (null == nextFreqNode || 0L != nextFreqNode.freq) {
      nextFreqNode = new FreqNode<>(0L);
      FreqNode.insertAfter(head, nextFreqNode);
    }
    cache.put(
        key,
        new CacheNode<>(new FastLFUEntry<>(key, value)).register(nextFreqNode)
    );
  }

  @Override
  public V get(K key) {
    CacheNode<K, V> cacheNode = cache.get(key);
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

    FreqNode(long freq) {
      this.freq = freq;
    }

    /**
     * Double-Linked List Utilities
     */
    static <K, V> FreqNode<K, V>
    insertAfter(@NotNull FreqNode<K, V> curr, @NotNull FreqNode<K, V> newNode) {
      Preconditions.checkArgument(null != curr && null != newNode, "arguments cannot be null");
      newNode.next = curr.next;
      curr.next = newNode;
      newNode.prev = curr;
      if (null != newNode.next) {
        newNode.next.prev = newNode;
      }
      return curr;
    }

    static <K, V> FreqNode<K, V> remove(@Nullable FreqNode<K, V> target) {
      if (null == target) {
        return null;
      }
      if (null != target.prev) {
        FreqNode<K, V> prev = target.prev;
        prev.next = target.next;
        if (null != target.next) {
          target.next.prev = prev;
        }
      }
      target.prev = null;
      target.next = null;
      return target;
    }

    FreqNode<K, V> addCacheNode(CacheNode<K, V> cacheNode) {
      set.add(cacheNode);
      return this;
    }

    FreqNode<K, V> removeCacheNode(CacheNode<K, V> cacheNode) {
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

    CacheNode(Entry<K, V> entry) {
      this.entry = entry;
    }

    CacheNode<K, V> register(@NotNull FreqNode<K, V> freqNode) {
      Preconditions.checkArgument(null != freqNode, "freqNode cannot be null.");
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

    FastLFUEntry(K key, V value) {
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
