package vn.com.fecredit.app.monitoring;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * Reporter for generating performance test reports with detailed statistics and visualizations
 */
public class PerformanceReporter implements MetricsReporter {
    private static final String LATENCY_PREFIX = "latency.";
    private static final String THROUGHPUT_PREFIX = "throughput.";
    private static final String MEMORY_PREFIX = "memory.";
    
    private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final int PERCENTILE_95 = 95;
    private static final int PERCENTILE_99 = 99;

    private final Map<String, List<Long>> metrics = new ConcurrentHashMap<>();
    private String reportDirectory;
    private final AtomicBoolean enabled = new AtomicBoolean(true);
    private final LocalDateTime startTime = LocalDateTime.now();

    public void recordLatency(String operation, double latency) {
        report(LATENCY_PREFIX + operation, (long) latency);
    }

    public void recordThroughput(String operation, double throughput) {
        report(THROUGHPUT_PREFIX + operation, (long) throughput);
    }

    public void recordMemoryUsage(String operation, long bytes) {
        report(MEMORY_PREFIX + operation, bytes);
    }

    @Override
    public void report(String metric, long value) {
        if (!enabled.get()) {
            return;
        }
        metrics.computeIfAbsent(metric, k -> Collections.synchronizedList(new ArrayList<>())).add(value);
    }

    @Override
    public void setReportDirectory(String directory) {
        this.reportDirectory = directory;
        File dir = new File(directory);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    @Override
    public void generateReport() {
        if (!enabled.get() || reportDirectory == null) {
            return;
        }

        try {
            generateTextReport();
            generateHtmlReport();
            generateJsonReport();
            generateCsvReport();
        } catch (IOException e) {
            System.err.println("Error generating reports: " + e.getMessage());
        }
    }

    private void generateTextReport() throws IOException {
        Path reportPath = Paths.get(reportDirectory, "performance-report.txt");
        StringBuilder report = new StringBuilder();
        report.append("Performance Test Report\n");
        report.append("Generated: ").append(LocalDateTime.now().format(TIMESTAMP_FORMAT)).append("\n");
        report.append("Test Duration: ").append(getDurationInMinutes()).append(" minutes\n\n");

        appendMetricCategory(report, "Latency Statistics", LATENCY_PREFIX);
        appendMetricCategory(report, "Throughput Results", THROUGHPUT_PREFIX);
        appendMetricCategory(report, "Memory Usage", MEMORY_PREFIX);

        Files.writeString(reportPath, report.toString());
    }

    private void appendMetricCategory(StringBuilder report, String title, String prefix) {
        report.append(title).append(":\n");
        metrics.entrySet().stream()
            .filter(e -> e.getKey().startsWith(prefix))
            .forEach(e -> appendDetailedStats(report, e.getKey(), e.getValue()));
        report.append("\n");
    }

    private void appendDetailedStats(StringBuilder report, String metric, List<Long> values) {
        if (values.isEmpty()) {
            return;
        }

        String operation = metric.substring(metric.indexOf('.') + 1);
        report.append(String.format("  %s:\n", operation));
        report.append(String.format("    Count: %d\n", values.size()));
        report.append(String.format("    Average: %.2f\n", getAverage(values)));
        report.append(String.format("    Min: %d\n", getMin(values)));
        report.append(String.format("    Max: %d\n", getMax(values)));
        report.append(String.format("    95th Percentile: %.2f\n", getPercentile(values, PERCENTILE_95)));
        report.append(String.format("    99th Percentile: %.2f\n", getPercentile(values, PERCENTILE_99)));
        report.append(String.format("    Standard Deviation: %.2f\n", getStandardDeviation(values)));
    }

    private void generateHtmlReport() throws IOException {
        Path reportPath = Paths.get(reportDirectory, "performance-report.html");
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n<html><head>\n");
        html.append("<title>Performance Test Report</title>\n");
        html.append("<script src='https://cdn.plot.ly/plotly-latest.min.js'></script>\n");
        html.append("<style>\n");
        html.append("  body { font-family: Arial, sans-serif; margin: 20px; }\n");
        html.append("  .chart { height: 400px; margin: 20px 0; }\n");
        html.append("  .stats { margin: 20px 0; }\n");
        html.append("</style>\n");
        html.append("</head><body>\n");
        
        // Header
        html.append("<h1>Performance Test Report</h1>\n");
        html.append("<p>Generated: ").append(LocalDateTime.now().format(TIMESTAMP_FORMAT)).append("</p>\n");
        html.append("<p>Test Duration: ").append(getDurationInMinutes()).append(" minutes</p>\n");

        // Charts
        html.append("<div id='latencyChart' class='chart'></div>\n");
        html.append("<div id='throughputChart' class='chart'></div>\n");
        html.append("<div id='memoryChart' class='chart'></div>\n");

        // Summary Tables
        appendHtmlSummaryTable(html, "Latency Statistics", LATENCY_PREFIX);
        appendHtmlSummaryTable(html, "Throughput Results", THROUGHPUT_PREFIX);
        appendHtmlSummaryTable(html, "Memory Usage", MEMORY_PREFIX);

        // Chart Scripts
        html.append("<script>\n");
        generateChartScripts(html);
        html.append("</script>\n");
        html.append("</body></html>");

        Files.writeString(reportPath, html.toString());
    }

    private void appendHtmlSummaryTable(StringBuilder html, String title, String prefix) {
        html.append("<div class='stats'>\n");
        html.append("<h2>").append(title).append("</h2>\n");
        html.append("<table border='1' cellpadding='4'>\n");
        html.append("<tr><th>Operation</th><th>Count</th><th>Average</th><th>Min</th><th>Max</th>" +
                   "<th>95th %ile</th><th>99th %ile</th><th>Std Dev</th></tr>\n");

        metrics.entrySet().stream()
            .filter(e -> e.getKey().startsWith(prefix))
            .forEach(e -> {
                String operation = e.getKey().substring(prefix.length());
                List<Long> values = e.getValue();
                html.append(String.format("<tr><td>%s</td><td>%d</td><td>%.2f</td><td>%d</td><td>%d</td><td>%.2f</td><td>%.2f</td><td>%.2f</td></tr>\n",
                    operation, values.size(), getAverage(values), getMin(values), getMax(values),
                    getPercentile(values, PERCENTILE_95), getPercentile(values, PERCENTILE_99),
                    getStandardDeviation(values)));
            });

        html.append("</table>\n</div>\n");
    }

    private void generateChartScripts(StringBuilder html) {
        generateMetricChart(html, LATENCY_PREFIX, "latencyChart", "Latency Distribution (ms)");
        generateMetricChart(html, THROUGHPUT_PREFIX, "throughputChart", "Throughput Distribution (ops/sec)");
        generateMetricChart(html, MEMORY_PREFIX, "memoryChart", "Memory Usage Distribution (bytes)");
    }

    private void generateMetricChart(StringBuilder html, String prefix, String divId, String title) {
        Map<String, List<Long>> filteredMetrics = metrics.entrySet().stream()
            .filter(e -> e.getKey().startsWith(prefix))
            .collect(Collectors.toMap(
                e -> e.getKey().substring(prefix.length()),
                Map.Entry::getValue
            ));

        if (filteredMetrics.isEmpty()) {
            return;
        }

        html.append("var data = [\n");
        filteredMetrics.forEach((operation, values) -> {
            html.append(String.format("  {type: 'violin', name: '%s', y: %s, box: {visible: true}, meanline: {visible: true}},\n",
                escapeJsString(operation), values));
        });
        html.append("];\n");

        html.append("var layout = {\n")
            .append("  title: '").append(escapeJsString(title)).append("',\n")
            .append("  yaxis: {title: 'Value'},\n")
            .append("  violinmode: 'group',\n")
            .append("  showlegend: true\n")
            .append("};\n");

        html.append(String.format("Plotly.newPlot('%s', data, layout);\n", divId));
    }

    private void generateCsvReport() throws IOException {
        Path reportPath = Paths.get(reportDirectory, "performance-report.csv");
        StringBuilder csv = new StringBuilder();
        csv.append("Category,Operation,Count,Average,Min,Max,95th Percentile,99th Percentile,Std Dev\n");

        metrics.entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .forEach(entry -> {
                String category = getCategoryFromMetric(entry.getKey());
                String operation = entry.getKey().substring(category.length() + 1);
                List<Long> values = entry.getValue();
                csv.append(String.format("%s,%s,%d,%.2f,%d,%d,%.2f,%.2f,%.2f\n",
                    category, operation, values.size(), getAverage(values), getMin(values), getMax(values),
                    getPercentile(values, PERCENTILE_95), getPercentile(values, PERCENTILE_99),
                    getStandardDeviation(values)));
            });

        Files.writeString(reportPath, csv.toString());
    }

    private String getCategoryFromMetric(String metric) {
        if (metric.startsWith(LATENCY_PREFIX)) return "Latency";
        if (metric.startsWith(THROUGHPUT_PREFIX)) return "Throughput";
        if (metric.startsWith(MEMORY_PREFIX)) return "Memory";
        return "Other";
    }

    private double getPercentile(List<Long> values, int percentile) {
        if (values.isEmpty()) return 0.0;
        List<Long> sorted = new ArrayList<>(values);
        Collections.sort(sorted);
        int index = (int) Math.ceil(percentile / 100.0 * sorted.size()) - 1;
        return sorted.get(index);
    }

    private double getStandardDeviation(List<Long> values) {
        if (values.isEmpty()) return 0.0;
        double mean = getAverage(values);
        double variance = values.stream()
            .mapToDouble(v -> Math.pow(v - mean, 2))
            .average()
            .orElse(0.0);
        return Math.sqrt(variance);
    }

    private double getAverage(List<Long> values) {
        return values.stream().mapToLong(Long::valueOf).average().orElse(0.0);
    }

    private long getMin(List<Long> values) {
        return values.stream().mapToLong(Long::valueOf).min().orElse(0L);
    }

    private long getMax(List<Long> values) {
        return values.stream().mapToLong(Long::valueOf).max().orElse(0L);
    }

    private String escapeJsString(String input) {
        return input.replace("'", "\\'").replace("\"", "\\\"");
    }

    private void generateJsonReport() throws IOException {
        Path reportPath = Paths.get(reportDirectory, "performance-report.json");
        StringBuilder json = new StringBuilder();
        json.append("{\n");
        json.append("  \"timestamp\": \"").append(LocalDateTime.now().format(TIMESTAMP_FORMAT)).append("\",\n");
        json.append("  \"duration\": ").append(getDurationInMinutes()).append(",\n");
        json.append("  \"metrics\": {\n");
        
        List<String> metricEntries = metrics.entrySet().stream()
            .map(entry -> {
                StringBuilder metricJson = new StringBuilder();
                List<Long> values = entry.getValue();
                metricJson.append("    \"").append(escapeJsonString(entry.getKey())).append("\": {\n");
                metricJson.append("      \"values\": ").append(formatJsonArray(values)).append(",\n");
                metricJson.append("      \"count\": ").append(values.size()).append(",\n");
                metricJson.append("      \"average\": ").append(String.format("%.2f", getAverage(values))).append(",\n");
                metricJson.append("      \"min\": ").append(getMin(values)).append(",\n");
                metricJson.append("      \"max\": ").append(getMax(values)).append(",\n");
                metricJson.append("      \"percentile95\": ").append(String.format("%.2f", getPercentile(values, PERCENTILE_95))).append(",\n");
                metricJson.append("      \"percentile99\": ").append(String.format("%.2f", getPercentile(values, PERCENTILE_99))).append(",\n");
                metricJson.append("      \"standardDeviation\": ").append(String.format("%.2f", getStandardDeviation(values))).append("\n");
                metricJson.append("    }");
                return metricJson.toString();
            })
            .collect(Collectors.toList());
        
        json.append(String.join(",\n", metricEntries));
        json.append("\n  }\n}");
        
        Files.writeString(reportPath, json.toString());
    }

    private String escapeJsonString(String input) {
        return input.replace("\"", "\\\"").replace("\n", "\\n");
    }

    private String formatJsonArray(List<Long> values) {
        return "[" + values.stream().map(String::valueOf).collect(Collectors.joining(",")) + "]";
    }

    private double getDurationInMinutes() {
        return LocalDateTime.now().until(startTime, java.time.temporal.ChronoUnit.MINUTES);
    }

    @Override
    public void clearMetrics() {
        metrics.clear();
    }

    @Override
    public boolean isEnabled() {
        return enabled.get();
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled.set(enabled);
    }

    public String getReportDirectory() {
        return reportDirectory;
    }

    public Map<String, List<Long>> getMetrics() {
        return metrics;
    }
}
