package vn.com.fecredit.app.performance.trend;

import java.util.ArrayList;
import java.util.List;

/**
 * Builder pattern implementation for creating TrendResult instances.
 * Helps with constructing complex TrendResult objects with proper validation.
 */
public class TrendAnalysisBuilder {
    private double avgChange;
    private double volatility;
    private boolean isSignificantTrend;
    private TrendDirection direction;
    private double confidence;
    private final List<String> insights = new ArrayList<>();

    public TrendAnalysisBuilder avgChange(double avgChange) {
        this.avgChange = avgChange;
        return this;
    }

    public TrendAnalysisBuilder volatility(double volatility) {
        this.volatility = volatility;
        return this;
    }

    public TrendAnalysisBuilder isSignificantTrend(boolean isSignificantTrend) {
        this.isSignificantTrend = isSignificantTrend;
        return this;
    }

    public TrendAnalysisBuilder direction(TrendDirection direction) {
        this.direction = direction;
        return this;
    }

    public TrendAnalysisBuilder confidence(double confidence) {
        this.confidence = confidence;
        return this;
    }

    public TrendAnalysisBuilder addInsight(String insight) {
        if (insight != null && !insight.isEmpty()) {
            insights.add(insight);
        }
        return this;
    }

    public TrendAnalysisBuilder addInsights(List<String> newInsights) {
        if (newInsights != null) {
            newInsights.stream()
                .filter(insight -> insight != null && !insight.isEmpty())
                .forEach(insights::add);
        }
        return this;
    }

    public TrendResult build() {
        validateBuild();
        return new TrendResult(
            avgChange,
            volatility,
            isSignificantTrend,
            direction,
            confidence,
            insights
        );
    }

    private void validateBuild() {
        if (direction == null) {
            throw new IllegalStateException("TrendDirection must be set");
        }
        if (confidence < 0 || confidence > 1) {
            throw new IllegalStateException("Confidence must be between 0 and 1");
        }
        if (volatility < 0) {
            throw new IllegalStateException("Volatility cannot be negative");
        }
    }

    public static TrendAnalysisBuilder builder() {
        return new TrendAnalysisBuilder();
    }
}
