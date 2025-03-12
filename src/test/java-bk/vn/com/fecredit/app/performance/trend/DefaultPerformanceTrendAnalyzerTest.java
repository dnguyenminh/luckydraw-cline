package vn.com.fecredit.app.performance.trend;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for DefaultPerformanceTrendAnalyzer implementation.
 * Focuses on configuration-based behavior and thread safety.
 */
class DefaultPerformanceTrendAnalyzerTest {

    @Test
    void shouldUseCustomConfiguration() {
        // Given
        var config = new AnalyzerConfig(5, 0.15, 0.3, 0.9);
        var analyzer = new DefaultPerformanceTrendAnalyzer(config);
        var dataPoints = generateDataPoints(6, 100.0, 1.20); // 20% increase each point

        // When
        var result = analyzer.analyzeTrend(dataPoints);

        // Then
        assertTrue(result.isSignificantTrend(), "Should detect significant trend with custom threshold");
        assertEquals(TrendDirection.DEGRADING, result.direction(), "Should detect degrading trend");
        assertTrue(result.confidence() >= config.confidenceThreshold(), 
            "Confidence should meet threshold");
    }

    @Test
    void shouldRejectInsufficientDataPoints() {
        // Given
        var config = new AnalyzerConfig(5, 0.1, 0.2, 0.8);
        var analyzer = new DefaultPerformanceTrendAnalyzer(config);
        var dataPoints = generateDataPoints(4, 100.0, 1.1);

        // When/Then
        var ex = assertThrows(IllegalArgumentException.class,
            () -> analyzer.analyzeTrend(dataPoints),
            "Should reject insufficient data points");

        assertTrue(ex.getMessage().contains("5"),
            "Error message should mention required points");
    }

    @Test
    void shouldHandleHighVolatility() {
        // Given
        var config = new AnalyzerConfig(3, 0.1, 0.25, 0.8);
        var analyzer = new DefaultPerformanceTrendAnalyzer(config);
        var dataPoints = List.of(
            new DataPoint(parseTime("2024-01-01T00:00:00"), 100.0, 0.0),
            new DataPoint(parseTime("2024-01-02T00:00:00"), 150.0, 0.0),
            new DataPoint(parseTime("2024-01-03T00:00:00"), 90.0, 0.0),
            new DataPoint(parseTime("2024-01-04T00:00:00"), 140.0, 0.0)
        );

        // When
        var result = analyzer.analyzeTrend(dataPoints);

        // Then
        assertEquals(TrendDirection.UNSTABLE, result.direction(),
            "Should detect unstable trend due to high volatility");
        assertTrue(result.volatility() > config.volatilityThreshold(),
            "Volatility should exceed threshold");
    }

    @Test
    void shouldBeThreadSafe() throws InterruptedException {
        // Given
        var analyzer = new DefaultPerformanceTrendAnalyzer();
        var dataPoints = generateDataPoints(10, 100.0, 1.1);
        int numThreads = 10;
        var latch = new CountDownLatch(numThreads);
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        // When
        List<TrendResult> results = new ArrayList<>();
        for (int i = 0; i < numThreads; i++) {
            executor.submit(() -> {
                try {
                    results.add(analyzer.analyzeTrend(dataPoints));
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();
        executor.shutdown();

        // Then
        assertEquals(numThreads, results.size(), "Should process all requests");
        var firstResult = results.get(0);
        for (var result : results) {
            assertEquals(firstResult.direction(), result.direction(),
                "All threads should get same direction");
            assertEquals(firstResult.avgChange(), result.avgChange(),
                "All threads should get same average change");
            assertEquals(firstResult.volatility(), result.volatility(),
                "All threads should get same volatility");
        }
    }

    @ParameterizedTest(name = "Window size {0} should produce {1} points")
    @MethodSource("provideMovingAverageScenarios")
    void shouldCalculateMovingAverage(int windowSize, int expectedPoints, double[] values) {
        // Given
        var analyzer = new DefaultPerformanceTrendAnalyzer();
        var dataPoints = generateDataPoints(values);

        // When
        var result = analyzer.calculateMovingAverage(dataPoints, windowSize);

        // Then
        assertEquals(expectedPoints, result.size(),
            "Should produce correct number of points");
        assertTrue(result.stream().allMatch(p -> p.deviation() >= 0),
            "All deviations should be non-negative");
    }

    private static Stream<Arguments> provideMovingAverageScenarios() {
        return Stream.of(
            Arguments.of(2, 4, new double[]{100, 110, 105, 115, 108}),
            Arguments.of(3, 3, new double[]{100, 110, 105, 115, 108}),
            Arguments.of(4, 2, new double[]{100, 110, 105, 115, 108})
        );
    }

    private List<DataPoint> generateDataPoints(int count, double startValue, double factor) {
        List<DataPoint> points = new ArrayList<>();
        LocalDateTime baseTime = parseTime("2024-01-01T00:00:00");
        double currentValue = startValue;

        for (int i = 0; i < count; i++) {
            points.add(new DataPoint(baseTime.plusDays(i), currentValue, 0.0));
            currentValue *= factor;
        }

        return points;
    }

    private List<DataPoint> generateDataPoints(double[] values) {
        List<DataPoint> points = new ArrayList<>();
        LocalDateTime baseTime = parseTime("2024-01-01T00:00:00");

        for (int i = 0; i < values.length; i++) {
            points.add(new DataPoint(baseTime.plusDays(i), values[i], 0.0));
        }

        return points;
    }

    private static LocalDateTime parseTime(String timestamp) {
        return LocalDateTime.parse(timestamp);
    }
}
