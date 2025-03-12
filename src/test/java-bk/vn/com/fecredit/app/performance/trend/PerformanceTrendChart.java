package vn.com.fecredit.app.performance.trend;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class PerformanceTrendChart {

    private static final DateTimeFormatter CHART_DATE_FORMAT = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public static void generateTrendCharts(List<PerformanceTrendAnalyzer.TrendResult> trends, Path outputPath) 
            throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8)) {
            writer.write(generateHtml(trends));
        }
    }

    private static String generateHtml(List<PerformanceTrendAnalyzer.TrendResult> trends) {
        StringBuilder html = new StringBuilder("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>Performance Trends</title>
                <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
                <style>
                    body { font-family: Arial, sans-serif; margin: 40px; }
                    .chart-container { margin: 20px 0; height: 400px; }
                    .trend-summary { 
                        background: #f8f9fa; 
                        padding: 15px; 
                        margin: 10px 0; 
                        border-radius: 5px;
                    }
                    .improving { color: green; }
                    .degrading { color: red; }
                    .stable { color: blue; }
                </style>
            </head>
            <body>
                <h1>Performance Trend Analysis</h1>
            """);

        for (var trend : trends) {
            html.append(generateTrendSection(trend));
        }

        html.append("""
                <script>
                    // Common chart options
                    const commonOptions = {
                        responsive: true,
                        maintainAspectRatio: false,
                        scales: {
                            y: {
                                beginAtZero: true,
                                title: { 
                                    display: true,
                                    text: 'Execution Time (seconds)'
                                }
                            },
                            x: {
                                title: { 
                                    display: true,
                                    text: 'Test Run Date/Time'
                                }
                            }
                        },
                        plugins: {
                            tooltip: {
                                callbacks: {
                                    label: function(context) {
                                        return `Execution Time: ${context.parsed.y.toFixed(2)} seconds`;
                                    }
                                }
                            }
                        }
                    };
                </script>
            </body>
            </html>
        """);

        return html.toString();
    }

    private static String generateTrendSection(PerformanceTrendAnalyzer.TrendResult trend) {
        String chartId = "chart_" + trend.testName().replaceAll("[^a-zA-Z0-9]", "_");
        String trendClass = getTrendClass(trend.trend());
        
        StringBuilder section = new StringBuilder();
        section.append(String.format("""
            <div class="trend-section">
                <h2>%s</h2>
                <div class="trend-summary">
                    <p>Average Execution Time: %.2f seconds</p>
                    <p>Success Rate: %.1f%%</p>
                    <p class="%s">Trend: %s</p>
                </div>
                <div class="chart-container">
                    <canvas id="%s"></canvas>
                </div>
                <script>
                    new Chart(document.getElementById('%s'), {
                        type: 'line',
                        data: {
                            labels: %s,
                            datasets: [{
                                label: 'Execution Time',
                                data: %s,
                                borderColor: '%s',
                                backgroundColor: '%s',
                                tension: 0.1
                            }]
                        },
                        options: commonOptions
                    });
                </script>
            </div>
            """,
            trend.testName(),
            trend.averageExecutionTime(),
            trend.successRate(),
            trendClass,
            trend.trend(),
            chartId,
            chartId,
            formatDateLabels(trend.timestamps()),
            formatExecutionTimes(trend.executionTimes()),
            getTrendColor(trend.trend()),
            getTrendBackgroundColor(trend.trend())
        ));

        return section.toString();
    }

    private static String getTrendClass(String trend) {
        if (trend.contains("improving")) return "improving";
        if (trend.contains("degrading")) return "degrading";
        return "stable";
    }

    private static String getTrendColor(String trend) {
        if (trend.contains("improving")) return "rgb(75, 192, 75)";
        if (trend.contains("degrading")) return "rgb(192, 75, 75)";
        return "rgb(75, 75, 192)";
    }

    private static String getTrendBackgroundColor(String trend) {
        if (trend.contains("improving")) return "rgba(75, 192, 75, 0.1)";
        if (trend.contains("degrading")) return "rgba(192, 75, 75, 0.1)";
        return "rgba(75, 75, 192, 0.1)";
    }

    private static String formatDateLabels(List<LocalDateTime> timestamps) {
        return timestamps.stream()
            .map(ts -> "'" + ts.format(CHART_DATE_FORMAT) + "'")
            .collect(Collectors.joining(", ", "[", "]"));
    }

    private static String formatExecutionTimes(List<Double> executionTimes) {
        return executionTimes.stream()
            .map(Object::toString)
            .collect(Collectors.joining(", ", "[", "]"));
    }
}
