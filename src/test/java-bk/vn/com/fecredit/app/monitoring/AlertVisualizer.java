package vn.com.fecredit.app.monitoring;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Visualization generator for alert history and statistics
 */
public class AlertVisualizer {

    private static final String CHART_JS_CDN = "https://cdn.jsdelivr.net/npm/chart.js";
    private static final DateTimeFormatter DATE_FORMATTER = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.systemDefault());

    public void generateVisualization(AlertHistory.AlertSummary summary, String outputPath) throws IOException {
        String html = generateHtml(summary);
        Files.writeString(Path.of(outputPath), html);
    }

    private String generateHtml(AlertHistory.AlertSummary summary) {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n");
        html.append("<html><head>\n");
        html.append("<title>Alert History Visualization</title>\n");
        html.append("<script src='").append(CHART_JS_CDN).append("'></script>\n");
        html.append("<style>\n");
        html.append(getStyles());
        html.append("</style>\n");
        html.append("</head><body>\n");

        // Header
        html.append("<div class='header'>\n");
        html.append("<h1>Alert History Report</h1>\n");
        html.append("<p class='summary'>Period: ")
            .append(DATE_FORMATTER.format(summary.getOldestAlert()))
            .append(" to ")
            .append(DATE_FORMATTER.format(summary.getNewestAlert()))
            .append("</p>\n");
        html.append("</div>\n");

        // Overview section
        html.append("<div class='section'>\n");
        html.append("<h2>Overview</h2>\n");
        html.append("<div class='stats'>\n");
        html.append("<div class='stat-box'>\n");
        html.append("<div class='stat-value'>").append(summary.getTotalAlerts()).append("</div>\n");
        html.append("<div class='stat-label'>Total Alerts</div>\n");
        html.append("</div>\n");
        html.append("</div>\n");
        html.append("</div>\n");

        // Charts section
        html.append("<div class='charts-container'>\n");
        
        // Severity distribution chart
        html.append("<div class='chart-box'>\n");
        html.append("<h3>Alert Severity Distribution</h3>\n");
        html.append("<canvas id='severityChart'></canvas>\n");
        html.append("</div>\n");

        // Resource usage chart
        html.append("<div class='chart-box'>\n");
        html.append("<h3>Resource Usage by Alert Type</h3>\n");
        html.append("<canvas id='resourceChart'></canvas>\n");
        html.append("</div>\n");
        
        html.append("</div>\n");

        // Alert details table
        html.append("<div class='section'>\n");
        html.append("<h2>Alert Details</h2>\n");
        html.append("<div class='table-container'>\n");
        html.append("<table>\n");
        html.append("<thead><tr><th>Alert Type</th><th>Count</th><th>Avg CPU</th><th>Max CPU</th>" +
                   "<th>Avg Memory</th><th>Max Memory</th></tr></thead>\n");
        html.append("<tbody>\n");
        
        summary.getAlertStats().forEach((name, stats) -> {
            html.append("<tr>")
                .append("<td>").append(name).append("</td>")
                .append("<td>").append(stats.getCount()).append("</td>")
                .append(String.format("<td>%.1f%%</td>", stats.getAvgCpu()))
                .append(String.format("<td>%.1f%%</td>", stats.getMaxCpu()))
                .append(String.format("<td>%.1f MB</td>", stats.getAvgMemory()))
                .append(String.format("<td>%.1f MB</td>", stats.getMaxMemory()))
                .append("</tr>\n");
        });
        
        html.append("</tbody></table>\n");
        html.append("</div>\n");
        html.append("</div>\n");

        // Charts initialization
        html.append("<script>\n");
        html.append(generateChartScripts(summary));
        html.append("</script>\n");

        html.append("</body></html>");
        return html.toString();
    }

    private String getStyles() {
        StringBuilder css = new StringBuilder();
        css.append("body { font-family: Arial, sans-serif; margin: 0; padding: 20px; background: #f5f5f5; }\n");
        css.append(".header { text-align: center; margin-bottom: 30px; }\n");
        css.append(".header h1 { color: #333; margin-bottom: 10px; }\n");
        css.append(".summary { color: #666; }\n");
        css.append(".section { background: white; padding: 20px; border-radius: 8px; margin-bottom: 20px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }\n");
        css.append(".stats { display: flex; justify-content: center; gap: 20px; margin-bottom: 20px; }\n");
        css.append(".stat-box { text-align: center; padding: 20px; background: #f8f9fa; border-radius: 8px; min-width: 150px; }\n");
        css.append(".stat-value { font-size: 24px; font-weight: bold; color: #007bff; }\n");
        css.append(".stat-label { color: #666; margin-top: 5px; }\n");
        css.append(".charts-container { display: flex; flex-wrap: wrap; gap: 20px; margin-bottom: 20px; }\n");
        css.append(".chart-box { flex: 1; min-width: 300px; background: white; padding: 20px; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1); }\n");
        css.append(".table-container { overflow-x: auto; }\n");
        css.append("table { width: 100%; border-collapse: collapse; }\n");
        css.append("th, td { padding: 12px; text-align: left; border-bottom: 1px solid #ddd; }\n");
        css.append("th { background: #f8f9fa; }\n");
        css.append("tr:hover { background: #f8f9fa; }\n");
        return css.toString();
    }

    private String generateChartScripts(AlertHistory.AlertSummary summary) {
        Map<ResourceAlert.AlertSeverity, Integer> severityCounts = summary.getSeverityCounts();
        Map<String, AlertHistory.AlertStats> alertStats = summary.getAlertStats();

        StringBuilder js = new StringBuilder();
        js.append("// Severity Distribution Chart\n");
        js.append("new Chart(document.getElementById('severityChart'), {\n");
        js.append("    type: 'pie',\n");
        js.append("    data: {\n");
        js.append("        labels: ").append(toJsArray(severityCounts.keySet().stream()
            .map(Enum::name)
            .collect(Collectors.toList()))).append(",\n");
        js.append("        datasets: [{\n");
        js.append("            data: ").append(toJsArray(new ArrayList<>(severityCounts.values()))).append(",\n");
        js.append("            backgroundColor: ['#28a745', '#ffc107', '#dc3545']\n");
        js.append("        }]\n");
        js.append("    },\n");
        js.append("    options: {\n");
        js.append("        responsive: true,\n");
        js.append("        plugins: { legend: { position: 'bottom' } }\n");
        js.append("    }\n");
        js.append("});\n\n");

        js.append("// Resource Usage Chart\n");
        js.append("new Chart(document.getElementById('resourceChart'), {\n");
        js.append("    type: 'bar',\n");
        js.append("    data: {\n");
        js.append("        labels: ").append(toJsArray(new ArrayList<>(alertStats.keySet()))).append(",\n");
        js.append("        datasets: [{\n");
        js.append("            label: 'Avg CPU Usage (%)',\n");
        js.append("            data: ").append(toJsArray(alertStats.values().stream()
            .map(AlertHistory.AlertStats::getAvgCpu)
            .collect(Collectors.toList()))).append(",\n");
        js.append("            backgroundColor: 'rgba(54, 162, 235, 0.5)'\n");
        js.append("        }, {\n");
        js.append("            label: 'Avg Memory Usage (MB)',\n");
        js.append("            data: ").append(toJsArray(alertStats.values().stream()
            .map(AlertHistory.AlertStats::getAvgMemory)
            .collect(Collectors.toList()))).append(",\n");
        js.append("            backgroundColor: 'rgba(255, 99, 132, 0.5)'\n");
        js.append("        }]\n");
        js.append("    },\n");
        js.append("    options: {\n");
        js.append("        responsive: true,\n");
        js.append("        scales: { y: { beginAtZero: true } },\n");
        js.append("        plugins: { legend: { position: 'bottom' } }\n");
        js.append("    }\n");
        js.append("});\n");

        return js.toString();
    }

    private String toJsArray(List<?> items) {
        return "[" + items.stream()
            .map(item -> item instanceof String ? "'" + item + "'" : item.toString())
            .collect(Collectors.joining(", ")) + "]";
    }
}
