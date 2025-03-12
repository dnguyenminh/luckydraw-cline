package vn.com.fecredit.app.monitoring;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.TestContext;
import org.springframework.test.context.TestContextManager;

/**
 * Unit tests for LoadTestRunner
 */
class LoadTestRunnerTest {
    
    @TempDir
    Path tempDir;
    
    private EventStatisticsMonitor monitor;
    private ApplicationContext applicationContext;
    private TestContextManager testContextManager;
    
    @BeforeEach
    void setUp() {
        monitor = mock(EventStatisticsMonitor.class);
        applicationContext = mock(ApplicationContext.class);
        testContextManager = mock(TestContextManager.class);
        TestContext testContext = mock(TestContext.class);
        
        when(applicationContext.getBean(EventStatisticsMonitor.class)).thenReturn(monitor);
        when(testContext.getApplicationContext()).thenReturn(applicationContext);
        when(testContextManager.getTestContext()).thenReturn(testContext);
    }

    @Test
    void shouldGenerateReportForSuccessfulTests() throws Exception {
        // Given
        File reportFile = tempDir.resolve("test-report.txt").toFile();
        System.setProperty("reportFile", reportFile.getAbsolutePath());
        System.setProperty("performanceThreshold", "5000");

        // When
        // Simulate successful test execution
        monitor.recordOperation("test1", 1000L);
        monitor.recordOperation("test2", 2000L);
        when(monitor.getAverageProcessingTime("test1")).thenReturn(1000.0);
        when(monitor.getAverageProcessingTime("test2")).thenReturn(2000.0);

        LoadTestRunner.main(new String[0]);

        // Then
        List<String> reportLines = Files.readAllLines(reportFile.toPath());
        assertThat(reportLines).anyMatch(line -> line.contains("Load Test Execution Report"));
        assertThat(reportLines).anyMatch(line -> line.contains("Tests Found:"));
        assertThat(reportLines).anyMatch(line -> line.contains("Performance Metrics"));
    }

    @Test
    void shouldHandleFailedTests() throws Exception {
        // Given
        File reportFile = tempDir.resolve("failed-test-report.txt").toFile();
        System.setProperty("reportFile", reportFile.getAbsolutePath());
        System.setProperty("performanceThreshold", "1000");

        // When
        // Simulate test failure with slow performance
        monitor.recordOperation("slowTest", 5000L);
        when(monitor.getAverageProcessingTime("slowTest")).thenReturn(5000.0);

        LoadTestRunner.main(new String[0]);

        // Then
        List<String> reportLines = Files.readAllLines(reportFile.toPath());
        assertThat(reportLines).anyMatch(line -> line.contains("Performance Warnings"));
        assertThat(reportLines).anyMatch(line -> line.contains("below threshold"));
    }

    @Test
    void shouldHandleConcurrentTestExecution() throws Exception {
        // Given
        File reportFile = tempDir.resolve("concurrent-test-report.txt").toFile();
        System.setProperty("reportFile", reportFile.getAbsolutePath());
        System.setProperty("performanceThreshold", "1000");

        // When
        // Simulate concurrent test execution
        Arrays.asList("test1", "test2", "test3")
            .parallelStream()
            .forEach(testName -> {
                monitor.recordOperation(testName, 500L);
                when(monitor.getAverageProcessingTime(testName)).thenReturn(500.0);
            });

        LoadTestRunner.main(new String[0]);

        // Then
        List<String> reportLines = Files.readAllLines(reportFile.toPath());
        assertThat(reportLines).anyMatch(line -> line.contains("Tests Found:"));
        assertThat(reportLines).noneMatch(line -> line.contains("Performance Warnings"));
    }

    @Test
    void shouldHandleEmptyTestExecution() throws Exception {
        // Given
        File reportFile = tempDir.resolve("empty-test-report.txt").toFile();
        System.setProperty("reportFile", reportFile.getAbsolutePath());

        // When
        LoadTestRunner.main(new String[0]);

        // Then
        List<String> reportLines = Files.readAllLines(reportFile.toPath());
        assertThat(reportLines).anyMatch(line -> line.contains("Tests Found: 0"));
    }

    @Test
    void shouldIncludeDetailedMetrics() throws Exception {
        // Given
        File reportFile = tempDir.resolve("metrics-test-report.txt").toFile();
        System.setProperty("reportFile", reportFile.getAbsolutePath());
        System.setProperty("performanceThreshold", "2000");

        // When
        // Simulate tests with varying performance
        Arrays.asList(1000L, 1500L, 2500L).forEach(duration -> {
            String testName = "test" + duration;
            monitor.recordOperation(testName, duration);
            when(monitor.getAverageProcessingTime(testName)).thenReturn((double) duration);
        });

        LoadTestRunner.main(new String[0]);

        // Then
        List<String> reportLines = Files.readAllLines(reportFile.toPath());
        assertThat(reportLines).anyMatch(line -> line.contains("Min Operation Rate:"));
        assertThat(reportLines).anyMatch(line -> line.contains("Max Operation Rate:"));
        assertThat(reportLines).anyMatch(line -> line.contains("Avg Operation Rate:"));
    }

    @Test
    void shouldHandleDifferentThresholdConfigurations() throws Exception {
        // Given
        File reportFile = tempDir.resolve("threshold-test-report.txt").toFile();
        
        // Test with different thresholds
        Arrays.asList(1000L, 2000L, 5000L).forEach(threshold -> {
            System.setProperty("performanceThreshold", String.valueOf(threshold));
            System.setProperty("reportFile", reportFile.getAbsolutePath());

            // When
            monitor.recordOperation("test", 3000L);
            when(monitor.getAverageProcessingTime("test")).thenReturn(3000.0);

            try {
                LoadTestRunner.main(new String[0]);
                List<String> reportLines = Files.readAllLines(reportFile.toPath());
                
                // Then
                if (threshold < 3000) {
                    assertThat(reportLines).anyMatch(line -> line.contains("Performance Warnings"));
                } else {
                    assertThat(reportLines).noneMatch(line -> line.contains("Performance Warnings"));
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }
}
