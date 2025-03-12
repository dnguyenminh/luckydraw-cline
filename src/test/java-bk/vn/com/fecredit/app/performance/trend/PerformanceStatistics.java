package vn.com.fecredit.app.performance.trend;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class PerformanceStatistics {
    
    public record StatisticalResult(
        double mean,
        double median,
        double standardDeviation,
        double coefficientOfVariation,
        double skewness,
        double p95,
        double p99,
        boolean isSignificantChange,
        String description
    ) {}

    private static final double SIGNIFICANCE_LEVEL = 0.05;
    private static final int MIN_SAMPLES = 3;

    public static StatisticalResult analyze(List<Double> samples) {
        if (samples == null || samples.size() < MIN_SAMPLES) {
            return new StatisticalResult(0, 0, 0, 0, 0, 0, 0, false,
                "Insufficient samples for statistical analysis");
        }

        // Basic statistics
        double mean = calculateMean(samples);
        double median = calculateMedian(samples);
        double stdDev = calculateStandardDeviation(samples, mean);
        double cv = stdDev / mean;
        double skewness = calculateSkewness(samples, mean, stdDev);
        double p95 = calculatePercentile(samples, 95);
        double p99 = calculatePercentile(samples, 99);

        // Trend analysis using Mann-Kendall test
        boolean significantChange = detectSignificantChange(samples);
        String description = generateDescription(mean, stdDev, cv, skewness, significantChange);

        return new StatisticalResult(
            mean, median, stdDev, cv, skewness, p95, p99,
            significantChange, description
        );
    }

    public static boolean isSignificantDifference(List<Double> baseline, List<Double> current) {
        if (baseline == null || current == null || 
            baseline.size() < MIN_SAMPLES || current.size() < MIN_SAMPLES) {
            return false;
        }

        // Perform two-sample t-test
        double baselineMean = calculateMean(baseline);
        double currentMean = calculateMean(current);
        double baselineVar = calculateVariance(baseline, baselineMean);
        double currentVar = calculateVariance(current, currentMean);

        // Calculate pooled standard error
        double pooledSE = Math.sqrt(
            (baselineVar / baseline.size()) + (currentVar / current.size())
        );

        // Calculate t-statistic
        double tStat = Math.abs(currentMean - baselineMean) / pooledSE;

        // Approximate degrees of freedom using Welch–Satterthwaite equation
        double df = Math.pow(pooledSE, 4) / 
            (Math.pow(baselineVar, 2) / (Math.pow(baseline.size(), 2) * (baseline.size() - 1)) +
             Math.pow(currentVar, 2) / (Math.pow(current.size(), 2) * (current.size() - 1)));

        // Get critical value for two-tailed test at significance level
        double criticalValue = approximateTCriticalValue(df);

        return tStat > criticalValue;
    }

    private static double calculateMean(List<Double> samples) {
        return samples.stream()
            .mapToDouble(Double::doubleValue)
            .average()
            .orElse(0.0);
    }

    private static double calculateMedian(List<Double> samples) {
        List<Double> sorted = new ArrayList<>(samples);
        Collections.sort(sorted);
        int size = sorted.size();
        if (size % 2 == 0) {
            return (sorted.get(size/2 - 1) + sorted.get(size/2)) / 2.0;
        } else {
            return sorted.get(size/2);
        }
    }

    private static double calculateStandardDeviation(List<Double> samples, double mean) {
        return Math.sqrt(calculateVariance(samples, mean));
    }

    private static double calculateVariance(List<Double> samples, double mean) {
        return samples.stream()
            .mapToDouble(x -> Math.pow(x - mean, 2))
            .average()
            .orElse(0.0);
    }

    private static double calculateSkewness(List<Double> samples, double mean, double stdDev) {
        if (stdDev == 0) return 0;
        double n = samples.size();
        double sum = samples.stream()
            .mapToDouble(x -> Math.pow((x - mean) / stdDev, 3))
            .sum();
        return (n / ((n-1) * (n-2))) * sum;
    }

    private static double calculatePercentile(List<Double> samples, double percentile) {
        List<Double> sorted = new ArrayList<>(samples);
        Collections.sort(sorted);
        int index = (int) Math.ceil((percentile / 100.0) * sorted.size()) - 1;
        return sorted.get(Math.min(index, sorted.size() - 1));
    }

    private static boolean detectSignificantChange(List<Double> samples) {
        // Mann-Kendall trend test
        int n = samples.size();
        int positiveCount = 0;
        int negativeCount = 0;

        for (int i = 0; i < n-1; i++) {
            for (int j = i+1; j < n; j++) {
                double diff = samples.get(j) - samples.get(i);
                if (diff > 0) positiveCount++;
                else if (diff < 0) negativeCount++;
            }
        }

        int S = positiveCount - negativeCount;
        double variance = (n * (n-1) * (2*n + 5)) / 18.0;
        double Z = (S > 0 ? S-1 : S+1) / Math.sqrt(variance);

        return Math.abs(Z) > approximateNormalCriticalValue(SIGNIFICANCE_LEVEL);
    }

    private static double approximateTCriticalValue(double df) {
        // Approximation for two-tailed t-critical value at 0.05 significance
        return 1.96 + (2.0 / df);
    }

    private static double approximateNormalCriticalValue(double alpha) {
        // Approximation for two-tailed normal critical value
        return 1.96; // For alpha = 0.05
    }

    private static String generateDescription(
            double mean, double stdDev, double cv, double skewness, boolean significantChange) {
        List<String> observations = new ArrayList<>();

        observations.add(String.format("Mean execution time: %.2f seconds", mean));
        observations.add(String.format("Standard deviation: %.2f seconds", stdDev));
        
        if (cv > 1.0) {
            observations.add("High variability in execution times");
        } else if (cv > 0.5) {
            observations.add("Moderate variability in execution times");
        } else {
            observations.add("Consistent execution times");
        }

        if (Math.abs(skewness) > 1) {
            String direction = skewness > 0 ? "right" : "left";
            observations.add("Distribution is significantly skewed to the " + direction);
        }

        if (significantChange) {
            observations.add("Statistically significant trend detected");
        } else {
            observations.add("No statistically significant trend detected");
        }

        return String.join("\n", observations);
    }
}
