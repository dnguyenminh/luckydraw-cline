package vn.com.fecredit.app.performance.trend;

import java.util.List;

/**
 * Represents the result of a performance trend analysis.
 * Immutable value class containing trend metrics and insights.
 *
 * @param avgChange Average change in performance values
 * @param volatility Measure of performance value variability
 * @param isSignificantTrend Whether the trend is statistically significant
 * @param direction Overall trend direction
 * @param confidence Confidence level in the trend analysis (0-1)
 * @param insights Human-readable insights about the trend
 */
public record TrendResult(
    double avgChange,
    double volatility,
    boolean isSignificantTrend,
    TrendDirection direction,
    double confidence,
    List<String> insights
) {
    /**
     * Creates a TrendResult with validation.
     */
    public TrendResult {
        if (direction == null) {
            throw new IllegalArgumentException("Direction cannot be null");
        }
        if (confidence < 0 || confidence > 1) {
            throw new IllegalArgumentException("Confidence must be between 0 and 1");
        }
        if (insights == null) {
            throw new IllegalArgumentException("Insights cannot be null");
        }
        // Make insights unmodifiable
        insights = List.copyOf(insights);
    }
}
