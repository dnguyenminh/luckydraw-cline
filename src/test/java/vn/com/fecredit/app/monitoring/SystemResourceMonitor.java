package vn.com.fecredit.app.monitoring;

import java.lang.management.*;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * System resource monitoring utility for performance tests
 */
public class SystemResourceMonitor implements AutoCloseable {
    
    private final ScheduledExecutorService scheduler;
    private final List<ResourceSnapshot> snapshots;
    private final List<ResourceAlert> alerts;
    private final AtomicBoolean isRunning;
    private final Runtime runtime;
    private final OperatingSystemMXBean osBean;
    private final MemoryMXBean memoryBean;
    private final ThreadMXBean threadBean;
    private final long startTime;
    private ScheduledFuture<?> monitoringTask;

    private static class ResourceSnapshot {
        private final Instant timestamp;
        private final double cpuUsage;
        private final long heapUsed;
        private final long heapMax;
        private final long nonHeapUsed;
        private final int threadCount;
        private final long totalGcTime;
        private final int gcCount;

        ResourceSnapshot(Instant timestamp, double cpuUsage, long heapUsed, long heapMax,
                        long nonHeapUsed, int threadCount, long totalGcTime, int gcCount) {
            this.timestamp = timestamp;
            this.cpuUsage = cpuUsage;
            this.heapUsed = heapUsed;
            this.heapMax = heapMax;
            this.nonHeapUsed = nonHeapUsed;
            this.threadCount = threadCount;
            this.totalGcTime = totalGcTime;
            this.gcCount = gcCount;
        }
    }

    public SystemResourceMonitor() {
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "ResourceMonitor");
            t.setDaemon(true);
            return t;
        });
        this.snapshots = new CopyOnWriteArrayList<>();
        this.alerts = new CopyOnWriteArrayList<>();
        this.isRunning = new AtomicBoolean(false);
        this.runtime = Runtime.getRuntime();
        this.osBean = ManagementFactory.getOperatingSystemMXBean();
        this.memoryBean = ManagementFactory.getMemoryMXBean();
        this.threadBean = ManagementFactory.getThreadMXBean();
        this.startTime = System.currentTimeMillis();

        // Add default alerts
        addDefaultAlerts();
    }

    private void addDefaultAlerts() {
        addAlert(ResourceAlert.highCpuAlert());
        addAlert(ResourceAlert.criticalMemoryAlert());
        addAlert(ResourceAlert.highThreadCountAlert());
        addAlert(ResourceAlert.frequentGCAlert());
    }

    public void addAlert(ResourceAlert alert) {
        if (alert != null) {
            alerts.add(alert);
        }
    }

    public void removeAlert(ResourceAlert alert) {
        alerts.remove(alert);
    }

    public void clearAlerts() {
        alerts.clear();
    }

    public void startMonitoring(Duration samplingInterval) {
        if (isRunning.compareAndSet(false, true)) {
            monitoringTask = scheduler.scheduleAtFixedRate(
                this::takeSnapshot,
                0,
                samplingInterval.toMillis(),
                TimeUnit.MILLISECONDS
            );
        }
    }

    public void stopMonitoring() {
        if (isRunning.compareAndSet(true, false) && monitoringTask != null) {
            monitoringTask.cancel(false);
        }
    }

    private void takeSnapshot() {
        try {
            double cpuUsage = getCpuUsage();
            MemoryUsage heap = memoryBean.getHeapMemoryUsage();
            MemoryUsage nonHeap = memoryBean.getNonHeapMemoryUsage();
            
            ResourceSnapshot snapshot = new ResourceSnapshot(
                Instant.now(),
                cpuUsage,
                heap.getUsed(),
                heap.getMax(),
                nonHeap.getUsed(),
                threadBean.getThreadCount(),
                getTotalGcTime(),
                getTotalGcCount()
            );
            snapshots.add(snapshot);

            // Evaluate alerts after each snapshot
            evaluateAlerts();
        } catch (Exception e) {
            // Log but don't fail monitoring
            e.printStackTrace();
        }
    }

    private void evaluateAlerts() {
        MonitoringSummary summary = calculateSummary();
        alerts.forEach(alert -> {
            try {
                alert.evaluate(summary);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private double getCpuUsage() {
        if (osBean != null && osBean instanceof com.sun.management.OperatingSystemMXBean) {
            com.sun.management.OperatingSystemMXBean sunOsBean = (com.sun.management.OperatingSystemMXBean) osBean;
            return sunOsBean.getProcessCpuLoad() * 100.0;
        }
        return -1.0; // Not available
    }

    private long getTotalGcTime() {
        return ManagementFactory.getGarbageCollectorMXBeans().stream()
            .mapToLong(GarbageCollectorMXBean::getCollectionTime)
            .sum();
    }

    private int getTotalGcCount() {
        return ManagementFactory.getGarbageCollectorMXBeans().stream()
            .mapToInt(gc -> (int) gc.getCollectionCount())
            .sum();
    }

    public MonitoringSummary getSummary() {
        MonitoringSummary summary = calculateSummary();
        evaluateAlerts();
        return summary;
    }

    private MonitoringSummary calculateSummary() {
        if (snapshots.isEmpty()) {
            return MonitoringSummary.empty();
        }

        DoubleSummaryStatistics cpuStats = snapshots.stream()
            .mapToDouble(s -> s.cpuUsage)
            .filter(cpu -> cpu >= 0)
            .summaryStatistics();

        LongSummaryStatistics heapStats = snapshots.stream()
            .mapToLong(s -> s.heapUsed)
            .summaryStatistics();

        LongSummaryStatistics threadStats = snapshots.stream()
            .mapToLong(s -> s.threadCount)
            .summaryStatistics();

        ResourceSnapshot latest = snapshots.get(snapshots.size() - 1);
        ResourceSnapshot first = snapshots.get(0);
        long totalGcTime = latest.totalGcTime - first.totalGcTime;
        int totalGcCount = latest.gcCount - first.gcCount;

        return new MonitoringSummary(
            Duration.between(first.timestamp, latest.timestamp),
            cpuStats.getAverage(),
            cpuStats.getMax(),
            bytesToMB(heapStats.getAverage()),
            bytesToMB(heapStats.getMax()),
            threadStats.getMin(),
            threadStats.getMax(),
            totalGcTime,
            totalGcCount,
            snapshots.size()
        );
    }

    private double bytesToMB(double bytes) {
        return bytes / (1024.0 * 1024.0);
    }

    @Override
    public void close() {
        stopMonitoring();
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(5, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            scheduler.shutdownNow();
        }
    }

    public static class MonitoringSummary {
        private final Duration monitoringDuration;
        private final double avgCpuUsage;
        private final double maxCpuUsage;
        private final double avgHeapUsageMB;
        private final double maxHeapUsageMB;
        private final long minThreadCount;
        private final long maxThreadCount;
        private final long totalGcTimeMs;
        private final int totalGcCount;
        private final int sampleCount;

        private MonitoringSummary(Duration monitoringDuration, double avgCpuUsage,
                                double maxCpuUsage, double avgHeapUsageMB,
                                double maxHeapUsageMB, long minThreadCount,
                                long maxThreadCount, long totalGcTimeMs,
                                int totalGcCount, int sampleCount) {
            this.monitoringDuration = monitoringDuration;
            this.avgCpuUsage = avgCpuUsage;
            this.maxCpuUsage = maxCpuUsage;
            this.avgHeapUsageMB = avgHeapUsageMB;
            this.maxHeapUsageMB = maxHeapUsageMB;
            this.minThreadCount = minThreadCount;
            this.maxThreadCount = maxThreadCount;
            this.totalGcTimeMs = totalGcTimeMs;
            this.totalGcCount = totalGcCount;
            this.sampleCount = sampleCount;
        }

        private static MonitoringSummary empty() {
            return new MonitoringSummary(
                Duration.ZERO, 0.0, 0.0, 0.0, 0.0, 0, 0, 0, 0, 0);
        }

        @Override
        public String toString() {
            return String.format(
                "System Resource Monitoring Summary:\n" +
                "Monitoring Duration: %s\n" +
                "Samples Collected: %d\n\n" +
                "CPU Usage:\n" +
                "  Average: %.1f%%\n" +
                "  Maximum: %.1f%%\n\n" +
                "Heap Memory:\n" +
                "  Average: %.1f MB\n" +
                "  Maximum: %.1f MB\n\n" +
                "Thread Count:\n" +
                "  Minimum: %d\n" +
                "  Maximum: %d\n\n" +
                "Garbage Collection:\n" +
                "  Total Collections: %d\n" +
                "  Total GC Time: %d ms\n",
                monitoringDuration,
                sampleCount,
                avgCpuUsage,
                maxCpuUsage,
                avgHeapUsageMB,
                maxHeapUsageMB,
                minThreadCount,
                maxThreadCount,
                totalGcCount,
                totalGcTimeMs
            );
        }

        // Getters for all fields
        public Duration getMonitoringDuration() { return monitoringDuration; }
        public double getAvgCpuUsage() { return avgCpuUsage; }
        public double getMaxCpuUsage() { return maxCpuUsage; }
        public double getAvgHeapUsageMB() { return avgHeapUsageMB; }
        public double getMaxHeapUsageMB() { return maxHeapUsageMB; }
        public long getMinThreadCount() { return minThreadCount; }
        public long getMaxThreadCount() { return maxThreadCount; }
        public long getTotalGcTimeMs() { return totalGcTimeMs; }
        public int getTotalGcCount() { return totalGcCount; }
        public int getSampleCount() { return sampleCount; }
    }
}
