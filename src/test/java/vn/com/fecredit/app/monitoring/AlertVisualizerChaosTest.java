package vn.com.fecredit.app.monitoring;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.*;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Chaos testing for AlertVisualizer to verify system resilience
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Tag("chaos")
class AlertVisualizerChaosTest {

    private static final Duration TEST_DURATION = Duration.ofMinutes(30);
    private static final int CHAOS_THREADS = 3;
    private static final int CHECK_INTERVAL_MS = 1000;

    @TempDir
    Path tempDir;

    private AlertVisualizer visualizer;
    private AlertHistory alertHistory;
    private SystemResourceMonitor monitor;
    private ExecutorService chaosExecutor;
    private ScheduledExecutorService scheduler;
    private volatile boolean running;
    private final Map<String, AtomicInteger> statistics = new ConcurrentHashMap<>();

    @BeforeEach
    void setUp() {
        visualizer = new AlertVisualizer();
        alertHistory = new AlertHistory(tempDir);
        monitor = new SystemResourceMonitor();
        monitor.startMonitoring(Duration.ofSeconds(1));
        chaosExecutor = Executors.newFixedThreadPool(CHAOS_THREADS);
        scheduler = Executors.newScheduledThreadPool(1);
        running = true;
        initializeStatistics();
    }

    private void initializeStatistics() {
        statistics.put("visualizations", new AtomicInteger(0));
        statistics.put("failures", new AtomicInteger(0));
        statistics.put("recoveries", new AtomicInteger(0));
        statistics.put("chaos_actions", new AtomicInteger(0));
    }

    @Test
    @Order(1)
    @DisplayName("Chaos Test: System Resilience")
    void systemResilienceTest() throws Exception {
        List<Future<?>> chaosTasks = new ArrayList<>();
        CountDownLatch testComplete = new CountDownLatch(1);

        // Start chaos agents
        chaosTasks.add(chaosExecutor.submit(this::memoryPressureAgent));
        chaosTasks.add(chaosExecutor.submit(this::fileSystemChaosAgent));
        chaosTasks.add(chaosExecutor.submit(this::cpuPressureAgent));

        // Start visualization worker
        Future<?> visualizationTask = chaosExecutor.submit(this::visualizationWorker);

        // Monitor system state
        ScheduledFuture<?> monitorTask = scheduler.scheduleAtFixedRate(
            this::monitorSystemState,
            CHECK_INTERVAL_MS,
            CHECK_INTERVAL_MS,
            TimeUnit.MILLISECONDS
        );

        // Run test for specified duration
        Thread.sleep(TEST_DURATION.toMillis());
        running = false;

        // Cleanup and verify results
        monitorTask.cancel(false);
        visualizationTask.get(1, TimeUnit.MINUTES);
        for (Future<?> task : chaosTasks) {
            task.get(1, TimeUnit.MINUTES);
        }

        analyzeResults();
    }

    private void memoryPressureAgent() {
        List<byte[]> memoryLeaks = new ArrayList<>();
        Random random = new Random();

        while (running) {
            try {
                if (random.nextBoolean()) {
                    // Allocate memory
                    memoryLeaks.add(new byte[1024 * 1024]); // 1MB
                } else {
                    // Release memory
                    int releaseCount = Math.min(10, memoryLeaks.size());
                    for (int i = 0; i < releaseCount && !memoryLeaks.isEmpty(); i++) {
                        memoryLeaks.remove(memoryLeaks.size() - 1);
                    }
                }
                statistics.get("chaos_actions").incrementAndGet();
                Thread.sleep(random.nextInt(1000));
            } catch (Exception e) {
                memoryLeaks.clear();
                System.gc();
            }
        }
        memoryLeaks.clear();
    }

    private void fileSystemChaosAgent() {
        Random random = new Random();

        while (running) {
            try {
                if (random.nextBoolean()) {
                    // Create temporary files
                    Path chaosFile = tempDir.resolve("chaos-" + UUID.randomUUID() + ".tmp");
                    Files.write(chaosFile, new byte[1024 * 1024]); // 1MB file
                } else {
                    // Delete random files
                    try (Stream<Path> stream = Files.list(tempDir)) {
                        stream.filter(p -> p.toString().contains("chaos-"))
                             .forEach(p -> {
                                 try {
                                     Files.deleteIfExists(p);
                                 } catch (IOException ignored) {}
                             });
                    }
                }
                statistics.get("chaos_actions").incrementAndGet();
                Thread.sleep(random.nextInt(500));
            } catch (Exception ignored) {}
        }
    }

    private void cpuPressureAgent() {
        Random random = new Random();

        while (running) {
            try {
                if (random.nextBoolean()) {
                    // Create CPU pressure
                    Instant end = Instant.now().plusMillis(random.nextInt(1000));
                    while (Instant.now().isBefore(end)) {
                        Math.pow(random.nextDouble(), random.nextDouble());
                    }
                }
                statistics.get("chaos_actions").incrementAndGet();
                Thread.sleep(random.nextInt(200));
            } catch (Exception ignored) {}
        }
    }

    private void visualizationWorker() {
        Random random = new Random();

        while (running) {
            try {
                // Generate test data
                generateChaosAlerts(random.nextInt(50) + 1);

                // Attempt visualization
                Path outputPath = tempDir.resolve("chaos-vis-" + UUID.randomUUID() + ".html");
                visualizer.generateVisualization(alertHistory.getAlertSummary(), outputPath.toString());

                if (Files.exists(outputPath) && isValidVisualization(outputPath)) {
                    statistics.get("visualizations").incrementAndGet();
                    statistics.get("recoveries").incrementAndGet();
                }

                Thread.sleep(random.nextInt(1000));
            } catch (Exception e) {
                statistics.get("failures").incrementAndGet();
            }
        }
    }

    private void monitorSystemState() {
        Runtime runtime = Runtime.getRuntime();
        long usedMemory = runtime.totalMemory() - runtime.freeMemory();
        long maxMemory = runtime.maxMemory();

        // Log system state for analysis
        System.out.printf("Memory Usage: %.2f%%, Visualizations: %d, Failures: %d, Recoveries: %d%n",
            (usedMemory * 100.0) / maxMemory,
            statistics.get("visualizations").get(),
            statistics.get("failures").get(),
            statistics.get("recoveries").get()
        );
    }

    private void generateChaosAlerts(int count) {
        ResourceAlert.AlertSeverity[] severities = ResourceAlert.AlertSeverity.values();
        Random random = new Random();

        for (int i = 0; i < count; i++) {
            try {
                alertHistory.recordAlert(new ResourceAlert.AlertEvent(
                    "Chaos Alert #" + UUID.randomUUID(),
                    severities[random.nextInt(severities.length)],
                    "Chaos test message " + random.nextInt(1000),
                    monitor.getSummary()
                ));
            } catch (Exception ignored) {}
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

    private void analyzeResults() {
        int visualizations = statistics.get("visualizations").get();
        int failures = statistics.get("failures").get();
        int recoveries = statistics.get("recoveries").get();
        int chaosActions = statistics.get("chaos_actions").get();

        // Calculate resilience metrics
        double successRate = (visualizations * 100.0) / (visualizations + failures);
        double recoveryRate = (recoveries * 100.0) / (failures + 1); // Add 1 to prevent division by zero

        // Verify system resilience
        assertAll("Chaos test results",
            () -> assertTrue(successRate > 80,
                String.format("Success rate should be >80%%, was: %.2f%%", successRate)),
            () -> assertTrue(recoveryRate > 90,
                String.format("Recovery rate should be >90%%, was: %.2f%%", recoveryRate)),
            () -> assertTrue(chaosActions > 100,
                "Should have executed sufficient chaos actions: " + chaosActions)
        );

        // Log detailed results
        System.out.println("\nChaos Test Results:");
        System.out.printf("Total Visualizations: %d%n", visualizations);
        System.out.printf("Total Failures: %d%n", failures);
        System.out.printf("Total Recoveries: %d%n", recoveries);
        System.out.printf("Total Chaos Actions: %d%n", chaosActions);
        System.out.printf("Success Rate: %.2f%%%n", successRate);
        System.out.printf("Recovery Rate: %.2f%%%n", recoveryRate);
    }

    @AfterEach
    void cleanup() {
        running = false;
        if (monitor != null) {
            monitor.close();
        }
        if (chaosExecutor != null) {
            chaosExecutor.shutdownNow();
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
