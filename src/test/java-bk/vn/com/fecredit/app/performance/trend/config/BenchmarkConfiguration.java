package vn.com.fecredit.app.performance.trend.config;

import org.springframework.lang.NonNull;

import java.util.concurrent.TimeUnit;

/**
 * Configuration class for performance benchmarks.
 * Provides standardized settings and configuration for JMH benchmarks.
 */
public class BenchmarkConfiguration {
    // Benchmark settings
    public static final int DEFAULT_FORKS = 2;
    public static final int DEFAULT_WARMUP_ITERATIONS = 3;
    public static final int DEFAULT_MEASUREMENT_ITERATIONS = 5;
    public static final int DEFAULT_WARMUP_TIME = 1;
    public static final int DEFAULT_MEASUREMENT_TIME = 1;
    public static final TimeUnit DEFAULT_TIME_UNIT = TimeUnit.MICROSECONDS;

    // Dataset sizes
    public static final int SMALL_DATASET_SIZE = 100;
    public static final int MEDIUM_DATASET_SIZE = 10_000;
    public static final int LARGE_DATASET_SIZE = 100_000;

    // Thread settings
    public static final int DEFAULT_THREAD_COUNT = 4;
    public static final int MAX_THREAD_COUNT = 8;

    // Performance thresholds
    public static final double MIN_OPERATIONS_PER_SECOND = 1_000_000.0; // 1M ops/sec
    public static final long MAX_LATENCY_MICROS = 100; // 100 microseconds
    public static final double MAX_DEVIATION_PERCENT = 10.0; // 10% deviation

    /**
     * Creates benchmark arguments with default settings.
     *
     * @return Array of JMH arguments
     */
    @NonNull
    public static String[] getDefaultArgs() {
        return new String[]{
            "-f", String.valueOf(DEFAULT_FORKS),
            "-wi", String.valueOf(DEFAULT_WARMUP_ITERATIONS),
            "-i", String.valueOf(DEFAULT_MEASUREMENT_ITERATIONS),
            "-tu", DEFAULT_TIME_UNIT.name(),
            "-to", String.valueOf(DEFAULT_MEASUREMENT_TIME),
            "-t", String.valueOf(DEFAULT_THREAD_COUNT)
        };
    }

    /**
     * Creates benchmark arguments for memory profiling.
     *
     * @return Array of JMH arguments with GC profiler enabled
     */
    @NonNull
    public static String[] getMemoryProfilingArgs() {
        return new String[]{
            "-f", "1",
            "-wi", "2",
            "-i", "3",
            "-prof", "gc",
            "-tu", TimeUnit.MILLISECONDS.name(),
            "-t", "1"
        };
    }

    /**
     * Creates benchmark arguments for throughput testing.
     *
     * @return Array of JMH arguments optimized for throughput
     */
    @NonNull
    public static String[] getThroughputArgs() {
        return new String[]{
            "-f", "2",
            "-wi", "5",
            "-i", "10",
            "-tu", TimeUnit.SECONDS.name(),
            "-t", String.valueOf(MAX_THREAD_COUNT),
            "-bm", "thrpt"
        };
    }

    /**
     * Creates benchmark arguments for latency testing.
     *
     * @return Array of JMH arguments optimized for latency
     */
    @NonNull
    public static String[] getLatencyArgs() {
        return new String[]{
            "-f", "2",
            "-wi", "5",
            "-i", "10",
            "-tu", TimeUnit.NANOSECONDS.name(),
            "-t", "1",
            "-bm", "avgt",
            "-prof", "jfr"
        };
    }

    /**
     * Checks if the operations per second meet the minimum threshold.
     *
     * @param opsPerSec Operations per second
     * @return true if performance meets threshold
     */
    public static boolean meetsPerformanceThreshold(double opsPerSec) {
        return opsPerSec >= MIN_OPERATIONS_PER_SECOND;
    }

    /**
     * Checks if the latency meets the maximum threshold.
     *
     * @param latencyMicros Latency in microseconds
     * @return true if latency is within acceptable range
     */
    public static boolean isLatencyAcceptable(long latencyMicros) {
        return latencyMicros <= MAX_LATENCY_MICROS;
    }

    /**
     * Checks if the performance deviation is within acceptable range.
     *
     * @param deviation Deviation percentage
     * @return true if deviation is acceptable
     */
    public static boolean isDeviationAcceptable(double deviation) {
        return deviation <= MAX_DEVIATION_PERCENT;
    }

    /**
     * Gets the appropriate dataset size based on the benchmark type.
     *
     * @param type Benchmark type (small, medium, large)
     * @return Dataset size
     */
    public static int getDatasetSize(@NonNull BenchmarkType type) {
        return switch (type) {
            case SMALL -> SMALL_DATASET_SIZE;
            case MEDIUM -> MEDIUM_DATASET_SIZE;
            case LARGE -> LARGE_DATASET_SIZE;
        };
    }

    /**
     * Benchmark dataset size types.
     */
    public enum BenchmarkType {
        SMALL,
        MEDIUM,
        LARGE
    }

    private BenchmarkConfiguration() {
        // Prevent instantiation
    }
}
