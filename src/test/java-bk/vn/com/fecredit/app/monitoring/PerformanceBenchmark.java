package vn.com.fecredit.app.monitoring;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

/**
 * Performance benchmarks for PerformanceReporter with concurrent testing and memory tracking
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PerformanceBenchmark {

    private PerformanceReporter reporter;
    private Random random;
    private String[] operations;
    private static final int WARMUP_ITERATIONS = 3;
    private static final int MEASUREMENT_ITERATIONS = 5;
    private static final int OPERATIONS_PER_TEST = 10_000;
    private static final int THREAD_COUNT = Runtime.getRuntime().availableProcessors();
    private Runtime runtime;

    @BeforeEach
    public void setup() {
        reporter = new PerformanceReporter();
        reporter.setEnabled(true);
        random = new Random();
        operations = new String[]{"login", "search", "checkout"};
        runtime = Runtime.getRuntime();
    }

    @Test
    @Order(1)
    @DisplayName("Benchmark Single Metric Recording")
    void benchmarkSingleMetricRecording() {
        // Warmup
        System.gc();
        long initialMemory = getUsedMemory();
        
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            recordSingleMetricBatch();
        }

        // Measurement
        List<Long> durations = new ArrayList<>();
        List<Long> memoryUsage = new ArrayList<>();
        
        for (int i = 0; i < MEASUREMENT_ITERATIONS; i++) {
            System.gc();
            long memoryBefore = getUsedMemory();
            long start = System.nanoTime();
            recordSingleMetricBatch();
            durations.add(System.nanoTime() - start);
            memoryUsage.add(getUsedMemory() - memoryBefore);
        }

        reportResults("Single Metric Recording", durations, memoryUsage, initialMemory);
    }

    @Test
    @Order(2)
    @DisplayName("Benchmark Concurrent Metric Recording")
    void benchmarkConcurrentRecording() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        System.gc();
        long initialMemory = getUsedMemory();

        // Warmup with concurrent access
        CountDownLatch warmupLatch = new CountDownLatch(THREAD_COUNT);
        for (int i = 0; i < THREAD_COUNT; i++) {
            executor.submit(() -> {
                try {
                    recordSingleMetricBatch();
                } finally {
                    warmupLatch.countDown();
                }
            });
        }
        warmupLatch.await(10, TimeUnit.SECONDS);

        // Measurement
        List<Long> durations = new ArrayList<>();
        List<Long> memoryUsage = new ArrayList<>();

        for (int i = 0; i < MEASUREMENT_ITERATIONS; i++) {
            System.gc();
            long memoryBefore = getUsedMemory();
            CountDownLatch latch = new CountDownLatch(THREAD_COUNT);
            long start = System.nanoTime();

            for (int t = 0; t < THREAD_COUNT; t++) {
                executor.submit(() -> {
                    try {
                        recordSingleMetricBatch();
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await(10, TimeUnit.SECONDS);
            durations.add(System.nanoTime() - start);
            memoryUsage.add(getUsedMemory() - memoryBefore);
        }

        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        reportResults("Concurrent Metric Recording", durations, memoryUsage, initialMemory);
    }

    @Test
    @Order(3)
    @DisplayName("Benchmark High Load Processing")
    void benchmarkHighLoad() throws InterruptedException {
        int highLoadOperations = OPERATIONS_PER_TEST * 10;
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        System.gc();
        long initialMemory = getUsedMemory();
        List<Long> durations = new ArrayList<>();
        List<Long> memoryUsage = new ArrayList<>();

        for (int i = 0; i < MEASUREMENT_ITERATIONS; i++) {
            System.gc();
            long memoryBefore = getUsedMemory();
            long start = System.nanoTime();
            
            CountDownLatch latch = new CountDownLatch(THREAD_COUNT);
            int opsPerThread = highLoadOperations / THREAD_COUNT;
            
            for (int t = 0; t < THREAD_COUNT; t++) {
                final int threadIndex = t;
                executor.submit(() -> {
                    try {
                        int startOp = threadIndex * opsPerThread;
                        int endOp = startOp + opsPerThread;
                        for (int j = startOp; j < endOp; j++) {
                            reporter.recordLatency(operations[j % operations.length], 
                                random.nextDouble() * 1000);
                            reporter.recordThroughput(operations[j % operations.length], 
                                random.nextDouble() * 100);
                        }
                    } finally {
                        latch.countDown();
                    }
                });
            }
            
            latch.await(20, TimeUnit.SECONDS);
            durations.add(System.nanoTime() - start);
            memoryUsage.add(getUsedMemory() - memoryBefore);
            reporter.generateReport();
            reporter.clearMetrics();
        }

        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        reportResults("High Load Processing", durations, memoryUsage, initialMemory);
    }

    @Test
    @Order(4)
    @DisplayName("Memory Leak Detection Test")
    void memoryLeakTest() {
        System.gc();
        long initialMemory = getUsedMemory();
        List<Long> memorySnapshots = new ArrayList<>();

        // Record metrics in multiple iterations to detect memory leaks
        for (int i = 0; i < 10; i++) {
            recordBatchMetrics();
            reporter.generateReport();
            reporter.clearMetrics();
            System.gc();
            memorySnapshots.add(getUsedMemory());
        }

        // Check for significant memory growth
        long memoryGrowth = memorySnapshots.get(memorySnapshots.size() - 1) - memorySnapshots.get(0);
        System.out.printf("Memory growth over iterations: %d bytes%n", memoryGrowth);
        assertTrue(memoryGrowth < 10_000_000, "Possible memory leak detected");
    }

    private void recordSingleMetricBatch() {
        for (int i = 0; i < OPERATIONS_PER_TEST; i++) {
            reporter.recordLatency(operations[0], random.nextDouble() * 1000);
        }
    }

    private void recordBatchMetrics() {
        for (int i = 0; i < OPERATIONS_PER_TEST / 3; i++) {
            for (String operation : operations) {
                reporter.recordLatency(operation, random.nextDouble() * 1000);
                reporter.recordThroughput(operation, random.nextDouble() * 100);
                reporter.recordMemoryUsage(operation, random.nextInt(1024 * 1024));
            }
        }
    }

    private long getUsedMemory() {
        return runtime.totalMemory() - runtime.freeMemory();
    }

    private void reportResults(String testName, List<Long> durations, List<Long> memoryUsage, long initialMemory) {
        double avgDurationMs = calculateAverageMs(durations);
        double avgMemoryMB = memoryUsage.stream()
            .mapToDouble(m -> m / (1024.0 * 1024.0))
            .average()
            .orElse(0.0);
        
        System.out.printf("%s Results:%n", testName);
        System.out.printf("  Average Duration: %.3f ms%n", avgDurationMs);
        System.out.printf("  Average Memory Usage: %.2f MB%n", avgMemoryMB);
        System.out.printf("  Memory Growth: %.2f MB%n", 
            (getUsedMemory() - initialMemory) / (1024.0 * 1024.0));
        
        assertTrue(avgDurationMs < 5000, testName + " took too long");
        assertTrue(avgMemoryMB < 100, testName + " used too much memory");
    }

    private double calculateAverageMs(List<Long> nanoTimes) {
        return nanoTimes.stream()
            .mapToDouble(t -> t / 1_000_000.0)
            .average()
            .orElse(0.0);
    }

    @AfterEach
    void cleanup() {
        reporter.clearMetrics();
        System.gc();
    }
}
