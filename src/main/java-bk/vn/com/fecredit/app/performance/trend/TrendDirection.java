package vn.com.fecredit.app.performance.trend;

/**
 * Enumeration of possible performance trend directions.
 * Each direction represents a distinct pattern in performance measurements.
 */
public enum TrendDirection {
    /**
     * Performance is getting better (e.g., lower response times, higher throughput).
     * This indicates a positive trend in system performance.
     */
    IMPROVING,

    /**
     * Performance is getting worse (e.g., higher response times, lower throughput).
     * This indicates a negative trend that may require attention.
     */
    DEGRADING,

    /**
     * Performance is relatively constant within acceptable variance.
     * This indicates a healthy, predictable system state.
     */
    STABLE,

    /**
     * Performance is highly variable with no clear trend.
     * This may indicate system instability or environmental issues.
     */
    UNSTABLE;

    /**
     * Determines if this direction represents a potentially problematic state.
     *
     * @return true if this direction indicates a potential problem
     */
    public boolean isProblematic() {
        return this == DEGRADING || this == UNSTABLE;
    }

    /**
     * Gets a human-readable description of this trend direction.
     *
     * @return A descriptive string explaining the trend
     */
    public String getDescription() {
        return switch (this) {
            case IMPROVING -> "Performance is improving over time";
            case DEGRADING -> "Performance is degrading and may require attention";
            case STABLE -> "Performance is stable within expected bounds";
            case UNSTABLE -> "Performance is unstable with high variability";
        };
    }

    /**
     * Gets the severity level of this trend direction.
     * Higher values indicate more severe conditions.
     *
     * @return severity level from 0 (best) to 3 (worst)
     */
    public int getSeverity() {
        return switch (this) {
            case STABLE -> 0;
            case IMPROVING -> 1;
            case UNSTABLE -> 2;
            case DEGRADING -> 3;
        };
    }

    @Override
    public String toString() {
        return name().toLowerCase();
    }
}
