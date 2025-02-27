package vn.com.fecredit.app.monitoring;

/**
 * Defines operations for monitoring metrics
 */
public interface MonitorOperations {
    void recordOperation(String name, long duration);
    void recordValue(String name, long value);
    void addReporter(MetricsReporter reporter);
}
