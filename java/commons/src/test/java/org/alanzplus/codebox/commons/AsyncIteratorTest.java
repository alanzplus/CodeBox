package org.alanzplus.codebox.commons;

import com.google.common.collect.Lists;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import org.junit.Assert;
import org.junit.Test;

public class AsyncIteratorTest {
  private class DelaySupplier implements Supplier<String> {
    private final long time;
    private final String value;
    private final CountDownLatch countDownLatch;
    private final CountDownLatch waitCountDownLatch;

    private DelaySupplier(long time, String value) {
      this.time = time;
      this.value = value;
      this.countDownLatch = null;
      this.waitCountDownLatch = null;
    }

    private DelaySupplier(long time, String value, CountDownLatch countDownLatch,
        CountDownLatch waitCountDownLatch) {
      this.time = time;
      this.value = value;
      this.countDownLatch = countDownLatch;
      this.waitCountDownLatch = waitCountDownLatch;
    }

    @Override
    public String get() {
      if (null != countDownLatch) {
        countDownLatch.countDown();
      }
      try {
        if (null != waitCountDownLatch) {
          waitCountDownLatch.await();
        }
        TimeUnit.MILLISECONDS.sleep(time);
      } catch (InterruptedException e) {
        throw new IllegalStateException(e);
      }
      return value;
    }
  }

  @Test
  public void test() throws Exception {
    try (AsyncIterator<String> asyncIterator = AsyncIterator.<String>custom().ofSuppliers(
        new DelaySupplier(1000, "1"),
        new DelaySupplier(100, "2")
    ).withConcurrent(10).create()) {
      Assert.assertEquals(
          Lists.newArrayList(asyncIterator),
          Arrays.asList("2", "1")
      );
    }
  }

  @Test
  public void testMultipleAsyncIterator() throws Exception {
    AsyncIterator asyncIterator1 =
        AsyncIterator.<String>custom().ofSuppliers(new DelaySupplier(1000, "1")).create();
    AsyncIterator asyncIterator2 =
        AsyncIterator.<String>custom().ofSuppliers(new DelaySupplier(1000, "2")).create();
    Assert.assertEquals("1", asyncIterator1.next());
    Assert.assertEquals("2", asyncIterator2.next());
    asyncIterator1.close();
    asyncIterator2.close();
  }

  /**
   * Test when shutdown is requested, there is one actively running task and no waiting task
   */
  @Test(timeout = 10000)
  public void testExternalExecutorShutdown1() throws Exception {
    ExecutorService externalES = Executors.newFixedThreadPool(1);
    CountDownLatch countDownLatch = new CountDownLatch(1);
    try (AsyncIterator<String> asyncIterator = AsyncIterator.<String>custom().ofSuppliers(
        Arrays.asList(
            new DelaySupplier(100, "2"),
            new DelaySupplier(3000, "1", countDownLatch, null)
        ),
        externalES
    ).create()) {
      Assert.assertEquals("2", asyncIterator.next());
      CompletableFuture<Boolean> check =
          CompletableFuture.supplyAsync(() -> asyncIterator.hasNext());
      /**
       * wait until the second task is executed
       */
      countDownLatch.await();
      externalES.shutdownNow();
      Assert.assertFalse(check.get());
      Assert.assertEquals(0, asyncIterator.getNumUncompletedFutures());
    }
  }

  /**
   * Test when shutdown is requested, there is no actively running task but one waiting task
   */
  @Test(timeout = 50000)
  public void testExternalExecutorShutdown2() throws Exception {
    ExecutorService externalES = Executors.newFixedThreadPool(1);
    CountDownLatch countDownLatch = new CountDownLatch(1);
    CountDownLatch waitCountDownLatch = new CountDownLatch(1);
    try (AsyncIterator<String> asyncIterator = AsyncIterator.<String>custom().ofSuppliers(
        Arrays.asList(
            new DelaySupplier(5000, "1", countDownLatch, waitCountDownLatch),
            new DelaySupplier(1000, "2")
        ),
        externalES
    ).create()) {
      /**
       * wait until the first task is executed
       */
      countDownLatch.await();
      externalES.shutdownNow();
      waitCountDownLatch.countDown();
      /**
       * after shutdownNow
       * 1. first task will be interrupted
       * 2. second task will not be executed
       */
      Assert.assertFalse(asyncIterator.hasNext());
      Assert.assertEquals(1, asyncIterator.getNumUncompletedFutures());
    }
  }

  @Test
  public void testCallerThreadInterrupted() throws Exception {
    try (AsyncIterator<String> asyncIterator = AsyncIterator.<String>custom().ofSuppliers(
        new DelaySupplier(10000, "1"),
        new DelaySupplier(100, "2")
    ).withConcurrent(100).create()) {
      Assert.assertEquals("2", asyncIterator.next());
      Thread.currentThread().interrupt();
      Assert.assertFalse(asyncIterator.hasNext());
    }
  }
}
