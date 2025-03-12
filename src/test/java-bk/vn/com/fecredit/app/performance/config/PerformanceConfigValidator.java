package vn.com.fecredit.app.performance.config;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Component
public class PerformanceConfigValidator implements InitializingBean {

    private final LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
    
    @Override
    public void afterPropertiesSet() {
        validator.afterPropertiesSet();
    }

    public void validateAndApply(PerformanceTestConfig config) {
        List<String> validationIssues = new ArrayList<>();
        
        // Basic JSR-303 validation
        Errors errors = new BeanPropertyBindingResult(config, "performanceConfig");
        validator.validate(config, errors);
        
        if (errors.hasErrors()) {
            errors.getAllErrors().forEach(error -> 
                validationIssues.add(error.getDefaultMessage())
            );
        }

        // Additional custom validations
        validateOutputDirectory(config, validationIssues);
        validateResourceLimits(config, validationIssues);
        validateMonitoringSettings(config, validationIssues);
        validateTestParameters(config, validationIssues);

        if (!validationIssues.isEmpty()) {
            throw PerformanceConfigValidationException.fromErrors(validationIssues);
        }
    }

    private void validateOutputDirectory(PerformanceTestConfig config, List<String> issues) {
        Path outputPath = Path.of(config.getMonitoring().getOutputDirectory());
        
        // Check if directory exists or can be created
        if (!Files.exists(outputPath)) {
            try {
                Files.createDirectories(outputPath);
            } catch (Exception e) {
                issues.add("Cannot create output directory: " + e.getMessage());
            }
        }
        
        // Check if directory is writable
        if (!Files.isWritable(outputPath)) {
            issues.add("Output directory is not writable: " + outputPath);
        }
    }

    private void validateResourceLimits(PerformanceTestConfig config, List<String> issues) {
        int availableProcessors = Runtime.getRuntime().availableProcessors();
        long maxMemory = Runtime.getRuntime().maxMemory();
        
        // Check concurrency level against available processors
        if (config.getConcurrencyLevel() > availableProcessors * 4) {
            issues.add(String.format(
                "Concurrency level (%d) is too high for available processors (%d). " +
                "Recommended maximum is %d",
                config.getConcurrencyLevel(),
                availableProcessors,
                availableProcessors * 4
            ));
        }

        // Check if memory thresholds are reasonable
        long estimatedMemoryPerOp = config.getThresholds().getMaxMemoryPerOpKb() * 1024L;
        long totalEstimatedMemory = estimatedMemoryPerOp * config.getOperationCount();
        if (totalEstimatedMemory > maxMemory * 0.8) {
            issues.add(String.format(
                "Estimated memory usage (%.2f GB) exceeds 80%% of maximum available memory (%.2f GB)",
                totalEstimatedMemory / (1024.0 * 1024.0 * 1024.0),
                maxMemory / (1024.0 * 1024.0 * 1024.0)
            ));
        }
    }

    private void validateMonitoringSettings(PerformanceTestConfig config, List<String> issues) {
        var monitoring = config.getMonitoring();
        
        // Check sampling interval
        if (monitoring.getSampleIntervalMs() < 10) {
            issues.add("Sample interval is too low, may impact performance");
        }
        
        // Validate monitoring compatibility
        if (monitoring.isEnableCpuProfiling()) {
            try {
                ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime();
            } catch (UnsupportedOperationException e) {
                issues.add("CPU profiling is not supported on this platform");
            }
        }

        // Check if all monitoring is disabled
        if (!monitoring.isEnableGcMonitoring() && 
            !monitoring.isEnableCpuProfiling() && 
            !monitoring.isEnableMemoryTracking() && 
            !monitoring.isEnableThreadStateTracking()) {
            issues.add("At least one type of monitoring must be enabled");
        }
    }

    private void validateTestParameters(PerformanceTestConfig config, List<String> issues) {
        // Validate operation count
        if (config.getOperationCount() < config.getConcurrencyLevel()) {
            issues.add(String.format(
                "Operation count (%d) should be greater than concurrency level (%d)",
                config.getOperationCount(),
                config.getConcurrencyLevel()
            ));
        }

        // Validate thresholds consistency
        var thresholds = config.getThresholds();
        if (thresholds.getMaxP95ResponseMs() < thresholds.getMaxAverageResponseMs()) {
            issues.add(String.format(
                "P95 response time threshold (%d ms) must be greater than average response time threshold (%d ms)",
                thresholds.getMaxP95ResponseMs(),
                thresholds.getMaxAverageResponseMs()
            ));
        }

        // Validate timeout
        int minTimeout = (config.getOperationCount() / config.getConcurrencyLevel()) / 60 + 1;
        if (config.getTimeoutMinutes() < minTimeout) {
            issues.add(String.format(
                "Timeout (%d minutes) may be too short for the configured operations. " +
                "Recommended minimum: %d minutes",
                config.getTimeoutMinutes(),
                minTimeout
            ));
        }
    }
}
