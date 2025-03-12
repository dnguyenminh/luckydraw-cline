package vn.com.fecredit.app.performance;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import vn.com.fecredit.app.performance.config.PerformanceConfigOverride;
import vn.com.fecredit.app.performance.config.PerformanceConfigValidator;
import vn.com.fecredit.app.performance.config.PerformanceTestConfig;
import vn.com.fecredit.app.performance.model.PerformanceTestResult;
import vn.com.fecredit.app.performance.trend.PerformanceTrendAnalyzer;
import vn.com.fecredit.app.performance.trend.PerformanceTrendAnalyzer.TrendResult;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.*;

@SpringBootTest
@ActiveProfiles("test")
public class SecurityPerformanceReport {

    @Autowired private TokenOperationsConcurrencyTest concurrencyTest;
    @Autowired private TokenOperationsThreadStateTest threadStateTest;
    @Autowired private TokenOperationsMemoryTest memoryTest;
    @Autowired private TokenOperationsCpuTest cpuTest;
    @Autowired private PerformanceConfigValidator configValidator;
    @Autowired private PerformanceTestConfig config;

    @FunctionalInterface
    private interface TestExecutor {
        void execute() throws Exception;
    }

    @Test
    void generateCompletePerformanceReport() throws Exception {
        runPerformanceTest(null);
    }

    @Test
    void generateHighLoadPerformanceReport() throws Exception {
        var override = PerformanceConfigOverride.create()
            .withConcurrencyLevel(Runtime.getRuntime().availableProcessors() * 2)
            .withOperationCount(5000)
            .withMemoryTracking(true)
            .withCpuProfiling(true)
            .withGcMonitoring(true);
        
        runPerformanceTest(override);
    }

    @Test
    void generateLongRunningPerformanceReport() throws Exception {
        var override = PerformanceConfigOverride.create()
            .withTimeout(30)
            .withOperationCount(10000)
            .withSampleInterval(500)
            .withMemoryTracking(true)
            .withGcMonitoring(true);
        
        runPerformanceTest(override);
    }

    private void runPerformanceTest(PerformanceConfigOverride override) throws Exception {
        // Apply configuration overrides if provided
        if (override != null) {
            override.applyTo(config);
        }
        
        // Validate configuration before running tests
        configValidator.validateAndApply(config);
        
        // Create report directories
        Path reportDir = Paths.get(config.getMonitoring().getOutputDirectory());
        Path historyDir = reportDir.resolve("history");
        Files.createDirectories(historyDir);

        String timestamp = LocalDateTime.now().format(
            DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm-ss")
        );
        
        String txtReportPath = reportDir.resolve("performance-report-" + timestamp + ".txt").toString();
        String htmlReportPath = reportDir.resolve("performance-report-" + timestamp + ".html").toString();
        String historyPath = historyDir.resolve("performance-history-" + timestamp + ".json").toString();
        String trendReportPath = reportDir.resolve("trend-report-" + timestamp + ".txt").toString();

        // Run performance tests with timeout from config
        PerformanceTestResult[] results = runPerformanceTests(
            Duration.ofMinutes(config.getTimeoutMinutes())
        );

        // Generate reports
        generateTextReport(txtReportPath, results);
        SecurityPerformanceHtmlReport.generateHtmlReport(htmlReportPath, results);

        // Generate trend analysis
        List<TrendResult> trends = PerformanceTrendAnalyzer.analyzeTrends(historyDir);
        PerformanceTrendAnalyzer.generateTrendReport(historyDir, Path.of(trendReportPath));

        // Print summary
        printPerformanceSummary(txtReportPath, htmlReportPath, historyPath, trendReportPath, results, trends);
    }

    private void printPerformanceSummary(
            String txtReportPath, 
            String htmlReportPath, 
            String historyPath,
            String trendReportPath,
            PerformanceTestResult[] results,
            List<TrendResult> trends) {
        
        System.out.printf("""
            
            Performance Reports Generated:
            --------------------------
            Text Report: %s
            HTML Report: %s
            History JSON: %s
            Trend Report: %s
            
            Test Results:
            ------------
            Total Tests: %d
            Passed: %d
            Failed: %d
            
            Configuration:
            -------------
            Concurrency Level: %d
            Operation Count: %d
            Timeout: %d minutes
            Sample Interval: %d ms
            Memory Tracking: %s
            CPU Profiling: %s
            GC Monitoring: %s
            
            Performance Trends:
            -----------------
            %s
            
            Performance monitoring data has been saved for trend analysis.
            Review the HTML report for detailed metrics and visualizations.
            """,
            txtReportPath,
            htmlReportPath,
            historyPath,
            trendReportPath,
            results.length,
            countPassed(results),
            results.length - countPassed(results),
            config.getConcurrencyLevel(),
            config.getOperationCount(),
            config.getTimeoutMinutes(),
            config.getMonitoring().getSampleIntervalMs(),
            config.getMonitoring().isEnableMemoryTracking(),
            config.getMonitoring().isEnableCpuProfiling(),
            config.getMonitoring().isEnableGcMonitoring(),
            formatTrendSummary(trends)
        );
    }

    private String formatTrendSummary(List<TrendResult> trends) {
        if (trends.isEmpty()) {
            return "No historical data available for trend analysis";
        }

        StringBuilder summary = new StringBuilder();
        for (TrendResult trend : trends) {
            summary.append(String.format("""
                %s:
                  Average Time: %.2f seconds
                  Success Rate: %.1f%%
                  Trend: %s
                """,
                trend.testName(),
                trend.averageExecutionTime(),
                trend.successRate(),
                trend.trend()
            ));
        }
        return summary.toString();
    }

    private PerformanceTestResult[] runPerformanceTests(Duration timeout) throws Exception {
        var concurrencyFuture = runTest("Concurrency Test", concurrencyTest::shouldHandleConcurrentOperations);
        var threadStateFuture = runTest("Thread State Test", threadStateTest::shouldMonitorThreadStates);
        var memoryFuture = runTest("Memory Usage Test", memoryTest::shouldMonitorMemoryUsage);
        var cpuFuture = runTest("CPU Usage Test", cpuTest::shouldMonitorCpuUsage);

        return CompletableFuture.allOf(
            concurrencyFuture,
            threadStateFuture,
            memoryFuture,
            cpuFuture
        )
        .thenApply(v -> new PerformanceTestResult[]{
            concurrencyFuture.join(),
            threadStateFuture.join(),
            memoryFuture.join(),
            cpuFuture.join()
        })
        .get(timeout.toMinutes(), TimeUnit.MINUTES);
    }

    private CompletableFuture<PerformanceTestResult> runTest(String testName, TestExecutor test) {
        return CompletableFuture.supplyAsync(() -> {
            long startTime = System.nanoTime();
            ByteArrayOutputStream outputCapture = new ByteArrayOutputStream();
            PrintStream originalOut = System.out;

            try (PrintStream captureStream = new PrintStream(outputCapture, true, StandardCharsets.UTF_8)) {
                System.setOut(captureStream);
                test.execute();
                
                return new PerformanceTestResult(
                    testName,
                    true,
                    String.format("%.2f seconds", (System.nanoTime() - startTime) / 1_000_000_000.0),
                    outputCapture.toString(StandardCharsets.UTF_8),
                    null
                );
            } catch (Throwable e) {
                return new PerformanceTestResult(
                    testName,
                    false,
                    String.format("%.2f seconds", (System.nanoTime() - startTime) / 1_000_000_000.0),
                    outputCapture.toString(StandardCharsets.UTF_8),
                    getStackTraceAsString(e)
                );
            } finally {
                System.setOut(originalOut);
            }
        });
    }

    private void generateTextReport(String path, PerformanceTestResult[] results) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(path), StandardCharsets.UTF_8))) {
            writer.write(String.format("""
                Security Performance Test Report
                ==============================
                Generated: %s
                
                Summary:
                --------
                Total Tests: %d
                Passed: %d
                Failed: %d
                
                Configuration:
                -------------
                Concurrency Level: %d
                Operation Count: %d
                Timeout: %d minutes
                Sample Interval: %d ms
                Memory Tracking: %s
                CPU Profiling: %s
                GC Monitoring: %s
                
                Detailed Results:
                ----------------
                
                """,
                LocalDateTime.now(),
                results.length,
                countPassed(results),
                results.length - countPassed(results),
                config.getConcurrencyLevel(),
                config.getOperationCount(),
                config.getTimeoutMinutes(),
                config.getMonitoring().getSampleIntervalMs(),
                config.getMonitoring().isEnableMemoryTracking(),
                config.getMonitoring().isEnableCpuProfiling(),
                config.getMonitoring().isEnableGcMonitoring()
            ));

            for (PerformanceTestResult result : results) {
                writer.write(String.format("""
                    Test: %s
                    Status: %s
                    Execution Time: %s
                    
                    Metrics:
                    %s
                    
                    %s
                    
                    ----------------------------------------
                    
                    """,
                    result.testName(),
                    result.passed() ? "PASSED" : "FAILED",
                    result.executionTime(),
                    result.metrics(),
                    result.errorMessage() != null ? 
                        "Error: " + result.errorMessage() : 
                        "No errors"
                ));
            }

            writer.write(String.format("""
                
                System Information:
                -----------------
                Java Version: %s
                Available Processors: %d
                Max Memory: %.2f GB
                OS: %s
                OS Architecture: %s
                Encoding: %s
                """,
                System.getProperty("java.version"),
                Runtime.getRuntime().availableProcessors(),
                Runtime.getRuntime().maxMemory() / (1024.0 * 1024.0 * 1024.0),
                System.getProperty("os.name"),
                System.getProperty("os.arch"),
                StandardCharsets.UTF_8
            ));
        }
    }

    private String getStackTraceAsString(Throwable throwable) {
        try (StringWriter sw = new StringWriter();
             PrintWriter pw = new PrintWriter(sw)) {
            throwable.printStackTrace(pw);
            return sw.toString();
        } catch (IOException e) {
            return String.format("Failed to capture stack trace: %s", throwable.getMessage());
        }
    }

    private int countPassed(PerformanceTestResult[] results) {
        return (int) java.util.Arrays.stream(results)
            .filter(PerformanceTestResult::passed)
            .count();
    }
}
