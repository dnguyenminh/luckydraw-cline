package vn.com.fecredit.app.monitoring;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.file.*;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.IntStream;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Stress and integration tests for AlertVisualizer
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Tag("stress")
class AlertVisualizerStressTest {

    private static final int STRESS_TEST_SIZE = 100_000;
    private static final int CONCURRENT_USERS = 10;
    private static final Duration TEST_DURATION = Duration.ofMinutes(5);
    private static final int BATCH_SIZE = 1000;

    @TempDir
    Path tempDir;

    private AlertVisualizer visualizer;
    private AlertHistory alertHistory;
    private SystemResourceMonitor monitor;
    private ExecutorService executorService;

    @BeforeEach
    void setUp() {
        visualizer = new AlertVisualizer();
        alertHistory = new AlertHistory(tempDir);
        monitor = new SystemResourceMonitor();
        monitor.startMonitoring(Duration.ofSeconds(1));
        executorService = Executors.newFixedThreadPool(CONCURRENT_USERS);
    }

    @Test
    @Order(1)
    @DisplayName("Stress Test: High Volume Data Generation")
    void highVolumeStressTest() throws Exception {
        // Generate large volume of test data
        generateStressTestData(STRESS_TEST_SIZE);

        // Measure visualization time
        long startTime = System.nanoTime();
        Path outputPath = tempDir.resolve("stress-test.html");
        visualizer.generateVisualization(alertHistory.getAlertSummary(), outputPath.toString());
        long duration = (System.nanoTime() - startTime) / 1_000_000; // Convert to ms

        // Verify performance and output
        assertTrue(duration < 30_000, 
            "High volume processing should complete within 30 seconds, took: " + duration + "ms");
        assertTrue(Files.exists(outputPath) && Files.size(outputPath) > 0,
            "Should generate valid visualization file");
        assertTrue(isValidVisualization(outputPath), 
            "Should generate well-formed visualization");
    }

    @Test
    @Order(2)
    @DisplayName("Stress Test: Concurrent Visualization Generation")
    void concurrentStressTest() throws Exception {
        // Generate test data once
        generateStressTestData(BATCH_SIZE);
        AlertHistory.AlertSummary summary = alertHistory.getAlertSummary();

        // Track successful and failed operations
        ConcurrentHashMap<String, Integer> results = new ConcurrentHashMap<>();
        results.put("success", 0);
        results.put("failure", 0);

        // Create concurrent tasks
        List<CompletableFuture<Void>> tasks = new ArrayList<>();
        for (int i = 0; i < CONCURRENT_USERS; i++) {
            tasks.add(CompletableFuture.runAsync(() -> {
                try {
                    for (int j = 0; j < 50; j++) {
                        Path outputPath = tempDir.resolve("concurrent-" + UUID.randomUUID() + ".html");
                        visualizer.generateVisualization(summary, outputPath.toString());
                        if (isValidVisualization(outputPath)) {
                            results.merge("success", 1, Integer::sum);
                        } else {
                            results.merge("failure", 1, Integer::sum);
                        }
                    }
                } catch (Exception e) {
                    results.merge("failure", 1, Integer::sum);
                }
            }, executorService));
        }

        // Wait for all tasks to complete
        CompletableFuture.allOf(tasks.toArray(new CompletableFuture[0])).join();

        // Verify results
        assertEquals(0, results.get("failure"), "Should have no failed visualizations");
        assertTrue(results.get("success") > 0, "Should have successful visualizations");
    }

    @Test
    @Order(3)
    @DisplayName("Stress Test: Memory Leak Check")
    void memoryLeakStressTest() throws Exception {
        // Initial garbage collection
        System.gc();
        Thread.sleep(1000);
        long initialMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();

        // Run multiple visualization cycles
        for (int i = 0; i < 10; i++) {
            generateStressTestData(BATCH_SIZE);
            Path outputPath = tempDir.resolve("memory-test-" + i + ".html");
            visualizer.generateVisualization(alertHistory.getAlertSummary(), outputPath.toString());
            
            // Clear references
            alertHistory = new AlertHistory(tempDir);
            System.gc();
            Thread.sleep(100);
        }

        // Check final memory usage
        System.gc();
        Thread.sleep(1000);
        long finalMemory = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        long memoryDiff = finalMemory - initialMemory;

        // Allow for some memory overhead but fail if it's excessive
        assertTrue(memoryDiff < 50 * 1024 * 1024L, // 50MB limit
            String.format("Possible memory leak: %d MB increase", memoryDiff / (1024 * 1024)));
    }

    @ParameterizedTest
    @ValueSource(ints = {1000, 5000, 10000, 50000})
    @Order(4)
    @DisplayName("Stress Test: Scaling Performance")
    void scalingPerformanceTest(int dataSize) throws Exception {
        // Generate test data
        generateStressTestData(dataSize);

        // Measure visualization time
        long startTime = System.nanoTime();
        Path outputPath = tempDir.resolve("scaling-test-" + dataSize + ".html");
        visualizer.generateVisualization(alertHistory.getAlertSummary(), outputPath.toString());
        long duration = (System.nanoTime() - startTime) / 1_000_000; // Convert to ms

        // Verify scaling is roughly linear (allow some overhead)
        long expectedMaxDuration = (long)(dataSize * 0.5); // 0.5ms per item
        assertTrue(duration < expectedMaxDuration,
            String.format("Processing time (%dms) exceeded linear scaling expectation for %d items", 
                duration, dataSize));
    }

    private void generateStressTestData(int size) throws Exception {
        ResourceAlert.AlertSeverity[] severities = ResourceAlert.AlertSeverity.values();
        String[] resources = {"CPU", "Memory", "Disk", "Network", "Thread"};
        
        // Use parallel stream for faster data generation
        IntStream.range(0, size).parallel().forEach(i -> {
            try {
                ResourceAlert.AlertSeverity severity = severities[i % severities.length];
                String resource = resources[i % resources.length];
                
                alertHistory.recordAlert(new ResourceAlert.AlertEvent(
                    resource + " Alert #" + i,
                    severity,
                    "Test message for " + resource + " #" + i,
                    monitor.getSummary()
                ));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    private boolean isValidVisualization(Path path) {
        try {
            String content = Files.readString(path);
            return content.contains("<!DOCTYPE html>") &&
                   content.contains("<canvas id='severityChart'>") &&
                   content.contains("<canvas id='resourceChart'>") &&
                   !content.contains("undefined") &&
                   !content.contains("null");
        } catch (Exception e) {
            return false;
        }
    }

    @AfterEach
    void cleanup() {
        if (monitor != null) {
            monitor.close();
        }
        if (executorService != null) {
            executorService.shutdownNow();
        }
        visualizer = null;
        alertHistory = null;
        monitor = null;
        System.gc();
    }
}
