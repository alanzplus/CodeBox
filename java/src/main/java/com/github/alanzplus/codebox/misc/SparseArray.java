package com.github.alanzplus.codebox.misc;

/**
 * Suppose you are given an array with large index space (key space) but there are only few indices have associated value.
 *
 * <p>For example, Object[] values = new Object[KEY_SPACE_SIZE]; But for most i in [0, KEY_SPACE_SIZE - 1], values[i] = null
 *
 * <p>How to create a effective data structure?
 */
public interface SparseArray<V> {
    V get(int idx);

    void set(int idx, V value);

    default long mem() {
        throw new UnsupportedOperationException("mem is not supported by this class " + getClass().getSimpleName());
    }
}
