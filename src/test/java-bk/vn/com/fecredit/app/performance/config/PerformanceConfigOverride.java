package vn.com.fecredit.app.performance.config;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class PerformanceConfigOverride {
    private final Map<String, Consumer<PerformanceTestConfig>> overrides = new HashMap<>();

    public PerformanceConfigOverride withConcurrencyLevel(int level) {
        overrides.put("concurrencyLevel", config -> config.setConcurrencyLevel(level));
        return this;
    }

    public PerformanceConfigOverride withOperationCount(int count) {
        overrides.put("operationCount", config -> config.setOperationCount(count));
        return this;
    }

    public PerformanceConfigOverride withTimeout(int minutes) {
        overrides.put("timeoutMinutes", config -> config.setTimeoutMinutes(minutes));
        return this;
    }

    public PerformanceConfigOverride withSampleInterval(int milliseconds) {
        overrides.put("sampleInterval", 
            config -> config.getMonitoring().setSampleIntervalMs(milliseconds));
        return this;
    }

    public PerformanceConfigOverride withOutputDirectory(String directory) {
        overrides.put("outputDirectory", 
            config -> config.getMonitoring().setOutputDirectory(directory));
        return this;
    }

    public PerformanceConfigOverride withGcMonitoring(boolean enabled) {
        overrides.put("gcMonitoring", 
            config -> config.getMonitoring().setEnableGcMonitoring(enabled));
        return this;
    }

    public PerformanceConfigOverride withCpuProfiling(boolean enabled) {
        overrides.put("cpuProfiling", 
            config -> config.getMonitoring().setEnableCpuProfiling(enabled));
        return this;
    }

    public PerformanceConfigOverride withMemoryTracking(boolean enabled) {
        overrides.put("memoryTracking", 
            config -> config.getMonitoring().setEnableMemoryTracking(enabled));
        return this;
    }

    public PerformanceConfigOverride withThreadStateTracking(boolean enabled) {
        overrides.put("threadStateTracking", 
            config -> config.getMonitoring().setEnableThreadStateTracking(enabled));
        return this;
    }

    public PerformanceConfigOverride withMaxAverageResponseMs(int ms) {
        overrides.put("maxAverageResponseMs", 
            config -> config.getThresholds().setMaxAverageResponseMs(ms));
        return this;
    }

    public PerformanceConfigOverride withMaxP95ResponseMs(int ms) {
        overrides.put("maxP95ResponseMs", 
            config -> config.getThresholds().setMaxP95ResponseMs(ms));
        return this;
    }

    public PerformanceConfigOverride withMaxMemoryPerOpKb(int kb) {
        overrides.put("maxMemoryPerOpKb", 
            config -> config.getThresholds().setMaxMemoryPerOpKb(kb));
        return this;
    }

    public PerformanceConfigOverride withMaxErrorRate(double rate) {
        overrides.put("maxErrorRate", 
            config -> config.getThresholds().setMaxErrorRate(rate));
        return this;
    }

    public void applyTo(PerformanceTestConfig config) {
        overrides.values().forEach(override -> override.accept(config));
    }

    public static PerformanceConfigOverride create() {
        return new PerformanceConfigOverride();
    }

    public Map<String, Consumer<PerformanceTestConfig>> getOverrides() {
        return new HashMap<>(overrides);
    }
}
