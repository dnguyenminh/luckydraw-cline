package vn.com.fecredit.app.monitoring;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Performance benchmarking suite for profiling components
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PerformanceBenchmarkRunner {

    private static final int WARMUP_ITERATIONS = 1000;
    private static final int BENCHMARK_ITERATIONS = 10_000;
    private static final int BENCHMARK_THREADS = 4;
    private static final Duration MAX_TEST_DURATION = Duration.ofMinutes(5);
    
    private static class BenchmarkResult {
        private final double averageTimeMs;
        private final double p95TimeMs;
        private final double p99TimeMs;
        private final double maxTimeMs;
        private final long totalOperations;
        private final Duration totalDuration;
        private final double operationsPerSecond;

        BenchmarkResult(double averageTimeMs, double p95TimeMs, double p99TimeMs,
                       double maxTimeMs, long totalOperations, Duration totalDuration,
                       double operationsPerSecond) {
            this.averageTimeMs = averageTimeMs;
            this.p95TimeMs = p95TimeMs;
            this.p99TimeMs = p99TimeMs;
            this.maxTimeMs = maxTimeMs;
            this.totalOperations = totalOperations;
            this.totalDuration = totalDuration;
            this.operationsPerSecond = operationsPerSecond;
        }

        public double getAverageTimeMs() { return averageTimeMs; }
        public double getP95TimeMs() { return p95TimeMs; }
        public double getP99TimeMs() { return p99TimeMs; }
        public double getMaxTimeMs() { return maxTimeMs; }
        public long getTotalOperations() { return totalOperations; }
        public Duration getTotalDuration() { return totalDuration; }
        public double getOperationsPerSecond() { return operationsPerSecond; }
    }

    @TempDir
    Path tempDir;
    
    private PerformanceProfiler profiler;
    private PerformanceProfilerVisualizer visualizer;
    private ExecutorService executor;
    private Random random;

    @BeforeEach
    void setUp() {
        profiler = new PerformanceProfiler();
        visualizer = new PerformanceProfilerVisualizer();
        executor = Executors.newFixedThreadPool(BENCHMARK_THREADS);
        random = new Random();
    }

    @Test
    @Order(1)
    @DisplayName("Benchmark: Record Latency Performance")
    void benchmarkLatencyRecording() {
        BenchmarkResult result = benchmarkOperation(() -> {
            String label = "latency-" + random.nextInt(1000);
            profiler.recordLatency(label, random.nextDouble() * 1000);
            return null;
        });
        
        assertBenchmarkResults("Latency Recording", result,
            500.0,  // max average time (microseconds)
            1000.0, // max p95 time (microseconds)
            2000.0, // max p99 time (microseconds)
            10000   // min operations per second
        );
    }

    @Test
    @Order(2)
    @DisplayName("Benchmark: Record Memory Usage Performance")
    void benchmarkMemoryRecording() {
        BenchmarkResult result = benchmarkOperation(() -> {
            String label = "memory-" + random.nextInt(1000);
            profiler.recordMemoryUsage(label, random.nextDouble() * 1024);
            return null;
        });
        
        assertBenchmarkResults("Memory Recording", result,
            500.0,  // max average time (microseconds)
            1000.0, // max p95 time (microseconds)
            2000.0, // max p99 time (microseconds)
            10000   // min operations per second
        );
    }

    @Test
    @Order(3)
    @DisplayName("Benchmark: Generate Visualization Performance")
    void benchmarkVisualization() throws Exception {
        // Prepare test data
        for (int i = 0; i < 1000; i++) {
            profiler.recordLatency("op-" + i, random.nextDouble() * 1000);
            profiler.recordMemoryUsage("op-" + i, random.nextDouble() * 512);
        }
        final PerformanceProfiler.ProfilerSummary summary = profiler.getAndResetSummary();

        BenchmarkResult result = benchmarkOperation(() -> {
            try {
                Path outputPath = tempDir.resolve("viz-" + System.nanoTime() + ".html");
                visualizer.generateVisualization(summary, outputPath.toString());
                return null;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        
        assertBenchmarkResults("Visualization Generation", result,
            100.0,  // max average time (milliseconds)
            200.0,  // max p95 time (milliseconds)
            300.0,  // max p99 time (milliseconds)
            10      // min operations per second
        );
    }

    @Test
    @Order(4)
    @DisplayName("Benchmark: Full Pipeline Performance")
    void benchmarkFullPipeline() {
        BenchmarkResult result = benchmarkOperation(() -> {
            try {
                // Record metrics
                String label = "pipeline-" + random.nextInt(1000);
                profiler.recordLatency(label, random.nextDouble() * 1000);
                profiler.recordMemoryUsage(label, random.nextDouble() * 512);
                
                // Generate visualization
                if (random.nextInt(100) < 5) { // 5% chance to visualize
                    Path outputPath = tempDir.resolve("pipeline-" + System.nanoTime() + ".html");
                    visualizer.generateVisualization(profiler.getAndResetSummary(), outputPath.toString());
                }
                return null;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        
        assertBenchmarkResults("Full Pipeline", result,
            200.0,  // max average time (milliseconds)
            400.0,  // max p95 time (milliseconds)
            600.0,  // max p99 time (milliseconds)
            100     // min operations per second
        );
    }

    private BenchmarkResult benchmarkOperation(Supplier<Void> operation) {
        // Warmup phase
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            operation.get();
        }
        
        // Benchmark phase
        List<Long> timings = Collections.synchronizedList(new ArrayList<>());
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch completionLatch = new CountDownLatch(BENCHMARK_THREADS);
        AtomicInteger operationCount = new AtomicInteger(0);
        
        Instant startTime = Instant.now();
        
        // Launch benchmark threads
        for (int t = 0; t < BENCHMARK_THREADS; t++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    while (operationCount.get() < BENCHMARK_ITERATIONS && 
                           Duration.between(startTime, Instant.now()).compareTo(MAX_TEST_DURATION) < 0) {
                        long start = System.nanoTime();
                        operation.get();
                        timings.add(TimeUnit.NANOSECONDS.toMicros(System.nanoTime() - start));
                        operationCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    completionLatch.countDown();
                }
            });
        }
        
        // Start benchmark
        startLatch.countDown();
        
        try {
            completionLatch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        Duration totalDuration = Duration.between(startTime, Instant.now());
        Collections.sort(timings);
        
        return new BenchmarkResult(
            calculateAverage(timings),
            calculatePercentile(timings, 0.95),
            calculatePercentile(timings, 0.99),
            timings.get(timings.size() - 1),
            operationCount.get(),
            totalDuration,
            calculateOperationsPerSecond(operationCount.get(), totalDuration)
        );
    }

    private void assertBenchmarkResults(String operation, BenchmarkResult result,
                                      double maxAvgTime, double maxP95Time,
                                      double maxP99Time, double minOpsPerSecond) {
        System.out.printf("\nBenchmark Results for %s:%n", operation);
        System.out.printf("Average Time: %.2f µs%n", result.getAverageTimeMs());
        System.out.printf("P95 Time: %.2f µs%n", result.getP95TimeMs());
        System.out.printf("P99 Time: %.2f µs%n", result.getP99TimeMs());
        System.out.printf("Max Time: %.2f µs%n", result.getMaxTimeMs());
        System.out.printf("Operations/Second: %.2f%n", result.getOperationsPerSecond());
        System.out.printf("Total Operations: %d%n", result.getTotalOperations());
        System.out.printf("Total Duration: %s%n", result.getTotalDuration());
        
        assertTrue(result.getAverageTimeMs() < maxAvgTime,
            String.format("Average time (%.2f µs) exceeds maximum (%.2f µs)",
                result.getAverageTimeMs(), maxAvgTime));
        assertTrue(result.getP95TimeMs() < maxP95Time,
            String.format("P95 time (%.2f µs) exceeds maximum (%.2f µs)",
                result.getP95TimeMs(), maxP95Time));
        assertTrue(result.getP99TimeMs() < maxP99Time,
            String.format("P99 time (%.2f µs) exceeds maximum (%.2f µs)",
                result.getP99TimeMs(), maxP99Time));
        assertTrue(result.getOperationsPerSecond() > minOpsPerSecond,
            String.format("Operations per second (%.2f) below minimum (%.2f)",
                result.getOperationsPerSecond(), minOpsPerSecond));
    }

    private double calculateAverage(List<Long> values) {
        return values.stream()
            .mapToDouble(Long::doubleValue)
            .average()
            .orElse(0.0);
    }

    private double calculatePercentile(List<Long> values, double percentile) {
        int index = (int) Math.ceil(percentile * values.size()) - 1;
        return values.get(Math.max(0, index));
    }

    private double calculateOperationsPerSecond(long operations, Duration duration) {
        return operations * 1000.0 / duration.toMillis();
    }

    @AfterEach
    void cleanup() throws Exception {
        executor.shutdownNow();
        assertTrue(executor.awaitTermination(30, TimeUnit.SECONDS),
            "Executor should shut down cleanly");
        
        profiler = null;
        visualizer = null;
        System.gc();
    }
}
