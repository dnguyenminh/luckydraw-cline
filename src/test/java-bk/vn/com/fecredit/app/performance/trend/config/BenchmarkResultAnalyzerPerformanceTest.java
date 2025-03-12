package vn.com.fecredit.app.performance.trend.config;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

@Tag("performance")
class BenchmarkResultAnalyzerPerformanceTest {
    private static final double CONFIDENCE_LEVEL = 0.95;
    private static final int LARGE_DATASET_SIZE = 1_000_000;
    private static final int CONCURRENT_THREADS = 10;
    private static final int WARMUP_ITERATIONS = 1000;

    @ParameterizedTest
    @ValueSource(ints = {1000, 10_000, 100_000, 1_000_000})
    void shouldScaleWithDatasetSize(int size) {
        // Given
        var analyzer = new BenchmarkResultAnalyzer(CONFIDENCE_LEVEL);
        long startMemory = getUsedMemory();

        // When
        IntStream.range(0, size)
            .forEach(i -> analyzer.addMetric("test", i, "units"));
        
        long endMemory = getUsedMemory();
        long startTime = System.nanoTime();
        var result = analyzer.analyze();
        long duration = System.nanoTime() - startTime;

        // Then
        long memoryPerEntry = (endMemory - startMemory) / size;
        long averageProcessingTime = duration / size;

        assertAll(
            () -> assertTrue(memoryPerEntry < 200,
                "Memory per entry should be less than 200 bytes, was: " + memoryPerEntry),
            () -> assertTrue(averageProcessingTime < 1000,
                "Average processing time per entry should be less than 1μs, was: " + 
                averageProcessingTime + "ns")
        );
    }

    @Test
    void shouldHandleConcurrentAccess() throws InterruptedException {
        // Given
        var analyzer = new BenchmarkResultAnalyzer(CONFIDENCE_LEVEL);
        ExecutorService executor = Executors.newFixedThreadPool(CONCURRENT_THREADS);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch completionLatch = new CountDownLatch(CONCURRENT_THREADS);
        List<Future<BenchmarkResultAnalyzer.AnalysisReport>> futures = new ArrayList<>();

        // When
        for (int i = 0; i < CONCURRENT_THREADS; i++) {
            final int threadId = i;
            futures.add(executor.submit(() -> {
                startLatch.await(); // Synchronize start
                try {
                    for (int j = 0; j < 1000; j++) {
                        analyzer.addMetric("metric" + threadId, j, "units");
                    }
                    return analyzer.analyze();
                } finally {
                    completionLatch.countDown();
                }
            }));
        }

        long start = System.nanoTime();
        startLatch.countDown(); // Start all threads
        boolean completed = completionLatch.await(10, TimeUnit.SECONDS);
        long duration = System.nanoTime() - start;

        // Then
        assertTrue(completed, "All threads should complete within timeout");
        for (Future<BenchmarkResultAnalyzer.AnalysisReport> future : futures) {
            assertDoesNotThrow(() -> future.get(1, TimeUnit.SECONDS),
                "Each thread should complete successfully");
        }
        assertTrue(duration < TimeUnit.SECONDS.toNanos(10),
            "Concurrent operations should complete within reasonable time");

        executor.shutdown();
    }

    @Test
    void shouldMaintainPerformanceOverTime() {
        // Given
        var analyzer = new BenchmarkResultAnalyzer(CONFIDENCE_LEVEL);
        List<Long> durations = new ArrayList<>();

        // Warm up
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            analyzer.addMetric("warmup", i, "units");
            if (i % 100 == 0) {
                analyzer.analyze();
            }
        }

        // When
        for (int batch = 0; batch < 10; batch++) {
            for (int i = 0; i < 1000; i++) {
                analyzer.addMetric("test", i, "units");
            }
            long start = System.nanoTime();
            analyzer.analyze();
            durations.add(System.nanoTime() - start);
        }

        // Then
        double avgDuration = durations.stream().mapToLong(Long::valueOf).average().orElse(0);
        double stdDev = calculateStdDev(durations, avgDuration);
        double variationCoefficient = (stdDev / avgDuration) * 100;

        assertTrue(variationCoefficient < 15.0,
            "Performance variation should be less than 15%, was: " + 
            String.format("%.2f%%", variationCoefficient));
    }

    @Test
    void shouldHandleMemoryPressure() {
        // Given
        var analyzer = new BenchmarkResultAnalyzer(CONFIDENCE_LEVEL);
        List<BenchmarkResultAnalyzer.AnalysisReport> reports = new ArrayList<>();
        Runtime runtime = Runtime.getRuntime();
        long initialMemory = runtime.totalMemory() - runtime.freeMemory();

        // When
        try {
            for (int i = 0; i < LARGE_DATASET_SIZE; i++) {
                analyzer.addMetric("memory-test", i, "units");
                if (i % 10000 == 0) {
                    reports.add(analyzer.analyze());
                    if (runtime.freeMemory() < runtime.totalMemory() * 0.2) {
                        System.gc(); // Request GC when memory is low
                    }
                }
            }
        } finally {
            reports.clear(); // Clean up
            System.gc();
        }

        long finalMemory = runtime.totalMemory() - runtime.freeMemory();
        long memoryLeak = finalMemory - initialMemory;

        // Then
        assertTrue(memoryLeak < 10 * 1024 * 1024, // 10MB
            "Memory leak should be less than 10MB, was: " + 
            String.format("%.2fMB", memoryLeak / (1024.0 * 1024.0)));
    }

    private long getUsedMemory() {
        System.gc();
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }

    private double calculateStdDev(List<Long> values, double mean) {
        return Math.sqrt(values.stream()
            .mapToDouble(v -> Math.pow(v - mean, 2))
            .average()
            .orElse(0.0));
    }
}
