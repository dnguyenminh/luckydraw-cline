package vn.com.fecredit.app.performance.trend;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Statistical analysis utility for performance metrics.
 * Provides detailed statistical measures for performance data.
 */
public class PerformanceStatistics {
    
    // private static final double SIGNIFICANCE_THRESHOLD = 0.05;
    private static final int MIN_SAMPLE_SIZE = 5;
    private static final double T_CRITICAL_VALUE = 2.0;

    public record StatisticalResult(
        double mean,
        double median,
        double p95,
        double standardDeviation,
        double coefficientOfVariation,
        boolean isSignificantChange
    ) {}

    /**
     * Analyzes a set of performance metrics and calculates statistical measures.
     *
     * @param metrics List of performance measurements
     * @return Statistical analysis results
     * @throws IllegalArgumentException if metrics are null or empty
     */
    public static StatisticalResult analyze(List<Double> metrics) {
        validateMetrics(metrics);

        List<Double> sortedMetrics = new ArrayList<>(metrics);
        Collections.sort(sortedMetrics);

        double mean = calculateMean(sortedMetrics);
        double median = calculateMedian(sortedMetrics);
        double p95 = calculatePercentile(sortedMetrics, 95);
        double stdDev = calculateStandardDeviation(sortedMetrics, mean);
        double cv = calculateCV(mean, stdDev);
        boolean isSignificant = isChangeSignificant(sortedMetrics, mean, stdDev);

        return new StatisticalResult(
            mean,
            median,
            p95,
            stdDev,
            cv,
            isSignificant
        );
    }

    private static void validateMetrics(List<Double> metrics) {
        if (metrics == null || metrics.isEmpty()) {
            throw new IllegalArgumentException("Metrics cannot be null or empty");
        }

        if (metrics.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("Metrics cannot contain null values");
        }
    }

    private static double calculateMean(List<Double> metrics) {
        return metrics.stream()
            .mapToDouble(Double::doubleValue)
            .average()
            .orElse(0.0);
    }

    private static double calculateMedian(List<Double> sortedMetrics) {
        int size = sortedMetrics.size();
        if (size % 2 == 0) {
            return (sortedMetrics.get(size/2 - 1) + sortedMetrics.get(size/2)) / 2.0;
        } else {
            return sortedMetrics.get(size/2);
        }
    }

    private static double calculatePercentile(List<Double> sortedMetrics, int percentile) {
        if (percentile < 0 || percentile > 100) {
            throw new IllegalArgumentException("Percentile must be between 0 and 100");
        }

        if (sortedMetrics.size() == 1) {
            return sortedMetrics.get(0);
        }

        double rank = (percentile / 100.0) * (sortedMetrics.size() - 1);
        int lowerIndex = (int) Math.floor(rank);
        int upperIndex = (int) Math.ceil(rank);

        if (lowerIndex == upperIndex) {
            return sortedMetrics.get(lowerIndex);
        }

        double weight = rank - lowerIndex;
        return sortedMetrics.get(lowerIndex) * (1 - weight) + sortedMetrics.get(upperIndex) * weight;
    }

    private static double calculateStandardDeviation(List<Double> metrics, double mean) {
        if (metrics.size() == 1) {
            return 0.0;
        }

        double sumSquaredDiff = metrics.stream()
            .mapToDouble(value -> Math.pow(value - mean, 2))
            .sum();

        return Math.sqrt(sumSquaredDiff / (metrics.size() - 1));
    }

    private static double calculateCV(double mean, double standardDeviation) {
        if (mean == 0.0) {
            return 0.0;
        }
        return standardDeviation / mean;
    }

    private static boolean isChangeSignificant(List<Double> metrics, double mean, double stdDev) {
        if (metrics.size() < MIN_SAMPLE_SIZE) {
            return false;
        }

        if (stdDev == 0.0) {
            return false;
        }

        double se = stdDev / Math.sqrt(metrics.size());
        double tStat = Math.abs(mean / se);

        return tStat > T_CRITICAL_VALUE;
    }

    /**
     * Checks if a change in metrics is statistically significant.
     *
     * @param baselineMetrics Original performance measurements
     * @param currentMetrics Current performance measurements
     * @return true if the change is statistically significant
     */
    public static boolean isSignificantChange(List<Double> baselineMetrics, List<Double> currentMetrics) {
        if (baselineMetrics == null || currentMetrics == null ||
            baselineMetrics.isEmpty() || currentMetrics.isEmpty()) {
            return false;
        }

        StatisticalResult baseline = analyze(baselineMetrics);
        StatisticalResult current = analyze(currentMetrics);

        // Check if means are significantly different
        double pooledStdDev = calculatePooledStdDev(baselineMetrics, currentMetrics,
            baseline.standardDeviation(), current.standardDeviation());

        double se = pooledStdDev * Math.sqrt(1.0/baselineMetrics.size() + 1.0/currentMetrics.size());
        double tStat = Math.abs((current.mean() - baseline.mean()) / se);

        return tStat > T_CRITICAL_VALUE;
    }

    private static double calculatePooledStdDev(
            List<Double> baseline, List<Double> current,
            double baselineStdDev, double currentStdDev) {
        int n1 = baseline.size();
        int n2 = current.size();

        return Math.sqrt(
            ((n1 - 1) * Math.pow(baselineStdDev, 2) + (n2 - 1) * Math.pow(currentStdDev, 2)) /
            (n1 + n2 - 2)
        );
    }
}
