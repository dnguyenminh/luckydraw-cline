package vn.com.fecredit.app.performance.trend;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Edge case tests for DefaultPerformanceTrendAnalyzer.
 * Tests behavior with extreme, unusual, and boundary input values.
 */
class DefaultPerformanceTrendAnalyzerEdgeCaseTest {

    private final PerformanceTrendAnalyzer analyzer = PerformanceTrendAnalyzer.create();

    @Test
    void shouldHandleExtremeValues() {
        // Given
        var dataPoints = List.of(
            new DataPoint(parseTime("2024-01-01T00:00:00"), Double.MAX_VALUE, 0.0),
            new DataPoint(parseTime("2024-01-02T00:00:00"), Double.MAX_VALUE / 2, 0.0),
            new DataPoint(parseTime("2024-01-03T00:00:00"), Double.MIN_VALUE, 0.0)
        );

        // When
        var result = analyzer.analyzeTrend(dataPoints);

        // Then
        assertNotNull(result.direction(), "Should determine direction even with extreme values");
        assertTrue(result.volatility() > 0, "Should calculate volatility for extreme values");
        assertFalse(Double.isNaN(result.avgChange()), "Average change should not be NaN");
        assertFalse(Double.isInfinite(result.avgChange()), "Average change should not be infinite");
    }

    @Test
    void shouldHandleAllZeroValues() {
        // Given
        var dataPoints = List.of(
            new DataPoint(parseTime("2024-01-01T00:00:00"), 0.0, 0.0),
            new DataPoint(parseTime("2024-01-02T00:00:00"), 0.0, 0.0),
            new DataPoint(parseTime("2024-01-03T00:00:00"), 0.0, 0.0)
        );

        // When
        var result = analyzer.analyzeTrend(dataPoints);

        // Then
        assertEquals(TrendDirection.STABLE, result.direction(), 
            "Should identify stable trend for all zero values");
        assertEquals(0.0, result.volatility(), "Volatility should be zero");
        assertEquals(0.0, result.avgChange(), "Average change should be zero");
    }

    @ParameterizedTest
    @MethodSource("provideSpecialValues")
    void shouldHandleSpecialValues(double value, String description) {
        // Given
        var dataPoints = List.of(
            new DataPoint(parseTime("2024-01-01T00:00:00"), value, 0.0),
            new DataPoint(parseTime("2024-01-02T00:00:00"), value * 1.1, 0.0),
            new DataPoint(parseTime("2024-01-03T00:00:00"), value * 1.2, 0.0)
        );

        // When/Then
        assertDoesNotThrow(() -> {
            var result = analyzer.analyzeTrend(dataPoints);
            assertNotNull(result, "Should handle " + description);
            assertFalse(Double.isNaN(result.confidence()),
                "Confidence should not be NaN for " + description);
        });
    }

    private static Stream<Arguments> provideSpecialValues() {
        return Stream.of(
            Arguments.of(Double.MIN_NORMAL, "minimum normal double"),
            Arguments.of(Double.MIN_VALUE, "minimum double value"),
            Arguments.of(1e-300, "very small number"),
            Arguments.of(1e300, "very large number")
        );
    }

    @Test
    void shouldHandleUnsortedData() {
        // Given
        var dataPoints = new ArrayList<>(List.of(
            new DataPoint(parseTime("2024-01-03T00:00:00"), 120.0, 0.0),
            new DataPoint(parseTime("2024-01-01T00:00:00"), 100.0, 0.0),
            new DataPoint(parseTime("2024-01-02T00:00:00"), 110.0, 0.0)
        ));

        // When
        var result = analyzer.analyzeTrend(dataPoints);

        // Then
        assertEquals(TrendDirection.DEGRADING, result.direction(),
            "Should correctly identify trend in unsorted data");
        assertTrue(result.avgChange() > 0, "Should calculate correct change direction");
    }

    @Test
    void shouldHandleDuplicateTimestamps() {
        // Given
        var timestamp = parseTime("2024-01-01T00:00:00");
        var dataPoints = List.of(
            new DataPoint(timestamp, 100.0, 0.0),
            new DataPoint(timestamp, 110.0, 0.0),
            new DataPoint(timestamp, 120.0, 0.0)
        );

        // When/Then
        assertDoesNotThrow(() -> {
            var result = analyzer.analyzeTrend(dataPoints);
            assertNotNull(result, "Should handle duplicate timestamps");
        });
    }

    @Test
    void shouldHandleNegativeValues() {
        // Given
        var dataPoints = List.of(
            new DataPoint(parseTime("2024-01-01T00:00:00"), -100.0, 0.0),
            new DataPoint(parseTime("2024-01-02T00:00:00"), -50.0, 0.0),
            new DataPoint(parseTime("2024-01-03T00:00:00"), -25.0, 0.0)
        );

        // When
        var result = analyzer.analyzeTrend(dataPoints);

        // Then
        assertEquals(TrendDirection.IMPROVING, result.direction(),
            "Should correctly identify improving trend with negative values");
        assertTrue(result.isSignificantTrend(),
            "Should detect significant trend with negative values");
    }

    @Test
    void shouldHandleAlternatingPositiveNegative() {
        // Given
        var dataPoints = List.of(
            new DataPoint(parseTime("2024-01-01T00:00:00"), 100.0, 0.0),
            new DataPoint(parseTime("2024-01-02T00:00:00"), -100.0, 0.0),
            new DataPoint(parseTime("2024-01-03T00:00:00"), 100.0, 0.0),
            new DataPoint(parseTime("2024-01-04T00:00:00"), -100.0, 0.0)
        );

        // When
        var result = analyzer.analyzeTrend(dataPoints);

        // Then
        assertEquals(TrendDirection.UNSTABLE, result.direction(),
            "Should identify unstable trend with alternating values");
        assertTrue(result.volatility() > 1.0,
            "Should indicate high volatility for alternating values");
    }

    @Test
    void shouldHandleMovingAverageEdgeCases() {
        // Given
        var dataPoints = List.of(
            new DataPoint(parseTime("2024-01-01T00:00:00"), Double.MAX_VALUE, 0.0),
            new DataPoint(parseTime("2024-01-02T00:00:00"), Double.MIN_VALUE, 0.0),
            new DataPoint(parseTime("2024-01-03T00:00:00"), 0.0, 0.0)
        );

        // When/Then
        assertAll(
            () -> assertThrows(IllegalArgumentException.class,
                () -> analyzer.calculateMovingAverage(dataPoints, 0),
                "Should reject zero window size"),
            
            () -> assertThrows(IllegalArgumentException.class,
                () -> analyzer.calculateMovingAverage(dataPoints, 4),
                "Should reject window size larger than data size"),
            
            () -> assertDoesNotThrow(() -> {
                var result = analyzer.calculateMovingAverage(dataPoints, 2);
                assertFalse(result.isEmpty(), "Should calculate moving average");
                assertTrue(result.stream().allMatch(p -> !Double.isNaN(p.value())),
                    "Should not produce NaN values");
            })
        );
    }

    private static LocalDateTime parseTime(String timestamp) {
        return LocalDateTime.parse(timestamp);
    }
}
