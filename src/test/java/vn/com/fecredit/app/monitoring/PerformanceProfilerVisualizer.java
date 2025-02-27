package vn.com.fecredit.app.monitoring;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Visualizer for performance profiling results with export options and interactive features
 */
public class PerformanceProfilerVisualizer {
    private static final Logger logger = LoggerFactory.getLogger(PerformanceProfilerVisualizer.class);
    private static final String DEFAULT_OUTPUT_FILENAME = "performance-report.html";
    private static final int MAX_LABEL_LENGTH = 100;
    private static final double MIN_VALID_VALUE = 0.0;
    private static final double MAX_VALID_VALUE = Double.MAX_VALUE;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final String HTML_TEMPLATE = 
        "<!DOCTYPE html>\n" +
        "<html>\n" +
        "<head>\n" +
        "    <title>Performance Profile Visualization</title>\n" +
        "    <script src='https://cdn.plot.ly/plotly-latest.min.js'></script>\n" +
        "    <script src='https://cdnjs.cloudflare.com/ajax/libs/FileSaver.js/2.0.5/FileSaver.min.js'></script>\n" +
        "    <style>\n" +
        "        body { font-family: Arial, sans-serif; margin: 20px; }\n" +
        "        .chart { height: 400px; margin: 20px 0; }\n" +
        "        .stats { margin: 20px 0; }\n" +
        "        .controls { margin: 20px 0; }\n" +
        "        .export-buttons { margin: 10px 0; }\n" +
        "        table { border-collapse: collapse; width: 100%%; }\n" +
        "        th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }\n" +
        "        th { background-color: #f5f5f5; }\n" +
        "        button { margin: 5px; padding: 8px 15px; cursor: pointer; }\n" +
        "        .filter-box { margin: 10px 0; padding: 10px; border: 1px solid #ddd; }\n" +
        "    </style>\n" +
        "</head>\n" +
        "<body>\n" +
        "    <h1>Performance Profile Visualization</h1>\n" +
        "    <div class='controls'>\n" +
        "        <div class='filter-box'>\n" +
        "            <label>Time Range: </label>\n" +
        "            <select id='timeRange' onchange='updateCharts()'>\n" +
        "                <option value='all'>All</option>\n" +
        "                <option value='last10'>Last 10</option>\n" +
        "                <option value='last20'>Last 20</option>\n" +
        "            </select>\n" +
        "            <label style='margin-left: 20px;'>Chart Type: </label>\n" +
        "            <select id='chartType' onchange='updateCharts()'>\n" +
        "                <option value='line'>Line</option>\n" +
        "                <option value='bar'>Bar</option>\n" +
        "                <option value='scatter'>Scatter</option>\n" +
        "            </select>\n" +
        "        </div>\n" +
        "        <div class='export-buttons'>\n" +
        "            <button onclick='exportToCSV()'>Export to CSV</button>\n" +
        "            <button onclick='exportToJSON()'>Export to JSON</button>\n" +
        "            <button onclick='exportChartsPNG()'>Export Charts (PNG)</button>\n" +
        "        </div>\n" +
        "    </div>\n" +
        "    <div id='timelineChart' class='chart'></div>\n" +
        "    <div id='cpuChart' class='chart'></div>\n" +
        "    <div id='memoryChart' class='chart'></div>\n" +
        "    <div class='stats'>\n" +
        "        <h2>Performance Statistics</h2>\n" +
        "        %s\n" +
        "    </div>\n" +
        "    <script>\n" +
        "        %s\n" +
        "    </script>\n" +
        "</body>\n" +
        "</html>";

    private final Set<String> errorMessages = new LinkedHashSet<>();
    private boolean failOnError = false;

    public void setFailOnError(boolean failOnError) {
        this.failOnError = failOnError;
    }

    public List<String> getErrors() {
        return new ArrayList<>(errorMessages);
    }

    public void clearErrors() {
        errorMessages.clear();
    }

    public void generateVisualization(PerformanceProfiler.ProfilerSummary summary) throws IOException {
        generateVisualization(summary, DEFAULT_OUTPUT_FILENAME);
    }

    public void generateVisualization(PerformanceProfiler.ProfilerSummary summary, String outputPath) throws IOException {
        try {
            validateInput(summary, outputPath);
            
            List<PerformanceProfiler.ProfilerSnapshot> snapshots = summary.getSnapshots();
            validateSnapshots(snapshots);
            
            Map<String, Object> reportData = prepareReportData(summary, snapshots);
            String html = generateHtml(reportData);
            
            writeToFile(outputPath, html);
            logErrorsIfAny();
            
            logger.info("Successfully generated performance report at: {}", outputPath);
        } catch (Exception e) {
            String error = "Failed to generate visualization: " + e.getMessage();
            logger.error(error, e);
            errorMessages.add(error);
            if (failOnError) {
                throw new PerformanceReportingException(error, e);
            }
        }
    }

    private Map<String, Object> prepareReportData(PerformanceProfiler.ProfilerSummary summary, 
            List<PerformanceProfiler.ProfilerSnapshot> snapshots) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("timestamp", LocalDateTime.now().format(DATE_FORMATTER));
        data.put("statistics", generateStatisticsTable(summary));
        data.put("charts", generateCharts(snapshots));
        data.put("metadata", generateMetadata(summary));
        return data;
    }

    private Map<String, Object> generateMetadata(PerformanceProfiler.ProfilerSummary summary) {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("generatedAt", LocalDateTime.now().format(DATE_FORMATTER));
        metadata.put("snapshotCount", summary.getSnapshots().size());
        metadata.put("totalDuration", formatDuration(summary.getTotalElapsedTimeMs()));
        metadata.put("peakMemory", formatMemory(summary.getMaxMemoryMB()));
        metadata.put("averageCpuTime", formatDuration(summary.getTotalCpuTimeMs() / summary.getSnapshots().size()));
        return metadata;
    }

    private String formatDuration(double ms) {
        if (ms < 1000) {
            return String.format("%.2f ms", ms);
        } else if (ms < 60000) {
            return String.format("%.2f seconds", ms / 1000);
        } else {
            return String.format("%.2f minutes", ms / 60000);
        }
    }

    private String formatMemory(double mb) {
        if (mb < 1024) {
            return String.format("%.2f MB", mb);
        } else {
            return String.format("%.2f GB", mb / 1024);
        }
    }

    private void validateInput(PerformanceProfiler.ProfilerSummary summary, String outputPath) {
        if (summary == null) {
            throw new IllegalArgumentException("Performance summary cannot be null");
        }
        if (outputPath == null || outputPath.trim().isEmpty()) {
            throw new IllegalArgumentException("Output path cannot be null or empty");
        }
        if (!outputPath.endsWith(".html")) {
            throw new IllegalArgumentException("Output file must be HTML format");
        }
    }

    private void validateSnapshots(List<PerformanceProfiler.ProfilerSnapshot> snapshots) {
        if (snapshots == null) {
            throw new IllegalArgumentException("Snapshots list cannot be null");
        }
        
        for (int i = 0; i < snapshots.size(); i++) {
            try {
                validateSnapshot(snapshots.get(i));
            } catch (Exception e) {
                throw new IllegalArgumentException("Invalid snapshot at index " + i + ": " + e.getMessage());
            }
        }
    }

    private void validateSnapshot(PerformanceProfiler.ProfilerSnapshot snapshot) {
        if (snapshot == null) {
            throw new IllegalArgumentException("Snapshot cannot be null");
        }
        
        String label = snapshot.getLabel();
        if (label == null || label.trim().isEmpty()) {
            throw new IllegalArgumentException("Snapshot label cannot be null or empty");
        }
        if (label.length() > MAX_LABEL_LENGTH) {
            addError("Snapshot label exceeds maximum length: " + label);
        }
        
        validateMetricValue(snapshot.getElapsedTimeMs(), "Elapsed time");
        validateMetricValue(snapshot.getCpuTimeMs(), "CPU time");
        validateMetricValue(snapshot.getMemoryMB(), "Memory usage");
    }

    private void validateMetricValue(double value, String metricName) {
        if (Double.isNaN(value) || Double.isInfinite(value)) {
            throw new IllegalArgumentException(metricName + " must be a finite number");
        }
        if (value < MIN_VALID_VALUE || value > MAX_VALID_VALUE) {
            addError(metricName + " value out of range: " + value);
        }
    }

    private void writeToFile(String outputPath, String content) throws IOException {
        try {
            Files.writeString(Paths.get(outputPath), content);
        } catch (IOException e) {
            throw new IOException("Failed to write visualization to file: " + outputPath, e);
        }
    }

    private void logErrorsIfAny() {
        if (!errorMessages.isEmpty()) {
            logger.warn("Performance report generated with {} warnings:", errorMessages.size());
            errorMessages.forEach(error -> logger.warn("- {}", error));
        }
    }

    private void addError(String error) {
        errorMessages.add(error);
        logger.warn(error);
    }

    private String generateHtml(Map<String, Object> data) {
        return String.format(HTML_TEMPLATE, 
            data.get("statistics"), 
            generateJavaScript(data));
    }

    private String generateJavaScript(Map<String, Object> data) {
        StringBuilder js = new StringBuilder();
        js.append("const reportMetadata = ")
          .append(toJson(data.get("metadata")))
          .append(";\n")
          .append(data.get("charts"));
        return js.toString();
    }

    private String toJson(Object obj) {
        if (obj == null) return "null";
        if (obj instanceof Map) {
            return ((Map<?, ?>) obj).entrySet().stream()
                .map(e -> "\"" + e.getKey() + "\": " + toJson(e.getValue()))
                .collect(Collectors.joining(", ", "{", "}"));
        }
        if (obj instanceof Collection) {
            return ((Collection<?>) obj).stream()
                .map(this::toJson)
                .collect(Collectors.joining(", ", "[", "]"));
        }
        if (obj instanceof Number) return obj.toString();
        if (obj instanceof Boolean) return obj.toString();
        return "\"" + escapeJsString(obj.toString()) + "\"";
    }

    private String escapeJsString(String input) {
        if (input == null) return "";
        return input.replace("\\", "\\\\")
                   .replace("'", "\\'")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }

    private String generateStatisticsTable(PerformanceProfiler.ProfilerSummary summary) {
        StringBuilder table = new StringBuilder();
        table.append("<table>");
        table.append("<tr><th>Metric</th><th>Value</th></tr>");
        
        addTableRow(table, "Total CPU Time", formatDuration(summary.getTotalCpuTimeMs()));
        addTableRow(table, "Total Elapsed Time", formatDuration(summary.getTotalElapsedTimeMs()));
        addTableRow(table, "Peak Memory Usage", formatMemory(summary.getMaxMemoryMB()));
        
        List<PerformanceProfiler.ProfilerSnapshot> snapshots = summary.getSnapshots();
        if (!snapshots.isEmpty()) {
            addTableRow(table, "Number of Operations", String.valueOf(snapshots.size()));
            addTableRow(table, "Average Operation Time", 
                formatDuration(summary.getTotalElapsedTimeMs() / snapshots.size()));
        }
        
        table.append("</table>");
        return table.toString();
    }

    private void addTableRow(StringBuilder table, String metric, String value) {
        table.append(String.format("<tr><td>%s</td><td>%s</td></tr>", 
            escapeHtml(metric), escapeHtml(value)));
    }

    private String escapeHtml(String input) {
        return Optional.ofNullable(input)
            .map(str -> str.replace("&", "&amp;")
                         .replace("<", "&lt;")
                         .replace(">", "&gt;")
                         .replace("\"", "&quot;")
                         .replace("'", "&#39;"))
            .orElse("");
    }

    private String generateCharts(List<PerformanceProfiler.ProfilerSnapshot> snapshots) {
        try {
            StringBuilder charts = new StringBuilder();
            
            // Initialize data for charts
            charts.append("const data = {\n")
                .append("    labels: ").append(getLabels(snapshots)).append(",\n")
                .append("    elapsedTime: ").append(getElapsedTimes(snapshots)).append(",\n")
                .append("    cpuTime: ").append(getCpuTimes(snapshots)).append(",\n")
                .append("    memory: ").append(getMemoryUsage(snapshots)).append("\n")
                .append("};\n\n")
                .append(getChartRenderingCode());

            return charts.toString();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to generate charts", e);
        }
    }

    private String getChartRenderingCode() {
        return "function renderCharts(data, type) {\n" +
               "    const commonLayout = {\n" +
               "        showlegend: true,\n" +
               "        hovermode: 'closest',\n" +
               "        margin: { t: 50, r: 50, l: 50, b: 50 }\n" +
               "    };\n\n" +
               generatePlotlyCharts() +
               "}\n\n" +
               "initializeData(data);\n";
    }

    private String generatePlotlyCharts() {
        return "    Plotly.newPlot('timelineChart', [{\n" +
               "        x: data.labels,\n" +
               "        y: data.elapsedTime,\n" +
               "        type: type,\n" +
               "        name: 'Elapsed Time (ms)',\n" +
               "        mode: type === 'scatter' ? 'lines+markers' : undefined\n" +
               "    }], Object.assign({}, commonLayout, {title: 'Operation Timeline'}));\n\n" +
               "    Plotly.newPlot('cpuChart', [{\n" +
               "        x: data.labels,\n" +
               "        y: data.cpuTime,\n" +
               "        type: type,\n" +
               "        name: 'CPU Time (ms)',\n" +
               "        mode: type === 'scatter' ? 'lines+markers' : undefined\n" +
               "    }], Object.assign({}, commonLayout, {title: 'CPU Usage'}));\n\n" +
               "    Plotly.newPlot('memoryChart', [{\n" +
               "        x: data.labels,\n" +
               "        y: data.memory,\n" +
               "        type: type,\n" +
               "        name: 'Memory Usage (MB)',\n" +
               "        fill: type === 'scatter' ? 'tozeroy' : undefined,\n" +
               "        mode: type === 'scatter' ? 'lines+markers' : undefined\n" +
               "    }], Object.assign({}, commonLayout, {title: 'Memory Usage'}));";
    }

    private String getLabels(List<PerformanceProfiler.ProfilerSnapshot> snapshots) {
        return snapshots.stream()
            .map(s -> "'" + escapeJsString(s.getLabel()) + "'")
            .collect(Collectors.joining(",", "[", "]"));
    }

    private String getElapsedTimes(List<PerformanceProfiler.ProfilerSnapshot> snapshots) {
        return snapshots.stream()
            .map(s -> String.format("%.2f", s.getElapsedTimeMs()))
            .collect(Collectors.joining(",", "[", "]"));
    }

    private String getCpuTimes(List<PerformanceProfiler.ProfilerSnapshot> snapshots) {
        return snapshots.stream()
            .map(s -> String.format("%.2f", s.getCpuTimeMs()))
            .collect(Collectors.joining(",", "[", "]"));
    }

    private String getMemoryUsage(List<PerformanceProfiler.ProfilerSnapshot> snapshots) {
        return snapshots.stream()
            .map(s -> String.format("%.2f", s.getMemoryMB()))
            .collect(Collectors.joining(",", "[", "]"));
    }
}
