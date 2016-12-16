package org.zlambda.codebox.algtoolbox.misc;

public interface SparseArray<V> {
    V get(int idx);
    void set(int idx, V value);
    default long mem() {
        throw new UnsupportedOperationException("mem is not supported by this class " + getClass().getSimpleName());
    }
}
