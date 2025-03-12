package vn.com.fecredit.app.performance.trend;

import org.springframework.lang.NonNull;
import java.util.*;

/**
 * Default implementation of the PerformanceTrendAnalyzer interface.
 * Uses builder pattern for constructing trend analysis results.
 * Thread-safe and immutable class.
 */
class DefaultPerformanceTrendAnalyzer implements PerformanceTrendAnalyzer {
    private final AnalyzerConfig config;

    /**
     * Creates analyzer with default configuration.
     * Package-private constructor used by factory methods.
     */
    DefaultPerformanceTrendAnalyzer() {
        this(AnalyzerConfig.getDefault());
    }

    /**
     * Creates analyzer with custom configuration.
     * Package-private constructor used by factory methods.
     *
     * @param config Custom analyzer configuration
     * @throws NullPointerException if config is null
     */
    DefaultPerformanceTrendAnalyzer(@NonNull AnalyzerConfig config) {
        this.config = Objects.requireNonNull(config, "Config cannot be null");
    }

    @Override
    @NonNull
    public TrendResult analyzeTrend(@NonNull List<DataPoint> dataPoints) {
        validateDataPoints(dataPoints);

        List<DataPoint> sortedPoints = new ArrayList<>(dataPoints);
        sortedPoints.sort(Comparator.comparing(DataPoint::timestamp));

        double avgChange = calculateAverageChange(sortedPoints);
        double volatility = calculateVolatility(sortedPoints);
        boolean isSignificant = isChangeSignificant(avgChange, volatility);
        TrendDirection direction = determineTrendDirection(avgChange, volatility);
        double confidence = calculateConfidence(avgChange, volatility);

        return TrendAnalysisBuilder.builder()
            .avgChange(avgChange)
            .volatility(volatility)
            .isSignificantTrend(isSignificant)
            .direction(direction)
            .confidence(confidence)
            .addInsights(generateInsights(sortedPoints, avgChange, volatility, direction))
            .build();
    }

    @Override
    @NonNull
    public List<DataPoint> calculateMovingAverage(@NonNull List<DataPoint> dataPoints, int windowSize) {
        if (windowSize <= 0 || dataPoints.size() < windowSize) {
            throw new IllegalArgumentException(
                String.format("Window size must be positive and <= data points size. Got: %d, Data size: %d",
                    windowSize, dataPoints.size())
            );
        }

        List<DataPoint> result = new ArrayList<>();
        Queue<DataPoint> window = new LinkedList<>();
        double sum = 0.0;

        for (DataPoint point : dataPoints) {
            window.offer(point);
            sum += point.value();

            if (window.size() > windowSize) {
                sum -= window.poll().value();
            }

            if (window.size() == windowSize) {
                double avg = sum / windowSize;
                double stdDev = calculateWindowStdDev(window, avg);
                result.add(new DataPoint(point.timestamp(), avg, stdDev));
            }
        }

        return Collections.unmodifiableList(result);
    }

    private void validateDataPoints(List<DataPoint> dataPoints) {
        Objects.requireNonNull(dataPoints, "Data points cannot be null");
        if (dataPoints.size() < config.minDataPoints()) {
            throw new IllegalArgumentException(
                String.format("At least %d data points required, got %d",
                    config.minDataPoints(),
                    dataPoints.size()
                )
            );
        }
        if (dataPoints.stream().anyMatch(Objects::isNull)) {
            throw new IllegalArgumentException("Data points cannot contain null values");
        }
    }

    private double calculateAverageChange(List<DataPoint> points) {
        double sumChanges = 0.0;
        int changes = 0;

        for (int i = 1; i < points.size(); i++) {
            double prev = points.get(i-1).value();
            double curr = points.get(i).value();
            
            if (prev != 0) {
                sumChanges += (curr - prev) / prev;
                changes++;
            }
        }

        return changes > 0 ? sumChanges / changes : 0.0;
    }

    private double calculateVolatility(List<DataPoint> points) {
        if (points.size() < 2) return 0.0;

        List<Double> changes = new ArrayList<>();
        for (int i = 1; i < points.size(); i++) {
            double prev = points.get(i-1).value();
            double curr = points.get(i).value();
            
            if (prev != 0) {
                changes.add((curr - prev) / prev);
            }
        }

        return changes.isEmpty() ? 0.0 : calculateStdDev(changes);
    }

    private boolean isChangeSignificant(double avgChange, double volatility) {
        return Math.abs(avgChange) > config.trendThreshold() && 
               volatility < Math.abs(avgChange) * 2;
    }

    private TrendDirection determineTrendDirection(double avgChange, double volatility) {
        if (volatility > config.volatilityThreshold()) {
            return TrendDirection.UNSTABLE;
        }
        
        if (Math.abs(avgChange) <= config.trendThreshold()) {
            return TrendDirection.STABLE;
        }
        
        return avgChange < 0 ? TrendDirection.IMPROVING : TrendDirection.DEGRADING;
    }

    private double calculateConfidence(double avgChange, double volatility) {
        if (volatility == 0) return avgChange == 0 ? 1.0 : 0.0;
        double confidence = Math.min(1.0, Math.abs(avgChange) / volatility);
        return confidence >= config.confidenceThreshold() ? confidence : 0.0;
    }

    private List<String> generateInsights(
            List<DataPoint> points,
            double avgChange,
            double volatility,
            TrendDirection direction) {
        List<String> insights = new ArrayList<>();

        // Basic trend insight
        insights.add(String.format("Performance is %s with %.1f%% average change",
            direction.toString().toLowerCase(),
            avgChange * 100));

        // Volatility insight
        if (volatility > config.trendThreshold()) {
            insights.add(String.format(
                "High volatility (%.1f%%) indicates unstable performance",
                volatility * 100));
        }

        // Recent trend insight
        if (points.size() >= 3) {
            DataPoint latest = points.get(points.size() - 1);
            DataPoint previous = points.get(points.size() - 2);
            double recentChange = (latest.value() - previous.value()) / previous.value();

            if (Math.abs(recentChange) > Math.abs(avgChange) * 1.5) {
                insights.add(String.format(
                    "Recent change (%.1f%%) is more severe than the average trend",
                    recentChange * 100));
            }
        }

        return Collections.unmodifiableList(insights);
    }

    private double calculateStdDev(List<Double> values) {
        double mean = values.stream()
            .mapToDouble(Double::doubleValue)
            .average()
            .orElse(0.0);
        
        double variance = values.stream()
            .mapToDouble(v -> Math.pow(v - mean, 2))
            .average()
            .orElse(0.0);
        
        return Math.sqrt(variance);
    }

    private double calculateWindowStdDev(Queue<DataPoint> window, double mean) {
        return Math.sqrt(window.stream()
            .mapToDouble(p -> Math.pow(p.value() - mean, 2))
            .average()
            .orElse(0.0));
    }
}
