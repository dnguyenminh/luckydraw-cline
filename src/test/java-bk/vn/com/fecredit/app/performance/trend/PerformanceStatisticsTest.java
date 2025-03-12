package vn.com.fecredit.app.performance.trend;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class PerformanceStatisticsTest {

    @Test
    void shouldCalculateBasicStatistics() {
        // Given
        List<Double> metrics = Arrays.asList(1.0, 2.0, 3.0, 4.0, 5.0);

        // When
        var result = PerformanceStatistics.analyze(metrics);

        // Then
        assertEquals(3.0, result.mean(), 0.001, "Mean should be correct");
        assertEquals(3.0, result.median(), 0.001, "Median should be correct");
        assertEquals(5.0, result.p95(), 0.001, "P95 should be correct");
        assertEquals(1.581, result.standardDeviation(), 0.001, "Standard deviation should be correct");
        assertEquals(0.527, result.coefficientOfVariation(), 0.001, "CV should be correct");
    }

    @Test
    void shouldHandleRepeatedValues() {
        // Given
        List<Double> metrics = Arrays.asList(1.0, 1.0, 1.0, 1.0, 1.0);

        // When
        var result = PerformanceStatistics.analyze(metrics);

        // Then
        assertEquals(1.0, result.mean());
        assertEquals(1.0, result.median());
        assertEquals(1.0, result.p95());
        assertEquals(0.0, result.standardDeviation());
        assertEquals(0.0, result.coefficientOfVariation());
        assertFalse(result.isSignificantChange(), "No variation should not be significant");
    }

    @Test
    void shouldHandleBoundaryPerformanceValues() {
        // Given
        List<Double> metrics = Arrays.asList(0.001, 0.002, 9999.999);

        // When
        var result = PerformanceStatistics.analyze(metrics);

        // Then
        assertTrue(result.isSignificantChange(), "Extreme variations should be significant");
        assertEquals(9999.999, result.p95(), 0.001, "P95 should handle large values");
        assertTrue(result.standardDeviation() > 5000, "Large std dev for wide range");
    }

    @Test
    void shouldDetectSignificantChange() {
        // Given - Values with high variation relative to mean
        List<Double> metrics = Arrays.asList(10.0, 11.0, 12.0, 13.0, 14.0);

        // When
        var result = PerformanceStatistics.analyze(metrics);

        // Then
        assertTrue(result.isSignificantChange(), "Should detect significant variation");
        assertTrue(result.standardDeviation() > 0, "Should have positive std dev");
        assertTrue(result.coefficientOfVariation() > 0, "Should have positive CV");
    }

    @Test
    void shouldHandleEdgeCases() {
        // Given - Single value
        List<Double> singleMetric = Arrays.asList(1.0);
        
        // When
        var result = PerformanceStatistics.analyze(singleMetric);

        // Then
        assertEquals(1.0, result.mean());
        assertEquals(1.0, result.median());
        assertEquals(1.0, result.p95());
        assertEquals(0.0, result.standardDeviation());
        assertFalse(result.isSignificantChange(), "Single value should not be significant");
    }

    @Test
    void shouldRejectNullMetrics() {
        assertThrows(IllegalArgumentException.class,
            () -> PerformanceStatistics.analyze(null),
            "Should reject null metrics"
        );
    }

    @Test
    void shouldRejectEmptyMetrics() {
        assertThrows(IllegalArgumentException.class,
            () -> PerformanceStatistics.analyze(Arrays.asList()),
            "Should reject empty metrics"
        );
    }

    @ParameterizedTest
    @MethodSource("provideSampleSets")
    void shouldCalculateCorrectPercentiles(List<Double> input, double expectedP95) {
        var result = PerformanceStatistics.analyze(input);
        assertEquals(expectedP95, result.p95(), 0.001,
            "P95 calculation should be correct for various sample sizes");
    }

    private static Stream<Arguments> provideSampleSets() {
        return Stream.of(
            Arguments.of(Arrays.asList(1.0, 2.0, 3.0, 4.0, 5.0), 5.0),
            Arguments.of(Arrays.asList(1.0, 1.0, 2.0, 2.0, 3.0, 3.0, 4.0, 4.0, 5.0, 5.0), 5.0),
            Arguments.of(Arrays.asList(1.0, 2.0, 3.0), 3.0),
            Arguments.of(Arrays.asList(1.0, 1.0, 1.0, 1.0, 2.0), 2.0)
        );
    }

    @Test
    void shouldHandleSignificanceThreshold() {
        // Given - Values with minimal variation
        List<Double> stableMetrics = Arrays.asList(1.0, 1.1, 0.9, 1.0, 1.1);
        
        // When
        var result = PerformanceStatistics.analyze(stableMetrics);

        // Then
        assertFalse(result.isSignificantChange(),
            "Small variations should not be considered significant");
        assertTrue(result.coefficientOfVariation() < 0.1,
            "CV should be small for stable metrics");
    }

    @Test
    void shouldCalculateAccurateVariability() {
        // Given - Known distribution with calculated values
        List<Double> metrics = Arrays.asList(10.0, 12.0, 8.0, 11.0, 9.0);

        // When
        var result = PerformanceStatistics.analyze(metrics);

        // Then
        assertEquals(10.0, result.mean(), 0.001, "Mean should be exactly 10.0");
        assertTrue(result.standardDeviation() > 1.4 && result.standardDeviation() < 1.7,
            "Standard deviation should be approximately 1.58");
        assertTrue(result.coefficientOfVariation() > 0.14 && result.coefficientOfVariation() < 0.17,
            "CV should be approximately 0.158");
    }

    @Test
    void shouldHandleOutliers() {
        // Given - Data with outliers
        List<Double> metrics = Arrays.asList(1.0, 1.1, 1.0, 1.2, 1.1, 10.0);

        // When
        var result = PerformanceStatistics.analyze(metrics);

        // Then
        assertTrue(result.mean() > result.median(),
            "Mean should be pulled up by outlier");
        assertEquals(10.0, result.p95(),
            "P95 should capture the outlier");
        assertTrue(result.isSignificantChange(),
            "Outlier should trigger significance");
    }

    @Test
    void shouldHandlePrecisionLimits() {
        // Given - Very small and very large numbers
        List<Double> metrics = Arrays.asList(0.0000001, 0.0000002, 1000000.0);

        // When
        var result = PerformanceStatistics.analyze(metrics);

        // Then
        assertTrue(result.mean() > 0,
            "Mean should handle very small numbers");
        assertTrue(result.p95() > 100000,
            "P95 should handle very large numbers");
        assertTrue(result.isSignificantChange(),
            "Extreme value ranges should be significant");
    }
}
