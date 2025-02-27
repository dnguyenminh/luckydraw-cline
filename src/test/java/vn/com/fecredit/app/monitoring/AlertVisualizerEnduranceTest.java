package vn.com.fecredit.app.monitoring;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Long-running endurance tests for AlertVisualizer
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Tag("endurance")
class AlertVisualizerEnduranceTest {

    private static final Duration TEST_DURATION = Duration.ofHours(1);
    private static final int WORKER_THREADS = 4;
    private static final int MONITOR_INTERVAL_MS = 5000;
    private static final int ALERT_BATCH_SIZE = 100;

    @TempDir
    Path tempDir;

    private AlertVisualizer visualizer;
    private AlertHistory alertHistory;
    private SystemResourceMonitor monitor;
    private ExecutorService workers;
    private ScheduledExecutorService scheduler;
    private Map<String, AtomicInteger> statistics;
    private volatile boolean running;

    @BeforeEach
    void setUp() {
        visualizer = new AlertVisualizer();
        alertHistory = new AlertHistory(tempDir);
        monitor = new SystemResourceMonitor();
        monitor.startMonitoring(Duration.ofSeconds(1));
        workers = Executors.newFixedThreadPool(WORKER_THREADS);
        scheduler = Executors.newSingleThreadScheduledExecutor();
        statistics = new ConcurrentHashMap<>();
        statistics.put("successful", new AtomicInteger(0));
        statistics.put("failed", new AtomicInteger(0));
        statistics.put("timeouts", new AtomicInteger(0));
        running = true;
    }

    @Test
    @Order(1)
    @DisplayName("Endurance Test: Continuous Operation")
    void continuousOperationTest() throws Exception {
        Instant startTime = Instant.now();
        List<MetricSample> metrics = Collections.synchronizedList(new ArrayList<>());
        CountDownLatch testComplete = new CountDownLatch(1);

        // Start performance monitoring
        ScheduledFuture<?> monitoringTask = scheduler.scheduleAtFixedRate(
            () -> collectMetrics(startTime, metrics),
            MONITOR_INTERVAL_MS,
            MONITOR_INTERVAL_MS,
            TimeUnit.MILLISECONDS
        );

        // Start worker threads
        List<Future<?>> workerTasks = new ArrayList<>();
        for (int i = 0; i < WORKER_THREADS; i++) {
            workerTasks.add(workers.submit(() -> runWorkerLoop(startTime)));
        }

        // Wait for test duration
        Thread.sleep(TEST_DURATION.toMillis());
        running = false;

        // Cleanup and collect results
        monitoringTask.cancel(false);
        for (Future<?> task : workerTasks) {
            task.get(1, TimeUnit.MINUTES);
        }

        // Analyze results
        analyzeEnduranceResults(metrics);
    }

    private void runWorkerLoop(Instant startTime) {
        Random random = new Random();
        while (running && Duration.between(startTime, Instant.now()).compareTo(TEST_DURATION) < 0) {
            try {
                // Generate test data
                generateTestBatch(ALERT_BATCH_SIZE);

                // Generate visualization
                Path outputPath = tempDir.resolve("endurance-" + UUID.randomUUID() + ".html");
                long startNanos = System.nanoTime();
                visualizer.generateVisualization(alertHistory.getAlertSummary(), outputPath.toString());
                long duration = (System.nanoTime() - startNanos) / 1_000_000; // ms

                if (duration > 5000) { // 5 second timeout
                    statistics.get("timeouts").incrementAndGet();
                } else if (isValidVisualization(outputPath)) {
                    statistics.get("successful").incrementAndGet();
                } else {
                    statistics.get("failed").incrementAndGet();
                }

                // Random delay between operations
                Thread.sleep(random.nextInt(1000));
            } catch (Exception e) {
                statistics.get("failed").incrementAndGet();
            }
        }
    }

    private void collectMetrics(Instant startTime, List<MetricSample> metrics) {
        Runtime runtime = Runtime.getRuntime();
        long usedMemory = runtime.totalMemory() - runtime.freeMemory();
        metrics.add(new MetricSample(
            Duration.between(startTime, Instant.now()),
            usedMemory,
            statistics.get("successful").get(),
            statistics.get("failed").get(),
            statistics.get("timeouts").get()
        ));
    }

    private void analyzeEnduranceResults(List<MetricSample> metrics) {
        assertFalse(metrics.isEmpty(), "Should have collected metrics");

        // Calculate statistics
        MetricSample first = metrics.get(0);
        MetricSample last = metrics.get(metrics.size() - 1);
        long totalOperations = last.successCount - first.successCount;
        Duration totalDuration = last.timestamp;
        double operationsPerSecond = totalOperations / totalDuration.getSeconds();

        // Verify success rate
        int totalFailed = statistics.get("failed").get();
        int totalTimeouts = statistics.get("timeouts").get();
        long successRate = (totalOperations * 100) / (totalOperations + totalFailed + totalTimeouts);

        // Assert performance criteria
        assertAll("Endurance test results",
            () -> assertTrue(successRate > 95, 
                String.format("Success rate should be >95%%, was: %d%%", successRate)),
            () -> assertTrue(operationsPerSecond > 0.1, 
                String.format("Should maintain minimum throughput, was: %.2f ops/sec", operationsPerSecond)),
            () -> assertTrue(last.memoryUsage - first.memoryUsage < 100 * 1024 * 1024L,
                "Memory usage should not increase significantly")
        );
    }

    private void generateTestBatch(int size) {
        ResourceAlert.AlertSeverity[] severities = ResourceAlert.AlertSeverity.values();
        String[] resources = {"CPU", "Memory", "Disk", "Network", "Thread"};
        
        for (int i = 0; i < size; i++) {
            ResourceAlert.AlertSeverity severity = severities[i % severities.length];
            String resource = resources[i % resources.length];
            
            alertHistory.recordAlert(new ResourceAlert.AlertEvent(
                resource + " Alert #" + UUID.randomUUID(),
                severity,
                "Test message for " + resource,
                monitor.getSummary()
            ));
        }
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

    private static class MetricSample {
        final Duration timestamp;
        final long memoryUsage;
        final int successCount;
        final int failureCount;
        final int timeoutCount;

        MetricSample(Duration timestamp, long memoryUsage, int successCount, int failureCount, int timeoutCount) {
            this.timestamp = timestamp;
            this.memoryUsage = memoryUsage;
            this.successCount = successCount;
            this.failureCount = failureCount;
            this.timeoutCount = timeoutCount;
        }
    }

    @AfterEach
    void cleanup() {
        running = false;
        if (monitor != null) {
            monitor.close();
        }
        if (workers != null) {
            workers.shutdownNow();
        }
        if (scheduler != null) {
            scheduler.shutdownNow();
        }
        visualizer = null;
        alertHistory = null;
        monitor = null;
        System.gc();
    }
}
