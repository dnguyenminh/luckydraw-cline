package vn.com.fecredit.app.monitoring;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Stress tests for the performance profiling components
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PerformanceProfilerStressTest {
    
    private static final int VERY_LARGE_DATASET_SIZE = 100_000;
    private static final int MAX_CONCURRENT_THREADS = 50;
    private static final int STRESS_TEST_DURATION_SECONDS = 60;
    private static final int BURST_SIZE = 1000;
    private static final int WARMUP_ITERATIONS = 1000;

    @TempDir
    Path tempDir;
    
    private PerformanceProfiler profiler;
    private PerformanceProfilerVisualizer visualizer;
    private Random random;
    private ExecutorService executor;
    private List<Path> generatedFiles;

    @BeforeEach
    void setUp() {
        profiler = new PerformanceProfiler();
        visualizer = new PerformanceProfilerVisualizer();
        random = new Random();
        executor = Executors.newCachedThreadPool();
        generatedFiles = new CopyOnWriteArrayList<>();
    }

    @Test
    @Order(1)
    @DisplayName("Should handle sustained high load")
    void shouldHandleSustainedLoad() throws Exception {
        AtomicInteger operationCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);
        CountDownLatch completionLatch = new CountDownLatch(MAX_CONCURRENT_THREADS);
        
        // Warm up
        performWarmup();
        
        // Start stress test
        long startTime = System.currentTimeMillis();
        List<Future<?>> futures = new ArrayList<>();
        
        for (int i = 0; i < MAX_CONCURRENT_THREADS; i++) {
            futures.add(executor.submit(() -> {
                try {
                    while (System.currentTimeMillis() - startTime < STRESS_TEST_DURATION_SECONDS * 1000) {
                        performStressOperation();
                        operationCount.incrementAndGet();
                    }
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                } finally {
                    completionLatch.countDown();
                }
            }));
        }

        // Wait for completion
        assertTrue(completionLatch.await(STRESS_TEST_DURATION_SECONDS + 30, TimeUnit.SECONDS),
            "Stress test should complete within timeout");
        
        // Verify results
        assertEquals(0, errorCount.get(), "Should have no errors during stress test");
        assertTrue(operationCount.get() > MAX_CONCURRENT_THREADS * 1000,
            "Should process sufficient operations: " + operationCount.get());
        
        // Verify system stability
        verifySystemStability();
    }

    @Test
    @Order(2)
    @DisplayName("Should handle burst loads")
    void shouldHandleBurstLoad() throws Exception {
        int numberOfBursts = 10;
        CountDownLatch burstLatch = new CountDownLatch(numberOfBursts * BURST_SIZE);
        AtomicInteger errorCount = new AtomicInteger(0);
        
        for (int burst = 0; burst < numberOfBursts; burst++) {
            // Create burst of concurrent requests
            for (int i = 0; i < BURST_SIZE; i++) {
                executor.submit(() -> {
                    try {
                        performBurstOperation();
                    } catch (Exception e) {
                        errorCount.incrementAndGet();
                    } finally {
                        burstLatch.countDown();
                    }
                });
            }
            
            // Short pause between bursts
            Thread.sleep(100);
        }
        
        assertTrue(burstLatch.await(5, TimeUnit.MINUTES), 
            "Burst operations should complete within timeout");
        assertEquals(0, errorCount.get(), 
            "Should handle burst load without errors");
    }

    @Test
    @Order(3)
    @DisplayName("Should maintain performance under memory pressure")
    void shouldHandleMemoryPressure() throws Exception {
        // Create memory pressure
        List<byte[]> memoryLoad = new ArrayList<>();
        try {
            for (int i = 0; i < 10; i++) {
                memoryLoad.add(new byte[10 * 1024 * 1024]); // Allocate 10MB chunks
            }
            
            // Run operations under memory pressure
            long startTime = System.currentTimeMillis();
            int operationCount = 0;
            
            while (System.currentTimeMillis() - startTime < 30000) { // 30 seconds test
                performMemoryPressureOperation();
                operationCount++;
                
                if (operationCount % 100 == 0) {
                    System.gc(); // Trigger GC periodically
                }
            }
            
            // Verify profiler data integrity
            PerformanceProfiler.ProfilerSummary summary = profiler.getAndResetSummary();
            assertNotNull(summary);
            assertFalse(summary.getSnapshots().isEmpty());
            
        } finally {
            memoryLoad.clear();
            System.gc();
        }
    }

    @Test
    @Order(4)
    @DisplayName("Should handle large data visualization under load")
    void shouldHandleLargeDataVisualization() throws Exception {
        // Generate very large dataset
        for (int i = 0; i < VERY_LARGE_DATASET_SIZE; i++) {
            profiler.recordLatency("large-op-" + i, random.nextDouble() * 1000);
            profiler.recordMemoryUsage("large-op-" + i, random.nextDouble() * 1024);
        }
        
        PerformanceProfiler.ProfilerSummary summary = profiler.getAndResetSummary();
        
        // Measure visualization time
        long startTime = System.nanoTime();
        Path outputPath = tempDir.resolve("large-visualization.html");
        visualizer.generateVisualization(summary, outputPath.toString());
        long duration = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
        
        assertTrue(duration < 10000, 
            "Large dataset visualization should complete within 10 seconds: " + duration + "ms");
        assertTrue(Files.exists(outputPath) && Files.size(outputPath) > 0,
            "Should generate valid visualization file");
    }

    private void performWarmup() throws Exception {
        for (int i = 0; i < WARMUP_ITERATIONS; i++) {
            profiler.recordLatency("warmup-" + i, random.nextDouble() * 100);
            if (i % 100 == 0) {
                visualizer.generateVisualization(
                    profiler.getAndResetSummary(),
                    tempDir.resolve("warmup-" + i + ".html").toString()
                );
            }
        }
    }

    private void performStressOperation() throws Exception {
        String label = "stress-" + random.nextInt(1000);
        profiler.recordLatency(label, random.nextDouble() * 1000);
        profiler.recordMemoryUsage(label, random.nextDouble() * 512);
        
        if (random.nextInt(100) < 5) { // 5% chance to generate visualization
            Path outputPath = tempDir.resolve("stress-" + System.nanoTime() + ".html");
            visualizer.generateVisualization(profiler.getAndResetSummary(), outputPath.toString());
            generatedFiles.add(outputPath);
        }
    }

    private void performBurstOperation() throws Exception {
        String label = "burst-" + System.nanoTime();
        profiler.recordLatency(label, random.nextDouble() * 500);
        profiler.recordMemoryUsage(label, random.nextDouble() * 256);
        
        if (random.nextInt(100) < 10) { // 10% chance to generate visualization
            Path outputPath = tempDir.resolve("burst-" + System.nanoTime() + ".html");
            visualizer.generateVisualization(profiler.getAndResetSummary(), outputPath.toString());
            generatedFiles.add(outputPath);
        }
    }

    private void performMemoryPressureOperation() throws Exception {
        byte[] tempBuffer = new byte[1024 * 1024]; // Allocate 1MB
        Arrays.fill(tempBuffer, (byte) random.nextInt(256));
        
        String label = "memory-" + System.nanoTime();
        profiler.recordLatency(label, random.nextDouble() * 200);
        profiler.recordMemoryUsage(label, random.nextDouble() * 128);
        
        if (random.nextInt(100) < 2) { // 2% chance to generate visualization
            Path outputPath = tempDir.resolve("memory-" + System.nanoTime() + ".html");
            visualizer.generateVisualization(profiler.getAndResetSummary(), outputPath.toString());
            generatedFiles.add(outputPath);
        }
    }

    private void verifySystemStability() throws Exception {
        // Verify file integrity
        for (Path file : generatedFiles) {
            assertTrue(Files.exists(file), "Generated file should exist: " + file);
            assertTrue(Files.size(file) > 0, "Generated file should not be empty: " + file);
            String content = Files.readString(file);
            assertTrue(content.contains("<!DOCTYPE html>"), "Should be valid HTML: " + file);
        }
        
        // Verify profiler state
        PerformanceProfiler.ProfilerSummary finalSummary = profiler.getAndResetSummary();
        assertNotNull(finalSummary, "Should get valid final summary");
        
        // Verify memory usage
        Runtime runtime = Runtime.getRuntime();
        long usedMemory = runtime.totalMemory() - runtime.freeMemory();
        assertTrue(usedMemory < runtime.maxMemory() * 0.9,
            "Memory usage should be within reasonable limits: " + 
            (usedMemory / (1024 * 1024)) + "MB");
    }

    @AfterEach
    void cleanup() throws Exception {
        executor.shutdownNow();
        assertTrue(executor.awaitTermination(30, TimeUnit.SECONDS),
            "Executor should shut down cleanly");
        
        profiler = null;
        visualizer = null;
        generatedFiles.clear();
        System.gc();
    }
}
