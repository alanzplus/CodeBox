package org.alanzplus.codebox.commons;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.alanzplus.codebox.commons.annotations.VisibleForTesting;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Design notes
 *
 * 1 Two executor services
 *  Producer Executor Service
 *  Consumer Executor Service
 *
 * 2 Supplier<E> should not produce null value
 *
 * 3 hasNext return false when one of the following condition met
 *  (1) consumerES is terminated and queue is empty
 *  (2) numUncompletedFutures is 0 and queue is empty
 */
public class AsyncIterator<E> implements Iterator<E>, Closeable {
  private static Logger LOGGER = LogManager.getLogger(AsyncIterator.class);
  public interface UncaughtExceptionHandler {
    void handle(Throwable throwable);
    UncaughtExceptionHandler DEFAULT = (throwable -> LOGGER.error("Got uncaught exception.", throwable));
  }
  private final long pollTime;
  private final BlockingQueue<E> queue = new LinkedBlockingQueue<>();
  private final ExecutorService producerEsProxy;
  private final ExecutorService consumerES = Executors.newSingleThreadExecutor(r ->
      new Thread(r,String.format("%s-consumer-single-thread", AsyncIterator.class.getSimpleName())));
  private E nextValue = null;
  private final AtomicInteger numUncompletedFutures;
  private List<CompletableFuture<Integer>> completableFutureList;
  private List<CompletableFuture<E>> originalCompletableFutureList;
  private boolean isClosed = false;
  private final UncaughtExceptionHandler uncaughtExceptionHandler;

  private AsyncIterator(Builder builder) {
    this.pollTime = builder.pollTime;
    this.producerEsProxy = builder.executorService;
    this.numUncompletedFutures = new AtomicInteger(builder.completableFutureList.size());
    this.originalCompletableFutureList = new ArrayList<>(builder.completableFutureList);
    this.uncaughtExceptionHandler = builder.uncaughtExceptionHandler;
  }

  private AsyncIterator<E> start() {
    completableFutureList = originalCompletableFutureList.stream().map(cf ->
        cf.handleAsync((val, th) -> {
          if (null != th) {
            uncaughtExceptionHandler.handle(th);
          } else {
            if (null == val)  {
              LOGGER.warn("{} is not supposed to got 'null' value.", getClass().getSimpleName());
            }
            queue.offer(val);
          }
          return numUncompletedFutures.decrementAndGet();
        }, consumerES)
    ).collect(Collectors.toList());
    originalCompletableFutureList = null;
    return this;
  }

  @Override
  public boolean hasNext() {
    if (isClosed) {
      return false;
    }
    try {
      while (null == nextValue) {
        if (numUncompletedFutures.get() == 0 && queue.isEmpty()) {
          break;
        }
        /**
         * If external producer executor service is terminated,
         * then shutdown the internal consumer executor service
         */
        if (producerEsProxy.isTerminated()) {
          consumerES.shutdown();
        }
        if (consumerES.isTerminated() && queue.isEmpty()) {
          break;
        }
        nextValue = queue.poll(pollTime, TimeUnit.MILLISECONDS);
      }
    } catch (InterruptedException e) {
      LOGGER.warn("{}#hasNext got interrupted exception.", getClass().getSimpleName(), e);
      try {
        close();
      } catch (IOException ioe) {
        LOGGER.warn("Unable to close {}.", getClass().getSimpleName(), ioe);
      }
    }
    return null != nextValue;
  }

  /**
   * @return value is never null
   */
  @Override
  public E next() {
    if (!hasNext()) {
      throw new NoSuchElementException("There is no more elements.");
    }
    E ret = nextValue;
    nextValue = null;
    return ret;
  }

  @VisibleForTesting
  int getNumUncompletedFutures() {
    return numUncompletedFutures.get();
  }

  @VisibleForTesting
  ExecutorService getConsumerES() {
    return consumerES;
  }

  @Override
  public void close() throws IOException {
    if (isClosed) {
      return;
    }
    for (CompletableFuture<Integer> completableFuture : completableFutureList) {
      completableFuture.cancel(true);
    }
    /**
     * if producerEsProxy is not own by this iterator,
     * the shutdownNow method will do nothing
     * see {@code createWrappedExecutorService}
     */
    producerEsProxy.shutdownNow();
    consumerES.shutdownNow();
    isClosed = true;
  }

  public static <E> Builder<E> custom() {
    return new Builder<>();
  }

  public static class Builder<E> {
    private long pollTime = 50;
    private int concurrent = Runtime.getRuntime().availableProcessors();
    private ExecutorService executorService;
    private List<CompletableFuture<E>> completableFutureList;
    private UncaughtExceptionHandler uncaughtExceptionHandler = UncaughtExceptionHandler.DEFAULT;

    private Builder() {}

    public Builder withConcurrent(int concurrent) {
      this.concurrent = concurrent;
      return this;
    }

    public Builder withPollTime(long pollTime) {
      this.pollTime = pollTime;
      return this;
    }

    public Builder withUncaughtExceptionHandler(UncaughtExceptionHandler handler) {
      this.uncaughtExceptionHandler = handler;
      return this;
    }

    /**
     * Supplier should not return 'null' value otherwise AsyncIterator will swallow it
     */
    public Builder ofSuppliers(Supplier<E>... suppliers) {
      return ofSuppliers(Stream.of(suppliers).collect(Collectors.toList()));
    }

    public Builder ofSuppliers(List<Supplier<E>> supplierList) {
      ExecutorService defaultExecutors = getDefaultExecutors(concurrent);
      List<CompletableFuture<E>> completableFutureList = supplierList.stream()
          .map(supplier -> CompletableFuture.supplyAsync(supplier, defaultExecutors))
          .collect(Collectors.toList());
      return ofCompletableFutures(completableFutureList, defaultExecutors, false);
    }

    public Builder ofSuppliers(List<Supplier<E>> supplierList, ExecutorService es) {
      return ofCompletableFutures(
          supplierList.stream()
              .map(supplier -> CompletableFuture.supplyAsync(supplier, es))
              .collect(Collectors.toList()),
          es
      );
    }

    /**
     * CompletableFuture should not return 'null' value otherwise AsyncIterator will swallow it
     */
    public Builder ofCompletableFutures(List<CompletableFuture<E>> completableFutureList, ExecutorService es) {
      return ofCompletableFutures(completableFutureList, es, true);
    }

    private Builder ofCompletableFutures(List<CompletableFuture<E>> completableFutureList, ExecutorService es,
        boolean isExternal) {
      this.completableFutureList = completableFutureList;
      this.executorService = createWrappedExecutorService(es, isExternal);
      return this;
    }

    public AsyncIterator<E> create() {
      return new AsyncIterator<E>(this).start();
    }

    private static ExecutorService getDefaultExecutors(int concurrent) {
      return Executors.newFixedThreadPool(
          concurrent,
          new ThreadFactory() {
            private int cnt = 0;
            @Override
            public Thread newThread(Runnable r) {
              Thread thread = new Thread(
                  r,
                  String.format("%s-producer-thread-%d", AsyncIterator.class.getSimpleName(), cnt++)
              );
              thread.setDaemon(true);
              return thread;
            }
          }
      );
    }

    private static ExecutorService createWrappedExecutorService(ExecutorService es, boolean isExternal) {
      return isExternal ? new ShutdownDisabledES(es) : es;
    }

    private static class ShutdownDisabledES implements ExecutorService {
      private final ExecutorService delegate;

      private ShutdownDisabledES(ExecutorService delegate) {
        this.delegate = delegate;
      }

      /**
       * When closing AsyncIterator, it will try to shutdown the executor service that is used to run the async tasks.
       * But if AsyncIterator doesn't own this executor service, the shutdown should be left to the owner.
       * This class is used for wrapping the external executor service, and calling shutdown or shutdownNow on it
       * has no effect on the underlying executor service.
       */
      @Override
      public void shutdown() {
        ;
      }

      @Override
      public List<Runnable> shutdownNow() {
        return Collections.emptyList();
      }

      @Override
      public boolean isShutdown() {
        return delegate.isShutdown();
      }

      @Override
      public boolean isTerminated() {
        return delegate.isTerminated();
      }

      @Override
      public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return delegate.awaitTermination(timeout, unit);
      }

      @Override
      public <T> Future<T> submit(Callable<T> task) {
        return delegate.submit(task);
      }

      @Override
      public <T> Future<T> submit(Runnable task, T result) {
        return delegate.submit(task, result);
      }

      @Override
      public Future<?> submit(Runnable task) {
        return delegate.submit(task);
      }

      @Override
      public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
        return delegate.invokeAll(tasks);
      }

      @Override
      public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException {
        return delegate.invokeAll(tasks, timeout, unit);
      }

      @Override
      public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException,
          ExecutionException {
        return delegate.invokeAny(tasks);
      }

      @Override
      public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit) throws InterruptedException, ExecutionException,
          TimeoutException {
        return delegate.invokeAny(tasks, timeout, unit);
      }

      @Override
      public void execute(Runnable command) {
        delegate.execute(command);
      }
    }
  }
}

