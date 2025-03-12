package vn.com.fecredit.app.performance.baseline;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class RegressionAlert {

    private static final DateTimeFormatter DATE_FORMAT = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public record AlertConfig(
        double criticalThresholdPercent,
        double warningThresholdPercent,
        int consecutiveFailuresForAlert,
        Path alertLogFile,
        Optional<String> emailRecipients,
        boolean enableConsoleOutput
    ) {
        public static AlertConfig defaultConfig(Path alertLogFile) {
            return new AlertConfig(
                20.0,  // Critical threshold: 20% degradation
                10.0,  // Warning threshold: 10% degradation
                3,     // Alert after 3 consecutive failures
                alertLogFile,
                Optional.empty(),
                true
            );
        }
    }

    public record Alert(
        String testName,
        AlertLevel level,
        double deviation,
        String analysis,
        LocalDateTime timestamp,
        String baselineReference,
        List<String> recommendations
    ) {
        public String format() {
            return String.format("""
                [%s] %s Alert - %s
                Timestamp: %s
                Deviation: %.1f%%
                Analysis: %s
                Baseline Reference: %s
                
                Recommendations:
                %s
                
                -------------------
                """,
                level,
                testName,
                level == AlertLevel.CRITICAL ? "IMMEDIATE ACTION REQUIRED" : "Investigation Recommended",
                timestamp.format(DATE_FORMAT),
                deviation,
                analysis,
                baselineReference,
                String.join("\n", recommendations)
            );
        }
    }

    public enum AlertLevel {
        CRITICAL,
        WARNING,
        INFO
    }

    private final AlertConfig config;
    private final Map<String, Integer> consecutiveFailures;
    private final Map<String, LocalDateTime> lastAlertTime;

    public RegressionAlert(AlertConfig config) {
        this.config = config;
        this.consecutiveFailures = new HashMap<>();
        this.lastAlertTime = new HashMap<>();
    }

    public void processResult(PerformanceBaseline.BaselineResult result) {
        if (!shouldProcessAlert(result)) {
            resetFailureCount(result.testName());
            return;
        }

        incrementFailureCount(result.testName());
        
        if (shouldGenerateAlert(result.testName())) {
            Alert alert = generateAlert(result);
            logAlert(alert);
            
            if (config.enableConsoleOutput()) {
                System.out.println(alert.format());
            }

            config.emailRecipients().ifPresent(recipients -> 
                sendAlertEmail(alert, recipients)
            );

            updateLastAlertTime(result.testName());
        }
    }

    private boolean shouldProcessAlert(PerformanceBaseline.BaselineResult result) {
        if (!result.isRegression()) {
            return false;
        }

        double absDeviation = Math.abs(result.deviation());
        return absDeviation >= config.warningThresholdPercent();
    }

    private void incrementFailureCount(String testName) {
        consecutiveFailures.merge(testName, 1, Integer::sum);
    }

    private void resetFailureCount(String testName) {
        consecutiveFailures.remove(testName);
    }

    private boolean shouldGenerateAlert(String testName) {
        int failures = consecutiveFailures.getOrDefault(testName, 0);
        
        if (failures < config.consecutiveFailuresForAlert()) {
            return false;
        }

        // Check if enough time has passed since last alert (at least 1 hour)
        LocalDateTime lastAlert = lastAlertTime.get(testName);
        if (lastAlert != null && 
            lastAlert.plusHours(1).isAfter(LocalDateTime.now())) {
            return false;
        }

        return true;
    }

    private Alert generateAlert(PerformanceBaseline.BaselineResult result) {
        AlertLevel level = determineAlertLevel(result.deviation());
        List<String> recommendations = generateRecommendations(result, level);

        return new Alert(
            result.testName(),
            level,
            result.deviation(),
            result.analysis(),
            LocalDateTime.now(),
            String.format("Baseline from %s", 
                result.baselineTimestamp().format(DATE_FORMAT)),
            recommendations
        );
    }

    private AlertLevel determineAlertLevel(double deviation) {
        double absDeviation = Math.abs(deviation);
        if (absDeviation >= config.criticalThresholdPercent()) {
            return AlertLevel.CRITICAL;
        } else if (absDeviation >= config.warningThresholdPercent()) {
            return AlertLevel.WARNING;
        }
        return AlertLevel.INFO;
    }

    private List<String> generateRecommendations(
            PerformanceBaseline.BaselineResult result,
            AlertLevel level) {
        List<String> recommendations = new ArrayList<>();
        
        if (level == AlertLevel.CRITICAL) {
            recommendations.add("1. Immediately review recent code changes");
            recommendations.add("2. Consider rolling back recent deployments");
            recommendations.add("3. Analyze system resources and infrastructure changes");
            recommendations.add("4. Check for external system dependencies issues");
        } else {
            recommendations.add("1. Review recent code changes for potential impacts");
            recommendations.add("2. Monitor system resources for anomalies");
            recommendations.add("3. Analyze performance metrics for patterns");
        }

        // Add specific recommendations based on the performance deviation
        if (result.deviation() > 50.0) {
            recommendations.add("4. Urgent: Performance degradation exceeds 50%");
            recommendations.add("5. Consider immediate system optimization");
        }

        return recommendations;
    }

    private void logAlert(Alert alert) {
        try {
            Files.createDirectories(config.alertLogFile().getParent());
            
            String logEntry = String.format("""
                
                ========================================
                Performance Regression Alert
                ========================================
                %s
                """, alert.format());

            Files.writeString(
                config.alertLogFile(),
                logEntry,
                StandardCharsets.UTF_8,
                StandardOpenOption.CREATE,
                StandardOpenOption.APPEND
            );
        } catch (IOException e) {
            System.err.println("Failed to log alert: " + e.getMessage());
        }
    }

    private void sendAlertEmail(Alert alert, String recipients) {
        // Email sending logic would go here
        // This is a placeholder for actual email implementation
        System.out.printf(
            "Would send email to %s with subject: %s Performance Alert - %s%n",
            recipients,
            alert.level(),
            alert.testName()
        );
    }

    private void updateLastAlertTime(String testName) {
        lastAlertTime.put(testName, LocalDateTime.now());
    }

    public Map<String, Integer> getConsecutiveFailures() {
        return new HashMap<>(consecutiveFailures);
    }

    public Map<String, LocalDateTime> getLastAlertTimes() {
        return new HashMap<>(lastAlertTime);
    }
}
