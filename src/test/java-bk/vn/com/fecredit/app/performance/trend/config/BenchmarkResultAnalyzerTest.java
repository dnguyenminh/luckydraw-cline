package vn.com.fecredit.app.performance.trend.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class BenchmarkResultAnalyzerTest {

    private static final double CONFIDENCE_LEVEL = 0.95;
    private static final double DELTA = 0.0001;

    @Test
    void shouldAnalyzeSingleMetric() {
        // Given
        var analyzer = new BenchmarkResultAnalyzer(CONFIDENCE_LEVEL);
        analyzer.addMetric("latency", 100.0, "ms")
               .addMetric("latency", 110.0, "ms")
               .addMetric("latency", 90.0, "ms");

        // When
        var report = analyzer.analyze();

        // Then
        assertEquals(1, report.summaries().size(),
            "Should generate summary for single metric");
        var summary = report.summaries().get(0);
        assertAll(
            () -> assertEquals("latency", summary.name()),
            () -> assertEquals(100.0, summary.mean(), DELTA),
            () -> assertEquals(90.0, summary.min(), DELTA),
            () -> assertEquals(110.0, summary.max(), DELTA),
            () -> assertTrue(summary.stdDev() > 0,
                "Should calculate non-zero standard deviation")
        );
    }

    @Test
    void shouldValidateThresholds() {
        // Given
        var analyzer = new BenchmarkResultAnalyzer(CONFIDENCE_LEVEL);
        analyzer.addMetric("latency", 150.0, "ms")
               .addMetric("latency", 160.0, "ms")
               .addValidator("latency", v -> v < 100.0,
                   "Latency should be under 100ms");

        // When
        var report = analyzer.analyze();

        // Then
        assertFalse(report.isSuccessful(),
            "Should detect threshold violation");
        assertEquals(1, report.violations().size(),
            "Should report one violation");
        assertTrue(report.violations().get(0).contains("Latency should be under 100ms"),
            "Should include validation description");
    }

    @Test
    void shouldHandleMultipleMetrics() {
        // Given
        var analyzer = new BenchmarkResultAnalyzer(CONFIDENCE_LEVEL);
        analyzer.addMetric("latency", 100.0, "ms")
               .addMetric("throughput", 1000.0, "ops/s")
               .addMetric("latency", 110.0, "ms")
               .addMetric("throughput", 950.0, "ops/s");

        // When
        var report = analyzer.analyze();

        // Then
        assertEquals(2, report.summaries().size(),
            "Should generate summaries for both metrics");
        assertTrue(report.summaries().stream()
            .map(BenchmarkResultAnalyzer.MetricSummary::name)
            .allMatch(name -> name.equals("latency") || name.equals("throughput")),
            "Should include both metric names");
    }

    @ParameterizedTest
    @ValueSource(doubles = {0.0, -0.1, 1.0, 1.1})
    void shouldRejectInvalidConfidenceLevels(double level) {
        assertThrows(IllegalArgumentException.class,
            () -> new BenchmarkResultAnalyzer(level),
            "Should reject invalid confidence level: " + level);
    }

    @Test
    void shouldGenerateReadableReport() {
        // Given
        var analyzer = new BenchmarkResultAnalyzer(CONFIDENCE_LEVEL);
        analyzer.addMetric("latency", 100.0, "ms")
               .addMetric("latency", 110.0, "ms")
               .addValidator("latency", v -> v < 90.0,
                   "Latency threshold exceeded");

        // When
        var report = analyzer.analyze();
        String reportString = report.toString();

        // Then
        assertAll(
            () -> assertTrue(reportString.contains("Benchmark Analysis Report"),
                "Should include report title"),
            () -> assertTrue(reportString.contains("latency"),
                "Should include metric name"),
            () -> assertTrue(reportString.contains("Threshold Violations"),
                "Should include violations section"),
            () -> assertTrue(reportString.contains("Latency threshold exceeded"),
                "Should include validation message")
        );
    }

    @Test
    void shouldCalculateConfidenceIntervals() {
        // Given
        var analyzer = new BenchmarkResultAnalyzer(CONFIDENCE_LEVEL);
        List<Double> values = List.of(100.0, 110.0, 90.0, 105.0, 95.0);
        values.forEach(v -> analyzer.addMetric("test", v, "units"));

        // When
        var report = analyzer.analyze();
        var summary = report.summaries().get(0);

        // Then
        assertAll(
            () -> assertTrue(summary.confidenceInterval() > 0,
                "Should calculate positive confidence interval"),
            () -> assertTrue(summary.confidenceInterval() < summary.stdDev(),
                "Confidence interval should be smaller than standard deviation"),
            () -> assertEquals(100.0, summary.mean(), DELTA,
                "Should calculate correct mean")
        );
    }

    @Test
    void shouldHandleZeroDeviation() {
        // Given
        var analyzer = new BenchmarkResultAnalyzer(CONFIDENCE_LEVEL);
        analyzer.addMetric("constant", 100.0, "units")
               .addMetric("constant", 100.0, "units")
               .addMetric("constant", 100.0, "units");

        // When
        var report = analyzer.analyze();
        var summary = report.summaries().get(0);

        // Then
        assertAll(
            () -> assertEquals(0.0, summary.stdDev(), DELTA,
                "Should calculate zero standard deviation"),
            () -> assertEquals(0.0, summary.confidenceInterval(), DELTA,
                "Should calculate zero confidence interval"),
            () -> assertEquals(summary.min(), summary.max(),
                "Min and max should be equal")
        );
    }

    @Test
    void shouldPreserveMetricUnits() {
        // Given
        var analyzer = new BenchmarkResultAnalyzer(CONFIDENCE_LEVEL);
        analyzer.addMetric("metric1", 100.0, "ms")
               .addMetric("metric2", 1000.0, "ops/s");

        // When
        var report = analyzer.analyze();

        // Then
        assertTrue(report.summaries().stream()
            .map(BenchmarkResultAnalyzer.MetricSummary::unit)
            .allMatch(unit -> unit.equals("ms") || unit.equals("ops/s")),
            "Should preserve original units");
    }

    @Test
    void shouldRejectNullInputs() {
        // Given
        var analyzer = new BenchmarkResultAnalyzer(CONFIDENCE_LEVEL);

        // Then
        assertAll(
            () -> assertThrows(NullPointerException.class,
                () -> analyzer.addMetric(null, 100.0, "ms"),
                "Should reject null metric name"),
            () -> assertThrows(NullPointerException.class,
                () -> analyzer.addMetric("test", 100.0, null),
                "Should reject null unit"),
            () -> assertThrows(NullPointerException.class,
                () -> analyzer.addValidator(null, d -> true, "description"),
                "Should reject null validator name"),
            () -> assertThrows(NullPointerException.class,
                () -> analyzer.addValidator("test", null, "description"),
                "Should reject null predicate"),
            () -> assertThrows(NullPointerException.class,
                () -> analyzer.addValidator("test", d -> true, null),
                "Should reject null description")
        );
    }
}
