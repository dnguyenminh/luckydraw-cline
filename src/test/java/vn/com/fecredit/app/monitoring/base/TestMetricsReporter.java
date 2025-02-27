package vn.com.fecredit.app.monitoring.base;

import java.io.*;
import java.nio.file.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Reporter for test metrics that provides visualization and export capabilities
 */
public class TestMetricsReporter {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
    private final TestMetricsCollector collector;
    private final Path reportDir;

    public TestMetricsReporter(Path reportDir) {
        this.collector = TestMetricsCollector.getInstance();
        this.reportDir = reportDir;
        createReportDirectory();
    }

    /**
     * Generate HTML report for all test suites
     */
    public void generateHtmlReport() throws IOException {
        StringBuilder html = new StringBuilder();
        html.append("<html><head><title>Test Metrics Report</title>")
            .append("<style>")
            .append("body { font-family: Arial; margin: 20px; }")
            .append("table { border-collapse: collapse; width: 100%; }")
            .append("th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }")
            .append("th { background-color: #f2f2f2; }")
            .append(".success { color: green; }")
            .append(".failure { color: red; }")
            .append(".chart { width: 600px; height: 400px; margin: 20px 0; }")
            .append("</style>")
            .append("<script src=\"https://cdn.plot.ly/plotly-latest.min.js\"></script>")
            .append("</head><body>");

        // Add summary section
        addSummarySection(html);

        // Add detailed results
        addDetailedResults(html);

        // Add charts
        addCharts(html);

        html.append("</body></html>");

        // Write HTML file
        Path reportFile = reportDir.resolve("test-report.html");
        Files.writeString(reportFile, html.toString());
    }

    /**
     * Export metrics to JSON
     */
    public void exportJson() throws IOException {
        Map<String, Object> json = new LinkedHashMap<>();
        Map<String, TestMetricsCollector.TestMetricsReport> reports = collector.getAllReports();

        // Add summary data
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("totalSuites", reports.size());
        summary.put("totalTests", reports.values().stream()
            .mapToInt(r -> r.counts.get("total"))
            .sum());
        json.put("summary", summary);

        // Add detailed reports
        Map<String, Object> details = new LinkedHashMap<>();
        reports.forEach((suite, report) -> {
            Map<String, Object> suiteData = new LinkedHashMap<>();
            suiteData.put("metrics", report.metrics);
            suiteData.put("counts", report.counts);
            details.put(suite, suiteData);
        });
        json.put("details", details);

        // Write JSON file using Jackson or Gson
        String jsonString = convertToJsonString(json);
        Path jsonFile = reportDir.resolve("metrics.json");
        Files.writeString(jsonFile, jsonString);
    }

    /**
     * Generate CSV report
     */
    public void exportCsv() throws IOException {
        List<String[]> rows = new ArrayList<>();
        
        // Header
        rows.add(new String[]{"Suite", "Total Tests", "Passed", "Failed", "Success Rate", "Avg Duration (ms)"});

        // Data rows
        collector.getAllReports().forEach((suite, report) -> {
            rows.add(new String[]{
                escapeCSV(suite),
                String.valueOf(report.counts.get("total")),
                String.valueOf(report.counts.get("passed")),
                String.valueOf(report.counts.get("failed")),
                String.format("%.2f%%", report.metrics.get("successRate") * 100),
                String.format("%.2f", report.metrics.get("averageDurationMs"))
            });
        });

        // Write CSV file
        Path csvFile = reportDir.resolve("metrics.csv");
        try (BufferedWriter writer = Files.newBufferedWriter(csvFile)) {
            for (String[] row : rows) {
                writer.write(String.join(",", row));
                writer.newLine();
            }
        }
    }

    private void createReportDirectory() {
        try {
            Files.createDirectories(reportDir);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create report directory", e);
        }
    }

    private void addSummarySection(StringBuilder html) {
        Map<String, TestMetricsCollector.TestMetricsReport> reports = collector.getAllReports();
        int totalTests = reports.values().stream()
            .mapToInt(r -> r.counts.get("total"))
            .sum();
        int totalPassed = reports.values().stream()
            .mapToInt(r -> r.counts.get("passed"))
            .sum();

        html.append("<h2>Test Execution Summary</h2>")
            .append("<table>")
            .append("<tr><th>Metric</th><th>Value</th></tr>")
            .append(String.format("<tr><td>Total Test Suites</td><td>%d</td></tr>", reports.size()))
            .append(String.format("<tr><td>Total Tests</td><td>%d</td></tr>", totalTests))
            .append(String.format("<tr><td>Overall Success Rate</td><td>%.2f%%</td></tr>",
                totalTests > 0 ? (totalPassed * 100.0 / totalTests) : 0));
    }

    private void addDetailedResults(StringBuilder html) {
        html.append("<h2>Test Suite Details</h2><table>")
            .append("<tr><th>Suite</th><th>Tests</th><th>Passed</th><th>Failed</th><th>Success Rate</th><th>Avg Duration</th></tr>");

        collector.getAllReports().forEach((suite, report) -> {
            html.append("<tr>")
                .append("<td>").append(escapeHtml(suite)).append("</td>")
                .append("<td>").append(report.counts.get("total")).append("</td>")
                .append("<td class=\"success\">").append(report.counts.get("passed")).append("</td>")
                .append("<td class=\"failure\">").append(report.counts.get("failed")).append("</td>")
                .append(String.format("<td>%.2f%%</td>", report.metrics.get("successRate") * 100))
                .append(String.format("<td>%.2f ms</td>", report.metrics.get("averageDurationMs")))
                .append("</tr>");
        });

        html.append("</table>");
    }

    private void addCharts(StringBuilder html) {
        // Success rate chart
        html.append("<div id=\"successRateChart\" class=\"chart\"></div>")
            .append("<script>")
            .append("var data = [{")
            .append("values: [")
            .append(collector.getAllReports().values().stream()
                .map(r -> String.valueOf(r.counts.get("passed")))
                .collect(Collectors.joining(",")))
            .append("],")
            .append("labels: [")
            .append(collector.getAllReports().keySet().stream()
                .map(s -> "'" + escapeJavaScript(s) + "'")
                .collect(Collectors.joining(",")))
            .append("],")
            .append("type: 'pie'")
            .append("}];")
            .append("var layout = {title: 'Test Success Distribution by Suite'};")
            .append("Plotly.newPlot('successRateChart', data, layout);")
            .append("</script>");
    }

    private String convertToJsonString(Map<String, Object> map) {
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        map.forEach((key, value) -> {
            json.append("  \"").append(escapeJson(key)).append("\": ");
            appendJsonValue(json, value);
            json.append(",\n");
        });
        if (json.charAt(json.length() - 2) == ',') {
            json.setLength(json.length() - 2);
            json.append('\n');
        }
        json.append("}");
        return json.toString();
    }

    private void appendJsonValue(StringBuilder json, Object value) {
        if (value == null) {
            json.append("null");
        } else if (value instanceof Number) {
            json.append(value);
        } else if (value instanceof Boolean) {
            json.append(value);
        } else if (value instanceof Map) {
            json.append(convertToJsonString((Map<String, Object>) value));
        } else {
            json.append("\"").append(escapeJson(value.toString())).append("\"");
        }
    }

    private String escapeHtml(String s) {
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private String escapeJavaScript(String s) {
        return s.replace("\\", "\\\\")
                .replace("'", "\\'")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

    private String escapeJson(String s) {
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

    private String escapeCSV(String s) {
        if (s.contains(",") || s.contains("\"") || s.contains("\n")) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }
}
