package vn.com.fecredit.app.monitoring;

/**
 * Interface for reporting metrics and statistics.
 * Implementations should handle metric persistence and formatting.
 */
public interface MetricsReporter {
    /**
     * Report a metric value
     * @param metric name of the metric
     * @param value value to report
     */
    void report(String metric, long value);

    /**
     * Configure the output directory for reports
     * @param directory absolute path to report directory
     */
    void setReportDirectory(String directory);

    /**
     * Generate and write report
     */
    void generateReport();

    /**
     * Clear all recorded metrics
     */
    void clearMetrics();

    /**
     * Check if reporter is enabled
     * @return true if enabled
     */
    boolean isEnabled();

    /**
     * Enable/disable the reporter
     * @param enabled desired state
     */
    void setEnabled(boolean enabled);
}
