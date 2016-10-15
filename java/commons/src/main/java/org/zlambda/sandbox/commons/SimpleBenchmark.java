package org.zlambda.sandbox.commons;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * A simple benchmark class mainly used in algorithm testing
 */
public class SimpleBenchmark {
    private static void printf(String format, Object...args) {
        System.out.printf(format + "\n", args);
    }

    public static class Builder {
        private TimeUnit timeUnit = TimeUnit.MILLISECONDS;

        public Builder timeUnit(TimeUnit timeUnit) {
            this.timeUnit = timeUnit;
            return this;
        }

        public SimpleBenchmark build() {
            return new SimpleBenchmark(this);
        }
    }

    private final TimeUnit timeUnit;
    private double divider;
    private SimpleBenchmark(Builder builder) {
        this.timeUnit = builder.timeUnit;
        switch (timeUnit) {
            case SECONDS:
                divider = 1e9;
                break;

            case MILLISECONDS:
                divider = 1e6;
                break;

            case MICROSECONDS:
                divider = 1e3;
                break;

            case NANOSECONDS:
                divider = 1;
                break;

            default:
                throw new IllegalArgumentException("Unsupported TimeUnit \"" + timeUnit.toString() + "\"");
        }
    }

    public <R> void eval(Action<R> action, PostAction<R> postAction, int times, String msg) {
        Preconditions.checkArgument(times > 0, "Execution times should be positive but given " + times);
        Preconditions.checkArgument(null != action, "Action cannot be null");
        postAction = null == postAction ? (res) -> {} : postAction;

        List<Long> executionTime = new ArrayList<>();
        for (int i = 0; i < times; ++i) {
            long beg = System.nanoTime();
            R res = action.execute();
            long t = System.nanoTime() - beg;
            executionTime.add(t);
            postAction.execute(res);
        }

        double first = executionTime.get(0) / divider;
        Collections.sort(executionTime);
        double min = executionTime.get(0) / divider;
        double max = executionTime.get(executionTime.size() - 1) / divider;
        int _95idx = (int)Math.ceil(executionTime.size() * 0.95);
        _95idx = executionTime.size() == _95idx ? _95idx - 1 : _95idx;
        double _95 = executionTime.get(_95idx) / divider;
        double avg = executionTime.stream().reduce(0L, (res, ele) -> res + ele) / (executionTime.size() * divider);

        printf("[EXECUTE %s (%d times)] [first: %f, min: %f, max: %f, 95th: %f, avg: %f] [%s]", msg, times, first, min, max, _95, avg, timeUnit);
    }


    @FunctionalInterface
    public interface Action<R> {
        R execute();
    }

    @FunctionalInterface
    public interface PostAction<R> {
        void execute(R res);
    }
}
