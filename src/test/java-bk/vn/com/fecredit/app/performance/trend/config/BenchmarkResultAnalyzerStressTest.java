package vn.com.fecredit.app.performance.trend.config;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@Tag("stress")
class BenchmarkResultAnalyzerStressTest {

    private static final double CONFIDENCE_LEVEL = 0.95;
    private static final int MAX_THREADS = 50;
    private static final int OPERATIONS_PER_THREAD = 10_000;
    private static final int STRESS_DURATION_SECONDS = 30;
    private static final Random RANDOM = new Random();

    @Test
    @Timeout(value = 60) // Timeout after 60 seconds
    void shouldHandleHighConcurrency() throws InterruptedException {
        // Given
        var analyzer = new BenchmarkResultAnalyzer(CONFIDENCE_LEVEL);
        ExecutorService executor = Executors.newFixedThreadPool(MAX_THREADS);
        CountDownLatch startLatch = new CountDownLatch(1);
        AtomicInteger successfulOperations = new AtomicInteger(0);
        AtomicInteger failedOperations = new AtomicInteger(0);
        CyclicBarrier barrier = new CyclicBarrier(MAX_THREADS);

        // When
        List<Future<?>> futures = new ArrayList<>();
        for (int i = 0; i < MAX_THREADS; i++) {
            futures.add(executor.submit(() -> {
                try {
                    barrier.await(); // Synchronize thread start
                    startLatch.await();
                    
                    for (int j = 0; j < OPERATIONS_PER_THREAD; j++) {
                        try {
                            analyzer.addMetric("stress-metric", RANDOM.nextDouble() * 1000, "units");
                            if (j % 100 == 0) {
                                analyzer.analyze();
                            }
                            successfulOperations.incrementAndGet();
                        } catch (Exception e) {
                            failedOperations.incrementAndGet();
                        }
                    }
                } catch (Exception e) {
                    failedOperations.incrementAndGet();
                }
            }));
        }

        startLatch.countDown();
        executor.shutdown();
        boolean completed = executor.awaitTermination(60, TimeUnit.SECONDS);

        // Then
        assertTrue(completed, "All threads should complete within timeout");
        assertEquals(0, failedOperations.get(), "Should have no failed operations");
        assertEquals(MAX_THREADS * OPERATIONS_PER_THREAD, successfulOperations.get(),
            "Should complete all operations");
    }

    @Test
    @Timeout(value = 60)
    void shouldHandleRandomWorkload() throws InterruptedException {
        // Given
        var analyzer = new BenchmarkResultAnalyzer(CONFIDENCE_LEVEL);
        ExecutorService executor = Executors.newFixedThreadPool(MAX_THREADS);
        AtomicBoolean running = new AtomicBoolean(true);
        AtomicInteger totalOperations = new AtomicInteger(0);
        ConcurrentHashMap<String, AtomicInteger> operationCounts = new ConcurrentHashMap<>();

        // When
        List<Future<?>> futures = new ArrayList<>();
        for (int i = 0; i < MAX_THREADS; i++) {
            futures.add(executor.submit(() -> {
                while (running.get()) {
                    try {
                        String operation = randomOperation();
                        operationCounts.computeIfAbsent(operation, k -> new AtomicInteger()).incrementAndGet();
                        
                        switch (operation) {
                            case "add" -> analyzer.addMetric("random", RANDOM.nextDouble(), "units");
                            case "analyze" -> analyzer.analyze();
                            case "validate" -> analyzer.addValidator("random",
                                v -> v < RANDOM.nextDouble() * 1000, "Random threshold");
                        }
                        totalOperations.incrementAndGet();
                    } catch (Exception e) {
                        // Log but continue
                    }
                }
            }));
        }

        // Run for specified duration
        Thread.sleep(TimeUnit.SECONDS.toMillis(STRESS_DURATION_SECONDS));
        running.set(false);
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        // Then
        System.out.printf("Total operations: %d%n", totalOperations.get());
        operationCounts.forEach((op, count) ->
            System.out.printf("%s: %d operations%n", op, count.get()));
        
        assertTrue(totalOperations.get() > 0, "Should perform some operations");
        assertTrue(operationCounts.get("analyze").get() > 0, "Should perform some analyses");
    }

    @Test
    @Timeout(value = 60)
    void shouldHandleMemoryStress() throws InterruptedException {
        // Given
        var analyzer = new BenchmarkResultAnalyzer(CONFIDENCE_LEVEL);
        ExecutorService executor = Executors.newFixedThreadPool(MAX_THREADS);
        AtomicBoolean running = new AtomicBoolean(true);
        List<List<BenchmarkResultAnalyzer.AnalysisReport>> reports = new CopyOnWriteArrayList<>();

        // When
        List<Future<?>> futures = new ArrayList<>();
        for (int i = 0; i < MAX_THREADS; i++) {
            futures.add(executor.submit(() -> {
                List<BenchmarkResultAnalyzer.AnalysisReport> threadReports = new ArrayList<>();
                while (running.get()) {
                    try {
                        // Generate large amount of data
                        for (int j = 0; j < 1000; j++) {
                            analyzer.addMetric("memory-stress", RANDOM.nextDouble(), "units");
                        }
                        threadReports.add(analyzer.analyze());
                        
                        // Simulate memory pressure
                        if (threadReports.size() > 100) {
                            threadReports.subList(0, 50).clear();
                        }
                    } catch (OutOfMemoryError e) {
                        running.set(false);
                        throw e;
                    }
                }
                reports.add(threadReports);
            }));
        }

        // Run for specified duration
        Thread.sleep(TimeUnit.SECONDS.toMillis(STRESS_DURATION_SECONDS));
        running.set(false);
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        // Then
        assertFalse(reports.isEmpty(), "Should generate some reports");
        reports.clear(); // Clean up
        System.gc();
    }

    @Test
    @Timeout(value = 60)
    void shouldHandleBurstyWorkload() throws InterruptedException {
        // Given
        var analyzer = new BenchmarkResultAnalyzer(CONFIDENCE_LEVEL);
        ExecutorService executor = Executors.newFixedThreadPool(MAX_THREADS);
        AtomicInteger burstCount = new AtomicInteger(0);
        int numberOfBursts = 5;

        // When
        for (int burst = 0; burst < numberOfBursts; burst++) {
            CountDownLatch burstLatch = new CountDownLatch(MAX_THREADS);
            
            // Create burst of concurrent operations
            for (int i = 0; i < MAX_THREADS; i++) {
                executor.submit(() -> {
                    try {
                        for (int j = 0; j < 1000; j++) {
                            analyzer.addMetric("burst", RANDOM.nextDouble(), "units");
                        }
                        analyzer.analyze();
                        burstCount.incrementAndGet();
                    } finally {
                        burstLatch.countDown();
                    }
                });
            }

            // Wait for burst to complete
            assertTrue(burstLatch.await(10, TimeUnit.SECONDS),
                "Burst should complete within timeout");
            
            // Small delay between bursts
            Thread.sleep(1000);
        }

        executor.shutdown();
        assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS),
            "Should complete all operations");

        // Then
        assertEquals(numberOfBursts * MAX_THREADS, burstCount.get(),
            "Should complete all burst operations");
    }

    private String randomOperation() {
        return switch (RANDOM.nextInt(10)) {
            case 0, 1 -> "analyze";
            case 2 -> "validate";
            default -> "add";
        };
    }
}
