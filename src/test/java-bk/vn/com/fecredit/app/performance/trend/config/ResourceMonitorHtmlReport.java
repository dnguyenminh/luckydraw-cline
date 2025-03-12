package vn.com.fecredit.app.performance.trend.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

public final class ResourceMonitorHtmlReport {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            .withLocale(Locale.getDefault())
            .withZone(ZoneId.systemDefault());
            
    private static final Pattern SAFE_FILENAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_.-]+$");
    private static final int MAX_FILENAME_LENGTH = 255;
    private static final int MAX_REPORT_SIZE_MB = 50;
    private static final AtomicBoolean HTML_ESCAPING_ENABLED = new AtomicBoolean(true);

    private ResourceMonitorHtmlReport() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static void generateReport(ResourceMonitor monitor, Path outputPath) throws IOException {
        validateInputs(monitor, outputPath);
        
        try {
            String lastUpdate = FORMATTER.format(monitor.getLastUpdateTime());
            String html = buildHtml(monitor, lastUpdate);
            
            validateOutputSize(html);
            writeReportSafely(outputPath, html);
        } catch (Exception e) {
            throw new MonitoringTestException("Failed to generate report", e);
        }
    }

    private static String buildHtml(ResourceMonitor monitor, String lastUpdate) {
        StringBuilder html = new StringBuilder();
        appendHtmlHeader(html, lastUpdate);
        appendHtmlBody(html, monitor, lastUpdate);
        return html.toString();
    }

    private static void appendHtmlHeader(StringBuilder html, String lastUpdate) {
        html.append("<!DOCTYPE html>\n")
            .append("<html lang=\"en\">\n")
            .append("<head>\n")
            .append("    <meta charset=\"UTF-8\">\n")
            .append("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n")
            .append("    <meta name=\"description\" content=\"Resource monitoring report showing system performance metrics\">\n")
            .append("    <title>Resource Monitor Report - ").append(escapeHtml(lastUpdate)).append("</title>\n")
            .append(getStyles())
            .append("</head>\n");
    }

    private static void appendHtmlBody(StringBuilder html, ResourceMonitor monitor, String lastUpdate) {
        html.append("<body>\n")
            .append("    <a href=\"#main-content\" class=\"skip-link\">Skip to main content</a>\n")
            .append("    <div class=\"container\">\n");

        appendHeader(html, monitor, lastUpdate);
        appendMainContent(html, monitor);
        appendFooter(html, monitor, lastUpdate);
        appendScripts(html);

        html.append("    </div>\n")
            .append("</body>\n")
            .append("</html>");
    }

    private static void appendHeader(StringBuilder html, ResourceMonitor monitor, String lastUpdate) {
        html.append(String.format("""
            <header role="banner">
                <h1>Resource Usage Summary</h1>
                <p>Generated on: <time datetime="%s">%s</time></p>
            </header>
            <div role="alert" id="status-message" aria-live="polite"></div>
            <main id="main-content" role="main">
            """, 
            monitor.getLastUpdateTime().toString(), 
            escapeHtml(lastUpdate)));
    }

    private static void appendMainContent(StringBuilder html, ResourceMonitor monitor) {
        ResourceMonitor.ResourceSummary summary = monitor.generateSummary();
        appendSummarySection(html, summary);
        appendMetricsSection(html, monitor.getSnapshots());
        html.append("</main>");
    }

    private static void appendSummarySection(StringBuilder html, ResourceMonitor.ResourceSummary summary) {
        html.append(String.format("""
            <section class="summary" aria-labelledby="summary-heading">
                <h2 id="summary-heading">System Overview</h2>
                <dl role="list">
                    <dt>Average CPU Usage</dt>
                    <dd>%.2f%%</dd>
                    <dt>Peak Memory Usage</dt>
                    <dd>%s</dd>
                    <dt>Total GC Count</dt>
                    <dd>%d</dd>
                </dl>
            </section>
            """,
            summary.averageCpuUsage() * 100,
            formatBytes(summary.peakMemoryUsage()),
            summary.totalGcCount()));
    }

    private static void appendMetricsSection(StringBuilder html, List<ResourceMonitor.MetricSnapshot> snapshots) {
        html.append("""
            <section aria-labelledby="metrics-heading">
                <h2 id="metrics-heading">Detailed Metrics</h2>
                <div class="table-controls">
                    <button type="button" onclick="sortTable('timestamp')" aria-controls="metrics-table">
                        Sort by Time
                    </button>
                    <button type="button" onclick="sortTable('cpu')" aria-controls="metrics-table">
                        Sort by CPU
                    </button>
                    <button type="button" onclick="sortTable('memory')" aria-controls="metrics-table">
                        Sort by Memory
                    </button>
                </div>
                <div class="table-container" role="region" aria-label="Scrollable metrics data" tabindex="0">
            """);

        appendMetricsTable(html, snapshots);
        html.append("</div></section>");
    }

    private static void appendMetricsTable(StringBuilder html, List<ResourceMonitor.MetricSnapshot> snapshots) {
        html.append("""
            <table class="metrics" id="metrics-table" aria-describedby="metrics-desc">
                <caption id="metrics-desc">System resource usage over time</caption>
                <thead>
                    <tr>
                        <th scope="col" role="columnheader" aria-sort="none">Timestamp</th>
                        <th scope="col" role="columnheader" aria-sort="none">CPU Usage</th>
                        <th scope="col" role="columnheader" aria-sort="none">Memory Usage</th>
                        <th scope="col" role="columnheader" aria-sort="none">Thread Count</th>
                    </tr>
                </thead>
                <tbody>
            """);

        for (ResourceMonitor.MetricSnapshot snapshot : snapshots) {
            html.append(String.format("""
                <tr>
                    <td>%s</td>
                    <td>%.2f%%</td>
                    <td>%s</td>
                    <td>%d</td>
                </tr>
                """,
                escapeHtml(FORMATTER.format(snapshot.timestamp())),
                snapshot.cpuUsage() * 100,
                formatBytes(snapshot.heapUsed() + snapshot.nonHeapUsed()),
                snapshot.threadCount()));
        }

        html.append("</tbody></table>");
    }

    private static void appendFooter(StringBuilder html, ResourceMonitor monitor, String lastUpdate) {
        html.append(String.format("""
            <footer role="contentinfo">
                <p>Last Updated: <time datetime="%s">%s</time></p>
                <div class="sr-only" aria-live="polite" id="update-notification"></div>
            </footer>
            """,
            monitor.getLastUpdateTime().toString(),
            escapeHtml(lastUpdate)));
    }

    private static void appendScripts(StringBuilder html) {
        html.append("""
            <script>
            (function() {
                document.addEventListener('keydown', function(e) {
                    if (e.key === 'Escape') {
                        document.getElementById('update-notification')
                            .textContent = 'Pressed escape key - returning to top of page';
                        window.scrollTo({ top: 0, behavior: 'smooth' });
                    }
                });

                const table = document.querySelector('.table-container');
                if (table) {
                    table.addEventListener('scroll', function() {
                        document.getElementById('update-notification')
                            .textContent = 'Scrolling through metrics table';
                    });
                }

                window.sortTable = function(column) {
                    const table = document.getElementById('metrics-table');
                    const tbody = table.querySelector('tbody');
                    const rows = Array.from(tbody.querySelectorAll('tr'));
                    const headers = table.querySelectorAll('th');
                    
                    headers.forEach(header => header.setAttribute('aria-sort', 'none'));
                    
                    const currentSort = tbody.getAttribute('data-sort') || '';
                    const ascending = currentSort !== column;
                    
                    const header = Array.from(headers).find(h => 
                        h.textContent.toLowerCase().includes(column.toLowerCase()));
                    header.setAttribute('aria-sort', ascending ? 'ascending' : 'descending');
                    
                    rows.sort((a, b) => {
                        let aVal = a.cells[getColumnIndex(column)].textContent;
                        let bVal = b.cells[getColumnIndex(column)].textContent;
                        
                        if (!isNaN(parseFloat(aVal))) {
                            aVal = parseFloat(aVal);
                            bVal = parseFloat(bVal);
                        }
                        
                        if (aVal < bVal) return ascending ? -1 : 1;
                        if (aVal > bVal) return ascending ? 1 : -1;
                        return 0;
                    });
                    
                    tbody.innerHTML = '';
                    rows.forEach(row => tbody.appendChild(row));
                    tbody.setAttribute('data-sort', ascending ? column : '');
                    
                    document.getElementById('update-notification').textContent =
                        `Table sorted by ${column} in ${ascending ? 'ascending' : 'descending'} order`;
                };

                function getColumnIndex(column) {
                    switch(column) {
                        case 'timestamp': return 0;
                        case 'cpu': return 1;
                        case 'memory': return 2;
                        default: return 0;
                    }
                }
            })();
            </script>
            """);
    }

    private static String getStyles() {
        return """
            <style>
                :root {
                    --main-bg: #ffffff;
                    --main-text: #333333;
                    --border: #dddddd;
                    --header-bg: #f8f8f8;
                    --hover-bg: #f0f0f0;
                    --focus-outline: #4a90e2;
                    --error-color: #d32f2f;
                    --success-color: #388e3c;
                }
                body { 
                    font-family: Arial, sans-serif; 
                    line-height: 1.6; 
                    margin: 0; 
                    padding: 0; 
                    background: var(--main-bg); 
                    color: var(--main-text);
                }
                .container { 
                    max-width: 1200px; 
                    margin: 0 auto; 
                    padding: 20px;
                }
                .summary { 
                    margin-bottom: 30px; 
                    padding: 20px; 
                    border: 1px solid var(--border); 
                    border-radius: 4px;
                }
                .metrics {
                    width: 100%;
                    border-collapse: collapse;
                    margin: 20px 0;
                    box-shadow: 0 1px 3px rgba(0,0,0,0.1);
                }
                .metrics th, .metrics td {
                    border: 1px solid var(--border);
                    padding: 12px;
                    text-align: left;
                }
                .metrics th {
                    background-color: var(--header-bg);
                    font-weight: bold;
                    position: sticky;
                    top: 0;
                    z-index: 1;
                }
                .metrics tr:nth-child(even) { background-color: #f5f5f5; }
                .metrics tr:hover { background-color: var(--hover-bg); }
                .metrics tr:focus-within { outline: 2px solid var(--focus-outline); }
                .metrics caption {
                    font-weight: bold;
                    padding: 10px;
                    text-align: left;
                    background-color: var(--header-bg);
                    border: 1px solid var(--border);
                }
                *:focus {
                    outline: 3px solid var(--focus-outline);
                    outline-offset: 2px;
                }
                .skip-link {
                    position: absolute;
                    top: -40px;
                    left: 0;
                    background: var(--focus-outline);
                    color: white;
                    padding: 8px;
                    z-index: 100;
                    transition: top 0.3s;
                }
                .skip-link:focus { top: 0; }
                .table-controls {
                    margin-bottom: 10px;
                    display: flex;
                    gap: 10px;
                }
                .table-controls button {
                    padding: 8px 16px;
                    border: 1px solid var(--border);
                    border-radius: 4px;
                    background: var(--header-bg);
                    cursor: pointer;
                }
                .table-controls button:hover {
                    background: var(--hover-bg);
                }
                @media (max-width: 768px) {
                    .metrics { display: block; overflow-x: auto; }
                    .container { padding: 10px; }
                    .summary { padding: 15px; }
                    .table-controls {
                        flex-direction: column;
                        align-items: stretch;
                    }
                }
                @media (prefers-reduced-motion: reduce) {
                    * { transition: none !important; }
                }
                .sr-only {
                    position: absolute;
                    width: 1px;
                    height: 1px;
                    padding: 0;
                    margin: -1px;
                    overflow: hidden;
                    clip: rect(0,0,0,0);
                    border: 0;
                }
                [role="alert"] {
                    padding: 10px;
                    margin: 10px 0;
                    border-radius: 4px;
                    display: none;
                }
                [role="alert"].error {
                    background-color: #ffebee;
                    color: var(--error-color);
                    border: 1px solid var(--error-color);
                }
                [role="alert"].success {
                    background-color: #e8f5e9;
                    color: var(--success-color);
                    border: 1px solid var(--success-color);
                }
            </style>
            """;
    }

    private static void validateInputs(ResourceMonitor monitor, Path outputPath) {
        Objects.requireNonNull(monitor, "Monitor cannot be null");
        Objects.requireNonNull(outputPath, "Output path cannot be null");

        String filename = outputPath.getFileName().toString();
        if (!SAFE_FILENAME_PATTERN.matcher(filename).matches() || 
            filename.length() > MAX_FILENAME_LENGTH) {
            throw new MonitoringTestException("Invalid output filename");
        }

        Path parent = outputPath.getParent();
        if (parent != null && !Files.exists(parent)) {
            throw new MonitoringTestException("Parent directory does not exist");
        }
    }

    private static void validateOutputSize(String html) {
        long sizeInMb = html.getBytes(StandardCharsets.UTF_8).length / (1024 * 1024);
        if (sizeInMb > MAX_REPORT_SIZE_MB) {
            throw new MonitoringTestException(String.format(
                "Report size exceeds limit: %d MB > %d MB", sizeInMb, MAX_REPORT_SIZE_MB));
        }
    }

    private static void writeReportSafely(Path outputPath, String html) throws IOException {
        Path tempFile = null;
        try {
            tempFile = Files.createTempFile(outputPath.getParent(), "report_", ".tmp");
            Files.writeString(tempFile, html, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            Files.move(tempFile, outputPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        } finally {
            if (tempFile != null) {
                try {
                    Files.deleteIfExists(tempFile);
                } catch (IOException e) {
                    System.err.println("Failed to delete temporary file: " + tempFile);
                }
            }
        }
    }

    private static String escapeHtml(String text) {
        if (!HTML_ESCAPING_ENABLED.get() || text == null) {
            return text;
        }
        return text.replace("&", "&amp;")
                  .replace("<", "&lt;")
                  .replace(">", "&gt;")
                  .replace("\"", "&quot;")
                  .replace("'", "&#x27;");
    }

    public static void setHtmlEscapingEnabled(boolean enabled) {
        HTML_ESCAPING_ENABLED.set(enabled);
    }

    private static String formatBytes(long bytes) {
        if (bytes < 0) {
            throw new IllegalArgumentException("Bytes cannot be negative");
        }
        if (bytes < 1024) return bytes + " B";
        
        try {
            int exp = (int) (Math.log(bytes) / Math.log(1024));
            if (exp >= "KMGTPE".length()) {
                return bytes + " B"; // Fallback for extremely large values
            }
            String pre = "KMGTPE".charAt(exp - 1) + "";
            return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
        } catch (Exception e) {
            return bytes + " B"; // Fallback for any calculation errors
        }
    }
}
