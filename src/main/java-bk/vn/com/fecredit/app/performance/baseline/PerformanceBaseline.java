package vn.com.fecredit.app.performance.baseline;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import vn.com.fecredit.app.performance.trend.PerformanceStatistics;
import vn.com.fecredit.app.performance.trend.PerformanceStatistics.StatisticalResult;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Core functionality for managing performance baselines and detecting regressions.
 */
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

    private record Baseline(
        String testName,
        List<Double> metrics,
        LocalDateTime timestamp,
        StatisticalResult statistics
    ) {}

    /**
     * Saves performance metrics as a baseline for future comparison.
     */
    public static void saveBaseline(Path baselineFile, String testName, List<Double> metrics) 
            throws IOException {
        if (metrics == null || metrics.isEmpty()) {
            throw new IllegalArgumentException("Metrics cannot be null or empty");
        }

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

    /**
     * Compares current metrics against stored baseline.
     */
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

        double deviation = calculateDeviation(
            currentStats.mean(), 
            baselineStats.mean()
        );

        boolean isRegression = isPerformanceRegression(
            currentStats, 
            baselineStats,
            deviation
        );

        String analysis = generateAnalysis(
            baselineStats,
            currentStats,
            deviation
        );

        return new BaselineResult(
            testName,
            baselineStats.mean(),
            baselineStats.p95(),
            currentStats.mean(),
            currentStats.p95(),
            deviation,
            isRegression,
            analysis,
            baseline.timestamp(),
            LocalDateTime.now()
        );
    }

    private static Map<String, Baseline> loadBaselines(Path file) throws IOException {
        String json = Files.readString(file, StandardCharsets.UTF_8);
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
            StatisticalResult current,
            StatisticalResult baseline,
            double deviation) {
        
        return current.isSignificantChange() && 
               deviation > 10.0 &&         // 10% degradation threshold
               current.coefficientOfVariation() > baseline.coefficientOfVariation();
    }

    private static String generateAnalysis(
            StatisticalResult baseline,
            StatisticalResult current,
            double deviation) {
        
        StringBuilder analysis = new StringBuilder();

        // Analyze mean change
        analysis.append(String.format(
            "Mean execution time %s by %.1f%% (%.2f → %.2f ms). ",
            deviation > 0 ? "increased" : "decreased",
            Math.abs(deviation),
            baseline.mean(),
            current.mean()
        ));

        // Analyze variability
        double variabilityChange = (current.coefficientOfVariation() - 
                                  baseline.coefficientOfVariation()) * 100.0;
        if (Math.abs(variabilityChange) > 10) {
            analysis.append(String.format(
                "Performance variability has %s by %.1f%%. ",
                variabilityChange > 0 ? "increased" : "decreased",
                Math.abs(variabilityChange)
            ));
        }

        // Add statistical significance
        if (current.isSignificantChange()) {
            analysis.append("This change is statistically significant.");
        }

        return analysis.toString().trim();
    }
}
