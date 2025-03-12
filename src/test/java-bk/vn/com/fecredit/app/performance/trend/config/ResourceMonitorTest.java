package vn.com.fecredit.app.performance.trend.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ResourceMonitorTest {
    
    private ResourceMonitor monitor;
    private static final int THREAD_COUNT = Runtime.getRuntime().availableProcessors();
    private static final Duration TEST_DURATION = Duration.ofSeconds(2);

    @BeforeEach
    void setUp() {
        monitor = new ResourceMonitor();
    }

    @Test
    void shouldInitializeWithDefaultValues() {
        assertNotNull(monitor.getLastUpdateTime());
        assertTrue(monitor.getSnapshots().isEmpty());
        
        ResourceMonitor.ResourceSummary summary = monitor.generateSummary();
        assertEquals(0.0, summary.averageCpuUsage());
        assertEquals(0L, summary.peakMemoryUsage());
        assertEquals(0, summary.totalGcCount());
    }

    @Test
    void shouldRecordMetrics() throws Exception {
        monitor.updateMetrics();
        
        List<ResourceMonitor.MetricSnapshot> snapshots = monitor.getSnapshots();
        assertEquals(1, snapshots.size());
        
        ResourceMonitor.MetricSnapshot snapshot = snapshots.get(0);
        assertNotNull(snapshot.timestamp());
        assertTrue(snapshot.heapUsed() > 0);
        assertTrue(snapshot.nonHeapUsed() > 0);
        assertTrue(snapshot.threadCount() > 0);
        assertTrue(snapshot.cpuUsage() >= 0.0);
    }

    @Test
    void shouldLimitSnapshotHistory() throws Exception {
        for (int i = 0; i < 2000; i++) {
            monitor.updateMetrics();
        }
        
        assertTrue(monitor.getSnapshots().size() <= 1000, 
            "Should limit snapshot history to 1000 entries");
    }

    @Test
    void shouldHandleConcurrentUpdates() throws Exception {
        int updateThreads = THREAD_COUNT;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch completionLatch = new CountDownLatch(updateThreads);
        ExecutorService executorService = Executors.newFixedThreadPool(updateThreads);
        
        try {
            // Create threads to update metrics simultaneously
            for (int i = 0; i < updateThreads; i++) {
                executorService.submit(() -> {
                    try {
                        startLatch.await();
                        for (int j = 0; j < 100; j++) {
                            monitor.updateMetrics();
                            Thread.sleep(10);
                        }
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        completionLatch.countDown();
                    }
                });
            }

            startLatch.countDown();
            assertTrue(completionLatch.await(30, TimeUnit.SECONDS),
                "All threads should complete updates");

            List<ResourceMonitor.MetricSnapshot> snapshots = monitor.getSnapshots();
            assertFalse(snapshots.isEmpty());
            assertTrue(snapshots.size() <= 1000);

        } finally {
            executorService.shutdownNow();
            assertTrue(executorService.awaitTermination(5, TimeUnit.SECONDS));
        }
    }

    @Test
    void shouldGenerateAccurateSummary() throws Exception {
        // Generate some CPU load
        ExecutorService loadGenerator = Executors.newFixedThreadPool(THREAD_COUNT);
        try {
            for (int i = 0; i < THREAD_COUNT; i++) {
                loadGenerator.submit(() -> {
                    Instant end = Instant.now().plus(TEST_DURATION);
                    while (Instant.now().isBefore(end)) {
                        Math.pow(Math.random() * 1000, 3);
                    }
                });
            }

            // Record metrics during load
            for (int i = 0; i < 10; i++) {
                monitor.updateMetrics();
                Thread.sleep(200);
            }

            ResourceMonitor.ResourceSummary summary = monitor.generateSummary();
            assertTrue(summary.averageCpuUsage() > 0.0,
                "CPU usage should be detected under load");
            assertTrue(summary.peakMemoryUsage() > 0L,
                "Memory usage should be detected");
            assertTrue(summary.totalGcCount() >= 0,
                "GC count should be non-negative");

        } finally {
            loadGenerator.shutdownNow();
            assertTrue(loadGenerator.awaitTermination(5, TimeUnit.SECONDS));
        }
    }

    @Test
    void shouldHandleMemoryPressure() throws Exception {
        List<byte[]> memoryHogs = new ArrayList<>();
        try {
            // Create memory pressure
            for (int i = 0; i < 10; i++) {
                memoryHogs.add(new byte[1024 * 1024]); // 1MB chunks
                monitor.updateMetrics();
            }

            ResourceMonitor.ResourceSummary summary = monitor.generateSummary();
            assertTrue(summary.peakMemoryUsage() > 5_000_000, // At least 5MB
                "Should detect increased memory usage");

        } finally {
            memoryHogs.clear();
            System.gc();
        }
    }

    @Test
    void shouldTrackThreadCount() throws Exception {
        int initialThreads = monitor.getSnapshots().isEmpty() ? 0 :
            monitor.getSnapshots().get(0).threadCount();
        
        // Create additional threads
        List<Thread> threads = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Thread thread = new Thread(() -> {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            thread.start();
            threads.add(thread);
        }

        // Allow threads to start and record metrics
        Thread.sleep(100);
        monitor.updateMetrics();

        int newThreadCount = monitor.getSnapshots().get(
            monitor.getSnapshots().size() - 1).threadCount();
        
        assertTrue(newThreadCount > initialThreads,
            "Should detect increased thread count");

        // Cleanup
        for (Thread thread : threads) {
            thread.interrupt();
            thread.join(1000);
        }
    }

    @Test
    void shouldMaintainTimeOrdering() throws Exception {
        for (int i = 0; i < 10; i++) {
            monitor.updateMetrics();
            Thread.sleep(50);
        }

        List<ResourceMonitor.MetricSnapshot> snapshots = monitor.getSnapshots();
        for (int i = 1; i < snapshots.size(); i++) {
            assertTrue(snapshots.get(i).timestamp().isAfter(snapshots.get(i-1).timestamp()),
                "Snapshots should be in chronological order");
        }
    }

    @Test
    void shouldUpdateLastUpdateTime() throws Exception {
        Instant before = Instant.now();
        Thread.sleep(100);
        
        monitor.updateMetrics();
        Instant updateTime = monitor.getLastUpdateTime();
        Thread.sleep(100);
        
        Instant after = Instant.now();

        assertTrue(updateTime.isAfter(before), "Update time should be after start");
        assertTrue(updateTime.isBefore(after), "Update time should be before end");
    }
}
