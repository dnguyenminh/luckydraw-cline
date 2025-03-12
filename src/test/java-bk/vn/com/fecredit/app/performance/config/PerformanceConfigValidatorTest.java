package vn.com.fecredit.app.performance.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PerformanceConfigValidatorTest {

    private PerformanceConfigValidator validator;
    private PerformanceTestConfig config;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        validator = new PerformanceConfigValidator();
        validator.afterPropertiesSet();
        
        // Initialize with valid configuration
        config = new PerformanceTestConfig();
        config.getMonitoring().setOutputDirectory(tempDir.toString());
        config.setConcurrencyLevel(4);
        config.setOperationCount(100);
        config.setTimeoutMinutes(5);
    }

    @Test
    void shouldValidateValidConfiguration() {
        assertDoesNotThrow(() -> validator.validateAndApply(config));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, -1, 1000})
    void shouldRejectInvalidConcurrencyLevels(int level) {
        config.setConcurrencyLevel(level);
        var exception = assertThrows(
            PerformanceConfigValidationException.class,
            () -> validator.validateAndApply(config)
        );
        assertTrue(exception.getMessage().contains("Concurrency level"));
    }

    @Test
    void shouldRejectInvalidOperationCount() {
        config.setOperationCount(1);
        config.setConcurrencyLevel(2);
        
        var exception = assertThrows(
            PerformanceConfigValidationException.class,
            () -> validator.validateAndApply(config)
        );
        assertTrue(exception.getMessage().contains("Operation count"));
    }

    @Test
    void shouldValidateMonitoringSettings() {
        var monitoring = config.getMonitoring();
        monitoring.setEnableGcMonitoring(false);
        monitoring.setEnableCpuProfiling(false);
        monitoring.setEnableMemoryTracking(false);
        monitoring.setEnableThreadStateTracking(false);

        var exception = assertThrows(
            PerformanceConfigValidationException.class,
            () -> validator.validateAndApply(config)
        );
        assertTrue(exception.getMessage().contains("monitoring must be enabled"));
    }

    @Test
    void shouldValidateOutputDirectory() throws Exception {
        // Test with non-existent directory that can't be created
        Path rootPath = Path.of("/non-existent-root/test");
        config.getMonitoring().setOutputDirectory(rootPath.toString());

        var exception = assertThrows(
            PerformanceConfigValidationException.class,
            () -> validator.validateAndApply(config)
        );
        assertTrue(exception.getMessage().contains("output directory"));
    }

    @Test
    void shouldValidateResponseTimeThresholds() {
        config.getThresholds().setMaxAverageResponseMs(1000);
        config.getThresholds().setMaxP95ResponseMs(500);

        var exception = assertThrows(
            PerformanceConfigValidationException.class,
            () -> validator.validateAndApply(config)
        );
        assertTrue(exception.getMessage().contains("response time threshold"));
    }

    @Test
    void shouldValidateTimeout() {
        config.setOperationCount(10000);
        config.setConcurrencyLevel(10);
        config.setTimeoutMinutes(1);

        var exception = assertThrows(
            PerformanceConfigValidationException.class,
            () -> validator.validateAndApply(config)
        );
        assertTrue(exception.getMessage().contains("Timeout"));
    }

    @Test
    void shouldValidateSampleInterval() {
        config.getMonitoring().setSampleIntervalMs(5);
        
        var exception = assertThrows(
            PerformanceConfigValidationException.class,
            () -> validator.validateAndApply(config)
        );
        assertTrue(exception.getMessage().contains("Sample interval"));
    }

    @Test
    void shouldCollectMultipleValidationErrors() {
        config.setConcurrencyLevel(0);
        config.setOperationCount(0);
        config.getMonitoring().setSampleIntervalMs(1);

        var exception = assertThrows(
            PerformanceConfigValidationException.class,
            () -> validator.validateAndApply(config)
        );
        
        List<String> errors = exception.getValidationErrors();
        assertTrue(errors.size() > 1, "Should collect multiple validation errors");
        assertTrue(errors.stream().anyMatch(e -> e.contains("Concurrency level")));
        assertTrue(errors.stream().anyMatch(e -> e.contains("Operation count")));
        assertTrue(errors.stream().anyMatch(e -> e.contains("Sample interval")));
    }

    @Test
    void shouldHandleReadOnlyDirectory() throws Exception {
        Path readOnlyDir = tempDir.resolve("readonly");
        Files.createDirectory(readOnlyDir);
        readOnlyDir.toFile().setReadOnly();
        
        config.getMonitoring().setOutputDirectory(readOnlyDir.toString());

        var exception = assertThrows(
            PerformanceConfigValidationException.class,
            () -> validator.validateAndApply(config)
        );
        assertTrue(exception.getMessage().contains("writable"));
    }
}
