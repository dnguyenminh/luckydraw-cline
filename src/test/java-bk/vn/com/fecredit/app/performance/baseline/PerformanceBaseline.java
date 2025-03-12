package vn.com.fecredit.app.performance.baseline;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import vn.com.fecredit.app.performance.trend.PerformanceStatistics;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;

public class PerformanceBaseline {

    private static final ObjectMapper mapper = new ObjectMapper()
        .registerModule(new JavaTimeModule());

    public record BaselineResult(
        String testName,
        double baselineMean,
        double baselineP95,
        double currentMean,
        double currentP95,
        double deviation,
        boolean isRegression,
        String analysis,
        LocalDateTime baselineTimestamp,
        LocalDateTime currentTimestamp
    ) {}

    public record Baseline(
        String testName,
        List<Double> metrics,
        LocalDateTime timestamp,
        PerformanceStatistics.StatisticalResult statistics
    ) {}

    private static final double REGRESSION_THRESHOLD = 0.10; // 10% degradation threshold

    public static void saveBaseline(Path baselineFile, String testName, List<Double> metrics) throws IOException {
        var statistics = PerformanceStatistics.analyze(metrics);
        var baseline = new Baseline(testName, metrics, LocalDateTime.now(), statistics);
        
        Map<String, Baseline> baselines;
        if (Files.exists(baselineFile)) {
            baselines = loadBaselines(baselineFile);
        } else {
            baselines = new HashMap<>();
        }
        
        baselines.put(testName, baseline);
        saveBaselines(baselineFile, baselines);
    }

    public static BaselineResult compareWithBaseline(
            Path baselineFile, 
            String testName, 
            List<Double> currentMetrics) throws IOException {
        
        if (!Files.exists(baselineFile)) {
            throw new IllegalStateException("No baseline data available");
        }

        Map<String, Baseline> baselines = loadBaselines(baselineFile);
        Baseline baseline = baselines.get(testName);
        
        if (baseline == null) {
            throw new IllegalStateException("No baseline data for test: " + testName);
        }

        var currentStats = PerformanceStatistics.analyze(currentMetrics);
        var baselineStats = baseline.statistics();

        double meanDeviation = calculateDeviation(
            currentStats.mean(), 
            baselineStats.mean()
        );

        boolean isRegression = isPerformanceRegression(
            currentStats,
            baselineStats,
            meanDeviation
        );

        String analysis = generateAnalysis(
            baselineStats,
            currentStats,
            meanDeviation,
            isRegression
        );

        return new BaselineResult(
            testName,
            baselineStats.mean(),
            baselineStats.p95(),
            currentStats.mean(),
            currentStats.p95(),
            meanDeviation,
            isRegression,
            analysis,
            baseline.timestamp(),
            LocalDateTime.now()
        );
    }

    public static void generateBaselineReport(Path baselineFile, Path reportFile) throws IOException {
        if (!Files.exists(baselineFile)) {
            throw new IllegalStateException("No baseline data available");
        }

        Map<String, Baseline> baselines = loadBaselines(baselineFile);
        
        try (BufferedWriter writer = Files.newBufferedWriter(reportFile, StandardCharsets.UTF_8)) {
            writer.write(String.format("""
                Performance Baseline Report
                =========================
                Generated: %s
                
                """, LocalDateTime.now()));

            for (var entry : baselines.entrySet()) {
                Baseline baseline = entry.getValue();
                var stats = baseline.statistics();
                
                writer.write(String.format("""
                    Test: %s
                    ------------------
                    Baseline Date: %s
                    Mean Execution Time: %.2f seconds
                    P95 Execution Time: %.2f seconds
                    Standard Deviation: %.2f seconds
                    Coefficient of Variation: %.2f
                    Sample Size: %d
                    
                    """,
                    baseline.testName(),
                    baseline.timestamp(),
                    stats.mean(),
                    stats.p95(),
                    stats.standardDeviation(),
                    stats.coefficientOfVariation(),
                    baseline.metrics().size()
                ));
            }
        }
    }

    private static Map<String, Baseline> loadBaselines(Path file) throws IOException {
        String json = Files.readString(file);
        return mapper.readValue(json, mapper.getTypeFactory()
            .constructMapType(Map.class, String.class, Baseline.class));
    }

    private static void saveBaselines(Path file, Map<String, Baseline> baselines) throws IOException {
        String json = mapper.writeValueAsString(baselines);
        Files.writeString(file, json, StandardCharsets.UTF_8);
    }

    private static double calculateDeviation(double current, double baseline) {
        return ((current - baseline) / baseline) * 100.0;
    }

    private static boolean isPerformanceRegression(
            PerformanceStatistics.StatisticalResult current,
            PerformanceStatistics.StatisticalResult baseline,
            double deviation) {
        
        // Check if the deviation exceeds the regression threshold
        if (Math.abs(deviation) > (REGRESSION_THRESHOLD * 100)) {
            return deviation > 0; // Positive deviation means slower performance
        }

        // Check if the change is statistically significant
        return current.isSignificantChange() && 
               current.mean() > baseline.mean() &&
               current.standardDeviation() > baseline.standardDeviation();
    }

    private static String generateAnalysis(
            PerformanceStatistics.StatisticalResult baseline,
            PerformanceStatistics.StatisticalResult current,
            double deviation,
            boolean isRegression) {
        
        List<String> observations = new ArrayList<>();

        if (isRegression) {
            observations.add(String.format(
                "PERFORMANCE REGRESSION DETECTED: %.1f%% slower than baseline",
                deviation
            ));
        } else if (deviation < 0) {
            observations.add(String.format(
                "Performance improved: %.1f%% faster than baseline",
                -deviation
            ));
        } else {
            observations.add(String.format(
                "Performance stable: %.1f%% deviation from baseline",
                deviation
            ));
        }

        // Analyze variability changes
        double variabilityChange = (current.coefficientOfVariation() - 
                                  baseline.coefficientOfVariation()) * 100.0;
        if (Math.abs(variabilityChange) > 10) {
            String direction = variabilityChange > 0 ? "increased" : "decreased";
            observations.add(String.format(
                "Performance variability has %s by %.1f%%",
                direction, Math.abs(variabilityChange)
            ));
        }

        // Analyze percentile changes
        double p95Change = ((current.p95() - baseline.p95()) / baseline.p95()) * 100.0;
        if (Math.abs(p95Change) > REGRESSION_THRESHOLD * 100) {
            observations.add(String.format(
                "95th percentile response time has %s by %.1f%%",
                p95Change > 0 ? "increased" : "decreased",
                Math.abs(p95Change)
            ));
        }

        return String.join("\n", observations);
    }
}
