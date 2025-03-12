package vn.com.fecredit.app.performance.trend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class PerformanceTrendAnalyzer {

    private static final ObjectMapper mapper = new ObjectMapper()
        .registerModule(new JavaTimeModule());

    public record TrendResult(
        String testName,
        double averageExecutionTime,
        double minExecutionTime,
        double maxExecutionTime,
        double successRate,
        boolean isSignificantChange,  // Added this field
        String trend,
        List<Double> executionTimes,
        List<LocalDateTime> timestamps
    ) {}

    public static List<TrendResult> analyzeTrends(Path historyDirectory) throws IOException {
        List<HistoricalRun> historicalRuns = loadHistoricalRuns(historyDirectory);
        return generateTrendAnalysis(historicalRuns);
    }

    private record HistoricalRun(
        LocalDateTime timestamp,
        Map<String, TestRun> testRuns
    ) {}

    private record TestRun(
        String testName,
        boolean passed,
        double executionTime,
        String metrics
    ) {}

    private static List<HistoricalRun> loadHistoricalRuns(Path historyDirectory) throws IOException {
        List<HistoricalRun> runs = new ArrayList<>();
        
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(historyDirectory, "performance-history-*.json")) {
            for (Path file : stream) {
                try {
                    LocalDateTime timestamp = parseTimestampFromFilename(file.getFileName().toString());
                    Map<String, TestRun> testRuns = parseTestRuns(file);
                    runs.add(new HistoricalRun(timestamp, testRuns));
                } catch (Exception e) {
                    System.err.println("Error parsing file: " + file + " - " + e.getMessage());
                }
            }
        }

        return runs.stream()
            .sorted(Comparator.comparing(HistoricalRun::timestamp))
            .collect(Collectors.toList());
    }

    private static LocalDateTime parseTimestampFromFilename(String filename) {
        String timestampStr = filename.replace("performance-history-", "").replace(".json", "");
        return LocalDateTime.parse(timestampStr, 
            DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss"));
    }

    private static Map<String, TestRun> parseTestRuns(Path file) throws IOException {
        String json = Files.readString(file, StandardCharsets.UTF_8);
        List<Map<String, Object>> data = mapper.readValue(json, List.class);
        
        Map<String, TestRun> results = new HashMap<>();
        for (Map<String, Object> test : data) {
            String testName = (String) test.get("testName");
            boolean passed = (Boolean) test.get("passed");
            String executionTimeStr = (String) test.get("executionTime");
            double executionTime = parseExecutionTime(executionTimeStr);
            String metrics = (String) test.get("metrics");

            results.put(testName, new TestRun(testName, passed, executionTime, metrics));
        }
        
        return results;
    }

    private static double parseExecutionTime(String timeStr) {
        try {
            return Double.parseDouble(timeStr.split(" ")[0]);
        } catch (Exception e) {
            return -1.0;
        }
    }

    private static List<TrendResult> generateTrendAnalysis(List<HistoricalRun> historicalRuns) {
        if (historicalRuns.isEmpty()) {
            return Collections.emptyList();
        }

        Set<String> testNames = historicalRuns.stream()
            .flatMap(run -> run.testRuns.keySet().stream())
            .collect(Collectors.toSet());

        return testNames.stream()
            .map(testName -> analyzeTrendForTest(testName, historicalRuns))
            .collect(Collectors.toList());
    }

    private static TrendResult analyzeTrendForTest(String testName, List<HistoricalRun> historicalRuns) {
        List<TestRun> testRuns = historicalRuns.stream()
            .filter(run -> run.testRuns.containsKey(testName))
            .map(run -> run.testRuns.get(testName))
            .collect(Collectors.toList());

        List<Double> executionTimes = testRuns.stream()
            .map(TestRun::executionTime)
            .collect(Collectors.toList());

        List<LocalDateTime> timestamps = historicalRuns.stream()
            .filter(run -> run.testRuns.containsKey(testName))
            .map(HistoricalRun::timestamp)
            .collect(Collectors.toList());

        double successRate = (double) testRuns.stream()
            .filter(TestRun::passed)
            .count() / testRuns.size() * 100;

        DoubleSummaryStatistics stats = executionTimes.stream()
            .mapToDouble(Double::doubleValue)
            .summaryStatistics();

        PerformanceStatistics.StatisticalResult statistics = 
            PerformanceStatistics.analyze(executionTimes);

        String trend = statistics.description();
        boolean isSignificant = statistics.isSignificantChange();

        return new TrendResult(
            testName,
            stats.getAverage(),
            stats.getMin(),
            stats.getMax(),
            successRate,
            isSignificant,
            trend,
            executionTimes,
            timestamps
        );
    }

    public static void generateTrendReport(Path historyDirectory, Path outputPath) throws IOException {
        List<TrendResult> trends = analyzeTrends(historyDirectory);
        
        // Generate text report
        generateTextReport(outputPath, trends);

        // Generate chart report
        Path chartPath = outputPath.resolveSibling(
            outputPath.getFileName().toString().replace(".txt", "-chart.html")
        );
        PerformanceTrendChart.generateTrendCharts(trends, chartPath);
    }

    private static void generateTextReport(Path outputPath, List<TrendResult> trends) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8)) {
            writer.write(String.format("""
                Performance Trend Analysis Report
                ===============================
                Generated: %s
                
                """, LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)));

            for (TrendResult trend : trends) {
                writer.write(String.format("""
                    Test: %s
                    ------------------
                    Average Execution Time: %.2f seconds
                    Min Execution Time: %.2f seconds
                    Max Execution Time: %.2f seconds
                    Success Rate: %.1f%%
                    Significant Change: %s
                    Trend: %s
                    
                    """,
                    trend.testName(),
                    trend.averageExecutionTime(),
                    trend.minExecutionTime(),
                    trend.maxExecutionTime(),
                    trend.successRate(),
                    trend.isSignificantChange() ? "Yes" : "No",
                    trend.trend()
                ));
            }

            writer.write("\nA visual representation of these trends is available in the chart report.");
        }
    }
}
