package vn.com.fecredit.app.monitoring.base;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import java.nio.file.*;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Abstract base class for integration tests that provides reusable test infrastructure
 */
@ExtendWith(TestExecutionListener.class)
public abstract class AbstractIntegrationTest extends BaseMonitoringTest {

    protected static final Path REPORT_DIR = Paths.get("build", "test-reports");
    protected final TestMetricsCollector metrics = TestMetricsCollector.getInstance();
    protected final TestMetricsReporter reporter = new TestMetricsReporter(REPORT_DIR);
    
    private final Map<String, Instant> testStartTimes = new ConcurrentHashMap<>();
    private final String testSuite = getClass().getSimpleName();
    
    @BeforeAll
    static void baseSetup() {
        // Ensure report directory exists
        try {
            Files.createDirectories(REPORT_DIR);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create report directory", e);
        }
    }

    @BeforeEach
    void setupTest(TestInfo testInfo) {
        testStartTimes.put(testInfo.getDisplayName(), Instant.now());
        metrics.startOperation();
    }

    @AfterEach 
    void tearDownTest(TestInfo testInfo) {
        Instant start = testStartTimes.remove(testInfo.getDisplayName());
        if (start != null) {
            Duration duration = Duration.between(start, Instant.now());
            boolean passed = testInfo.getTestMethod()
                .map(method -> !method.isAnnotationPresent(Disabled.class))
                .orElse(true);
                
            // Record test metrics
            metrics.recordTest(
                testSuite,
                testInfo.getDisplayName(),
                duration,
                passed,
                getTestCategories(testInfo),
                collectTestMetrics()
            );
        }
    }

    @AfterAll
    void generateReports() {
        try {
            reporter.generateHtmlReport();
            reporter.exportJson();
            reporter.exportCsv();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Run test with retry logic
     */
    protected <T> T runWithRetry(TestOperation<T> operation, int maxAttempts, Duration delay) {
        Exception lastException = null;
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return operation.execute();
            } catch (Exception e) {
                lastException = e;
                if (attempt < maxAttempts) {
                    try {
                        Thread.sleep(delay.toMillis());
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Retry interrupted", ie);
                    }
                }
            }
        }
        throw new AssertionError("Operation failed after " + maxAttempts + " attempts", lastException);
    }

    /**
     * Run load test with specified parameters
     */
    protected void runLoadTest(Consumer<Integer> operation, LoadTestConfig config) throws InterruptedException {
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch completionLatch = new CountDownLatch(config.threads);
        
        for (int i = 0; i < config.threads; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    startLatch.await();
                    Instant deadline = Instant.now().plus(config.duration);
                    while (Instant.now().isBefore(deadline)) {
                        operation.accept(threadId);
                        if (config.delay.toMillis() > 0) {
                            Thread.sleep(config.delay.toMillis());
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    completionLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        assertTrue(completionLatch.await(config.duration.toMillis() * 2, TimeUnit.MILLISECONDS),
            "Load test did not complete within timeout");
    }

    protected Set<String> getTestCategories(TestInfo testInfo) {
        Set<String> categories = new HashSet<>();
        testInfo.getTestClass().ifPresent(clazz -> {
            if (clazz.isAnnotationPresent(TestCategory.StressTest.class)) {
                categories.add("stress");
            }
            if (clazz.isAnnotationPresent(TestCategory.IntegrationTest.class)) {
                categories.add("integration");
            }
            // Add more category checks as needed
        });
        return categories;
    }

    protected Map<String, Number> collectTestMetrics() {
        Map<String, Number> metrics = new HashMap<>();
        Runtime runtime = Runtime.getRuntime();
        metrics.put("memoryUsed", runtime.totalMemory() - runtime.freeMemory());
        metrics.put("threadCount", Thread.activeCount());
        return metrics;
    }

    @FunctionalInterface
    protected interface TestOperation<T> {
        T execute() throws Exception;
    }

    protected static class LoadTestConfig {
        final int threads;
        final Duration duration;
        final Duration delay;

        public LoadTestConfig(int threads, Duration duration, Duration delay) {
            this.threads = threads;
            this.duration = duration;
            this.delay = delay;
        }

        public static LoadTestConfig withDefaults() {
            return new LoadTestConfig(
                TestCategory.Config.DEFAULT_CONCURRENT_THREADS,
                TestCategory.Config.DEFAULT_STRESS_DURATION,
                Duration.ofMillis(100)
            );
        }
    }
}
