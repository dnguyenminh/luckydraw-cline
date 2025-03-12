package vn.com.fecredit.app.performance.baseline;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PerformanceBaselineTest {

    @TempDir
    Path tempDir;
    private Path baselineFile;

    @BeforeEach
    void setUp() {
        baselineFile = tempDir.resolve("test-baseline.json");
    }

    @Test
    void shouldSaveAndLoadBaseline() throws Exception {
        // Given
        String testName = "SimpleTest";
        List<Double> metrics = Arrays.asList(1.0, 1.1, 1.2, 1.0, 1.1);

        // When
        PerformanceBaseline.saveBaseline(baselineFile, testName, metrics);

        // Then - Should be able to compare against saved baseline
        var result = PerformanceBaseline.compareWithBaseline(baselineFile, testName, metrics);
        
        assertEquals(testName, result.testName());
        assertEquals(1.08, result.baselineMean(), 0.01); // Average of metrics
        assertFalse(result.isRegression());
    }

    @Test
    void shouldDetectPerformanceRegression() throws Exception {
        // Given
        String testName = "RegressionTest";
        List<Double> baselineMetrics = Arrays.asList(1.0, 1.1, 1.0, 1.2, 1.1);
        List<Double> degradedMetrics = Arrays.asList(2.0, 2.1, 2.0, 2.2, 2.1); // 100% slower

        // When
        PerformanceBaseline.saveBaseline(baselineFile, testName, baselineMetrics);
        var result = PerformanceBaseline.compareWithBaseline(baselineFile, testName, degradedMetrics);

        // Then
        assertTrue(result.isRegression(), "Should detect significant regression");
        assertTrue(result.deviation() > 90.0, "Should show large deviation");
        assertTrue(result.analysis().contains("increased"), "Analysis should indicate increase");
    }

    @Test
    void shouldHandlePerformanceImprovement() throws Exception {
        // Given
        String testName = "ImprovementTest";
        List<Double> baselineMetrics = Arrays.asList(2.0, 2.1, 2.0, 2.2, 2.1);
        List<Double> improvedMetrics = Arrays.asList(1.0, 1.1, 1.0, 1.2, 1.1); // 50% faster

        // When
        PerformanceBaseline.saveBaseline(baselineFile, testName, baselineMetrics);
        var result = PerformanceBaseline.compareWithBaseline(baselineFile, testName, improvedMetrics);

        // Then
        assertFalse(result.isRegression(), "Should not flag improvement as regression");
        assertTrue(result.deviation() < 0, "Should show negative deviation");
        assertTrue(result.analysis().contains("decreased"), "Analysis should indicate decrease");
    }

    @Test
    void shouldHandleStablePerformance() throws Exception {
        // Given
        String testName = "StableTest";
        List<Double> baselineMetrics = Arrays.asList(1.0, 1.1, 1.0, 1.2, 1.1);
        List<Double> stableMetrics = Arrays.asList(1.05, 1.15, 1.05, 1.25, 1.15); // ~5% variation

        // When
        PerformanceBaseline.saveBaseline(baselineFile, testName, baselineMetrics);
        var result = PerformanceBaseline.compareWithBaseline(baselineFile, testName, stableMetrics);

        // Then
        assertFalse(result.isRegression(), "Should not detect regression for stable performance");
        assertTrue(Math.abs(result.deviation()) < 10.0, "Deviation should be small");
        assertTrue(result.analysis().contains("stable"), "Analysis should indicate stability");
    }

    @Test
    void shouldRejectEmptyMetrics() {
        // Given
        String testName = "EmptyTest";
        List<Double> emptyMetrics = Arrays.asList();

        // Then
        assertThrows(IllegalArgumentException.class,
            () -> PerformanceBaseline.saveBaseline(baselineFile, testName, emptyMetrics),
            "Should reject empty metrics"
        );
    }

    @Test
    void shouldHandleNonexistentBaseline() {
        // Given
        String testName = "NonexistentTest";
        List<Double> metrics = Arrays.asList(1.0, 1.1, 1.0);

        // Then
        assertThrows(IllegalStateException.class,
            () -> PerformanceBaseline.compareWithBaseline(baselineFile, testName, metrics),
            "Should handle nonexistent baseline file"
        );
    }

    @Test
    void shouldHandleMultipleBaselines() throws Exception {
        // Given
        String test1 = "Test1";
        String test2 = "Test2";
        List<Double> metrics1 = Arrays.asList(1.0, 1.1, 1.0);
        List<Double> metrics2 = Arrays.asList(2.0, 2.1, 2.0);

        // When
        PerformanceBaseline.saveBaseline(baselineFile, test1, metrics1);
        PerformanceBaseline.saveBaseline(baselineFile, test2, metrics2);

        // Then
        var result1 = PerformanceBaseline.compareWithBaseline(baselineFile, test1, metrics1);
        var result2 = PerformanceBaseline.compareWithBaseline(baselineFile, test2, metrics2);

        assertEquals(test1, result1.testName());
        assertEquals(test2, result2.testName());
        assertEquals(1.03, result1.baselineMean(), 0.01);
        assertEquals(2.03, result2.baselineMean(), 0.01);
    }

    @Test
    void shouldUpdateExistingBaseline() throws Exception {
        // Given
        String testName = "UpdateTest";
        List<Double> initialMetrics = Arrays.asList(1.0, 1.1, 1.0);
        List<Double> updatedMetrics = Arrays.asList(2.0, 2.1, 2.0);

        // When
        PerformanceBaseline.saveBaseline(baselineFile, testName, initialMetrics);
        var initialResult = PerformanceBaseline.compareWithBaseline(baselineFile, testName, updatedMetrics);
        
        PerformanceBaseline.saveBaseline(baselineFile, testName, updatedMetrics);
        var updatedResult = PerformanceBaseline.compareWithBaseline(baselineFile, testName, updatedMetrics);

        // Then
        assertTrue(initialResult.isRegression(), "Should detect regression against initial baseline");
        assertFalse(updatedResult.isRegression(), "Should not detect regression against updated baseline");
    }
}
