package vn.com.fecredit.app.monitoring;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.OptionalDouble;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
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
import org.junit.jupiter.api.io.TempDir;

/**
 * Integration and Performance tests for PerformanceProfilerVisualizer
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PerformanceProfilerVisualizerIT {

    private PerformanceProfilerVisualizer visualizer;
    private PerformanceProfiler profiler;
    private static final int LARGE_DATASET_SIZE = 10_000;
    private static final int CONCURRENT_USERS = 10;
    private static final int OPERATIONS_PER_USER = 100;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        visualizer = new PerformanceProfilerVisualizer();
        profiler = new PerformanceProfiler();
    }

    @Test
    @Order(1)
    @DisplayName("Should handle large datasets efficiently")
    void shouldHandleLargeDatasets() throws Exception {
        // Generate large dataset
        long startGeneration = System.nanoTime();
        generateLargeDataset();
        long generationTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startGeneration);
        assertTrue(generationTime < 30000, "Data generation should complete within 30 seconds");
        
        // Measure visualization performance
        long startVisualization = System.nanoTime();
        Path outputPath = tempDir.resolve("large-dataset.html");
        
        visualizer.generateVisualization(profiler.getAndResetSummary(), outputPath.toString());
        
        long visualizationTime = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startVisualization);
        
        // Verify performance constraints
        assertTrue(visualizationTime < 5000, 
            "Visualization should complete within 5 seconds, took: " + visualizationTime + "ms");
        assertTrue(Files.size(outputPath) < 10 * 1024 * 1024, 
            "Output file should be less than 10MB");
        
        // Verify file content
        String content = Files.readString(outputPath);
        assertTrue(content.contains("timelineChart"));
        assertTrue(content.contains("cpuChart"));
        assertTrue(content.contains("memoryChart"));
    }

    @Test
    @Order(2)
    @DisplayName("Should maintain performance under concurrent load")
    void shouldHandleConcurrentLoad() throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(CONCURRENT_USERS);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch completionLatch = new CountDownLatch(CONCURRENT_USERS);
        ConcurrentHashMap<Integer, Long> userTimes = new ConcurrentHashMap<>();
        List<Exception> errors = Collections.synchronizedList(new ArrayList<>());

        // Generate test data
        generateLargeDataset();
        PerformanceProfiler.ProfilerSummary summary = profiler.getAndResetSummary();

        // Launch concurrent users
        for (int i = 0; i < CONCURRENT_USERS; i++) {
            final int userId = i;
            executor.submit(() -> {
                try {
                    startLatch.await(); // Wait for all threads to be ready
                    long start = System.nanoTime();
                    
                    for (int j = 0; j < OPERATIONS_PER_USER; j++) {
                        Path outputPath = tempDir.resolve(String.format("concurrent-test-%d-%d.html", userId, j));
                        visualizer.generateVisualization(summary, outputPath.toString());
                        assertTrue(Files.exists(outputPath), "Report file should be created");
                    }
                    
                    userTimes.put(userId, TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start));
                } catch (Exception e) {
                    errors.add(e);
                } finally {
                    completionLatch.countDown();
                }
            });
        }

        // Start all threads simultaneously
        long testStart = System.nanoTime();
        startLatch.countDown();
        
        // Wait for completion with timeout
        assertTrue(completionLatch.await(5, TimeUnit.MINUTES), 
            "Concurrent test should complete within timeout");
        long totalDuration = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - testStart);
        
        executor.shutdown();
        assertTrue(executor.awaitTermination(1, TimeUnit.MINUTES), 
            "Executor should shut down cleanly");
        
        // Verify results
        assertTrue(errors.isEmpty(), 
            "No errors should occur during concurrent execution: " + errors);
        
        OptionalDouble avgTime = userTimes.values().stream()
            .mapToLong(Long::longValue)
            .average();
        long maxTime = userTimes.values().stream()
            .mapToLong(Long::longValue)
            .max()
            .orElse(0L);
        
        assertTrue(avgTime.isPresent(), "Should have valid average time");
        assertTrue(avgTime.getAsDouble() < 30000, 
            "Average user time should be under 30 seconds: " + avgTime.getAsDouble());
        assertTrue(maxTime < 60000, 
            "Maximum user time should be under 60 seconds: " + maxTime);
        assertTrue(totalDuration < 300000, 
            "Total test duration should be under 5 minutes: " + totalDuration);
    }

    @Test
    @Order(3)
    @DisplayName("Should handle memory efficiently")
    void shouldManageMemoryEfficiently() throws Exception {
        Runtime runtime = Runtime.getRuntime();
        System.gc();
        Thread.sleep(100); // Allow GC to complete
        long initialMemory = runtime.totalMemory() - runtime.freeMemory();
        
        // Generate and visualize multiple large datasets
        for (int i = 0; i < 5; i++) {
            generateLargeDataset();
            Path outputPath = tempDir.resolve("memory-test-" + i + ".html");
            visualizer.generateVisualization(profiler.getAndResetSummary(), outputPath.toString());
            
            // Verify each file
            assertTrue(Files.exists(outputPath), "Report file should be created");
            String content = Files.readString(outputPath);
            assertTrue(content.contains("timelineChart"), "Should contain timeline chart");
            assertTrue(content.contains("cpuChart"), "Should contain CPU chart");
            assertTrue(content.contains("memoryChart"), "Should contain memory chart");
            
            System.gc();
            Thread.sleep(100); // Allow GC to complete
        }

        long finalMemory = runtime.totalMemory() - runtime.freeMemory();
        long memoryGrowth = finalMemory - initialMemory;
        
        assertTrue(memoryGrowth < 50 * 1024 * 1024, 
            "Memory growth should be less than 50MB, was: " + (memoryGrowth / (1024 * 1024)) + "MB");
    }

    @Test
    @Order(4)
    @DisplayName("Should maintain data consistency")
    void shouldMaintainDataConsistency() throws Exception {
        // Record a sequence of operations
        for (int i = 0; i < 100; i++) {
            String label = "test-operation-" + i;
            double elapsed = i * 10.0;
            double memory = 100.0 + i;
            
            profiler.recordLatency(label, elapsed);
            profiler.recordMemoryUsage(label, memory);
        }
        
        PerformanceProfiler.ProfilerSummary summary = profiler.getAndResetSummary();
        Path outputPath = tempDir.resolve("consistency-test.html");
        visualizer.generateVisualization(summary, outputPath.toString());
        
        String content = Files.readString(outputPath);
        
        // Verify data consistency
        for (int i = 0; i < 100; i++) {
            String label = "test-operation-" + i;
            assertTrue(content.contains(label), "Should contain operation label: " + label);
        }
        
        // Verify summary reset
        assertTrue(profiler.getAndResetSummary().getSnapshots().isEmpty(), 
            "Summary should be empty after reset");
    }

    private void generateLargeDataset() throws InterruptedException {
        Random random = new Random();
        for (int i = 0; i < LARGE_DATASET_SIZE; i++) {
            String label = "operation-" + (i % 100);
            profiler.recordLatency(label, random.nextDouble() * 1000);
            profiler.recordMemoryUsage(label, random.nextDouble() * 1024);
            
            if (i % 1000 == 0) {
                Thread.sleep(1); // Simulate real-world timing
            }
        }
    }

    @AfterEach
    void cleanup() {
        profiler = null;
        visualizer = null;
        System.gc();
    }
}
