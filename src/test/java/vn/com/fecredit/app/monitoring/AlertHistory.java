package vn.com.fecredit.app.monitoring;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.nio.file.*;
import java.io.*;

/**
 * Alert history tracking and reporting system
 */
public class AlertHistory {
    private static final int DEFAULT_RETENTION_DAYS = 7;
    private final Map<String, List<ResourceAlert.AlertEvent>> alertHistory;
    private final Path storageDirectory;
    private final int retentionDays;

    public AlertHistory(Path storageDirectory) {
        this(storageDirectory, DEFAULT_RETENTION_DAYS);
    }

    public AlertHistory(Path storageDirectory, int retentionDays) {
        this.alertHistory = new ConcurrentHashMap<>();
        this.storageDirectory = storageDirectory;
        this.retentionDays = retentionDays;
        loadHistory();
    }

    public void recordAlert(ResourceAlert.AlertEvent event) {
        String alertKey = event.getName();
        alertHistory.computeIfAbsent(alertKey, k -> Collections.synchronizedList(new ArrayList<>()))
                   .add(event);
        persistAlert(event);
        pruneOldAlerts();
    }

    private void persistAlert(ResourceAlert.AlertEvent event) {
        try {
            Files.createDirectories(storageDirectory);
            Path alertFile = storageDirectory.resolve("alerts_" + 
                Instant.now().truncatedTo(ChronoUnit.DAYS).toString().replace(":", "-") + ".log");
            
            String alertEntry = String.format("%d|%s|%s|%s|%.1f|%.1f|%d%n",
                event.getTimestamp(),
                event.getName(),
                event.getSeverity(),
                event.getMessage(),
                event.getSummary().getMaxCpuUsage(),
                event.getSummary().getMaxHeapUsageMB(),
                event.getSummary().getTotalGcCount()
            );

            Files.write(alertFile, 
                Collections.singleton(alertEntry), 
                StandardOpenOption.CREATE, 
                StandardOpenOption.APPEND
            );
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadHistory() {
        try {
            if (Files.exists(storageDirectory)) {
                Files.list(storageDirectory)
                    .filter(p -> p.toString().endsWith(".log"))
                    .forEach(this::loadAlertsFromFile);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadAlertsFromFile(Path file) {
        try {
            List<String> lines = Files.readAllLines(file);
            for (String line : lines) {
                String[] parts = line.split("\\|");
                if (parts.length >= 4) {
                    String alertKey = parts[1];
                    alertHistory.computeIfAbsent(alertKey, k -> Collections.synchronizedList(new ArrayList<>()));
                    // Note: Full reconstruction of AlertEvent objects not needed for history
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void pruneOldAlerts() {
        Instant cutoff = Instant.now().minus(retentionDays, ChronoUnit.DAYS);
        
        // Prune memory
        alertHistory.values().forEach(alerts ->
            alerts.removeIf(alert -> Instant.ofEpochMilli(alert.getTimestamp()).isBefore(cutoff))
        );
        alertHistory.entrySet().removeIf(entry -> entry.getValue().isEmpty());

        // Prune files
        try {
            if (Files.exists(storageDirectory)) {
                Files.list(storageDirectory)
                    .filter(p -> p.toString().endsWith(".log"))
                    .filter(p -> {
                        try {
                            return Files.getLastModifiedTime(p)
                                      .toInstant()
                                      .isBefore(cutoff);
                        } catch (IOException e) {
                            return false;
                        }
                    })
                    .forEach(p -> {
                        try {
                            Files.delete(p);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public AlertSummary getAlertSummary() {
        return new AlertSummary(alertHistory);
    }

    public static class AlertSummary {
        private final Map<String, AlertStats> alertStats;
        private final int totalAlerts;
        private final Map<ResourceAlert.AlertSeverity, Integer> severityCounts;
        private final Instant oldestAlert;
        private final Instant newestAlert;

        private AlertSummary(Map<String, List<ResourceAlert.AlertEvent>> history) {
            this.alertStats = new HashMap<>();
            this.severityCounts = new EnumMap<>(ResourceAlert.AlertSeverity.class);
            
            long totalCount = 0;
            Instant oldest = Instant.now();
            Instant newest = Instant.EPOCH;

            for (Map.Entry<String, List<ResourceAlert.AlertEvent>> entry : history.entrySet()) {
                List<ResourceAlert.AlertEvent> alerts = entry.getValue();
                AlertStats stats = computeStats(alerts);
                alertStats.put(entry.getKey(), stats);
                
                totalCount += alerts.size();
                alerts.forEach(alert -> 
                    severityCounts.merge(alert.getSeverity(), 1, Integer::sum)
                );

                if (!alerts.isEmpty()) {
                    Instant alertOldest = Instant.ofEpochMilli(alerts.get(0).getTimestamp());
                    Instant alertNewest = Instant.ofEpochMilli(alerts.get(alerts.size() - 1).getTimestamp());
                    
                    if (alertOldest.isBefore(oldest)) oldest = alertOldest;
                    if (alertNewest.isAfter(newest)) newest = alertNewest;
                }
            }

            this.totalAlerts = (int) totalCount;
            this.oldestAlert = oldest;
            this.newestAlert = newest;
        }

        private AlertStats computeStats(List<ResourceAlert.AlertEvent> alerts) {
            DoubleSummaryStatistics cpuStats = alerts.stream()
                .mapToDouble(a -> a.getSummary().getMaxCpuUsage())
                .summaryStatistics();

            DoubleSummaryStatistics memStats = alerts.stream()
                .mapToDouble(a -> a.getSummary().getMaxHeapUsageMB())
                .summaryStatistics();

            return new AlertStats(
                alerts.size(),
                cpuStats.getAverage(),
                cpuStats.getMax(),
                memStats.getAverage(),
                memStats.getMax()
            );
        }

        public Map<String, AlertStats> getAlertStats() {
            return Collections.unmodifiableMap(alertStats);
        }

        public int getTotalAlerts() { return totalAlerts; }
        public Map<ResourceAlert.AlertSeverity, Integer> getSeverityCounts() {
            return Collections.unmodifiableMap(severityCounts);
        }
        public Instant getOldestAlert() { return oldestAlert; }
        public Instant getNewestAlert() { return newestAlert; }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("Alert Summary Report\n");
            sb.append("===================\n\n");
            
            sb.append(String.format("Total Alerts: %d\n", totalAlerts));
            sb.append(String.format("Time Range: %s to %s\n\n", oldestAlert, newestAlert));
            
            sb.append("Severity Distribution:\n");
            severityCounts.forEach((severity, count) ->
                sb.append(String.format("  %s: %d\n", severity, count))
            );
            sb.append("\n");

            sb.append("Alert Details:\n");
            alertStats.forEach((name, stats) ->
                sb.append(String.format("  %s:\n    Count: %d\n    Avg CPU: %.1f%%\n    Max CPU: %.1f%%\n" +
                    "    Avg Memory: %.1f MB\n    Max Memory: %.1f MB\n\n",
                    name, stats.count, stats.avgCpu, stats.maxCpu, stats.avgMemory, stats.maxMemory))
            );

            return sb.toString();
        }
    }

    public static class AlertStats {
        private final int count;
        private final double avgCpu;
        private final double maxCpu;
        private final double avgMemory;
        private final double maxMemory;

        public AlertStats(int count, double avgCpu, double maxCpu, double avgMemory, double maxMemory) {
            this.count = count;
            this.avgCpu = avgCpu;
            this.maxCpu = maxCpu;
            this.avgMemory = avgMemory;
            this.maxMemory = maxMemory;
        }

        public int getCount() { return count; }
        public double getAvgCpu() { return avgCpu; }
        public double getMaxCpu() { return maxCpu; }
        public double getAvgMemory() { return avgMemory; }
        public double getMaxMemory() { return maxMemory; }
    }
}
