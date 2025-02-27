package vn.com.fecredit.app.monitoring;

import java.util.function.Predicate;

/**
 * Resource alerting system for monitoring thresholds
 */
public class ResourceAlert {
    private final String name;
    private final AlertSeverity severity;
    private final Predicate<SystemResourceMonitor.MonitoringSummary> condition;
    private final String message;
    private final AlertHandler handler;

    public enum AlertSeverity {
        INFO,
        WARNING,
        CRITICAL
    }

    @FunctionalInterface
    public interface AlertHandler {
        void handleAlert(AlertEvent event);
    }

    public static class AlertEvent {
        private final String name;
        private final AlertSeverity severity;
        private final String message;
        private final SystemResourceMonitor.MonitoringSummary summary;
        private final long timestamp;

        public AlertEvent(String name, AlertSeverity severity, String message, 
                         SystemResourceMonitor.MonitoringSummary summary) {
            this.name = name;
            this.severity = severity;
            this.message = message;
            this.summary = summary;
            this.timestamp = System.currentTimeMillis();
        }

        public String getName() { return name; }
        public AlertSeverity getSeverity() { return severity; }
        public String getMessage() { return message; }
        public SystemResourceMonitor.MonitoringSummary getSummary() { return summary; }
        public long getTimestamp() { return timestamp; }
    }

    private ResourceAlert(Builder builder) {
        this.name = builder.name;
        this.severity = builder.severity;
        this.condition = builder.condition;
        this.message = builder.message;
        this.handler = builder.handler;
    }

    public void evaluate(SystemResourceMonitor.MonitoringSummary summary) {
        if (condition.test(summary)) {
            AlertEvent event = new AlertEvent(name, severity, message, summary);
            handler.handleAlert(event);
        }
    }

    public static class Builder {
        private String name;
        private AlertSeverity severity = AlertSeverity.WARNING;
        private Predicate<SystemResourceMonitor.MonitoringSummary> condition;
        private String message;
        private AlertHandler handler = event -> {
            System.err.printf("[%s] %s Alert: %s%n", 
                event.getSeverity(), event.getName(), event.getMessage());
        };

        public Builder(String name) {
            this.name = name;
        }

        public Builder severity(AlertSeverity severity) {
            this.severity = severity;
            return this;
        }

        public Builder condition(Predicate<SystemResourceMonitor.MonitoringSummary> condition) {
            this.condition = condition;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder handler(AlertHandler handler) {
            this.handler = handler;
            return this;
        }

        public ResourceAlert build() {
            if (condition == null) {
                throw new IllegalStateException("Alert condition must be specified");
            }
            if (message == null) {
                message = "Resource threshold exceeded";
            }
            return new ResourceAlert(this);
        }
    }

    // Common alert conditions
    public static class Conditions {
        public static Predicate<SystemResourceMonitor.MonitoringSummary> cpuUsageAbove(double threshold) {
            return summary -> summary.getMaxCpuUsage() > threshold;
        }

        public static Predicate<SystemResourceMonitor.MonitoringSummary> heapUsageAbove(double thresholdMB) {
            return summary -> summary.getMaxHeapUsageMB() > thresholdMB;
        }

        public static Predicate<SystemResourceMonitor.MonitoringSummary> threadCountAbove(int threshold) {
            return summary -> summary.getMaxThreadCount() > threshold;
        }

        public static Predicate<SystemResourceMonitor.MonitoringSummary> gcTimeAbove(long thresholdMs) {
            return summary -> summary.getTotalGcTimeMs() > thresholdMs;
        }

        public static Predicate<SystemResourceMonitor.MonitoringSummary> avgCpuUsageAbove(double threshold) {
            return summary -> summary.getAvgCpuUsage() > threshold;
        }

        public static Predicate<SystemResourceMonitor.MonitoringSummary> avgHeapUsageAbove(double thresholdMB) {
            return summary -> summary.getAvgHeapUsageMB() > thresholdMB;
        }

        public static Predicate<SystemResourceMonitor.MonitoringSummary> gcCountAbove(int threshold) {
            return summary -> summary.getTotalGcCount() > threshold;
        }

        // Combine conditions
        public static Predicate<SystemResourceMonitor.MonitoringSummary> and(
                Predicate<SystemResourceMonitor.MonitoringSummary> first,
                Predicate<SystemResourceMonitor.MonitoringSummary> second) {
            return first.and(second);
        }

        public static Predicate<SystemResourceMonitor.MonitoringSummary> or(
                Predicate<SystemResourceMonitor.MonitoringSummary> first,
                Predicate<SystemResourceMonitor.MonitoringSummary> second) {
            return first.or(second);
        }
    }

    // Sample alert definitions
    public static ResourceAlert highCpuAlert() {
        return new Builder("High CPU Usage")
            .severity(AlertSeverity.WARNING)
            .condition(Conditions.cpuUsageAbove(80.0))
            .message("CPU usage exceeds 80%")
            .build();
    }

    public static ResourceAlert criticalMemoryAlert() {
        return new Builder("Critical Memory Usage")
            .severity(AlertSeverity.CRITICAL)
            .condition(Conditions.heapUsageAbove(1024.0)) // 1GB
            .message("Heap usage exceeds 1GB")
            .build();
    }

    public static ResourceAlert highThreadCountAlert() {
        return new Builder("High Thread Count")
            .severity(AlertSeverity.WARNING)
            .condition(Conditions.threadCountAbove(100))
            .message("Thread count exceeds 100")
            .build();
    }

    public static ResourceAlert frequentGCAlert() {
        return new Builder("Frequent GC")
            .severity(AlertSeverity.WARNING)
            .condition(Conditions.or(
                Conditions.gcTimeAbove(5000), // 5 seconds
                Conditions.gcCountAbove(100)
            ))
            .message("Excessive garbage collection activity")
            .build();
    }
}
