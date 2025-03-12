package vn.com.fecredit.app.monitoring;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.context.ApplicationContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Files;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

/**
 * Integration tests for LoadTestRunner with real Spring context
 */
@SpringBootTest(classes = TestConfig.class)
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1"
})
class LoadTestRunnerIntegrationTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private EventStatisticsMonitor monitor;

    @TempDir
    Path tempDir;

    private String originalReportFile;
    private String originalThreshold;

    @BeforeEach
    void setUp() {
        originalReportFile = System.getProperty("reportFile");
        originalThreshold = System.getProperty("performanceThreshold");
        monitor.clearMetrics();
        monitor.enableMonitoring();
    }

    @AfterEach
    void tearDown() {
        // Restore system properties
        if (originalReportFile != null) {
            System.setProperty("reportFile", originalReportFile);
        }
        if (originalThreshold != null) {
            System.setProperty("performanceThreshold", originalThreshold);
        }
        monitor.clearMetrics();
    }

    @Test
    void shouldRunLoadTestWithRealMonitor() throws Exception {
        // Given
        File reportFile = tempDir.resolve("integration-test-report.txt").toFile();
        System.setProperty("reportFile", reportFile.getAbsolutePath());
        System.setProperty("performanceThreshold", "1000");

        // When
        ExecutorService executor = Executors.newFixedThreadPool(10);
        IntStream.range(0, 100).forEach(i -> 
            executor.submit(() -> monitor.recordOperation("integrationTest", 500L))
        );
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        LoadTestRunner.main(new String[0]);

        // Then
        List<String> reportLines = Files.readAllLines(reportFile.toPath());
        assertReport(reportLines);
    }

    @Test
    void shouldHandleHighLoadWithRealMonitor() throws Exception {
        // Given
        File reportFile = tempDir.resolve("high-load-test-report.txt").toFile();
        System.setProperty("reportFile", reportFile.getAbsolutePath());
        System.setProperty("performanceThreshold", "5000");

        int processors = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(processors);
        int operationsPerThread = 10_000;

        // When
        long startTime = System.nanoTime();
        IntStream.range(0, processors).forEach(thread -> 
            executor.submit(() -> {
                for (int i = 0; i < operationsPerThread; i++) {
                    monitor.recordOperation("highLoadTest", 1L);
                }
            })
        );
        executor.shutdown();
        executor.awaitTermination(30, TimeUnit.SECONDS);
        long duration = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);

        LoadTestRunner.main(new String[0]);

        // Then
        List<String> reportLines = Files.readAllLines(reportFile.toPath());
        assertHighLoadReport(reportLines, processors * operationsPerThread, duration);
    }

    @Test
    void shouldHandleMultipleOperationTypes() throws Exception {
        // Given
        File reportFile = tempDir.resolve("multi-op-test-report.txt").toFile();
        System.setProperty("reportFile", reportFile.getAbsolutePath());
        System.setProperty("performanceThreshold", "2000");

        String[] operations = {"login", "search", "update", "delete"};
        ExecutorService executor = Executors.newFixedThreadPool(operations.length);

        // When
        for (String operation : operations) {
            executor.submit(() -> {
                for (int i = 0; i < 1000; i++) {
                    monitor.recordOperation(operation, (long)(Math.random() * 1000));
                }
            });
        }
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        LoadTestRunner.main(new String[0]);

        // Then
        List<String> reportLines = Files.readAllLines(reportFile.toPath());
        assertMultiOperationReport(reportLines, operations);
    }

    private void assertReport(List<String> reportLines) {
        org.assertj.core.api.Assertions.assertThat(reportLines)
            .anyMatch(line -> line.contains("Load Test Execution Report"))
            .anyMatch(line -> line.contains("Performance Metrics"))
            .anyMatch(line -> line.contains("Min Operation Rate:"))
            .anyMatch(line -> line.contains("Max Operation Rate:"))
            .noneMatch(line -> line.contains("Performance Warnings"));
    }

    private void assertHighLoadReport(List<String> reportLines, int expectedOperations, long duration) {
        double opsPerSecond = (expectedOperations * 1000.0) / duration;
        
        org.assertj.core.api.Assertions.assertThat(reportLines)
            .anyMatch(line -> line.contains("Load Test Execution Report"))
            .anyMatch(line -> line.contains("Total Time: " + duration))
            .anyMatch(line -> {
                if (line.contains("Avg Operation Rate:")) {
                    double reportedRate = Double.parseDouble(
                        line.split(":")[1].trim().split(" ")[0]);
                    return reportedRate >= opsPerSecond * 0.8; // Allow 20% variation
                }
                return false;
            });
    }

    private void assertMultiOperationReport(List<String> reportLines, String[] operations) {
        org.assertj.core.api.Assertions.assertThat(reportLines)
            .anyMatch(line -> line.contains("Load Test Execution Report"))
            .anyMatch(line -> line.contains("Performance Metrics"))
            .anyMatch(line -> {
                double avgRate = Double.parseDouble(
                    line.split(":")[1].trim().split(" ")[0]);
                return avgRate > 0 && line.contains("Avg Operation Rate:");
            });
    }
}
