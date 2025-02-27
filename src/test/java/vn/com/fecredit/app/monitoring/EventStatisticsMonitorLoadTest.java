package vn.com.fecredit.app.monitoring;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

@Tag("load")
class EventStatisticsMonitorLoadTest {
    private EventStatisticsMonitor monitor;
    private static final int WARMUP_COUNT = 1000;
    
    @BeforeEach
    void setUp() {
        monitor = EventStatisticsMonitor.getInstance();
        monitor.clearMetrics();
        monitor.enableMonitoring();
        
        // Warm up the JVM
        for (int i = 0; i < WARMUP_COUNT; i++) {
            monitor.recordOperation("warmup", 1L);
        }
        monitor.clearMetrics();
    }

    @ParameterizedTest
    @ValueSource(ints = {100_000, 500_000, 1_000_000})
    void shouldHandleHighVolume(int operationCount) {
        // Given
        String operation = "highVolumeOperation";
        long startTime = System.nanoTime();

        // When
        for (int i = 0; i < operationCount; i++) {
            monitor.recordOperation(operation, 1L);
        }

        // Then
        long duration = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
        double operationsPerSecond = (operationCount * 1000.0) / duration;

        assertThat(monitor.getTotalOperations(operation)).isEqualTo(operationCount);
        assertThat(operationsPerSecond).isGreaterThan(10000); // At least 10K ops/sec
        
        System.out.printf("Processed %d operations in %dms (%.2f ops/sec)%n", 
            operationCount, duration, operationsPerSecond);
    }

    @Test
    void shouldHandleMultipleHighVolumeOperations() throws InterruptedException {
        // Given
        int operationTypes = 100;
        int operationsPerType = 10_000;
        CountDownLatch completionLatch = new CountDownLatch(operationTypes);
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        ConcurrentHashMap<String, AtomicInteger> operationCounts = new ConcurrentHashMap<>();

        // When
        long startTime = System.nanoTime();
        
        for (int i = 0; i < operationTypes; i++) {
            String operation = "operation" + i;
            executor.submit(() -> {
                try {
                    for (int j = 0; j < operationsPerType; j++) {
                        monitor.recordOperation(operation, ThreadLocalRandom.current().nextLong(1, 100));
                        operationCounts.computeIfAbsent(operation, k -> new AtomicInteger()).incrementAndGet();
                    }
                } finally {
                    completionLatch.countDown();
                }
            });
        }

        boolean completed = completionLatch.await(30, TimeUnit.SECONDS);
        executor.shutdown();
        long duration = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);

        // Then
        assertThat(completed).isTrue();
        assertThat(monitor.getMonitoredOperations()).hasSize(operationTypes);
        
        long totalOperations = operationCounts.values().stream()
            .mapToInt(AtomicInteger::get)
            .sum();
        
        double operationsPerSecond = (totalOperations * 1000.0) / duration;
        System.out.printf("Processed %d operations across %d types in %dms (%.2f ops/sec)%n",
            totalOperations, operationTypes, duration, operationsPerSecond);
    }

    @Test
    void shouldHandleConcurrentThresholdUpdates() throws InterruptedException {
        // Given
        int threadCount = 20;
        int iterationsPerThread = 1000;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch completionLatch = new CountDownLatch(threadCount);
        String operation = "thresholdTestOperation";
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        AtomicInteger thresholdUpdateCount = new AtomicInteger();

        // When
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    for (int j = 0; j < iterationsPerThread; j++) {
                        double threshold = ThreadLocalRandom.current().nextDouble(100);
                        monitor.setPerformanceThreshold(operation, threshold);
                        thresholdUpdateCount.incrementAndGet();
                        
                        // Verify we can still read the threshold
                        assertThat(monitor.getPerformanceThreshold(operation)).isPositive();
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    completionLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        boolean completed = completionLatch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        // Then
        assertThat(completed).isTrue();
        assertThat(thresholdUpdateCount.get()).isEqualTo(threadCount * iterationsPerThread);
    }

    @Test
    void shouldHandleBurstOperations() throws InterruptedException {
        // Given
        int burstSize = 100_000;
        int burstCount = 10;
        String operation = "burstOperation";
        CountDownLatch burstCompletionLatch = new CountDownLatch(burstCount);
        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        // When
        long startTime = System.nanoTime();
        
        for (int burst = 0; burst < burstCount; burst++) {
            executor.submit(() -> {
                try {
                    // Simulate burst of operations
                    for (int i = 0; i < burstSize; i++) {
                        monitor.recordOperation(operation, 1L);
                    }
                    Thread.sleep(100); // Brief pause between bursts
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    burstCompletionLatch.countDown();
                }
            });
        }

        boolean completed = burstCompletionLatch.await(20, TimeUnit.SECONDS);
        executor.shutdown();
        long duration = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);

        // Then
        assertThat(completed).isTrue();
        long expectedOperations = (long) burstSize * burstCount;
        assertThat(monitor.getTotalOperations(operation)).isEqualTo(expectedOperations);
        
        double operationsPerSecond = (expectedOperations * 1000.0) / duration;
        System.out.printf("Processed %d burst operations in %dms (%.2f ops/sec)%n",
            expectedOperations, duration, operationsPerSecond);
    }

    @Test
    void shouldHandleMemoryPressure() {
        // Given
        int operationCount = 1_000_000;
        Runtime runtime = Runtime.getRuntime();
        long initialMemory = runtime.totalMemory() - runtime.freeMemory();
        
        // When
        IntStream.range(0, operationCount)
            .parallel()
            .forEach(i -> monitor.recordOperation("memoryTest" + (i % 1000), 1L));
        
        System.gc(); // Suggest garbage collection
        long finalMemory = runtime.totalMemory() - runtime.freeMemory();
        long memoryIncrease = finalMemory - initialMemory;
        
        // Then
        double bytesPerOperation = (double) memoryIncrease / operationCount;
        System.out.printf("Memory usage: %.2f bytes per operation%n", bytesPerOperation);
        assertThat(bytesPerOperation).isLessThan(100); // Less than 100 bytes per operation
    }

    @Test
    void shouldHandleLongRunningMonitoring() throws InterruptedException {
        // Given
        int durationSeconds = 10;
        int threadCount = Runtime.getRuntime().availableProcessors();
        CountDownLatch completionLatch = new CountDownLatch(threadCount);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        AtomicInteger totalOperations = new AtomicInteger();
        AtomicBoolean running = new AtomicBoolean(true);

        // When
        long startTime = System.nanoTime();
        
        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    while (running.get()) {
                        monitor.recordOperation("longRunning", 1L);
                        totalOperations.incrementAndGet();
                    }
                } finally {
                    completionLatch.countDown();
                }
            });
        }

        Thread.sleep(durationSeconds * 1000);
        running.set(false);
        completionLatch.await(1, TimeUnit.SECONDS);
        executor.shutdown();
        
        long duration = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);

        // Then
        double operationsPerSecond = (totalOperations.get() * 1000.0) / duration;
        System.out.printf("Sustained throughput: %.2f ops/sec over %d seconds%n", 
            operationsPerSecond, durationSeconds);
        assertThat(operationsPerSecond).isGreaterThan(10000); // Minimum sustained throughput
    }
}
