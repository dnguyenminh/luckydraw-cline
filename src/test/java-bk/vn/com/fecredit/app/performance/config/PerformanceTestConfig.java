package vn.com.fecredit.app.performance.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import java.nio.file.Path;

@Configuration
@ConfigurationProperties(prefix = "performance.test")
@Validated
public class PerformanceTestConfig {
    
    @Min(value = 1, message = "Concurrency level must be at least 1")
    @Max(value = 100, message = "Concurrency level must not exceed 100")
    private int concurrencyLevel = 20;

    @Min(value = 1, message = "Operation count must be at least 1")
    @Max(value = 10000, message = "Operation count must not exceed 10000")
    private int operationCount = 1000;

    @Min(value = 1, message = "Timeout minutes must be at least 1")
    @Max(value = 60, message = "Timeout minutes must not exceed 60")
    private int timeoutMinutes = 5;

    private boolean detailedMetrics = true;

    @Valid
    private final Thresholds thresholds = new Thresholds();

    @Valid
    private final MonitoringConfig monitoring = new MonitoringConfig();

    public static class Thresholds {
        @Min(value = 0, message = "Maximum average response time must be non-negative")
        private int maxAverageResponseMs = 500;

        @Min(value = 0, message = "Maximum P95 response time must be non-negative")
        private int maxP95ResponseMs = 1000;

        @Min(value = 0, message = "Maximum memory per operation must be non-negative")
        private int maxMemoryPerOpKb = 50;

        @DecimalMin(value = "0.0", message = "Maximum error rate must be non-negative")
        @DecimalMax(value = "100.0", message = "Maximum error rate must not exceed 100%")
        private double maxErrorRate = 1.0;

        @Min(value = 0, message = "Maximum GC time percentage must be non-negative")
        @Max(value = 100, message = "Maximum GC time percentage must not exceed 100")
        private int maxGcTimePercentage = 10;

        @DecimalMin(value = "0.0", message = "Maximum CPU usage percentage must be non-negative")
        @DecimalMax(value = "100.0", message = "Maximum CPU usage percentage must not exceed 100%")
        private double maxCpuUsagePercentage = 80.0;

        // Getters and setters remain the same
        public int getMaxAverageResponseMs() { return maxAverageResponseMs; }
        public void setMaxAverageResponseMs(int value) { maxAverageResponseMs = value; }
        
        public int getMaxP95ResponseMs() { return maxP95ResponseMs; }
        public void setMaxP95ResponseMs(int value) { maxP95ResponseMs = value; }
        
        public int getMaxMemoryPerOpKb() { return maxMemoryPerOpKb; }
        public void setMaxMemoryPerOpKb(int value) { maxMemoryPerOpKb = value; }
        
        public double getMaxErrorRate() { return maxErrorRate; }
        public void setMaxErrorRate(double value) { maxErrorRate = value; }
        
        public int getMaxGcTimePercentage() { return maxGcTimePercentage; }
        public void setMaxGcTimePercentage(int value) { maxGcTimePercentage = value; }
        
        public double getMaxCpuUsagePercentage() { return maxCpuUsagePercentage; }
        public void setMaxCpuUsagePercentage(double value) { maxCpuUsagePercentage = value; }
    }

    public static class MonitoringConfig {
        @Min(value = 10, message = "Sample interval must be at least 10ms")
        @Max(value = 10000, message = "Sample interval must not exceed 10 seconds")
        private int sampleIntervalMs = 100;

        private boolean enableGcMonitoring = true;
        private boolean enableCpuProfiling = true;
        private boolean enableMemoryTracking = true;
        private boolean enableThreadStateTracking = true;

        @NotBlank(message = "Output directory must not be blank")
        private String outputDirectory = "target/performance-reports";

        @AssertTrue(message = "At least one monitoring type must be enabled")
        private boolean isAtLeastOneMonitoringEnabled() {
            return enableGcMonitoring || enableCpuProfiling || 
                   enableMemoryTracking || enableThreadStateTracking;
        }

        // Pre-validate output directory
        @AssertTrue(message = "Output directory must be a valid path")
        private boolean isValidOutputDirectory() {
            try {
                Path.of(outputDirectory);
                return true;
            } catch (Exception e) {
                return false;
            }
        }

        // Getters and setters remain the same
        public int getSampleIntervalMs() { return sampleIntervalMs; }
        public void setSampleIntervalMs(int value) { sampleIntervalMs = value; }
        
        public boolean isEnableGcMonitoring() { return enableGcMonitoring; }
        public void setEnableGcMonitoring(boolean value) { enableGcMonitoring = value; }
        
        public boolean isEnableCpuProfiling() { return enableCpuProfiling; }
        public void setEnableCpuProfiling(boolean value) { enableCpuProfiling = value; }
        
        public boolean isEnableMemoryTracking() { return enableMemoryTracking; }
        public void setEnableMemoryTracking(boolean value) { enableMemoryTracking = value; }
        
        public boolean isEnableThreadStateTracking() { return enableThreadStateTracking; }
        public void setEnableThreadStateTracking(boolean value) { enableThreadStateTracking = value; }
        
        public String getOutputDirectory() { return outputDirectory; }
        public void setOutputDirectory(String value) { outputDirectory = value; }
    }

    // Main class getters and setters remain the same
    public int getConcurrencyLevel() { return concurrencyLevel; }
    public void setConcurrencyLevel(int value) { concurrencyLevel = value; }

    public int getOperationCount() { return operationCount; }
    public void setOperationCount(int value) { operationCount = value; }

    public int getTimeoutMinutes() { return timeoutMinutes; }
    public void setTimeoutMinutes(int value) { timeoutMinutes = value; }

    public boolean isDetailedMetrics() { return detailedMetrics; }
    public void setDetailedMetrics(boolean value) { detailedMetrics = value; }

    public Thresholds getThresholds() { return thresholds; }

    public MonitoringConfig getMonitoring() { return monitoring; }

    @AssertTrue(message = "P95 response time threshold must be greater than average response time threshold")
    private boolean isValidResponseTimeThresholds() {
        return thresholds.maxP95ResponseMs >= thresholds.maxAverageResponseMs;
    }
}
