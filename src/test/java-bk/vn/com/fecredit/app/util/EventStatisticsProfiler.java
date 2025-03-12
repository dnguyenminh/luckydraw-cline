package vn.com.fecredit.app.util;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

/**
 * Profiler utility for measuring performance of EventStatistics operations
 */
public class EventStatisticsProfiler {
    private static final Map<String, ProfileMetrics> metrics = new ConcurrentHashMap<>();
    private static final ThreadLocal<Stack<ProfilerContext>> activeContexts = 
        ThreadLocal.withInitial(Stack::new);
    private static volatile boolean profilingEnabled = false;

    public static class ProfileMetrics {
        private final AtomicLong totalCalls = new AtomicLong();
        private final AtomicLong totalTimeNanos = new AtomicLong();
        private volatile long minTimeNanos = Long.MAX_VALUE;
        private volatile long maxTimeNanos = 0;
        private final List<Long> recentTimes = Collections.synchronizedList(new ArrayList<>());
        private static final int MAX_RECENT_SAMPLES = 100;

        void recordTime(long timeNanos) {
            totalCalls.incrementAndGet();
            totalTimeNanos.addAndGet(timeNanos);
            updateMinMax(timeNanos);
            addRecentTime(timeNanos);
        }

        private void updateMinMax(long timeNanos) {
            synchronized (this) {
                minTimeNanos = Math.min(minTimeNanos, timeNanos);
                maxTimeNanos = Math.max(maxTimeNanos, timeNanos);
            }
        }

        private void addRecentTime(long timeNanos) {
            synchronized (recentTimes) {
                recentTimes.add(timeNanos);
                if (recentTimes.size() > MAX_RECENT_SAMPLES) {
                    recentTimes.remove(0);
                }
            }
        }

        public long getTotalCalls() {
            return totalCalls.get();
        }

        public double getAverageTimeMs() {
            long calls = totalCalls.get();
            return calls > 0 ? totalTimeNanos.get() / (calls * 1_000_000.0) : 0.0;
        }

        public double getMinTimeMs() {
            return minTimeNanos == Long.MAX_VALUE ? 0.0 : minTimeNanos / 1_000_000.0;
        }

        public double getMaxTimeMs() {
            return maxTimeNanos / 1_000_000.0;
        }

        public double getStdDevMs() {
            synchronized (recentTimes) {
                if (recentTimes.isEmpty()) return 0.0;
                double mean = recentTimes.stream().mapToLong(l -> l).average().orElse(0.0);
                double variance = recentTimes.stream()
                    .mapToDouble(time -> Math.pow(time - mean, 2))
                    .average()
                    .orElse(0.0);
                return Math.sqrt(variance) / 1_000_000.0;
            }
        }
    }

    private static class ProfilerContext {
        final String name;
        final Instant startTime;

        ProfilerContext(String name) {
            this.name = name;
            this.startTime = Instant.now();
        }

        long getDurationNanos() {
            return Duration.between(startTime, Instant.now()).toNanos();
        }
    }

    public static void enable() {
        profilingEnabled = true;
    }

    public static void disable() {
        profilingEnabled = false;
    }

    public static void reset() {
        metrics.clear();
    }

    public static void startOperation(String name) {
        if (!profilingEnabled) return;
        activeContexts.get().push(new ProfilerContext(name));
    }

    public static void endOperation() {
        if (!profilingEnabled) return;
        Stack<ProfilerContext> contexts = activeContexts.get();
        if (contexts.isEmpty()) return;

        ProfilerContext context = contexts.pop();
        recordMetrics(context.name, context.getDurationNanos());
    }

    public static <T> T profile(String name, Supplier<T> operation) {
        if (!profilingEnabled) return operation.get();

        startOperation(name);
        try {
            return operation.get();
        } finally {
            endOperation();
        }
    }

    public static void profile(String name, Runnable operation) {
        if (!profilingEnabled) {
            operation.run();
            return;
        }

        startOperation(name);
        try {
            operation.run();
        } finally {
            endOperation();
        }
    }

    private static void recordMetrics(String operationName, long timeNanos) {
        metrics.computeIfAbsent(operationName, k -> new ProfileMetrics())
               .recordTime(timeNanos);
    }

    public static Map<String, ProfileMetrics> getMetrics() {
        return new HashMap<>(metrics);
    }

    public static void printReport() {
        if (metrics.isEmpty()) {
            System.out.println("No profiling data available");
            return;
        }

        System.out.println("\nProfiling Report:");
        System.out.println("================");
        metrics.forEach((operation, metric) -> {
            System.out.printf("\nOperation: %s%n", operation);
            System.out.printf("  Total calls: %d%n", metric.getTotalCalls());
            System.out.printf("  Average time: %.2f ms%n", metric.getAverageTimeMs());
            System.out.printf("  Min time: %.2f ms%n", metric.getMinTimeMs());
            System.out.printf("  Max time: %.2f ms%n", metric.getMaxTimeMs());
            System.out.printf("  Std Dev: %.2f ms%n", metric.getStdDevMs());
        });
    }

    public static boolean isProfilingEnabled() {
        return profilingEnabled;
    }

    public static void assertPerformance(String operation, double maxAverageTimeMs) {
        ProfileMetrics metric = metrics.get(operation);
        if (metric == null) {
            throw new IllegalStateException("No metrics available for operation: " + operation);
        }
        if (metric.getAverageTimeMs() > maxAverageTimeMs) {
            throw new AssertionError(String.format(
                "Performance degradation detected for %s: %.2f ms (max allowed: %.2f ms)",
                operation, metric.getAverageTimeMs(), maxAverageTimeMs
            ));
        }
    }
}
