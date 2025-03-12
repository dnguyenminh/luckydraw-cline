package vn.com.fecredit.app.performance.trend;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for PerformanceTrendAnalyzer functionality.
 * Verifies trend detection, moving average calculation, and insight generation.
 */
class PerformanceTrendAnalyzerTest {

    private PerformanceTrendAnalyzer analyzer;

    @BeforeEach
    void setUp() {
        analyzer = new PerformanceTrendAnalyzer();
    }
    
    @Test
    void shouldDetectDegradingTrend() {
        // Given
        var dataPoints = List.of(
            new DataPoint(parseTime("2024-01-01T00:00:00"), 100.0, 0.0),
            new DataPoint(parseTime("2024-01-02T00:00:00"), 110.0, 0.0),
            new DataPoint(parseTime("2024-01-03T00:00:00"), 120.0, 0.0),
            new DataPoint(parseTime("2024-01-04T00:00:00"), 135.0, 0.0)
        );

        // When
        var result = analyzer.analyzeTrend(dataPoints);

        // Then
        assertEquals(TrendDirection.DEGRADING, result.direction(),
            "Should detect degrading performance trend");
        assertTrue(result.isSignificantTrend(), "Changes should be significant");
        assertTrue(result.avgChange() > 0, "Average change should be positive");
        assertInsightContains(result, "degrading");
    }

    @Test
    void shouldDetectImprovingTrend() {
        // Given
        var dataPoints = List.of(
            new DataPoint(parseTime("2024-01-01T00:00:00"), 100.0, 0.0),
            new DataPoint(parseTime("2024-01-02T00:00:00"), 90.0, 0.0),
            new DataPoint(parseTime("2024-01-03T00:00:00"), 85.0, 0.0),
            new DataPoint(parseTime("2024-01-04T00:00:00"), 80.0, 0.0)
        );

        // When
        var result = analyzer.analyzeTrend(dataPoints);

        // Then
        assertEquals(TrendDirection.IMPROVING, result.direction(),
            "Should detect improving performance trend");
        assertTrue(result.isSignificantTrend(), "Changes should be significant");
        assertTrue(result.avgChange() < 0, "Average change should be negative");
        assertInsightContains(result, "improving");
    }

    @Test
    void shouldDetectStableTrend() {
        // Given
        var dataPoints = List.of(
            new DataPoint(parseTime("2024-01-01T00:00:00"), 100.0, 0.0),
            new DataPoint(parseTime("2024-01-02T00:00:00"), 101.0, 0.0),
            new DataPoint(parseTime("2024-01-03T00:00:00"), 99.0, 0.0),
            new DataPoint(parseTime("2024-01-04T00:00:00"), 100.5, 0.0)
        );

        // When
        var result = analyzer.analyzeTrend(dataPoints);

        // Then
        assertEquals(TrendDirection.STABLE, result.direction(),
            "Should detect stable performance");
        assertFalse(result.isSignificantTrend(), "Changes should not be significant");
        assertTrue(Math.abs(result.avgChange()) < 0.1, "Average change should be small");
        assertInsightContains(result, "stable");
    }

    @Test
    void shouldDetectUnstableTrend() {
        // Given
        var dataPoints = List.of(
            new DataPoint(parseTime("2024-01-01T00:00:00"), 100.0, 0.0),
            new DataPoint(parseTime("2024-01-02T00:00:00"), 150.0, 0.0),
            new DataPoint(parseTime("2024-01-03T00:00:00"), 90.0, 0.0),
            new DataPoint(parseTime("2024-01-04T00:00:00"), 130.0, 0.0)
        );

        // When
        var result = analyzer.analyzeTrend(dataPoints);

        // Then
        assertEquals(TrendDirection.UNSTABLE, result.direction(),
            "Should detect unstable performance");
        assertTrue(result.volatility() > 0.2, "Volatility should be high");
        assertInsightContains(result, "unstable");
    }

    @Test
    void shouldCalculateMovingAverage() {
        // Given
        var dataPoints = List.of(
            new DataPoint(parseTime("2024-01-01T00:00:00"), 100.0, 0.0),
            new DataPoint(parseTime("2024-01-02T00:00:00"), 110.0, 0.0),
            new DataPoint(parseTime("2024-01-03T00:00:00"), 90.0, 0.0),
            new DataPoint(parseTime("2024-01-04T00:00:00"), 120.0, 0.0),
            new DataPoint(parseTime("2024-01-05T00:00:00"), 95.0, 0.0)
        );

        // When
        var movingAvg = analyzer.calculateMovingAverage(dataPoints, 3);

        // Then
        assertEquals(3, movingAvg.size(), "Should have correct number of points");
        assertTrue(movingAvg.get(0).value() > 95 && movingAvg.get(0).value() < 105,
            "First moving average should be around 100");
        assertNotEquals(0.0, movingAvg.get(0).deviation(), "Should calculate deviation");
    }

    @Test
    void shouldHandleEdgeCases() {
        // Given - Minimal data points
        var minimalPoints = List.of(
            new DataPoint(parseTime("2024-01-01T00:00:00"), 100.0, 0.0),
            new DataPoint(parseTime("2024-01-02T00:00:00"), 110.0, 0.0),
            new DataPoint(parseTime("2024-01-03T00:00:00"), 120.0, 0.0)
        );

        // When/Then
        assertDoesNotThrow(() -> analyzer.analyzeTrend(minimalPoints),
            "Should handle minimal data points");
        
        // Given - Invalid data
        var invalidPoints = List.of(
            new DataPoint(parseTime("2024-01-01T00:00:00"), 100.0, 0.0),
            new DataPoint(parseTime("2024-01-02T00:00:00"), 110.0, 0.0)
        );

        // When/Then
        assertThrows(IllegalArgumentException.class,
            () -> analyzer.analyzeTrend(invalidPoints),
            "Should reject insufficient data points");
    }

    @ParameterizedTest
    @MethodSource("provideWindowSizes")
    void shouldHandleVariousWindowSizes(int windowSize, int expectedResults) {
        // Given
        var dataPoints = generateSequentialPoints(10);

        // When
        var result = analyzer.calculateMovingAverage(dataPoints, windowSize);

        // Then
        assertEquals(expectedResults, result.size(),
            "Should generate correct number of points");
        assertFalse(result.isEmpty(), "Should not be empty");
        assertTrue(result.stream().allMatch(p -> p.deviation() >= 0),
            "All deviations should be non-negative");
    }

    private static Stream<Arguments> provideWindowSizes() {
        return Stream.of(
            Arguments.of(2, 9),
            Arguments.of(3, 8),
            Arguments.of(5, 6)
        );
    }

    private static LocalDateTime parseTime(String timestamp) {
        return LocalDateTime.parse(timestamp);
    }

    private static List<DataPoint> generateSequentialPoints(int count) {
        var points = new ArrayList<DataPoint>();
        var baseTime = parseTime("2024-01-01T00:00:00");
        
        for (int i = 0; i < count; i++) {
            points.add(new DataPoint(
                baseTime.plusDays(i),
                100.0 + i,
                0.0
            ));
        }
        
        return points;
    }

    private static void assertInsightContains(TrendResult result, String expected) {
        assertFalse(result.insights().isEmpty(), "Should have insights");
        assertTrue(result.insights().stream().anyMatch(i -> i.contains(expected)),
            "Should contain '" + expected + "' in insights");
    }
}
