package vn.com.fecredit.app.monitoring;

import java.lang.management.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

public class SystemResourceMonitor implements AutoCloseable {
    private final RuntimeMXBean runtimeMXBean;
    private final OperatingSystemMXBean osMXBean;
    private final MemoryMXBean memoryMXBean;
    private final List<GarbageCollectorMXBean> gcMXBeans;
    private final ThreadMXBean threadMXBean;
    
    private final List<MetricSnapshot> snapshots;
    private final Instant startTime;
    private volatile long lastGcCount;
    private volatile long lastGcTime;

    public SystemResourceMonitor() {
        this.runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        this.osMXBean = ManagementFactory.getOperatingSystemMXBean();
        this.memoryMXBean = ManagementFactory.getMemoryMXBean();
        this.gcMXBeans = ManagementFactory.getGarbageCollectorMXBeans();
        this.threadMXBean = ManagementFactory.getThreadMXBean();
        
        this.snapshots = new CopyOnWriteArrayList<>();
        this.startTime = Instant.now();
        this.lastGcCount = getTotalGcCount();
        this.lastGcTime = getTotalGcTime();
    }

    public synchronized void takeSnapshot() {
        try {
            MemoryUsage heapUsage = memoryMXBean.getHeapMemoryUsage();
            MemoryUsage nonHeapUsage = memoryMXBean.getNonHeapMemoryUsage();
            
            long currentGcCount = getTotalGcCount();
            long currentGcTime = getTotalGcTime();
            
            double cpuUsage = getCpuUsage();
            long threadCount = threadMXBean.getThreadCount();
            
            snapshots.add(new MetricSnapshot(
                Instant.now(),
                cpuUsage,
                heapUsage.getUsed(),
                nonHeapUsage.getUsed(),
                threadCount,
                currentGcCount - lastGcCount,
                currentGcTime - lastGcTime
            ));
            
            lastGcCount = currentGcCount;
            lastGcTime = currentGcTime;
        } catch (Exception e) {
            throw new MonitoringException("Failed to take system metrics snapshot", e);
        }
    }

    public List<MetricSnapshot> getSnapshots() {
        return Collections.unmodifiableList(snapshots);
    }

    private double getCpuUsage() {
        try {
            if (osMXBean instanceof com.sun.management.OperatingSystemMXBean sunOsMXBean) {
                return sunOsMXBean.getProcessCpuLoad();
            }
            return -1.0;
        } catch (Exception e) {
            return -1.0;
        }
    }

    private long getTotalGcCount() {
        try {
            return gcMXBeans.stream()
                .mapToLong(GarbageCollectorMXBean::getCollectionCount)
                .sum();
        } catch (Exception e) {
            return 0L;
        }
    }

    private long getTotalGcTime() {
        try {
            return gcMXBeans.stream()
                .mapToLong(GarbageCollectorMXBean::getCollectionTime)
                .sum();
        } catch (Exception e) {
            return 0L;
        }
    }

    public record MetricSnapshot(
        Instant timestamp,
        double cpuUsage,
        long heapUsed,
        long nonHeapUsed,
        long threadCount,
        long gcCount,
        long gcTime
    ) {
        public MetricSnapshot {
            if (timestamp == null) {
                throw new IllegalArgumentException("Timestamp cannot be null");
            }
            if (heapUsed < 0 || nonHeapUsed < 0) {
                throw new IllegalArgumentException("Memory usage cannot be negative");
            }
            if (threadCount < 0) {
                throw new IllegalArgumentException("Thread count cannot be negative");
            }
        }
    }

    public Instant getStartTime() {
        return startTime;
    }

    public RuntimeMetrics getRuntimeMetrics() {
        try {
            Map<String, String> systemProperties = runtimeMXBean.getSystemProperties().entrySet().stream()
                .collect(Collectors.toUnmodifiableMap(
                    e -> String.valueOf(e.getKey()),
                    e -> String.valueOf(e.getValue()),
                    (v1, v2) -> v2
                ));

            return new RuntimeMetrics(
                runtimeMXBean.getUptime(),
                systemProperties,
                osMXBean.getAvailableProcessors(),
                memoryMXBean.getHeapMemoryUsage().getMax(),
                threadMXBean.getPeakThreadCount()
            );
        } catch (Exception e) {
            throw new MonitoringException("Failed to get runtime metrics", e);
        }
    }

    public record RuntimeMetrics(
        long uptime,
        Map<String, String> systemProperties,
        int availableProcessors,
        long maxHeapMemory,
        int peakThreadCount
    ) {
        public RuntimeMetrics {
            if (systemProperties == null) {
                systemProperties = Collections.emptyMap();
            }
            if (uptime < 0) {
                throw new IllegalArgumentException("Uptime cannot be negative");
            }
            if (availableProcessors <= 0) {
                throw new IllegalArgumentException("Available processors must be positive");
            }
        }
    }

    public synchronized void clearSnapshots() {
        snapshots.clear();
    }

    public MetricsSummary getMetricsSummary() {
        if (snapshots.isEmpty()) {
            return new MetricsSummary(0.0, 0L, 0L, 0, 0L);
        }

        try {
            double avgCpuUsage = snapshots.stream()
                .mapToDouble(MetricSnapshot::cpuUsage)
                .filter(cpu -> cpu >= 0)
                .average()
                .orElse(0.0);

            long maxHeapUsed = snapshots.stream()
                .mapToLong(MetricSnapshot::heapUsed)
                .max()
                .orElse(0L);

            long totalGcCount = snapshots.stream()
                .mapToLong(MetricSnapshot::gcCount)
                .sum();

            int avgThreadCount = (int) snapshots.stream()
                .mapToLong(MetricSnapshot::threadCount)
                .average()
                .orElse(0.0);

            long totalGcTime = snapshots.stream()
                .mapToLong(MetricSnapshot::gcTime)
                .sum();

            return new MetricsSummary(
                avgCpuUsage,
                maxHeapUsed,
                totalGcCount,
                avgThreadCount,
                totalGcTime
            );
        } catch (Exception e) {
            throw new MonitoringException("Failed to generate metrics summary", e);
        }
    }

    public record MetricsSummary(
        double averageCpuUsage,
        long peakMemoryUsage,
        long totalGcCount,
        int averageThreadCount,
        long totalGcTime
    ) {
        public MetricsSummary {
            if (peakMemoryUsage < 0) {
                throw new IllegalArgumentException("Peak memory usage cannot be negative");
            }
            if (totalGcCount < 0) {
                throw new IllegalArgumentException("Total GC count cannot be negative");
            }
        }
    }

    @Override
    public void close() {
        clearSnapshots();
    }

    public static class MonitoringException extends RuntimeException {
        public MonitoringException(String message) {
            super(message);
        }

        public MonitoringException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
