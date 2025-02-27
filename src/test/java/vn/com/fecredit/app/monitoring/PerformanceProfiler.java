package vn.com.fecredit.app.monitoring;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Performance profiling interface and implementation
 */
public class PerformanceProfiler {
    
    public interface ProfilerSnapshot {
        String getLabel();
        double getElapsedTimeMs();
        double getCpuTimeMs();
        double getMemoryMB();
    }

    public interface ProfilerSummary {
        List<ProfilerSnapshot> getSnapshots();
        double getTotalCpuTimeMs();
        double getTotalElapsedTimeMs();
        double getMaxMemoryMB();
    }

    private final List<ProfilerSnapshot> snapshots = new CopyOnWriteArrayList<>();
    private final AtomicReference<Double> maxMemory = new AtomicReference<>(0.0);
    private double totalCpuTime = 0;
    private double totalElapsedTime = 0;

    public synchronized void recordLatency(String label, double elapsedTimeMs) {
        recordSnapshot(label, elapsedTimeMs, elapsedTimeMs * 0.8, getCurrentMemoryUsage());
    }

    public synchronized void recordMemoryUsage(String label, double memoryMB) {
        maxMemory.updateAndGet(current -> Math.max(current, memoryMB));
        recordSnapshot(label, 0, 0, memoryMB);
    }

    private synchronized void recordSnapshot(String label, double elapsed, double cpu, double memory) {
        ProfilerSnapshot snapshot = new SimpleSnapshot(label, elapsed, cpu, memory);
        snapshots.add(snapshot);
        totalCpuTime += cpu;
        totalElapsedTime += elapsed;
    }

    public synchronized ProfilerSummary getAndResetSummary() {
        ProfilerSummary summary = new SimpleSummary(
            new ArrayList<>(snapshots),
            totalCpuTime,
            totalElapsedTime,
            maxMemory.get()
        );
        reset();
        return summary;
    }

    public synchronized void reset() {
        snapshots.clear();
        maxMemory.set(0.0);
        totalCpuTime = 0;
        totalElapsedTime = 0;
    }

    private double getCurrentMemoryUsage() {
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        return (totalMemory - freeMemory) / (1024.0 * 1024.0); // Convert to MB
    }

    private static class SimpleSnapshot implements ProfilerSnapshot {
        private final String label;
        private final double elapsedTime;
        private final double cpuTime;
        private final double memory;

        SimpleSnapshot(String label, double elapsedTime, double cpuTime, double memory) {
            this.label = label;
            this.elapsedTime = elapsedTime;
            this.cpuTime = cpuTime;
            this.memory = memory;
        }

        @Override public String getLabel() { return label; }
        @Override public double getElapsedTimeMs() { return elapsedTime; }
        @Override public double getCpuTimeMs() { return cpuTime; }
        @Override public double getMemoryMB() { return memory; }
    }

    private static class SimpleSummary implements ProfilerSummary {
        private final List<ProfilerSnapshot> snapshots;
        private final double totalCpuTime;
        private final double totalElapsedTime;
        private final double maxMemory;

        SimpleSummary(List<ProfilerSnapshot> snapshots, double totalCpuTime, 
                     double totalElapsedTime, double maxMemory) {
            this.snapshots = snapshots;
            this.totalCpuTime = totalCpuTime;
            this.totalElapsedTime = totalElapsedTime;
            this.maxMemory = maxMemory;
        }

        @Override public List<ProfilerSnapshot> getSnapshots() { return snapshots; }
        @Override public double getTotalCpuTimeMs() { return totalCpuTime; }
        @Override public double getTotalElapsedTimeMs() { return totalElapsedTime; }
        @Override public double getMaxMemoryMB() { return maxMemory; }
    }
}
