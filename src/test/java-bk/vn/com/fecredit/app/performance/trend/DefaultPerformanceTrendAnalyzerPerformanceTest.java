package vn.com.fecredit.app.performance.trend;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Performance and stress tests for DefaultPerformanceTrendAnalyzer.
 * Tests throughput, memory usage, and behavior under load.
 */
@Tag("performance")
class DefaultPerformanceTrendAnalyzerPerformanceTest {
    
    private static final int WARM_UP_ITERATIONS = 1000;
    private static final int BENCHMARK_ITERATIONS = 10000;
    private static final int LARGE_DATASET_SIZE = 10000;
    private static final int CONCURRENT_THREADS = 10;

    @Test
    void shouldHandleLargeDataset() {
        // Given
        var analyzer = new DefaultPerformanceTrendAnalyzer();
        var dataPoints = generateLargeDataset(LARGE_DATASET_SIZE);
        
        // When
        long start = System.nanoTime();
        var result = analyzer.analyzeTrend(dataPoints);
        long duration = System.nanoTime() - start;

        // Then
        assertNotNull(result, "Should process large dataset");
        assertTrue(duration < TimeUnit.SECONDS.toNanos(1),
            "Should process " + LARGE_DATASET_SIZE + " points within 1 second");
    }

    @ParameterizedTest
    @ValueSource(ints = {100, 1000, 10000})
    void shouldMaintainPerformanceWithIncreasingDataSize(int dataSize) {
        // Given
        var analyzer = new DefaultPerformanceTrendAnalyzer();
        var dataPoints = generateLargeDataset(dataSize);
        
        // Warm up
        for (int i = 0; i < 100; i++) {
            analyzer.analyzeTrend(dataPoints);
        }

        // When
        long start = System.nanoTime();
        analyzer.analyzeTrend(dataPoints);
        long duration = System.nanoTime() - start;

        // Then
        double pointsPerSecond = (double) dataSize / TimeUnit.NANOSECONDS.toSeconds(duration);
        assertTrue(pointsPerSecond > 10000,
            "Should process at least 10K points per second, got " + pointsPerSecond);
    }

    @Test
    void shouldHandleHighConcurrency() throws InterruptedException {
        // Given
        var analyzer = new DefaultPerformanceTrendAnalyzer();
        var dataPoints = generateLargeDataset(1000);
        var executor = Executors.newFixedThreadPool(CONCURRENT_THREADS);
        var latch = new CountDownLatch(CONCURRENT_THREADS);
        List<Future<TrendResult>> futures = new ArrayList<>();

        // When
        long start = System.nanoTime();
        for (int i = 0; i < CONCURRENT_THREADS; i++) {
            futures.add(executor.submit(() -> {
                try {
                    return analyzer.analyzeTrend(dataPoints);
                } finally {
                    latch.countDown();
                }
            }));
        }

        // Then
        latch.await(5, TimeUnit.SECONDS);
        long duration = System.nanoTime() - start;
        executor.shutdown();

        assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS),
            "All tasks should complete within timeout");
        
        var results = futures.stream()
            .map(f -> {
                try {
                    return f.get();
                } catch (Exception e) {
                    fail("Task execution failed: " + e.getMessage());
                    return null;
                }
            })
            .toList();

        assertEquals(CONCURRENT_THREADS, results.size(),
            "All concurrent requests should complete");
        
        var firstResult = results.get(0);
        results.forEach(result -> 
            assertEquals(firstResult.direction(), result.direction(),
                "All concurrent results should be consistent")
        );

        double avgTimePerRequest = duration / (double) CONCURRENT_THREADS;
        assertTrue(avgTimePerRequest < TimeUnit.MILLISECONDS.toNanos(100),
            "Average processing time should be under 100ms");
    }

    @Test
    void shouldHandleMemoryEfficiently() {
        // Given
        var analyzer = new DefaultPerformanceTrendAnalyzer();
        var dataPoints = generateLargeDataset(LARGE_DATASET_SIZE);
        
        // When
        long memoryBefore = getUsedMemory();
        var result = analyzer.analyzeTrend(dataPoints);
        long memoryAfter = getUsedMemory();

        // Then
        long memoryUsed = memoryAfter - memoryBefore;
        assertTrue(memoryUsed < 10 * 1024 * 1024,
            "Memory usage should be under 10MB, was: " + (memoryUsed / (1024 * 1024)) + "MB");
    }

    @Test
    void shouldMaintainPerformanceOverTime() {
        // Given
        var analyzer = new DefaultPerformanceTrendAnalyzer();
        var dataPoints = generateLargeDataset(1000);
        
        // Warm up
        for (int i = 0; i < WARM_UP_ITERATIONS; i++) {
            analyzer.analyzeTrend(dataPoints);
        }

        // When
        List<Long> durations = new ArrayList<>();
        for (int i = 0; i < BENCHMARK_ITERATIONS; i++) {
            long start = System.nanoTime();
            analyzer.analyzeTrend(dataPoints);
            durations.add(System.nanoTime() - start);
        }

        // Then
        double avgDuration = durations.stream().mapToLong(Long::valueOf).average().orElse(0);
        double stdDev = calculateStdDev(durations, avgDuration);
        
        assertTrue(stdDev / avgDuration < 0.2,
            "Performance variance should be under 20%, was: " + (stdDev / avgDuration * 100) + "%");
    }

    private List<DataPoint> generateLargeDataset(int size) {
        return IntStream.range(0, size)
            .mapToObj(i -> new DataPoint(
                LocalDateTime.now().plusMinutes(i),
                100.0 + Math.sin(i * 0.1) * 10,
                0.0
            ))
            .toList();
    }

    private long getUsedMemory() {
        System.gc();
        Runtime rt = Runtime.getRuntime();
        return rt.totalMemory() - rt.freeMemory();
    }

    private double calculateStdDev(List<Long> values, double mean) {
        return Math.sqrt(values.stream()
            .mapToDouble(v -> Math.pow(v - mean, 2))
            .average()
            .orElse(0.0));
    }
}
