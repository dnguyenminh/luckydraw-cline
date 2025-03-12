package vn.com.fecredit.app.performance.trend;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import vn.com.fecredit.app.performance.model.PerformanceTestResult;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PerformanceTrendIntegrationTest {

    @TempDir
    Path tempDir;
    
    private Path historyDir;
    private Path outputDir;
    private static final DateTimeFormatter DATE_FORMAT = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss");

    @BeforeEach
    void setUp() throws IOException {
        historyDir = tempDir.resolve("history");
        outputDir = tempDir.resolve("output");
        Files.createDirectories(historyDir);
        Files.createDirectories(outputDir);
    }

    @Test
    void shouldGenerateCompleteTrendAnalysis() throws Exception {
        // Generate historical performance data
        generateHistoricalData();

        // Analyze trends
        List<PerformanceTrendAnalyzer.TrendResult> trends = 
            PerformanceTrendAnalyzer.analyzeTrends(historyDir);

        // Generate reports
        Path textReport = outputDir.resolve("trend-report.txt");
        Path chartReport = outputDir.resolve("trend-chart.html");
        
        PerformanceTrendAnalyzer.generateTrendReport(historyDir, textReport);
        PerformanceTrendChart.generateTrendCharts(trends, chartReport);

        // Verify results
        assertReportGeneration(textReport, chartReport);
        verifyTrendAnalysis(trends);
    }

    @Test
    void shouldIntegrateWithStatisticalAnalysis() throws Exception {
        // Generate data with known statistical properties
        generateStatisticalTestData();

        List<PerformanceTrendAnalyzer.TrendResult> trends = 
            PerformanceTrendAnalyzer.analyzeTrends(historyDir);

        for (var trend : trends) {
            var stats = PerformanceStatistics.analyze(trend.executionTimes());
            
            // Verify statistical analysis integration
            assertEquals(trend.averageExecutionTime(), stats.mean(), 0.001);
            assertEquals(
                trend.isSignificantChange(),
                stats.isSignificantChange()
            );
        }
    }

    @Test
    void shouldHandlePerformanceDegradation() throws Exception {
        // Generate data showing clear performance degradation
        generateDegradationData();

        List<PerformanceTrendAnalyzer.TrendResult> trends = 
            PerformanceTrendAnalyzer.analyzeTrends(historyDir);

        var textReport = outputDir.resolve("degradation-report.txt");
        var chartReport = outputDir.resolve("degradation-chart.html");

        PerformanceTrendAnalyzer.generateTrendReport(historyDir, textReport);
        PerformanceTrendChart.generateTrendCharts(trends, chartReport);

        // Verify degradation detection
        assertTrue(trends.stream().anyMatch(t -> 
            t.trend().contains("degrading") && t.isSignificantChange()
        ));
    }

    @Test
    void shouldTrackMultipleMetrics() throws Exception {
        // Generate data for multiple performance metrics
        generateMultiMetricData();

        List<PerformanceTrendAnalyzer.TrendResult> trends = 
            PerformanceTrendAnalyzer.analyzeTrends(historyDir);

        // Verify all metrics are tracked
        assertEquals(3, trends.size(), "Should track all metrics");
        assertTrue(trends.stream().anyMatch(t -> t.testName().contains("Response")));
        assertTrue(trends.stream().anyMatch(t -> t.testName().contains("Memory")));
        assertTrue(trends.stream().anyMatch(t -> t.testName().contains("CPU")));
    }

    private void generateHistoricalData() throws IOException {
        // Generate 5 days of historical data
        for (int i = -4; i <= 0; i++) {
            LocalDateTime timestamp = LocalDateTime.now().plusDays(i);
            String json = generateTestResultJson(
                timestamp,
                "Test1", true, 1.0 + i * 0.1,
                "Test2", true, 2.0 - i * 0.1
            );
            writeHistoryFile(timestamp, json);
        }
    }

    private void generateStatisticalTestData() throws IOException {
        // Generate data with known statistical properties
        double[] values = {1.0, 1.2, 1.1, 1.3, 1.2, 1.4, 1.3, 1.5};
        for (int i = 0; i < values.length; i++) {
            LocalDateTime timestamp = LocalDateTime.now().plusDays(i - values.length);
            String json = generateTestResultJson(
                timestamp,
                "StatTest", true, values[i],
                null, false, 0.0
            );
            writeHistoryFile(timestamp, json);
        }
    }

    private void generateDegradationData() throws IOException {
        // Generate clearly degrading performance data
        for (int i = 0; i < 5; i++) {
            LocalDateTime timestamp = LocalDateTime.now().plusDays(i - 5);
            String json = generateTestResultJson(
                timestamp,
                "DegradingTest", true, 1.0 + i * 0.5,
                null, false, 0.0
            );
            writeHistoryFile(timestamp, json);
        }
    }

    private void generateMultiMetricData() throws IOException {
        // Generate data for multiple metrics
        for (int i = 0; i < 5; i++) {
            LocalDateTime timestamp = LocalDateTime.now().plusDays(i - 5);
            String json = String.format("""
                [
                    {"testName": "Response Time", "passed": true, "executionTime": "%.1f seconds"},
                    {"testName": "Memory Usage", "passed": true, "executionTime": "%.1f seconds"},
                    {"testName": "CPU Usage", "passed": true, "executionTime": "%.1f seconds"}
                ]
                """,
                1.0 + i * 0.1,
                50.0 + i * 2.0,
                30.0 + i * 1.5
            );
            writeHistoryFile(timestamp, json);
        }
    }

    private String generateTestResultJson(
            LocalDateTime timestamp,
            String test1Name, boolean test1Passed, double test1Time,
            String test2Name, boolean test2Passed, double test2Time) {
        if (test2Name == null) {
            return String.format("""
                [
                    {
                        "testName": "%s",
                        "passed": %s,
                        "executionTime": "%.1f seconds"
                    }
                ]
                """, test1Name, test1Passed, test1Time);
        }
        
        return String.format("""
            [
                {
                    "testName": "%s",
                    "passed": %s,
                    "executionTime": "%.1f seconds"
                },
                {
                    "testName": "%s",
                    "passed": %s,
                    "executionTime": "%.1f seconds"
                }
            ]
            """, 
            test1Name, test1Passed, test1Time,
            test2Name, test2Passed, test2Time
        );
    }

    private void writeHistoryFile(LocalDateTime timestamp, String content) throws IOException {
        Path filePath = historyDir.resolve(
            "performance-history-" + timestamp.format(DATE_FORMAT) + ".json"
        );
        Files.write(filePath, content.getBytes(StandardCharsets.UTF_8));
    }

    private void assertReportGeneration(Path textReport, Path chartReport) throws IOException {
        assertTrue(Files.exists(textReport), "Text report should exist");
        assertTrue(Files.exists(chartReport), "Chart report should exist");
        
        String textContent = Files.readString(textReport);
        String chartContent = Files.readString(chartReport);

        assertTrue(textContent.contains("Performance Trend Analysis Report"));
        assertTrue(chartContent.contains("<title>Performance Trends</title>"));
        assertTrue(chartContent.contains("chart.js"));
    }

    private void verifyTrendAnalysis(List<PerformanceTrendAnalyzer.TrendResult> trends) {
        assertFalse(trends.isEmpty(), "Should have trend results");
        
        for (var trend : trends) {
            assertNotNull(trend.testName());
            assertNotNull(trend.trend());
            assertFalse(trend.executionTimes().isEmpty());
            assertFalse(trend.timestamps().isEmpty());
            assertTrue(trend.averageExecutionTime() > 0);
            assertTrue(trend.successRate() >= 0 && trend.successRate() <= 100);
        }
    }
}
