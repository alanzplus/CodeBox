package com.github.alanzplus.codebox.misc;

import com.google.common.annotations.VisibleForTesting;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

public interface ImmutableSparseArray<V> extends SparseArray<V> {
    @Override
    default void set(final int idx, final V value) {
        throw new IllegalArgumentException(
                getClass().getSimpleName()
                        + " is subclass of "
                        + ImmutableSparseArray.class.getSimpleName()
                        + ", so \"set\" operation is not supported");
    }

    class PageBasedBuilder<V> {
        private static final int EMPTY_PAGE = -1;
        private static final List<Integer> PAGE_SIZES = Arrays.asList(8, 16, 32, 64, 128, 256);
        private final SortedMap<Integer, V> kvs = new TreeMap<>();
        private final List<Page> pages;

        public PageBasedBuilder(final int length) {
            pages = PAGE_SIZES.stream().map((size) -> new Page(length, size)).collect(Collectors.toList());
        }

        public PageBasedBuilder<V> set(final int idx, final V value) {
            kvs.put(idx, value);
            pages.forEach((page) -> page.addHit(idx));
            return this;
        }

        public ImmutableSparseArray<V> build() {
            pages.sort(Comparator.comparingLong(Page::calMem));
            return pages.get(0).toSparseArray(kvs);
        }

        @VisibleForTesting
        static class Page {
            private final int keyLength;
            private final int pageSize;
            private final SortedSet<Integer> nonEmptyPages = new TreeSet<>();
            private int[] pagePtr;
            private Object[] values;

            Page(final int keyLength, final int pageSize) {
                this.keyLength = keyLength;
                this.pageSize = pageSize;
            }

            void addHit(final int idx) {
                nonEmptyPages.add(idx / pageSize);
            }

            long calMem() {
                return SizeEstimator.intArrShallow(numPage()) + SizeEstimator.refArrShallow(pageSize * nonEmptyPages.size());
            }

            <V> ImmutableSparseArray<V> toSparseArray(final SortedMap<Integer, V> kvs) {
                pagePtr = new int[numPage()];
                Arrays.fill(pagePtr, EMPTY_PAGE);
                values = new Object[nonEmptyPages.size() * pageSize];
                final Integer[] pageNums = nonEmptyPages.toArray(new Integer[0]);
                pagePtr[0] = 0;
                for (int i = 1; i < pageNums.length; ++i) {
                    pagePtr[pageNums[i]] = i * pageSize;
                }
                kvs.forEach((i, v) -> values[pagePtr[i / pageSize] + i % pageSize] = v);
                return new PageBasedImpl<>(this);
            }

            int numPage() {
                return (keyLength + pageSize - 1) / pageSize;
            }
        }

        /**
         * 1. Allocate a values array of same size with actual existing values Object[] values = new Object[SIZE_OF_ACTUAL_EXISTING_VALUES];
         *
         * <p>2. Allocate two arrays related to index lookup 2.1 pagePtr array. Its size = index_space_size / page_size. 2.2 pages array.
         * Its size = page_size * pagePtr.length
         *
         * <p>3. so the lookup will become value = values[ page_size * pagePtr[index / page_size] + index % page_size ]
         */
        @VisibleForTesting
        static class PageBasedImpl<V> implements ImmutableSparseArray<V> {
            private final int length;
            private final int[] pagePtr;
            private final Object[] values;
            private final int pageSize;

            PageBasedImpl(final Page page) {
                length = page.keyLength;
                pagePtr = page.pagePtr;
                values = page.values;
                pageSize = page.pageSize;
            }

            @SuppressWarnings("unchecked")
            @Override
            public V get(final int idx) {
                if (idx >= length) {
                    throw new ArrayIndexOutOfBoundsException("idx " + idx + " >= " + length);
                }
                final int pageNum = idx / pageSize;
                if (EMPTY_PAGE == pagePtr[pageNum]) {
                    return null;
                }
                return (V) values[pagePtr[pageNum] + idx % pageSize];
            }

            @Override
            public long mem() {
                return SizeEstimator.intArrShallow(pagePtr.length) + SizeEstimator.refArrShallow(values.length);
            }

            public int pageSize() {
                return pageSize;
            }
        }
    }

    /**
     * Binary Search Based Implementation 1. allocate an array storing keys having associate values 2. allocate an array storing values
     * having associate keys
     */
    class BinarySearchBasedBuilder<V> {
        private final SortedMap<Integer, V> kvs = new TreeMap<>();

        public BinarySearchBasedBuilder() {}

        public void set(final int idx, final V value) {
            kvs.put(idx, value);
        }

        public ImmutableSparseArray<V> build() {
            final Object[] values = new Object[kvs.size()];
            final Integer[] keySetArr = kvs.keySet().toArray(new Integer[0]);
            final int[] keys = new int[keySetArr.length];
            for (int i = 0; i < keySetArr.length; ++i) {
                keys[i] = keySetArr[i];
                values[i] = kvs.get(keys[i]);
            }
            return new BinarySearchBasedImpl<>(keys, values);
        }

        @VisibleForTesting
        static class BinarySearchBasedImpl<V> implements ImmutableSparseArray<V> {
            private final int[] keys;
            private final Object[] values;

            BinarySearchBasedImpl(final int[] keys, final Object[] values) {
                this.keys = keys;
                this.values = values;
            }

            @SuppressWarnings("unchecked")
            @Override
            public V get(final int idx) {
                final int i = Arrays.binarySearch(keys, idx);
                return i < 0 ? null : (V) values[i];
            }

            @Override
            public long mem() {
                return SizeEstimator.intArrShallow(keys.length) + SizeEstimator.refArrShallow(values.length);
            }
        }
    }
}
