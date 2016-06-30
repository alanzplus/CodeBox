package org.zlambda.sandbox.algtoolbox.cache;

public interface Cache<K, V> {
    void put(K key, V value);

    V get(K key);

    Entry<K, V> victim();

    int size();

    int capacity();

    interface Entry<K, V> {
        K key();

        V value();
    }
}
