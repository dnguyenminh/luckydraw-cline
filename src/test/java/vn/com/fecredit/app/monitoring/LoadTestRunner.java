package vn.com.fecredit.app.monitoring;

import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.TestContextManager;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.support.AbstractTestExecutionListener;
import org.springframework.context.ApplicationContext;

import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.util.Arrays;
import java.lang.reflect.Method;

/**
 * Custom runner for load tests that provides detailed performance metrics
 * and configurable test execution.
 */
@SpringJUnitConfig(TestConfig.class)
@TestExecutionListeners(DependencyInjectionTestExecutionListener.class)
public class LoadTestRunner {
    private static final String PERFORMANCE_THRESHOLD_PROPERTY = "performanceThreshold";
    private static final String REPORT_FILE_PROPERTY = "reportFile";
    private static final DateTimeFormatter TIME_FORMATTER = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static void main(String[] args) {
        try {
            // Configure test execution
            long performanceThreshold = Long.parseLong(
                System.getProperty(PERFORMANCE_THRESHOLD_PROPERTY, "10000")); // Default 10K ops/sec
            String reportFile = System.getProperty(REPORT_FILE_PROPERTY, "load-test-report.txt");

            // Create test context and listeners
            TestContextManager testContextManager = new TestContextManager(EventStatisticsMonitorLoadTest.class);
            PerformanceListener performanceListener = new PerformanceListener(performanceThreshold);
            SummaryListener summaryListener = new SummaryListener();

            // Add listeners
            testContextManager.registerTestExecutionListeners(Arrays.asList(
                new DependencyInjectionTestExecutionListener(),
                performanceListener,
                summaryListener
            ));

            // Execute tests
            System.out.println("Starting load tests at " + LocalDateTime.now().format(TIME_FORMATTER));
            
            EventStatisticsMonitorLoadTest testInstance = new EventStatisticsMonitorLoadTest();
            testContextManager.prepareTestInstance(testInstance);

            ApplicationContext context = testContextManager.getTestContext().getApplicationContext();
            EventStatisticsMonitor monitor = context.getBean(EventStatisticsMonitor.class);
            monitor.enableMonitoring();

            // Run all test methods
            for (Method method : testInstance.getClass().getMethods()) {
                if (method.isAnnotationPresent(Test.class)) {
                    try {
                        testContextManager.beforeTestMethod(testInstance, method);
                        method.invoke(testInstance);
                        testContextManager.afterTestMethod(testInstance, method, null);
                    } catch (Exception e) {
                        testContextManager.afterTestMethod(testInstance, method, e);
                        summaryListener.recordFailure(method.getName(), e);
                    }
                }
            }

            // Generate report
            generateReport(summaryListener, performanceListener, reportFile);

            // Exit with appropriate status
            if (summaryListener.hasFailures() || !performanceListener.isPerformanceAcceptable()) {
                System.exit(1);
            }
        } catch (Exception e) {
            System.err.println("Test execution failed: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static void generateReport(SummaryListener summaryListener, 
                                     PerformanceListener performanceListener,
                                     String reportFile) {
        try (PrintWriter writer = new PrintWriter(reportFile)) {
            writer.println("Load Test Execution Report");
            writer.println("=========================");
            writer.println("Executed at: " + LocalDateTime.now().format(TIME_FORMATTER));
            writer.println();
            
            writer.printf("Tests Found: %d%n", summaryListener.getTestCount());
            writer.printf("Tests Passed: %d%n", summaryListener.getPassCount());
            writer.printf("Tests Failed: %d%n", summaryListener.getFailureCount());
            writer.printf("Total Time: %dms%n", summaryListener.getTotalTimeMs());
            writer.println();

            // Performance metrics
            writer.println("Performance Metrics");
            writer.println("-----------------");
            writer.printf("Min Operation Rate: %.2f ops/sec%n", performanceListener.getMinOperationRate());
            writer.printf("Max Operation Rate: %.2f ops/sec%n", performanceListener.getMaxOperationRate());
            writer.printf("Avg Operation Rate: %.2f ops/sec%n", performanceListener.getAvgOperationRate());
            writer.println();

            // Failures
            if (summaryListener.hasFailures()) {
                writer.println("Test Failures");
                writer.println("-------------");
                summaryListener.getFailures().forEach((testName, error) -> {
                    writer.printf("Failed Test: %s%n", testName);
                    writer.printf("Reason: %s%n", error.getMessage());
                    writer.println();
                });
            }

            // Performance warnings
            if (!performanceListener.isPerformanceAcceptable()) {
                writer.println("Performance Warnings");
                writer.println("-------------------");
                for (String warning : performanceListener.getPerformanceWarnings()) {
                    writer.println(warning);
                }
            }

            System.out.println("Report generated: " + reportFile);
        } catch (Exception e) {
            System.err.println("Failed to generate report: " + e.getMessage());
        }
    }

    private static class SummaryListener extends AbstractTestExecutionListener {
        private final List<String> executedTests = Collections.synchronizedList(new ArrayList<>());
        private final Map<String, Throwable> failures = Collections.synchronizedMap(new HashMap<>());
        private final long startTime = System.currentTimeMillis();

        void recordFailure(String testName, Throwable error) {
            failures.put(testName, error);
        }

        int getTestCount() {
            return executedTests.size();
        }

        int getPassCount() {
            return getTestCount() - getFailureCount();
        }

        int getFailureCount() {
            return failures.size();
        }

        long getTotalTimeMs() {
            return System.currentTimeMillis() - startTime;
        }

        boolean hasFailures() {
            return !failures.isEmpty();
        }

        Map<String, Throwable> getFailures() {
            return Collections.unmodifiableMap(new HashMap<>(failures));
        }

        @Override
        public void beforeTestMethod(TestContext testContext) {
            executedTests.add(testContext.getTestMethod().getName());
        }
    }

    private static class PerformanceListener extends AbstractTestExecutionListener {
        private final long performanceThreshold;
        private final AtomicBoolean performanceAcceptable = new AtomicBoolean(true);
        private volatile double minRate = Double.MAX_VALUE;
        private volatile double maxRate = 0.0;
        private volatile double totalRate = 0.0;
        private final AtomicBoolean firstOperation = new AtomicBoolean(true);
        private volatile int rateCount = 0;
        private final List<String> performanceWarnings = Collections.synchronizedList(new ArrayList<>());

        PerformanceListener(long performanceThreshold) {
            this.performanceThreshold = performanceThreshold;
        }

        synchronized void recordOperationRate(double rate) {
            if (firstOperation.getAndSet(false)) {
                minRate = rate;
                maxRate = rate;
                totalRate = rate;
                rateCount = 1;
            } else {
                minRate = Math.min(minRate, rate);
                maxRate = Math.max(maxRate, rate);
                totalRate += rate;
                rateCount++;
            }

            if (rate < performanceThreshold) {
                performanceAcceptable.set(false);
                performanceWarnings.add(String.format(
                    "Performance below threshold: %.2f ops/sec (required: %d ops/sec)",
                    rate, performanceThreshold));
            }
        }

        double getMinOperationRate() {
            return minRate == Double.MAX_VALUE ? 0.0 : minRate;
        }

        double getMaxOperationRate() {
            return maxRate;
        }

        double getAvgOperationRate() {
            return rateCount > 0 ? totalRate / rateCount : 0.0;
        }

        boolean isPerformanceAcceptable() {
            return performanceAcceptable.get();
        }

        List<String> getPerformanceWarnings() {
            return Collections.unmodifiableList(new ArrayList<>(performanceWarnings));
        }

        @Override
        public void afterTestMethod(TestContext testContext) {
            // Record performance metrics after each test method
            EventStatisticsMonitor monitor = testContext.getApplicationContext()
                .getBean(EventStatisticsMonitor.class);
            String methodName = testContext.getTestMethod().getName();
            recordOperationRate(monitor.getAverageProcessingTime(methodName));
        }
    }
}
