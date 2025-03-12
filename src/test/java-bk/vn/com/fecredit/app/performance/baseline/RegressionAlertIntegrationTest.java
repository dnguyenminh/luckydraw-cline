package vn.com.fecredit.app.performance.baseline;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import vn.com.fecredit.app.performance.trend.PerformanceStatistics;

class RegressionAlertIntegrationTest {

    @TempDir
    Path tempDir;

    private Path baselinePath;
    private Path alertLogPath;
    private Path reportPath;
    private RegressionAlert.AlertConfig alertConfig;
    private RegressionAlert alert;

    @BeforeEach
    void setUp() {
        baselinePath = tempDir.resolve("baseline.json");
        alertLogPath = tempDir.resolve("alerts.log");
        reportPath = tempDir.resolve("report");
        
        alertConfig = new RegressionAlert.AlertConfig(
            20.0,  // criticalThreshold
            10.0,  // warningThreshold
            3,     // consecutiveFailures
            alertLogPath,
            Optional.empty(),
            false  // disable console output
        );
        
        alert = new RegressionAlert(alertConfig);
    }

    @Test
    void shouldIntegrateWithBaselineComparison() throws Exception {
        // Given - Set up baseline
        String testName = "IntegrationTest";
        List<Double> baselineMetrics = Arrays.asList(1.0, 1.1, 1.0, 1.2, 1.1);
        PerformanceBaseline.saveBaseline(baselinePath, testName, baselineMetrics);

        // When - Compare degraded performance
        List<Double> currentMetrics = Arrays.asList(2.0, 2.1, 2.0, 2.2, 2.1);
        var result = PerformanceBaseline.compareWithBaseline(baselinePath, testName, currentMetrics);

        // Process through alert system
        for (int i = 0; i < alertConfig.consecutiveFailuresForAlert(); i++) {
            alert.processResult(result);
        }

        // Then
        assertTrue(Files.exists(alertLogPath), "Alert log should be created");
        String alertContent = Files.readString(alertLogPath, StandardCharsets.UTF_8);
        assertTrue(alertContent.contains("[CRITICAL]"), "Should detect critical regression");
        assertTrue(alertContent.contains(testName), "Should reference correct test");
    }

    @Test
    void shouldHandleConcurrentAlerts() throws Exception {
        // Given
        int numThreads = 5;
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        String testName = "ConcurrentTest";
        
        // Save baseline
        List<Double> baselineMetrics = Arrays.asList(1.0, 1.1, 1.0);
        PerformanceBaseline.saveBaseline(baselinePath, testName, baselineMetrics);

        // When - Generate concurrent alerts
        List<Double> degradedMetrics = Arrays.asList(2.0, 2.1, 2.0);
        var result = PerformanceBaseline.compareWithBaseline(baselinePath, testName, degradedMetrics);

        IntStream.range(0, numThreads).forEach(i -> 
            executor.submit(() -> {
                for (int j = 0; j < alertConfig.consecutiveFailuresForAlert(); j++) {
                    alert.processResult(result);
                }
            })
        );

        executor.shutdown();
        assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));

        // Then
        String alertContent = Files.readString(alertLogPath, StandardCharsets.UTF_8);
        long alertCount = alertContent.lines()
            .filter(line -> line.contains("[CRITICAL]"))
            .count();
        assertEquals(1, alertCount, "Should generate only one alert despite concurrent access");
    }

    @Test
    void shouldGenerateVisualizationWithAlerts() throws Exception {
        // Given
        String testName = "VisualizationTest";
        List<Double> baselineMetrics = Arrays.asList(1.0, 1.1, 1.2, 1.0, 1.1);
        List<Double> currentMetrics = Arrays.asList(2.0, 2.1, 2.2, 2.0, 2.1);

        // Create baseline and comparison
        PerformanceBaseline.saveBaseline(baselinePath, testName, baselineMetrics);
        var result = PerformanceBaseline.compareWithBaseline(baselinePath, testName, currentMetrics);

        // Generate alerts
        for (int i = 0; i < alertConfig.consecutiveFailuresForAlert(); i++) {
            alert.processResult(result);
        }

        // When - Generate visualization
        Path chartPath = reportPath.resolve("visualization.html");
        BaselineVisualization.generateComparisonChart(
            result,
            baselineMetrics,
            currentMetrics,
            chartPath
        );

        // Then
        assertTrue(Files.exists(chartPath), "Visualization file should be created");
        String chartContent = Files.readString(chartPath, StandardCharsets.UTF_8);
        assertTrue(chartContent.contains("regression"), "Should indicate regression");
        assertTrue(chartContent.contains("chart.js"), "Should include charting library");
        assertTrue(chartContent.contains("Recommendations"), "Should include recommendations");
    }

    @Test
    void shouldIntegrateWithStatisticalAnalysis() throws Exception {
        // Given
        String testName = "StatisticalTest";
        List<Double> baselineMetrics = Arrays.asList(1.0, 1.1, 1.0, 1.2, 1.1, 1.0, 1.1);
        List<Double> currentMetrics = Arrays.asList(1.5, 1.6, 1.5, 1.7, 1.6, 1.5, 1.6);

        // Analyze with statistics
        var baselineStats = PerformanceStatistics.analyze(baselineMetrics);
        var currentStats = PerformanceStatistics.analyze(currentMetrics);

        // Save baseline and compare
        PerformanceBaseline.saveBaseline(baselinePath, testName, baselineMetrics);
        var result = PerformanceBaseline.compareWithBaseline(baselinePath, testName, currentMetrics);

        // When - Process alerts
        for (int i = 0; i < alertConfig.consecutiveFailuresForAlert(); i++) {
            alert.processResult(result);
        }

        // Then
        String alertContent = Files.readString(alertLogPath, StandardCharsets.UTF_8);
        assertTrue(alertContent.contains("Standard deviation"), 
            "Should include statistical measures");
        assertTrue(alertContent.contains("variance"), 
            "Should include variability analysis");
        assertTrue(result.isRegression() == currentStats.isSignificantChange(),
            "Statistical and baseline regression detection should agree");
    }

    @Test
    void shouldHandleMultipleTestBaselines() throws Exception {
        // Given - Set up multiple baselines
        setupTestBaseline("Test1", 1.0, 2.0);
        setupTestBaseline("Test2", 2.0, 3.0);
        setupTestBaseline("Test3", 0.5, 0.6);

        // When - Generate alerts for all
        for (String testName : Arrays.asList("Test1", "Test2", "Test3")) {
            processTestResults(testName);
        }

        // Then
        String alertContent = Files.readString(alertLogPath, StandardCharsets.UTF_8);
        assertTrue(alertContent.contains("Test1"), "Should include Test1 alerts");
        assertTrue(alertContent.contains("Test2"), "Should include Test2 alerts");
        assertTrue(alertContent.contains("Test3"), "Should include Test3 alerts");
    }

    private void setupTestBaseline(String testName, double baselineValue, double currentValue) 
            throws Exception {
        List<Double> baselineMetrics = Arrays.asList(
            baselineValue, baselineValue + 0.1, baselineValue - 0.1
        );
        PerformanceBaseline.saveBaseline(baselinePath, testName, baselineMetrics);
    }

    private void processTestResults(String testName) throws Exception {
        List<Double> currentMetrics = Arrays.asList(2.0, 2.1, 2.0);
        var result = PerformanceBaseline.compareWithBaseline(baselinePath, testName, currentMetrics);
        
        for (int i = 0; i < alertConfig.consecutiveFailuresForAlert(); i++) {
            alert.processResult(result);
        }
    }
}
