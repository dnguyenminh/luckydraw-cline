package vn.com.fecredit.app.monitoring;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

/**
 * Utility class to generate test data for performance testing
 */
public class TestDataGenerator {
    private static final Random random = new Random();

    public static List<PerformanceProfiler.ProfilerSnapshot> generateSnapshots(int count) {
        List<PerformanceProfiler.ProfilerSnapshot> snapshots = new ArrayList<>();
        
        IntStream.range(0, count).forEach(i -> {
            snapshots.add(createSnapshot(
                "operation-" + (i % 100),
                random.nextDouble() * 1000,  // elapsed time
                random.nextDouble() * 500,   // CPU time
                random.nextDouble() * 1024   // memory usage
            ));
        });
        
        return snapshots;
    }

    public static PerformanceProfiler.ProfilerSummary createSummary(List<PerformanceProfiler.ProfilerSnapshot> snapshots) {
        double totalCpuTime = snapshots.stream().mapToDouble(s -> ((TestSnapshot)s).cpuTime).sum();
        double totalElapsedTime = snapshots.stream().mapToDouble(s -> ((TestSnapshot)s).elapsedTime).sum();
        double maxMemory = snapshots.stream().mapToDouble(s -> ((TestSnapshot)s).memory).max().orElse(0.0);

        return new TestSummary(snapshots, totalCpuTime, totalElapsedTime, maxMemory);
    }

    private static PerformanceProfiler.ProfilerSnapshot createSnapshot(String label, double elapsed, double cpu, double memory) {
        return new TestSnapshot(label, elapsed, cpu, memory);
    }

    private static class TestSnapshot implements PerformanceProfiler.ProfilerSnapshot {
        private final String label;
        private final double elapsedTime;
        private final double cpuTime;
        private final double memory;

        TestSnapshot(String label, double elapsedTime, double cpuTime, double memory) {
            this.label = label;
            this.elapsedTime = elapsedTime;
            this.cpuTime = cpuTime;
            this.memory = memory;
        }

        @Override
        public String getLabel() {
            return label;
        }

        @Override
        public double getElapsedTimeMs() {
            return elapsedTime;
        }

        @Override
        public double getCpuTimeMs() {
            return cpuTime;
        }

        @Override
        public double getMemoryMB() {
            return memory;
        }
    }

    private static class TestSummary implements PerformanceProfiler.ProfilerSummary {
        private final List<PerformanceProfiler.ProfilerSnapshot> snapshots;
        private final double totalCpuTime;
        private final double totalElapsedTime;
        private final double maxMemory;

        TestSummary(List<PerformanceProfiler.ProfilerSnapshot> snapshots, double totalCpuTime, 
                   double totalElapsedTime, double maxMemory) {
            this.snapshots = snapshots;
            this.totalCpuTime = totalCpuTime;
            this.totalElapsedTime = totalElapsedTime;
            this.maxMemory = maxMemory;
        }

        @Override
        public List<PerformanceProfiler.ProfilerSnapshot> getSnapshots() {
            return snapshots;
        }

        @Override
        public double getTotalCpuTimeMs() {
            return totalCpuTime;
        }

        @Override
        public double getTotalElapsedTimeMs() {
            return totalElapsedTime;
        }

        @Override
        public double getMaxMemoryMB() {
            return maxMemory;
        }
    }
}
