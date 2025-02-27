package vn.com.fecredit.app.monitoring.base;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Centralized collector for test metrics to avoid duplicating metrics gathering code
 */
public class TestMetricsCollector {
    private static final TestMetricsCollector INSTANCE = new TestMetricsCollector();
    
    private final Map<String, TestSuite> suites = new ConcurrentHashMap<>();
    private final Map<String, AtomicInteger> categoryCounters = new ConcurrentHashMap<>();
    private final ThreadLocal<Instant> operationStart = new ThreadLocal<>();

    private TestMetricsCollector() {} // Singleton

    public static TestMetricsCollector getInstance() {
        return INSTANCE;
    }

    /**
     * Start timing an operation
     */
    public void startOperation() {
        operationStart.set(Instant.now());
    }

    /**
     * Record a test execution
     */
    public void recordTest(String suiteId, String testId, Duration duration, boolean passed, 
                          Set<String> categories, Map<String, Number> metrics) {
        TestSuite suite = suites.computeIfAbsent(suiteId, k -> new TestSuite(suiteId));
        suite.addTest(new TestExecution(testId, duration, passed, categories, metrics));
        
        // Update category counters
        categories.forEach(category -> 
            categoryCounters.computeIfAbsent(category, k -> new AtomicInteger()).incrementAndGet());
    }

    /**
     * Get metrics report for a suite
     */
    public TestMetricsReport getReport(String suiteId) {
        TestSuite suite = suites.get(suiteId);
        if (suite == null) {
            return new TestMetricsReport(Collections.emptyMap(), Collections.emptyMap());
        }
        return suite.generateReport();
    }

    /**
     * Get overall metrics across all suites
     */
    public Map<String, TestMetricsReport> getAllReports() {
        return suites.entrySet().stream()
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                e -> e.getValue().generateReport()
            ));
    }

    /**
     * Clear all collected metrics
     */
    public void reset() {
        suites.clear();
        categoryCounters.clear();
    }

    /**
     * Get tests matching criteria
     */
    public List<TestExecution> findTests(Predicate<TestExecution> criteria) {
        return suites.values().stream()
            .flatMap(s -> s.tests.stream())
            .filter(criteria)
            .collect(Collectors.toList());
    }

    /**
     * Represents a test execution
     */
    public static class TestExecution {
        public final String id;
        public final Duration duration;
        public final boolean passed;
        public final Set<String> categories;
        public final Map<String, Number> metrics;
        public final Instant timestamp;

        private TestExecution(String id, Duration duration, boolean passed,
                            Set<String> categories, Map<String, Number> metrics) {
            this.id = id;
            this.duration = duration;
            this.passed = passed;
            this.categories = new HashSet<>(categories);
            this.metrics = new HashMap<>(metrics);
            this.timestamp = Instant.now();
        }
    }

    /**
     * Represents a test suite
     */
    private static class TestSuite {
        private final String id;
        private final List<TestExecution> tests = new ArrayList<>();

        private TestSuite(String id) {
            this.id = id;
        }

        private synchronized void addTest(TestExecution test) {
            tests.add(test);
        }

        private TestMetricsReport generateReport() {
            Map<String, Double> metrics = new HashMap<>();
            Map<String, Integer> counts = new HashMap<>();

            // Calculate basic metrics
            int total = tests.size();
            long passed = tests.stream().filter(t -> t.passed).count();
            double successRate = total == 0 ? 0 : (double) passed / total;
            double avgDuration = tests.stream()
                .mapToLong(t -> t.duration.toMillis())
                .average()
                .orElse(0);

            metrics.put("successRate", successRate);
            metrics.put("averageDurationMs", avgDuration);
            counts.put("total", total);
            counts.put("passed", (int) passed);
            counts.put("failed", total - (int) passed);

            return new TestMetricsReport(metrics, counts);
        }
    }

    /**
     * Test metrics report
     */
    public static class TestMetricsReport {
        public final Map<String, Double> metrics;
        public final Map<String, Integer> counts;

        private TestMetricsReport(Map<String, Double> metrics, Map<String, Integer> counts) {
            this.metrics = Collections.unmodifiableMap(metrics);
            this.counts = Collections.unmodifiableMap(counts);
        }
    }
}
