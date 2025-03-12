package vn.com.fecredit.app.monitoring.base;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.*;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Base template for monitoring and performance tests
 * This provides reusable test infrastructure to avoid regenerating similar test code
 */
public abstract class BaseMonitoringTest {

    // Common test configurations 
    protected static final Duration DEFAULT_TIMEOUT = Duration.ofMinutes(5);
    protected static final int DEFAULT_BATCH_SIZE = 1000;
    protected static final int DEFAULT_THREADS = 4;
    
    @TempDir
    protected Path tempDir;
    
    protected ExecutorService executor;
    protected ScheduledExecutorService scheduler;

    @BeforeEach
    void baseSetUp() {
        executor = Executors.newCachedThreadPool();
        scheduler = Executors.newSingleThreadScheduledExecutor();
    }

    /**
     * Run concurrent load test with specified parameters
     */
    protected void runLoadTest(int threads, Duration duration, Consumer<Integer> operation) throws InterruptedException {
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch completionLatch = new CountDownLatch(threads);
        
        // Launch worker threads
        for (int i = 0; i < threads; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    startLatch.await();
                    Instant deadline = Instant.now().plus(duration);
                    while (Instant.now().isBefore(deadline)) {
                        operation.accept(threadId);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    completionLatch.countDown();
                }
            });
        }

        // Start all threads simultaneously
        startLatch.countDown();
        assertTrue(completionLatch.await(duration.toMillis() * 2, TimeUnit.MILLISECONDS),
            "Test did not complete within timeout");
    }

    /**
     * Execute with retry logic
     */
    protected <T> T executeWithRetry(Callable<T> operation, int maxAttempts) throws Exception {
        Exception lastException = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return operation.call();
            } catch (Exception e) {
                lastException = e;
                if (attempt < maxAttempts) {
                    Thread.sleep(attempt * 100L); // Exponential backoff
                }
            }
        }
        throw new AssertionError("Operation failed after " + maxAttempts + " attempts", lastException);
    }

    /**
     * Verify test results meet criteria
     */
    protected void verifyResults(Collection<?> results, Predicate<?> condition, String message) {
        assertFalse(results.isEmpty(), "Results should not be empty");
        assertTrue(results.stream().allMatch(r -> ((Predicate<Object>)condition).test(r)),
            message);
    }

    /**
     * Create test files with content
     */
    protected List<Path> createTestFiles(int count, String contentPrefix) throws IOException {
        List<Path> files = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Path file = tempDir.resolve("test-" + i + ".txt");
            Files.writeString(file, contentPrefix + "-" + i);
            files.add(file);
        }
        return files;
    }

    /**
     * Monitor system metrics during test
     */
    protected void monitorMetrics(Duration interval, Consumer<RuntimeMetrics> handler) {
        scheduler.scheduleAtFixedRate(() -> {
            Runtime runtime = Runtime.getRuntime();
            handler.accept(new RuntimeMetrics(
                runtime.totalMemory() - runtime.freeMemory(),
                runtime.maxMemory(),
                Thread.activeCount()
            ));
        }, 0, interval.toMillis(), TimeUnit.MILLISECONDS);
    }

    /**
     * System runtime metrics snapshot
     */
    protected static class RuntimeMetrics {
        public final long usedMemory;
        public final long maxMemory; 
        public final int threadCount;

        public RuntimeMetrics(long usedMemory, long maxMemory, int threadCount) {
            this.usedMemory = usedMemory;
            this.maxMemory = maxMemory;
            this.threadCount = threadCount;
        }

        public double getMemoryUtilization() {
            return (usedMemory * 100.0) / maxMemory;
        }
    }

    @AfterEach
    void baseTearDown() {
        if (executor != null) {
            executor.shutdownNow();
            try {
                executor.awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        if (scheduler != null) {
            scheduler.shutdownNow();
        }
        System.gc();
    }
}
