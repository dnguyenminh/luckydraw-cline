package vn.com.fecredit.app.performance.baseline;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class RegressionAlertTest {

    @TempDir
    Path tempDir;

    private Path alertLogFile;
    private RegressionAlert.AlertConfig config;
    private RegressionAlert alert;
    private LocalDateTime baselineTime;
    private LocalDateTime currentTime;

    @BeforeEach
    void setUp() {
        alertLogFile = tempDir.resolve("regression-alerts.log");
        baselineTime = LocalDateTime.now().minusDays(1);
        currentTime = LocalDateTime.now();
        
        config = new RegressionAlert.AlertConfig(
            20.0,  // criticalThreshold
            10.0,  // warningThreshold
            3,     // consecutiveFailures
            alertLogFile,
            Optional.empty(),
            false  // disable console output for testing
        );
        
        alert = new RegressionAlert(config);
    }

    @Test
    void shouldGenerateCriticalAlert() throws IOException {
        // Given
        var result = createBaselineResult(
            "CriticalTest", 
            1.0, 1.1,   // baseline metrics
            2.5, 2.6,   // current metrics (150% slower)
            150.0,      // deviation
            true,       // isRegression
            "Severe performance degradation"
        );

        // When
        for (int i = 0; i < config.consecutiveFailuresForAlert(); i++) {
            alert.processResult(result);
        }

        // Then
        assertTrue(Files.exists(alertLogFile), "Alert log file should be created");
        String content = Files.readString(alertLogFile, StandardCharsets.UTF_8);
        assertTrue(content.contains("[CRITICAL]"), "Should contain CRITICAL level");
        assertTrue(content.contains("IMMEDIATE ACTION REQUIRED"), "Should indicate immediate action");
        assertTrue(content.contains("CriticalTest"), "Should include test name");
    }

    @Test
    void shouldGenerateWarningAlert() throws IOException {
        // Given
        var result = createBaselineResult(
            "WarningTest",
            1.0, 1.1,   // baseline metrics
            1.15, 1.25, // current metrics (15% slower)
            15.0,       // deviation
            true,       // isRegression
            "Performance degradation detected"
        );

        // When
        for (int i = 0; i < config.consecutiveFailuresForAlert(); i++) {
            alert.processResult(result);
        }

        // Then
        String content = Files.readString(alertLogFile, StandardCharsets.UTF_8);
        assertTrue(content.contains("[WARNING]"), "Should contain WARNING level");
        assertTrue(content.contains("Investigation Recommended"), "Should recommend investigation");
    }

    @Test
    void shouldNotAlertForStablePerformance() {
        // Given
        var result = createBaselineResult(
            "StableTest",
            1.0, 1.1,   // baseline metrics
            1.02, 1.12, // current metrics
            2.0,        // deviation
            false,      // not regression
            "Performance is stable"
        );

        // When
        alert.processResult(result);

        // Then
        assertFalse(Files.exists(alertLogFile), "Should not create alert file for stable performance");
    }

    @Test
    void shouldTrackConsecutiveFailures() {
        // Given
        var result = createBaselineResult(
            "TrackingTest",
            1.0, 1.1,
            1.5, 1.6,
            50.0,
            true,
            "Performance regression"
        );

        // When
        alert.processResult(result);
        alert.processResult(result);

        // Then
        Map<String, Integer> failures = alert.getConsecutiveFailures();
        assertEquals(2, failures.get("TrackingTest"), "Should track correct number of failures");
    }

    @Test
    void shouldResetFailureCountOnSuccess() {
        // Given
        var failureResult = createBaselineResult(
            "ResetTest",
            1.0, 1.1,
            1.5, 1.6,
            50.0,
            true,
            "Performance regression"
        );

        var successResult = createBaselineResult(
            "ResetTest",
            1.0, 1.1,
            1.02, 1.12,
            2.0,
            false,
            "Performance is stable"
        );

        // When
        alert.processResult(failureResult);
        alert.processResult(failureResult);
        alert.processResult(successResult);

        // Then
        Map<String, Integer> failures = alert.getConsecutiveFailures();
        assertNull(failures.get("ResetTest"), "Should reset failure count after success");
    }

    @Test
    void shouldRespectAlertThrottling() throws IOException {
        // Given
        var result = createBaselineResult(
            "ThrottleTest",
            1.0, 1.1,
            2.0, 2.1,
            100.0,
            true,
            "Performance regression"
        );

        // When
        for (int i = 0; i < config.consecutiveFailuresForAlert(); i++) {
            alert.processResult(result);
        }

        alert.processResult(result);
        alert.processResult(result);

        // Then
        List<String> alertLines = Files.readAllLines(alertLogFile, StandardCharsets.UTF_8);
        long alertCount = alertLines.stream()
            .filter(line -> line.contains("ThrottleTest"))
            .count();
        assertEquals(1, alertCount, "Should generate only one alert during throttle period");
    }

    @Test
    void shouldIncludeRecommendations() throws IOException {
        // Given
        var result = createBaselineResult(
            "RecommendationTest",
            1.0, 1.1,
            3.0, 3.1,
            200.0,
            true,
            "Severe regression"
        );

        // When
        for (int i = 0; i < config.consecutiveFailuresForAlert(); i++) {
            alert.processResult(result);
        }

        // Then
        String content = Files.readString(alertLogFile, StandardCharsets.UTF_8);
        assertTrue(content.contains("Recommendations:"), "Should include recommendations section");
        assertTrue(content.contains("1."), "Should include numbered recommendations");
        assertTrue(content.contains("2."), "Should include multiple recommendations");
    }

    @Test
    void shouldHandleEmailNotifications() {
        // Given
        var emailConfig = new RegressionAlert.AlertConfig(
            20.0, 10.0, 3,
            alertLogFile,
            Optional.of("test@example.com"),
            false
        );
        var emailAlert = new RegressionAlert(emailConfig);

        var result = createBaselineResult(
            "EmailTest",
            1.0, 1.1,
            2.0, 2.1,
            100.0,
            true,
            "Performance regression"
        );

        // When/Then
        assertDoesNotThrow(() -> {
            for (int i = 0; i < emailConfig.consecutiveFailuresForAlert(); i++) {
                emailAlert.processResult(result);
            }
        }, "Should handle email notifications without errors");
    }

    @Test
    void shouldHandleInvalidAlertLogPath() {
        // Given
        var invalidConfig = new RegressionAlert.AlertConfig(
            20.0, 10.0, 3,
            Path.of("/invalid/path/alerts.log"),
            Optional.empty(),
            false
        );
        var invalidAlert = new RegressionAlert(invalidConfig);

        var result = createBaselineResult(
            "InvalidPathTest",
            1.0, 1.1,
            2.0, 2.1,
            100.0,
            true,
            "Test regression"
        );

        // When/Then
        assertDoesNotThrow(() -> {
            for (int i = 0; i < invalidConfig.consecutiveFailuresForAlert(); i++) {
                invalidAlert.processResult(result);
            }
        }, "Should handle invalid log path gracefully");
    }

    private PerformanceBaseline.BaselineResult createBaselineResult(
            String testName,
            double baselineMean,
            double baselineP95,
            double currentMean,
            double currentP95,
            double deviation,
            boolean isRegression,
            String analysis) {
        return new PerformanceBaseline.BaselineResult(
            testName,
            baselineMean,
            baselineP95,
            currentMean,
            currentP95,
            deviation,
            isRegression,
            analysis,
            baselineTime,
            currentTime
        );
    }
}
