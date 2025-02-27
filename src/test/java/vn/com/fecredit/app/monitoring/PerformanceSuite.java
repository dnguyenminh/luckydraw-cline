package vn.com.fecredit.app.monitoring;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.annotation.DirtiesContext;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.time.Duration;
import java.time.Instant;

/**
 * Performance test suite for monitoring system
 */
@SpringBootTest(classes = TestConfig.class)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Execution(ExecutionMode.CONCURRENT)
public class PerformanceSuite {

    private static final int WARMUP_ITERATIONS = 10_000;
    private static final int TEST_ITERATIONS = 100_000;
    private static final int THREAD_COUNT = Runtime.getRuntime().availableProcessors();
    private static final Duration TEST_DURATION = Duration.ofSeconds(30);
    
    private EventStatisticsMonitor monitor;
    private ExecutorService executor;
    private ScheduledExecutorService scheduler;

    @BeforeAll
    void setUp() {
        monitor = EventStatisticsMonitor.getInstance();
        monitor.clearMetrics();
        monitor.enableMonitoring();
        
        executor = Executors.newFixedThreadPool(THREAD_COUNT);
        scheduler = Executors.newScheduledThreadPool(2);
        
        // Warm up the JVM
        warmup();
    }

    private void warmup() {
        IntStream.range(0, WARMUP_ITERATIONS).parallel().forEach(i -> 
            monitor.recordOperation("warmup", 1L)
        );
        monitor.clearMetrics();
    }

    @Test
    void throughputTest() throws Exception {
        // Given
        LongAdder operationCount = new LongAdder();
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch endLatch = new CountDownLatch(THREAD_COUNT);
        
        // When
        for (int i = 0; i < THREAD_COUNT; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    while (!Thread.currentThread().isInterrupted()) {
                        monitor.recordOperation("throughput", 1L);
                        operationCount.increment();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    endLatch.countDown();
                }
            });
        }

        // Start test
        Instant start = Instant.now();
        startLatch.countDown();
        
        // Run for specified duration
        Thread.sleep(TEST_DURATION.toMillis());
        executor.shutdownNow();
        endLatch.await(5, TimeUnit.SECONDS);
        
        // Then
        Duration elapsed = Duration.between(start, Instant.now());
        double opsPerSecond = operationCount.sum() * 1000.0 / elapsed.toMillis();
        
        System.out.printf("Throughput: %.2f ops/sec%n", opsPerSecond);
        assert opsPerSecond > 10000 : "Minimum throughput not met";
    }

    @Test
    void latencyTest() throws Exception {
        // Given
        int sampleSize = 10_000;
        ConcurrentLinkedQueue<Long> latencies = new ConcurrentLinkedQueue<>();
        CountDownLatch completionLatch = new CountDownLatch(sampleSize);
        
        // When
        IntStream.range(0, sampleSize).forEach(i -> {
            executor.submit(() -> {
                try {
                    long start = System.nanoTime();
                    monitor.recordOperation("latency", 1L);
                    long elapsed = System.nanoTime() - start;
                    latencies.add(TimeUnit.NANOSECONDS.toMicros(elapsed));
                } finally {
                    completionLatch.countDown();
                }
            });
        });

        completionLatch.await();

        // Then
        double avgLatency = latencies.stream()
            .mapToLong(Long::valueOf)
            .average()
            .orElse(0.0);
        
        long p99Latency = latencies.stream()
            .sorted()
            .skip((long)(sampleSize * 0.99))
            .findFirst()
            .orElse(0L);
        
        System.out.printf("Average Latency: %.2f μs%n", avgLatency);
        System.out.printf("99th Percentile Latency: %d μs%n", p99Latency);
        
        assert avgLatency < 100 : "Average latency too high";
        assert p99Latency < 500 : "P99 latency too high";
    }

    @Test
    void memoryLeakTest() throws Exception {
        // Given
        Runtime runtime = Runtime.getRuntime();
        long initialMemory = runtime.totalMemory() - runtime.freeMemory();
        AtomicLong peakMemory = new AtomicLong(initialMemory);
        
        // Monitor memory usage
        ScheduledFuture<?> memoryMonitor = scheduler.scheduleAtFixedRate(() -> {
            long currentMemory = runtime.totalMemory() - runtime.freeMemory();
            peakMemory.updateAndGet(prev -> Math.max(prev, currentMemory));
        }, 0, 100, TimeUnit.MILLISECONDS);

        // When
        for (int batch = 0; batch < 10; batch++) {
            CountDownLatch batchLatch = new CountDownLatch(THREAD_COUNT);
            
            for (int thread = 0; thread < THREAD_COUNT; thread++) {
                executor.submit(() -> {
                    try {
                        for (int i = 0; i < TEST_ITERATIONS / 10; i++) {
                            monitor.recordOperation("memory" + i, 1L);
                        }
                    } finally {
                        batchLatch.countDown();
                    }
                });
            }
            
            batchLatch.await();
            System.gc(); // Suggest GC between batches
            Thread.sleep(100); // Allow time for GC
        }

        memoryMonitor.cancel(false);
        
        // Then
        long memoryIncrease = peakMemory.get() - initialMemory;
        double bytesPerOperation = (double) memoryIncrease / TEST_ITERATIONS;
        
        System.out.printf("Peak Memory Increase: %d bytes%n", memoryIncrease);
        System.out.printf("Memory per Operation: %.2f bytes%n", bytesPerOperation);
        
        assert bytesPerOperation < 100 : "Memory usage per operation too high";
    }

    @Test
    void concurrencyTest() throws Exception {
        // Given
        int concurrentOperations = THREAD_COUNT * 2;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch completionLatch = new CountDownLatch(concurrentOperations);
        ConcurrentHashMap<String, LongAdder> operationCounts = new ConcurrentHashMap<>();
        
        // When
        for (int i = 0; i < concurrentOperations; i++) {
            final String operationType = "concurrent" + (i % 4);
            executor.submit(() -> {
                try {
                    startLatch.await();
                    for (int j = 0; j < TEST_ITERATIONS / concurrentOperations; j++) {
                        monitor.recordOperation(operationType, 1L);
                        operationCounts.computeIfAbsent(operationType, k -> new LongAdder()).increment();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    completionLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        completionLatch.await();

        // Then
        operationCounts.forEach((type, count) -> {
            long recorded = monitor.getTotalOperations(type);
            assert recorded == count.sum() : 
                String.format("Operation count mismatch for %s: expected %d, got %d", 
                    type, count.sum(), recorded);
        });
    }
}
