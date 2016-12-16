package org.zlambda.codebox.algtoolbox.misc;

import junit.framework.Assert;
import org.junit.Test;
import org.zlambda.codebox.commons.SizeEstimator;

import java.util.Arrays;
import java.util.stream.IntStream;

public class ImmutableSparseArrayTest {
    @Test
    public void testPageBased() throws Exception {
        {
            ImmutableSparseArray.PageBasedBuilder<Integer> builder = new ImmutableSparseArray.PageBasedBuilder<>(15);
            Arrays.asList(0, 1, 2, 3, 4, 5, 6).forEach(i -> builder.set(i, i));

            ImmutableSparseArray<Integer> array = builder.build();
            Assert.assertEquals(8, ((ImmutableSparseArray.PageBasedBuilder.PageBasedImpl)array).pageSize());
            Assert.assertEquals(SizeEstimator.intArrShallow(2) + SizeEstimator.refArrShallow(8), array.mem());

            Arrays.asList(0, 1, 2, 3, 4, 5, 6).forEach(i -> Assert.assertEquals(i, array.get(i)));
            IntStream.range(7, 15).forEach(i -> Assert.assertNull(array.get(i)));
        }

        {
            ImmutableSparseArray.PageBasedBuilder<Integer> builder = new ImmutableSparseArray.PageBasedBuilder<>(15);
            Arrays.asList(0, 8).forEach(i -> builder.set(i, i));
            ImmutableSparseArray<Integer> array = builder.build();
            Assert.assertEquals(16, ((ImmutableSparseArray.PageBasedBuilder.PageBasedImpl)array).pageSize());
            Assert.assertEquals(SizeEstimator.intArrShallow(1) + SizeEstimator.refArrShallow(16), array.mem());
            Arrays.asList(0, 8).forEach(i -> Assert.assertEquals(i, array.get(i)));
            IntStream.range(9, 15).forEach(i -> Assert.assertNull(array.get(i)));
        }
    }

    @Test
    public void testBinarySearchBased() throws Exception {
        {
            ImmutableSparseArray.BinarySearchBasedBuilder<Integer> builder = new ImmutableSparseArray.BinarySearchBasedBuilder<>();
            Arrays.asList(0, 1, 2, 3, 4, 5, 6).forEach(i -> builder.set(i, i));
            ImmutableSparseArray<Integer> array = builder.build();
            Assert.assertEquals(SizeEstimator.intArrShallow(7) + SizeEstimator.refArrShallow(7), array.mem());
            Arrays.asList(0, 1, 2, 3, 4, 5, 6).forEach(i -> Assert.assertEquals(i, array.get(i)));
            IntStream.range(7, 15).forEach(i -> Assert.assertNull(array.get(i)));
        }
    }
}