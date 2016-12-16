package org.zlambda.codebox.algtoolbox.misc;

import org.zlambda.codebox.commons.SizeEstimator;
import org.zlambda.codebox.commons.annotations.VisibleForTesting;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

public interface ImmutableSparseArray<V> extends SparseArray<V> {
    default void set(int idx, V value) {
        throw new IllegalArgumentException(
                getClass().getSimpleName() +
                " is subclass of " +
                ImmutableSparseArray.class.getSimpleName() +
                ", so \"set\" operation is not supported"
        );
    }

    class PageBasedBuilder<V> {
        private static final int EMPTY_PAGE = -1;
        private static final List<Integer> PAGE_SIZES = Arrays.asList(8, 16, 32, 64, 128, 256);
        private final SortedMap<Integer, V> kvs = new TreeMap<>();
        private final List<Page> pages;

        @VisibleForTesting
        static class Page {
            private final int keyLength;
            private final int pageSize;
            private int[] pagePtr;
            private Object[] values;
            private final SortedSet<Integer> nonEmptyPages = new TreeSet<>();

            Page(int keyLength, int pageSize) {
                this.keyLength = keyLength;
                this.pageSize = pageSize;
            }

            void addHit(int idx) {
                this.nonEmptyPages.add(idx / pageSize);
            }

            long calMem() {
                return SizeEstimator.intArrShallow(numPage()) + SizeEstimator.refArrShallow(pageSize * nonEmptyPages.size());
            }

            <V> ImmutableSparseArray<V> toSparseArray(SortedMap<Integer, V> kvs) {
                this.pagePtr = new int[numPage()];
                Arrays.fill(this.pagePtr, EMPTY_PAGE);
                this.values = new Object[nonEmptyPages.size() * pageSize];
                Integer[] pageNums = nonEmptyPages.toArray(new Integer[0]);
                this.pagePtr[0] = 0;
                for (int i = 1; i < pageNums.length; ++i) {
                    this.pagePtr[pageNums[i]] = i * pageSize;
                }
                kvs.forEach((i, v) -> this.values[pagePtr[i / pageSize] + i % pageSize] = v);
                return new PageBasedImpl<>(this);
            }

            int numPage() {
                return (keyLength + pageSize - 1) / pageSize;
            }
        }

        public PageBasedBuilder(int length) {
            this.pages = PAGE_SIZES.stream().map((size) -> new Page(length, size)).collect(Collectors.toList());
        }

        public PageBasedBuilder<V> set(int idx, V value) {
            kvs.put(idx, value);
            pages.forEach((page) -> page.addHit(idx));
            return this;
        }

        public ImmutableSparseArray<V> build() {
            pages.sort(Comparator.comparingLong(Page::calMem));
            return pages.get(0).toSparseArray(kvs);
        }

        /**
         * 1.
         * pagePtr [ page_0 begOffset | page_1 begOffset | ... | page_(N-1) begOffset ]
         *  N = (len(keySpace) + pageSize - 1) / pageSize
         *
         *  offset points to the page beginning index in the values array. if offset is null, means there this page is empty,
         *  there is no entries in values for this page
         *
         * 2.
         * values [ 0...(N-1) ]
         *  N = numberOfNonEmptyPage * pageSize
         *
         *
         * 3. structure
         * pagePtr [ page_0 begOffset | page_1 begOffset NULL | ... | page_(N-1) begOffset ]
         *          |                                                 |
         *          |                   -------------------------------
         *          |                  |
         *         \/                 \/
         * values [ ....              |       ]
         */

        @VisibleForTesting
        static class PageBasedImpl<V> implements ImmutableSparseArray<V> {
            private final int length;
            private final int[] pagePtr;
            private final Object[] values;
            private final int pageSize;

            PageBasedImpl(Page page) {
                this.length = page.keyLength;
                this.pagePtr = page.pagePtr;
                this.values = page.values;
                this.pageSize = page.pageSize;
            }

            @SuppressWarnings("unchecked")
            @Override
            public V get(int idx) {
                if (idx >= length) {
                    throw new ArrayIndexOutOfBoundsException("idx " + idx + " >= " + length);
                }
                int pageNum = idx / pageSize;
                if (EMPTY_PAGE == pagePtr[pageNum]) {
                    return null;
                }
                return (V)values[pagePtr[pageNum] + idx % pageSize];
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
     * Binary Search Based Implementation
     */
    class BinarySearchBasedBuilder<V> {
        private final SortedMap<Integer, V> kvs = new TreeMap<>();
        public BinarySearchBasedBuilder() {

        }

        public void set(int idx, V value) {
            kvs.put(idx, value);
        }

        public ImmutableSparseArray<V> build() {
            Object[] values = new Object[kvs.size()];
            Integer[] keySetArr = kvs.keySet().toArray(new Integer[0]);
            int[] keys = new int[keySetArr.length];
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

            BinarySearchBasedImpl(int[] keys, Object[] values) {
                this.keys = keys;
                this.values = values;
            }

            @SuppressWarnings("unchecked")
            @Override
            public V get(int idx) {
                int i = Arrays.binarySearch(keys, idx);
                return i < 0 ? null : (V)values[i];
            }

            @Override
            public long mem() {
                return SizeEstimator.intArrShallow(keys.length) + SizeEstimator.refArrShallow(values.length);
            }
        }
    }
}

