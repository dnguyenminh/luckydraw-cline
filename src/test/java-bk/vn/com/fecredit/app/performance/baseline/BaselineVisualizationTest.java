package vn.com.fecredit.app.performance.baseline;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BaselineVisualizationTest {

    @TempDir
    Path tempDir;

    private Path outputPath;
    private PerformanceBaseline.BaselineResult sampleResult;
    private List<Double> baselineHistory;
    private List<Double> currentHistory;

    @BeforeEach
    void setUp() {
        outputPath = tempDir.resolve("baseline-comparison.html");
        baselineHistory = Arrays.asList(1.0, 1.1, 1.2, 1.0, 1.1);
        currentHistory = Arrays.asList(1.2, 1.3, 1.4, 1.2, 1.3);

        sampleResult = new PerformanceBaseline.BaselineResult(
            "TestOperation",
            1.08, // baselineMean
            1.15, // baselineP95
            1.28, // currentMean
            1.35, // currentP95
            18.5, // deviation
            true, // isRegression
            "Performance has degraded significantly",
            LocalDateTime.now().minusDays(1),
            LocalDateTime.now()
        );
    }

    @Test
    void shouldGenerateComparisonChart() throws Exception {
        // When
        BaselineVisualization.generateComparisonChart(
            sampleResult,
            baselineHistory,
            currentHistory,
            outputPath
        );

        // Then
        assertTrue(Files.exists(outputPath));
        String content = Files.readString(outputPath, StandardCharsets.UTF_8);
        
        // Verify HTML structure
        assertTrue(content.contains("<!DOCTYPE html>"));
        assertTrue(content.contains("<html>"));
        assertTrue(content.contains("</html>"));

        // Verify chart.js inclusion
        assertTrue(content.contains("chart.js"));
        
        // Verify content elements
        assertTrue(content.contains("Performance Baseline Comparison"));
        assertTrue(content.contains(sampleResult.testName()));
        assertTrue(content.contains("regression"));
    }

    @Test
    void shouldIncludeAllMetrics() throws Exception {
        // When
        BaselineVisualization.generateComparisonChart(
            sampleResult,
            baselineHistory,
            currentHistory,
            outputPath
        );

        String content = Files.readString(outputPath, StandardCharsets.UTF_8);

        // Then - verify all metrics are included
        assertTrue(content.contains(String.format("%.2f seconds", sampleResult.baselineMean())));
        assertTrue(content.contains(String.format("%.2f seconds", sampleResult.currentMean())));
        assertTrue(content.contains(String.format("%.2f seconds", sampleResult.baselineP95())));
        assertTrue(content.contains(String.format("%.2f seconds", sampleResult.currentP95())));
        assertTrue(content.contains(String.format("%.1f%%", sampleResult.deviation())));
    }

    @Test
    void shouldHandleEmptyHistories() throws Exception {
        // When
        BaselineVisualization.generateComparisonChart(
            sampleResult,
            Arrays.asList(),
            Arrays.asList(),
            outputPath
        );

        // Then
        assertTrue(Files.exists(outputPath));
        String content = Files.readString(outputPath, StandardCharsets.UTF_8);
        assertTrue(content.contains("data: []")); // Empty datasets should be handled
    }

    @Test
    void shouldIncludeDistributionChart() throws Exception {
        // When
        BaselineVisualization.generateComparisonChart(
            sampleResult,
            baselineHistory,
            currentHistory,
            outputPath
        );

        String content = Files.readString(outputPath, StandardCharsets.UTF_8);

        // Then
        assertTrue(content.contains("distributionChart"));
        assertTrue(content.contains("Performance Distribution Comparison"));
        assertTrue(content.contains("Min"));
        assertTrue(content.contains("P95"));
        assertTrue(content.contains("Max"));
    }

    @Test
    void shouldStyleBasedOnPerformance() throws Exception {
        // Test regression case
        var regressionResult = new PerformanceBaseline.BaselineResult(
            "RegressionTest", 1.0, 1.1, 2.0, 2.1, 100.0, true,
            "Regression", LocalDateTime.now().minusDays(1), LocalDateTime.now()
        );

        BaselineVisualization.generateComparisonChart(
            regressionResult,
            baselineHistory,
            currentHistory,
            outputPath
        );

        String content = Files.readString(outputPath, StandardCharsets.UTF_8);
        assertTrue(content.contains("class=\"regression\""));

        // Test improvement case
        var improvementResult = new PerformanceBaseline.BaselineResult(
            "ImprovementTest", 2.0, 2.1, 1.0, 1.1, -50.0, false,
            "Improvement", LocalDateTime.now().minusDays(1), LocalDateTime.now()
        );

        Path improvementPath = tempDir.resolve("improvement.html");
        BaselineVisualization.generateComparisonChart(
            improvementResult,
            baselineHistory,
            currentHistory,
            improvementPath
        );

        content = Files.readString(improvementPath, StandardCharsets.UTF_8);
        assertTrue(content.contains("class=\"improvement\""));
    }

    @Test
    void shouldHandleLongHistories() throws Exception {
        // Create longer histories to test scaling
        List<Double> longBaseline = Arrays.asList(
            1.0, 1.1, 1.0, 1.2, 1.1, 1.0, 1.1, 1.2, 1.0, 1.1,
            1.2, 1.0, 1.1, 1.0, 1.2, 1.1, 1.0, 1.1, 1.2, 1.0
        );
        List<Double> longCurrent = Arrays.asList(
            1.2, 1.3, 1.2, 1.4, 1.3, 1.2, 1.3, 1.4, 1.2, 1.3,
            1.4, 1.2, 1.3, 1.2, 1.4, 1.3, 1.2, 1.3, 1.4, 1.2
        );

        BaselineVisualization.generateComparisonChart(
            sampleResult,
            longBaseline,
            longCurrent,
            outputPath
        );

        assertTrue(Files.exists(outputPath));
        String content = Files.readString(outputPath, StandardCharsets.UTF_8);
        assertTrue(content.contains("Sample 20")); // Should handle 20 samples
    }

    @Test
    void shouldHandleNullValues() throws Exception {
        // Create histories with null values
        List<Double> historyWithNulls = Arrays.asList(1.0, null, 1.2, null, 1.1);
        
        assertDoesNotThrow(() -> BaselineVisualization.generateComparisonChart(
            sampleResult,
            historyWithNulls,
            currentHistory,
            outputPath
        ));
    }
}
