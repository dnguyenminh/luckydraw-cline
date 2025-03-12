package vn.com.fecredit.app.performance.trend;

import org.springframework.lang.NonNull;
import java.util.List;

/**
 * Analyzes performance trends over time using statistical measures.
 * This interface defines methods for analyzing performance data points
 * and detecting trends in the measurements.
 * 
 * Implementations must be thread-safe and handle concurrent access appropriately.
 */
public interface PerformanceTrendAnalyzer {

    /**
     * Analyzes a series of performance metrics to detect trends.
     * 
     * This method analyzes the provided data points to determine:
     * - The overall trend direction (improving, degrading, stable, unstable)
     * - Whether the changes are statistically significant
     * - The volatility of the measurements
     * - The confidence level in the trend detection
     * - Human-readable insights about the trend
     *
     * @param dataPoints List of performance measurements ordered by time.
     *                   Must contain at least 3 data points.
     * @return Analysis results including trend direction and significance
     * @throws IllegalArgumentException if input is null or contains fewer than 3 points
     * @throws IllegalStateException if analysis cannot be completed due to invalid data
     */
    @NonNull
    TrendResult analyzeTrend(@NonNull List<DataPoint> dataPoints);

    /**
     * Calculates moving average over a window of data points.
     * 
     * For each window position, calculates:
     * - The average value within the window
     * - The standard deviation within the window
     * - Uses the timestamp from the last point in each window
     *
     * @param dataPoints Ordered time series data points to analyze
     * @param windowSize Size of the moving window. Must be positive and less than 
     *                  or equal to the number of data points.
     * @return List of smoothed data points containing moving averages and their deviations.
     *         The size will be dataPoints.size() - windowSize + 1
     * @throws IllegalArgumentException if windowSize is invalid or data points are insufficient
     */
    @NonNull
    List<DataPoint> calculateMovingAverage(@NonNull List<DataPoint> dataPoints, int windowSize);

    /**
     * Creates a new instance of the default implementation.
     *
     * @return A new instance of the default PerformanceTrendAnalyzer implementation
     */
    @NonNull
    static PerformanceTrendAnalyzer create() {
        return new DefaultPerformanceTrendAnalyzer();
    }

    /**
     * Creates a configured instance with specified thresholds.
     *
     * @param config Configuration parameters for the analyzer
     * @return A new configured instance of PerformanceTrendAnalyzer
     * @throws IllegalArgumentException if config is invalid
     */
    @NonNull
    static PerformanceTrendAnalyzer createWithConfig(@NonNull AnalyzerConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("Config cannot be null");
        }
        return new DefaultPerformanceTrendAnalyzer(config);
    }
}

/**
 * Configuration parameters for PerformanceTrendAnalyzer.
 * Records immutable configuration settings.
 * 
 * @param minDataPoints Minimum number of data points required for analysis
 * @param trendThreshold Minimum change required to consider a trend significant (0-1)
 * @param volatilityThreshold Maximum acceptable volatility before marking as unstable
 * @param confidenceThreshold Minimum confidence level required for trend detection
 */
record AnalyzerConfig(
    int minDataPoints,
    double trendThreshold,
    double volatilityThreshold,
    double confidenceThreshold
) {
    /**
     * Creates config with validation.
     * 
     * @throws IllegalArgumentException if any parameter is invalid
     */
    public AnalyzerConfig {
        if (minDataPoints < 2) {
            throw new IllegalArgumentException("Minimum data points must be at least 2");
        }
        if (trendThreshold <= 0 || trendThreshold >= 1) {
            throw new IllegalArgumentException("Trend threshold must be between 0 and 1");
        }
        if (volatilityThreshold <= 0) {
            throw new IllegalArgumentException("Volatility threshold must be positive");
        }
        if (confidenceThreshold <= 0 || confidenceThreshold > 1) {
            throw new IllegalArgumentException("Confidence threshold must be between 0 and 1");
        }
    }

    /**
     * Creates a default configuration with standard thresholds.
     * Uses conservative values suitable for most use cases.
     *
     * @return Default configuration instance
     */
    @NonNull
    public static AnalyzerConfig getDefault() {
        return new AnalyzerConfig(3, 0.1, 0.2, 0.8);
    }
}
