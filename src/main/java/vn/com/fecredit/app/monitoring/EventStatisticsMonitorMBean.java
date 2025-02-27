package vn.com.fecredit.app.monitoring;

/**
 * MBean interface for EventStatistics monitoring
 */
public interface EventStatisticsMonitorMBean {
    double getAverageProcessingTime(String operation);
    long getTotalOperations(String operation);
    double getMaxProcessingTime(String operation);
    void setPerformanceThreshold(String operation, double thresholdMs);
    double getPerformanceThreshold(String operation);
    void clearMetrics();
    String[] getMonitoredOperations();
}
