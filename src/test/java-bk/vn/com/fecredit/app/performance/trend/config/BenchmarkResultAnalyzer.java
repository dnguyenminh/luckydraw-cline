package vn.com.fecredit.app.performance.trend.config;

import org.springframework.lang.NonNull;

import java.util.*;
import java.util.function.DoublePredicate;
import java.util.stream.Collectors;

/**
 * Analyzes and reports benchmark results.
 * Provides statistical analysis and threshold validation for benchmark metrics.
 */
public class BenchmarkResultAnalyzer {

    private final List<BenchmarkMetric> metrics;
    private final double confidenceLevel;
    private final Map<String, ThresholdValidator> validators;

    /**
     * Creates an analyzer with the specified confidence level.
     *
     * @param confidenceLevel Confidence level for statistical analysis (0.0-1.0)
     */
    public BenchmarkResultAnalyzer(double confidenceLevel) {
        if (confidenceLevel <= 0.0 || confidenceLevel >= 1.0) {
            throw new IllegalArgumentException("Confidence level must be between 0 and 1");
        }
        this.confidenceLevel = confidenceLevel;
        this.metrics = new ArrayList<>();
        this.validators = new HashMap<>();
    }

    /**
     * Adds a metric measurement to the analysis.
     *
     * @param name Name of the metric
     * @param value Measured value
     * @param unit Unit of measurement
     * @return this analyzer for chaining
     */
    public BenchmarkResultAnalyzer addMetric(@NonNull String name, double value, @NonNull String unit) {
        metrics.add(new BenchmarkMetric(name, value, unit));
        return this;
    }

    /**
     * Adds a threshold validator for a metric.
     *
     * @param metricName Name of the metric to validate
     * @param validator Validation predicate
     * @param description Description of the validation
     * @return this analyzer for chaining
     */
    public BenchmarkResultAnalyzer addValidator(
            @NonNull String metricName,
            @NonNull DoublePredicate validator,
            @NonNull String description) {
        validators.put(metricName, new ThresholdValidator(validator, description));
        return this;
    }

    /**
     * Performs analysis and generates a report.
     *
     * @return Analysis report
     */
    @NonNull
    public AnalysisReport analyze() {
        Map<String, List<BenchmarkMetric>> groupedMetrics = metrics.stream()
            .collect(Collectors.groupingBy(BenchmarkMetric::name));

        List<MetricSummary> summaries = new ArrayList<>();
        List<String> violations = new ArrayList<>();

        for (var entry : groupedMetrics.entrySet()) {
            String name = entry.getKey();
            List<BenchmarkMetric> measurements = entry.getValue();
            
            DoubleSummaryStatistics stats = measurements.stream()
                .mapToDouble(BenchmarkMetric::value)
                .summaryStatistics();

            double stdDev = calculateStdDev(measurements, stats.getAverage());
            double ci = calculateConfidenceInterval(stdDev, measurements.size());

            MetricSummary summary = new MetricSummary(
                name,
                stats.getAverage(),
                stats.getMin(),
                stats.getMax(),
                stdDev,
                ci,
                measurements.get(0).unit()
            );
            summaries.add(summary);

            // Check thresholds
            ThresholdValidator validator = validators.get(name);
            if (validator != null && !validator.predicate().test(stats.getAverage())) {
                violations.add(String.format("%s: %s (actual: %.2f %s)",
                    name, validator.description(), stats.getAverage(), measurements.get(0).unit()));
            }
        }

        return new AnalysisReport(summaries, violations);
    }

    private double calculateStdDev(List<BenchmarkMetric> metrics, double mean) {
        return Math.sqrt(metrics.stream()
            .mapToDouble(m -> Math.pow(m.value() - mean, 2))
            .average()
            .orElse(0.0));
    }

    private double calculateConfidenceInterval(double stdDev, int sampleSize) {
        // Using t-distribution with alpha=0.05 for small sample sizes
        double criticalValue = 1.96; // For large samples, using normal distribution
        return criticalValue * (stdDev / Math.sqrt(sampleSize));
    }

    /**
     * Represents a single benchmark measurement.
     */
    private record BenchmarkMetric(
        @NonNull String name,
        double value,
        @NonNull String unit
    ) {}

    /**
     * Represents a threshold validation rule.
     */
    private record ThresholdValidator(
        @NonNull DoublePredicate predicate,
        @NonNull String description
    ) {}

    /**
     * Statistical summary of a metric's measurements.
     */
    public record MetricSummary(
        @NonNull String name,
        double mean,
        double min,
        double max,
        double stdDev,
        double confidenceInterval,
        @NonNull String unit
    ) {
        @Override
        public String toString() {
            return String.format("%s: %.2f ± %.2f %s (min=%.2f, max=%.2f)",
                name, mean, confidenceInterval, unit, min, max);
        }
    }

    /**
     * Complete analysis report including summaries and violations.
     */
    public record AnalysisReport(
        @NonNull List<MetricSummary> summaries,
        @NonNull List<String> violations
    ) {
        /**
         * Checks if all thresholds were met.
         *
         * @return true if no violations were found
         */
        public boolean isSuccessful() {
            return violations.isEmpty();
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder("Benchmark Analysis Report\n");
            sb.append("=========================\n\n");
            
            sb.append("Metric Summaries:\n");
            summaries.forEach(s -> sb.append("  ").append(s).append('\n'));
            
            if (!violations.isEmpty()) {
                sb.append("\nThreshold Violations:\n");
                violations.forEach(v -> sb.append("  - ").append(v).append('\n'));
            }
            
            return sb.toString();
        }
    }
}
