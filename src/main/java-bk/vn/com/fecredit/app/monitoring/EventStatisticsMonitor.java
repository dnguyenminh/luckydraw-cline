package vn.com.fecredit.app.monitoring;

import org.springframework.stereotype.Component;
import javax.management.NotificationListener;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Monitor implementation for tracking event statistics and performance metrics
 */
@Component
public class EventStatisticsMonitor implements MonitorOperations {

    private static final EventStatisticsMonitor INSTANCE = new EventStatisticsMonitor();
    
    private final Map<String, List<Long>> metrics = new ConcurrentHashMap<>();
    private final Map<String, Double> thresholds = new ConcurrentHashMap<>();
    private final List<MetricsReporter> reporters = new CopyOnWriteArrayList<>();
    private final List<NotificationListener> notificationListeners = new CopyOnWriteArrayList<>();
    private final AtomicBoolean enabled = new AtomicBoolean(false);

    private EventStatisticsMonitor() {
        // Private constructor for singleton
    }

    public static EventStatisticsMonitor getInstance() {
        return INSTANCE;
    }

    @Override
    public void recordOperation(String name, long duration) {
        if (!enabled.get()) {
            return;
        }
        metrics.computeIfAbsent(name, k -> Collections.synchronizedList(new ArrayList<>())).add(duration);
        notifyReporters(name, duration);
    }

    public void recordMetric(String name, long value) {
        recordValue(name, value);
    }

    @Override
    public void recordValue(String name, long value) {
        if (!enabled.get()) {
            return;
        }
        metrics.computeIfAbsent(name, k -> Collections.synchronizedList(new ArrayList<>())).add(value);
        notifyReporters(name, value);
    }

    @Override
    public void addReporter(MetricsReporter reporter) {
        if (reporter != null) {
            reporters.add(reporter);
        }
    }

    // Compatibility method for older tests
    public void registerReporter(MetricsReporter reporter) {
        addReporter(reporter);
    }

    public void addNotificationListener(NotificationListener listener, Object handback, Object filter) {
        if (listener != null) {
            notificationListeners.add(listener);
        }
    }

    public void enableMonitoring() {
        enabled.set(true);
    }

    public void disableMonitoring() {
        enabled.set(false);
    }

    public void clearMetrics() {
        metrics.clear();
        reporters.forEach(MetricsReporter::clearMetrics);
    }

    // Get operations as array for compatibility
    public String[] getMonitoredOperations() {
        Set<String> operations = metrics.keySet();
        return operations.toArray(new String[0]);
    }

    public long getTotalOperations(String operation) {
        List<Long> values = metrics.get(operation);
        return values != null ? values.size() : 0;
    }

    public double getAverageProcessingTime(String operation) {
        List<Long> values = metrics.get(operation);
        if (values == null || values.isEmpty()) {
            return 0.0;
        }
        synchronized (values) {
            return values.stream()
                .mapToLong(Long::valueOf)
                .average()
                .orElse(0.0);
        }
    }

    public long getMaxProcessingTime(String operation) {
        List<Long> values = metrics.get(operation);
        if (values == null || values.isEmpty()) {
            return 0L;
        }
        synchronized (values) {
            return values.stream()
                .mapToLong(Long::valueOf)
                .max()
                .orElse(0L);
        }
    }

    public void setPerformanceThreshold(String operation, double threshold) {
        thresholds.put(operation, threshold);
    }

    public double getPerformanceThreshold(String operation) {
        return thresholds.getOrDefault(operation, Double.MAX_VALUE);
    }

    private void notifyReporters(String metric, long value) {
        reporters.forEach(reporter -> {
            if (reporter.isEnabled()) {
                try {
                    reporter.report(metric, value);
                } catch (Exception e) {
                    System.err.println("Error reporting metric: " + e.getMessage());
                }
            }
        });

        // Check thresholds and notify listeners
        Double threshold = thresholds.get(metric);
        if (threshold != null && value > threshold) {
            notifyListeners(metric, value, threshold);
        }
    }

    private void notifyListeners(String metric, long value, double threshold) {
        for (NotificationListener listener : notificationListeners) {
            try {
                // Create notification object with details
                Map<String, Object> userData = new HashMap<>();
                userData.put("metric", metric);
                userData.put("value", value);
                userData.put("threshold", threshold);
                userData.put("timestamp", System.currentTimeMillis());

                listener.handleNotification(null, userData);
            } catch (Exception e) {
                System.err.println("Error notifying listener: " + e.getMessage());
            }
        }
    }

    public Map<String, Double> getAverages() {
        Map<String, Double> averages = new ConcurrentHashMap<>();
        metrics.forEach((key, values) -> 
            averages.put(key, getAverageProcessingTime(key))
        );
        return averages;
    }

    public boolean isEnabled() {
        return enabled.get();
    }
}
