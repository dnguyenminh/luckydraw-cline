package vn.com.fecredit.app.monitoring;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import java.nio.file.Path;
import java.nio.file.Files;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.IntStream;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for PerformanceReporter with stress and memory tests
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PerformanceReporterTest {
    
    private PerformanceReporter reporter;
    private Random random;
    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        reporter = new PerformanceReporter();
        reporter.setReportDirectory(tempDir.toString());
        reporter.setEnabled(true);
        random = new Random();
    }

    @Test
    @Order(1)
    void shouldGenerateReportsWithAllMetrics() {
        recordSampleMetrics();
        reporter.generateReport();
        assertReportFilesExist();
    }

    @ParameterizedTest
    @Order(2)
    @CsvSource({
        "100,200,300,200.0",
        "0,50,100,50.0",
        "1000,2000,3000,2000.0"
    })
    void shouldCalculateCorrectStatistics(long val1, long val2, long val3, double expectedAvg) {
        String operation = "test-operation";
        reporter.recordLatency(operation, val1);
        reporter.recordLatency(operation, val2);
        reporter.recordLatency(operation, val3);

        Map<String, List<Long>> metrics = reporter.getMetrics();
        List<Long> values = metrics.get("latency." + operation);

        assertNotNull(values);
        assertEquals(3, values.size());
        assertEquals(expectedAvg, MetricsUtils.calculateAverage(values), 0.01);
        assertEquals(Math.min(Math.min(val1, val2), val3), MetricsUtils.calculateMin(values));
        assertEquals(Math.max(Math.max(val1, val2), val3), MetricsUtils.calculateMax(values));
    }

    @Test
    @Order(3)
    void shouldHandleEmptyMetrics() {
        reporter.generateReport();
        assertReportFilesExist();
    }

    @Test
    @Order(4)
    void shouldHandleDisabledState() {
        reporter.setEnabled(false);
        recordSampleMetrics();
        assertTrue(reporter.getMetrics().isEmpty());
    }

    @Test
    @Order(5)
    void shouldClearMetrics() {
        recordSampleMetrics();
        assertFalse(reporter.getMetrics().isEmpty());
        reporter.clearMetrics();
        assertTrue(reporter.getMetrics().isEmpty());
    }

    @ParameterizedTest
    @Order(6)
    @ValueSource(ints = {90, 95, 99})
    void shouldCalculatePercentiles(int percentile) {
        String operation = "percentile-test";
        for (int i = 1; i <= 100; i++) {
            reporter.recordLatency(operation, i);
        }

        List<Long> values = reporter.getMetrics().get("latency." + operation);
        assertEquals(percentile, MetricsUtils.calculatePercentile(values, percentile), 0.01);
    }

    @Test
    @Order(7)
    void shouldCalculateStandardDeviation() {
        String operation = "stddev-test";
        reporter.recordLatency(operation, 2);
        reporter.recordLatency(operation, 4);
        reporter.recordLatency(operation, 4);
        reporter.recordLatency(operation, 4);
        reporter.recordLatency(operation, 6);

        List<Long> values = reporter.getMetrics().get("latency." + operation);
        assertEquals(1.414, MetricsUtils.calculateStandardDeviation(values), 0.001);
    }

    @Test
    @Order(8)
    void shouldHandleNullAndEmptyValues() {
        assertEquals(0.0, MetricsUtils.calculateAverage(null));
        assertEquals(0.0, MetricsUtils.calculatePercentile(null, 95));
        assertEquals(0.0, MetricsUtils.calculateStandardDeviation(null));
        assertEquals(0L, MetricsUtils.calculateMin(null));
        assertEquals(0L, MetricsUtils.calculateMax(null));
    }

    @Test
    @Order(9)
    void shouldHandleConcurrentAccess() throws InterruptedException {
        int threadCount = 10;
        int operationsPerThread = 1000;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    for (int j = 0; j < operationsPerThread; j++) {
                        reporter.recordLatency("concurrent-test", random.nextDouble() * 1000);
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        assertTrue(latch.await(10, TimeUnit.SECONDS));
        executor.shutdown();
        assertTrue(executor.awaitTermination(5, TimeUnit.SECONDS));

        List<Long> values = reporter.getMetrics().get("latency.concurrent-test");
        assertEquals(threadCount * operationsPerThread, values.size());
    }

    @Test
    @Order(10)
    @DisplayName("Stress Test - High Volume")
    void shouldHandleHighVolume() throws InterruptedException {
        int operationCount = 100_000;
        long startTime = System.currentTimeMillis();
        
        IntStream.range(0, operationCount).parallel().forEach(i -> 
            reporter.recordLatency("stress-test", random.nextDouble() * 100));
            
        long duration = System.currentTimeMillis() - startTime;
        List<Long> values = reporter.getMetrics().get("latency.stress-test");
        
        assertEquals(operationCount, values.size());
        assertTrue(duration < 5000, "Stress test took too long: " + duration + "ms");
    }

    @Test
    @Order(11)
    @DisplayName("Memory Test - Large Dataset")
    void shouldHandleLargeDatasetWithoutMemoryLeak() {
        int iterations = 10;
        long initialMemory = getUsedMemory();
        
        for (int i = 0; i < iterations; i++) {
            recordLargeBatchOfMetrics();
            System.gc(); // Hint for garbage collection
            long currentMemory = getUsedMemory();
            assertTrue(currentMemory - initialMemory < 50_000_000, 
                "Memory usage increased significantly: " + (currentMemory - initialMemory) + " bytes");
        }
    }

    @Test
    @Order(12)
    void shouldGenerateValidReports() throws IOException {
        recordSampleMetrics();
        reporter.generateReport();
        assertReportContents();
    }

    @ParameterizedTest
    @Order(13)
    @ValueSource(strings = {"login", "search", "checkout", "payment", "profile"})
    void shouldHandleMultipleOperations(String operation) {
        for (int i = 0; i < 100; i++) {
            reporter.recordLatency(operation, random.nextDouble() * 1000);
        }

        Map<String, List<Long>> metrics = reporter.getMetrics();
        List<Long> values = metrics.get("latency." + operation);
        assertNotNull(values);
        assertEquals(100, values.size());
    }

    private void recordSampleMetrics() {
        String[] operations = {"login", "search", "checkout"};
        
        for (String operation : operations) {
            // Record latencies
            for (int i = 0; i < 100; i++) {
                reporter.recordLatency(operation, random.nextDouble() * 1000);
            }
            
            // Record throughput
            for (int i = 0; i < 100; i++) {
                reporter.recordThroughput(operation, random.nextDouble() * 100);
            }
            
            // Record memory usage
            for (int i = 0; i < 100; i++) {
                reporter.recordMemoryUsage(operation, random.nextInt(1024 * 1024));
            }
        }
    }

    private void recordLargeBatchOfMetrics() {
        String[] operations = {"op1", "op2", "op3", "op4", "op5"};
        int metricsPerOperation = 10_000;
        
        for (String operation : operations) {
            for (int i = 0; i < metricsPerOperation; i++) {
                reporter.recordLatency(operation, random.nextDouble() * 1000);
                reporter.recordThroughput(operation, random.nextDouble() * 100);
                reporter.recordMemoryUsage(operation, random.nextInt(1024 * 1024));
            }
        }
        reporter.generateReport();
        reporter.clearMetrics();
    }

    private long getUsedMemory() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }

    private void assertReportFilesExist() {
        assertTrue(tempDir.resolve("performance-report.txt").toFile().exists());
        assertTrue(tempDir.resolve("performance-report.html").toFile().exists());
        assertTrue(tempDir.resolve("performance-report.json").toFile().exists());
        assertTrue(tempDir.resolve("performance-report.csv").toFile().exists());
    }

    private void assertReportContents() throws IOException {
        String jsonContent = Files.readString(tempDir.resolve("performance-report.json"));
        assertTrue(jsonContent.contains("\"metrics\""));
        assertTrue(jsonContent.contains("\"timestamp\""));
        
        String htmlContent = Files.readString(tempDir.resolve("performance-report.html"));
        assertTrue(htmlContent.contains("<script src='https://cdn.plot.ly/plotly-latest.min.js'>"));
        assertTrue(htmlContent.contains("<div id='latencyChart'"));
        
        String csvContent = Files.readString(tempDir.resolve("performance-report.csv"));
        assertTrue(csvContent.contains("Category,Operation,Count,Average,Min,Max"));
    }
}
