package vn.com.fecredit.app.monitoring.base;

import org.junit.jupiter.api.extension.*;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * Custom test execution listener to handle test categories and monitoring
 */
public class TestExecutionListener implements BeforeTestExecutionCallback, 
                                           AfterTestExecutionCallback,
                                           BeforeAllCallback,
                                           AfterAllCallback {

    private static final Logger logger = Logger.getLogger(TestExecutionListener.class.getName());
    private static final Map<String, TestMetrics> testMetrics = new ConcurrentHashMap<>();
    private static final ThreadLocal<Instant> testStartTime = new ThreadLocal<>();
    
    @Override
    public void beforeAll(ExtensionContext context) {
        Class<?> testClass = context.getRequiredTestClass();
        logTestConfiguration(testClass);
    }

    @Override
    public void beforeTestExecution(ExtensionContext context) {
        testStartTime.set(Instant.now());
        logTestStart(context);
        setupTestEnvironment(context);
    }

    @Override
    public void afterTestExecution(ExtensionContext context) {
        Instant start = testStartTime.get();
        Duration duration = Duration.between(start, Instant.now());
        updateMetrics(context, duration);
        logTestCompletion(context, duration);
        testStartTime.remove();
    }

    @Override
    public void afterAll(ExtensionContext context) {
        summarizeResults(context);
    }

    private void logTestConfiguration(Class<?> testClass) {
        logger.info(() -> String.format("Test Configuration for %s:", testClass.getSimpleName()));
        
        // Log category-specific configurations
        if (isStressTest(testClass)) {
            logger.info(() -> String.format("Stress Test Configuration:" +
                "\n  Duration: %s" +
                "\n  Threads: %d" +
                "\n  Batch Size: %d",
                TestCategory.Config.DEFAULT_STRESS_DURATION,
                TestCategory.Config.DEFAULT_STRESS_THREADS,
                TestCategory.Config.DEFAULT_BATCH_SIZE));
        }
        
        if (isChaosTest(testClass)) {
            logger.info(() -> String.format("Chaos Test Configuration:" +
                "\n  Duration: %s" +
                "\n  Error Rate Threshold: %.2f",
                TestCategory.Config.DEFAULT_CHAOS_DURATION,
                TestCategory.Config.ERROR_RATE_THRESHOLD));
        }
    }

    private void setupTestEnvironment(ExtensionContext context) {
        Class<?> testClass = context.getRequiredTestClass();
        
        if (isPerformanceTest(testClass)) {
            // Warmup period for performance tests
            try {
                Thread.sleep(TestCategory.Config.DEFAULT_WARMUP_SECONDS * 1000L);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private void updateMetrics(ExtensionContext context, Duration duration) {
        String testId = getTestId(context);
        testMetrics.computeIfAbsent(testId, k -> new TestMetrics())
                  .addExecution(duration, context.getExecutionException().isPresent());
    }

    private void logTestStart(ExtensionContext context) {
        logger.info(() -> String.format("Starting test: %s",
            context.getTestMethod().map(m -> m.getName()).orElse("unknown")));
    }

    private void logTestCompletion(ExtensionContext context, Duration duration) {
        String testName = context.getTestMethod().map(m -> m.getName()).orElse("unknown");
        String status = context.getExecutionException().isPresent() ? "FAILED" : "PASSED";
        
        logger.info(() -> String.format("Test completed: %s [%s] Duration: %dms",
            testName, status, duration.toMillis()));
    }

    private void summarizeResults(ExtensionContext context) {
        Class<?> testClass = context.getRequiredTestClass();
        TestMetrics classMetrics = getClassMetrics(testClass);
        
        logger.info(() -> String.format("\nTest Summary for %s:" +
            "\nTotal Executions: %d" +
            "\nTotal Failures: %d" +
            "\nSuccess Rate: %.2f%%" +
            "\nAverage Duration: %dms" +
            "\nMax Duration: %dms",
            testClass.getSimpleName(),
            classMetrics.totalExecutions,
            classMetrics.failures,
            classMetrics.getSuccessRate() * 100.0,
            classMetrics.getAverageDuration(),
            classMetrics.maxDuration));
    }

    private String getTestId(ExtensionContext context) {
        return context.getRequiredTestClass().getName() + "#" +
               context.getTestMethod().map(m -> m.getName()).orElse("unknown");
    }

    private TestMetrics getClassMetrics(Class<?> testClass) {
        return testMetrics.entrySet().stream()
            .filter(e -> e.getKey().startsWith(testClass.getName()))
            .map(Map.Entry::getValue)
            .reduce(new TestMetrics(), TestMetrics::combine);
    }

    private boolean isStressTest(Class<?> testClass) {
        return testClass.isAnnotationPresent(TestCategory.StressTest.class);
    }

    private boolean isChaosTest(Class<?> testClass) {
        return testClass.isAnnotationPresent(TestCategory.ChaosTest.class);
    }

    private boolean isPerformanceTest(Class<?> testClass) {
        return testClass.isAnnotationPresent(TestCategory.PerformanceTest.class);
    }

    private static class TestMetrics {
        private int totalExecutions;
        private int failures;
        private long totalDuration;
        private long maxDuration;

        void addExecution(Duration duration, boolean failed) {
            totalExecutions++;
            if (failed) failures++;
            long durationMs = duration.toMillis();
            totalDuration += durationMs;
            maxDuration = Math.max(maxDuration, durationMs);
        }

        TestMetrics combine(TestMetrics other) {
            TestMetrics result = new TestMetrics();
            result.totalExecutions = this.totalExecutions + other.totalExecutions;
            result.failures = this.failures + other.failures;
            result.totalDuration = this.totalDuration + other.totalDuration;
            result.maxDuration = Math.max(this.maxDuration, other.maxDuration);
            return result;
        }

        double getSuccessRate() {
            return totalExecutions == 0 ? 0 : 
                (double)(totalExecutions - failures) / totalExecutions;
        }

        long getAverageDuration() {
            return totalExecutions == 0 ? 0 : totalDuration / totalExecutions;
        }
    }
}
