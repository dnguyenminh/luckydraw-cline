package vn.com.fecredit.app.performance;

import vn.com.fecredit.app.performance.model.PerformanceTestResult;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

public class SecurityPerformanceHtmlReport {
    
    private static final String CSS = """
        <style>
            body { font-family: Arial, sans-serif; margin: 40px; }
            .header { background: #f8f9fa; padding: 20px; border-radius: 5px; }
            .summary { margin: 20px 0; }
            .test-result { border: 1px solid #dee2e6; padding: 15px; margin: 10px 0; border-radius: 5px; }
            .test-result.passed { border-left: 5px solid #28a745; }
            .test-result.failed { border-left: 5px solid #dc3545; }
            .metrics { background: #f8f9fa; padding: 15px; margin: 10px 0; font-family: monospace; white-space: pre; }
            .recommendations { background: #e9ecef; padding: 20px; margin: 20px 0; border-radius: 5px; }
            .system-info { background: #f8f9fa; padding: 20px; margin: 20px 0; border-radius: 5px; }
            .chart { margin: 20px 0; height: 300px; }
            table { width: 100%; border-collapse: collapse; margin: 10px 0; }
            th, td { padding: 8px; text-align: left; border-bottom: 1px solid #dee2e6; }
            th { background-color: #f8f9fa; }
            .error { color: #dc3545; margin-top: 10px; }
        </style>
    """;

    private static final String CHART_JS = """
        <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
        <script>
            function createChart(ctx, labels, data, title) {
                new Chart(ctx, {
                    type: 'bar',
                    data: {
                        labels: labels,
                        datasets: [{
                            label: title,
                            data: data,
                            backgroundColor: 'rgba(54, 162, 235, 0.2)',
                            borderColor: 'rgba(54, 162, 235, 1)',
                            borderWidth: 1
                        }]
                    },
                    options: {
                        responsive: true,
                        maintainAspectRatio: false,
                        scales: {
                            y: {
                                beginAtZero: true
                            }
                        }
                    }
                });
            }
        </script>
    """;

    public static void generateHtmlReport(String path, PerformanceTestResult[] results) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(path), StandardCharsets.UTF_8))) {
            
            writer.write("""
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                    <title>Security Performance Test Report</title>
                    %s
                    %s
                </head>
                <body>
                """.formatted(CSS, CHART_JS));

            // Header
            writer.write("""
                <div class="header">
                    <h1>Security Performance Test Report</h1>
                    <p>Generated: %s</p>
                </div>
                """.formatted(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)));

            // Summary
            int passed = (int) Arrays.stream(results).filter(PerformanceTestResult::passed).count();
            writer.write("""
                <div class="summary">
                    <h2>Summary</h2>
                    <table>
                        <tr><th>Total Tests</th><td>%d</td></tr>
                        <tr><th>Passed</th><td>%d</td></tr>
                        <tr><th>Failed</th><td>%d</td></tr>
                    </table>
                </div>
                """.formatted(results.length, passed, results.length - passed));

            // Test Results Chart
            writer.write("""
                <div class="chart">
                    <canvas id="resultsChart"></canvas>
                </div>
                <script>
                    createChart(
                        document.getElementById('resultsChart'),
                        ['Passed', 'Failed'],
                        [%d, %d],
                        'Test Results'
                    );
                </script>
                """.formatted(passed, results.length - passed));

            // Detailed Results
            writer.write("<h2>Detailed Results</h2>");
            for (PerformanceTestResult result : results) {
                writer.write("""
                    <div class="test-result %s">
                        <h3>%s</h3>
                        <table>
                            <tr><th>Status</th><td>%s</td></tr>
                            <tr><th>Execution Time</th><td>%s</td></tr>
                        </table>
                        <div class="metrics">%s</div>
                        %s
                    </div>
                    """.formatted(
                        result.passed() ? "passed" : "failed",
                        result.testName(),
                        result.passed() ? "PASSED" : "FAILED",
                        result.executionTime(),
                        escapeHtml(result.metrics()),
                        result.errorMessage() != null ? 
                            "<div class='error'><pre>" + escapeHtml(result.errorMessage()) + "</pre></div>" : 
                            ""
                    ));
            }

            // System Information
            writer.write("""
                <div class="system-info">
                    <h2>System Information</h2>
                    <table>
                        <tr><th>Java Version</th><td>%s</td></tr>
                        <tr><th>Available Processors</th><td>%d</td></tr>
                        <tr><th>Max Memory</th><td>%.2f GB</td></tr>
                        <tr><th>OS</th><td>%s</td></tr>
                        <tr><th>OS Architecture</th><td>%s</td></tr>
                        <tr><th>Encoding</th><td>%s</td></tr>
                    </table>
                </div>
                """.formatted(
                    System.getProperty("java.version"),
                    Runtime.getRuntime().availableProcessors(),
                    Runtime.getRuntime().maxMemory() / (1024.0 * 1024.0 * 1024.0),
                    System.getProperty("os.name"),
                    System.getProperty("os.arch"),
                    StandardCharsets.UTF_8
                ));

            // Recommendations
            writer.write("""
                <div class="recommendations">
                    <h2>Performance Recommendations</h2>
                    <ul>
                        <li>Monitor thread contention in high-load scenarios</li>
                        <li>Consider increasing thread pool size if CPU utilization is low</li>
                        <li>Monitor memory allocation patterns for potential leaks</li>
                        <li>Review GC behavior under sustained load</li>
                        <li>Consider implementing rate limiting for production</li>
                    </ul>
                </div>
                """);

            writer.write("</body></html>");
        }
    }

    private static String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                  .replace("<", "&lt;")
                  .replace(">", "&gt;")
                  .replace("\"", "&quot;")
                  .replace("'", "&#39;");
    }
}
