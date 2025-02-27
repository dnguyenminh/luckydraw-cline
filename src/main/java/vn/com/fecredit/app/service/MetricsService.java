package vn.com.fecredit.app.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface MetricsService {

    /**
     * Record event metric
     */
    void recordEvent(String eventName);

    /**
     * Record event metric with value
     */
    void recordEvent(String eventName, double value);

    /**
     * Record event metric with tags
     */
    void recordEvent(String eventName, Map<String, String> tags);

    /**
     * Record timing metric
     */
    void recordTiming(String name, long timeInMillis);

    /**
     * Record timing metric with tags
     */
    void recordTiming(String name, long timeInMillis, Map<String, String> tags);

    /**
     * Increment counter
     */
    void incrementCounter(String name);

    /**
     * Increment counter by value
     */
    void incrementCounter(String name, long value);

    /**
     * Record gauge value
     */
    void recordGaugeValue(String name, double value);

    /**
     * Record histogram value
     */
    void recordHistogramValue(String name, double value);

    /**
     * Get metric value
     */
    double getMetricValue(String name);

    /**
     * Get metric values for time range
     */
    List<MetricValue> getMetricValues(String name, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * Get metrics by prefix
     */
    Map<String, Double> getMetricsByPrefix(String prefix);

    /**
     * Get application metrics
     */
    ApplicationMetrics getApplicationMetrics();

    /**
     * Get JVM metrics
     */
    JVMMetrics getJVMMetrics();

    /**
     * Get system metrics
     */
    SystemMetrics getSystemMetrics();

    /**
     * Get custom metrics
     */
    Map<String, Object> getCustomMetrics();

    /**
     * Reset metrics
     */
    void resetMetrics();

    /**
     * Export metrics
     */
    byte[] exportMetrics(String format);

    /**
     * Metric value class
     */
    @lombok.Data
    class MetricValue {
        private final String name;
        private final double value;
        private final LocalDateTime timestamp;
        private final Map<String, String> tags;
    }

    /**
     * Application metrics class
     */
    @lombok.Data 
    class ApplicationMetrics {
        private final int activeUsers;
        private final int totalRequests;
        private final double averageResponseTime;
        private final int errorCount;
        private final double errorRate;
        private final Map<String, Integer> endpointHits;
        private final Map<String, Double> endpointLatencies;
        private final Map<String, Object> customMetrics;
    }

    /**
     * JVM metrics class
     */
    @lombok.Data
    class JVMMetrics {
        private final long heapUsed;
        private final long heapMax;
        private final double heapUsage;
        private final int threadCount;
        private final int peakThreadCount;
        private final long uptime;
        private final Map<String, Object> garbageCollection;
        private final Map<String, Object> memoryPools;
    }

    /**
     * System metrics class
     */
    @lombok.Data
    class SystemMetrics {
        private final double cpuUsage;
        private final long totalMemory;
        private final long freeMemory;
        private final double memoryUsage;
        private final long diskSpace;
        private final double diskUsage;
        private final Map<String, Object> networkStats;
        private final Map<String, Object> processStats;
    }
}
