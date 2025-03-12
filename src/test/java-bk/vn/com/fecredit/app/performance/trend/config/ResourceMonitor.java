package vn.com.fecredit.app.performance.trend.config;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.ThreadMXBean;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class ResourceMonitor {
    private final MemoryMXBean memoryBean;
    private final ThreadMXBean threadBean;
    private final List<MetricSnapshot> snapshots;
    private final AtomicLong totalGcCount;
    private volatile Instant lastUpdateTime;

    public ResourceMonitor() {
        this.memoryBean = ManagementFactory.getMemoryMXBean();
        this.threadBean = ManagementFactory.getThreadMXBean();
        this.snapshots = new ArrayList<>();
        this.totalGcCount = new AtomicLong(0);
        this.lastUpdateTime = Instant.now();
    }

    public synchronized void updateMetrics() {
        long heapUsed = memoryBean.getHeapMemoryUsage().getUsed();
        long nonHeapUsed = memoryBean.getNonHeapMemoryUsage().getUsed();
        int threadCount = threadBean.getThreadCount();
        double cpuUsage = calculateCpuUsage();
        
        snapshots.add(new MetricSnapshot(
            Instant.now(),
            heapUsed,
            nonHeapUsed,
            threadCount,
            cpuUsage
        ));

        // Keep only recent snapshots
        if (snapshots.size() > 1000) {
            snapshots.remove(0);
        }

        lastUpdateTime = Instant.now();
    }

    private double calculateCpuUsage() {
        long totalCpu = 0;
        for (long threadId : threadBean.getAllThreadIds()) {
            totalCpu += threadBean.getThreadCpuTime(threadId);
        }
        return totalCpu / (double) Runtime.getRuntime().availableProcessors();
    }

    public ResourceSummary generateSummary() {
        if (snapshots.isEmpty()) {
            return new ResourceSummary(0.0, 0L, 0);
        }

        double avgCpu = snapshots.stream()
            .mapToDouble(MetricSnapshot::cpuUsage)
            .average()
            .orElse(0.0);

        long peakMemory = snapshots.stream()
            .mapToLong(s -> s.heapUsed() + s.nonHeapUsed())
            .max()
            .orElse(0L);

        return new ResourceSummary(
            avgCpu,
            peakMemory,
            totalGcCount.intValue()
        );
    }

    public List<MetricSnapshot> getSnapshots() {
        return new ArrayList<>(snapshots);
    }

    public Instant getLastUpdateTime() {
        return lastUpdateTime;
    }

    public record MetricSnapshot(
        Instant timestamp,
        long heapUsed,
        long nonHeapUsed,
        int threadCount,
        double cpuUsage
    ) {}

    public record ResourceSummary(
        double averageCpuUsage,
        long peakMemoryUsage,
        int totalGcCount
    ) {}
}
