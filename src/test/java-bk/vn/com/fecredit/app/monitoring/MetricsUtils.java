package vn.com.fecredit.app.monitoring;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Utility class for calculating performance metrics
 */
public class MetricsUtils {
    
    public static double calculateAverage(List<Long> values) {
        if (values == null || values.isEmpty()) {
            return 0.0;
        }
        return values.stream()
            .mapToLong(Long::valueOf)
            .average()
            .orElse(0.0);
    }

    public static long calculateMin(List<Long> values) {
        if (values == null || values.isEmpty()) {
            return 0L;
        }
        return values.stream()
            .mapToLong(Long::valueOf)
            .min()
            .orElse(0L);
    }

    public static long calculateMax(List<Long> values) {
        if (values == null || values.isEmpty()) {
            return 0L;
        }
        return values.stream()
            .mapToLong(Long::valueOf)
            .max()
            .orElse(0L);
    }

    public static double calculatePercentile(List<Long> values, int percentile) {
        if (values == null || values.isEmpty() || percentile < 0 || percentile > 100) {
            return 0.0;
        }
        List<Long> sorted = new ArrayList<>(values);
        Collections.sort(sorted);
        int index = (int) Math.ceil(percentile / 100.0 * sorted.size()) - 1;
        return sorted.get(Math.max(0, index));
    }

    public static double calculateStandardDeviation(List<Long> values) {
        if (values == null || values.isEmpty()) {
            return 0.0;
        }
        double mean = calculateAverage(values);
        double variance = values.stream()
            .mapToDouble(v -> Math.pow(v - mean, 2))
            .average()
            .orElse(0.0);
        return Math.sqrt(variance);
    }
}
