package vn.com.fecredit.app.performance.baseline;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class BaselineVisualization {
    
    private static final DateTimeFormatter DATE_FORMAT = 
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public static void generateComparisonChart(
            PerformanceBaseline.BaselineResult comparison,
            List<Double> baselineHistory,
            List<Double> currentHistory,
            Path outputPath) throws IOException {
        
        try (BufferedWriter writer = Files.newBufferedWriter(outputPath, StandardCharsets.UTF_8)) {
            writer.write(generateHtml(comparison, baselineHistory, currentHistory));
        }
    }

    private static String generateHtml(
            PerformanceBaseline.BaselineResult comparison,
            List<Double> baselineHistory,
            List<Double> currentHistory) {
        
        return String.format("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>Performance Baseline Comparison</title>
                <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
                <style>
                    body { font-family: Arial, sans-serif; margin: 40px; }
                    .chart-container { margin: 20px 0; height: 400px; }
                    .comparison-summary {
                        background: #f8f9fa;
                        padding: 20px;
                        border-radius: 5px;
                        margin-bottom: 20px;
                    }
                    .regression { color: red; font-weight: bold; }
                    .improvement { color: green; font-weight: bold; }
                    .stable { color: blue; }
                </style>
            </head>
            <body>
                <h1>Performance Baseline Comparison</h1>
                
                <div class="comparison-summary">
                    <h2>%s</h2>
                    <p>Baseline Timestamp: %s</p>
                    <p>Current Timestamp: %s</p>
                    <p>Baseline Mean: %.2f seconds</p>
                    <p>Current Mean: %.2f seconds</p>
                    <p>Baseline P95: %.2f seconds</p>
                    <p>Current P95: %.2f seconds</p>
                    <p class="%s">Deviation: %.1f%%</p>
                    <p>Analysis: %s</p>
                </div>

                <div class="chart-container">
                    <canvas id="comparisonChart"></canvas>
                </div>

                <script>
                    new Chart(document.getElementById('comparisonChart'), {
                        type: 'line',
                        data: {
                            labels: Array.from({length: Math.max(%d, %d)}, (_, i) => `Sample ${i + 1}`),
                            datasets: [
                                {
                                    label: 'Baseline',
                                    data: %s,
                                    borderColor: 'rgba(54, 162, 235, 1)',
                                    backgroundColor: 'rgba(54, 162, 235, 0.1)',
                                    tension: 0.1
                                },
                                {
                                    label: 'Current',
                                    data: %s,
                                    borderColor: 'rgba(255, 99, 132, 1)',
                                    backgroundColor: 'rgba(255, 99, 132, 0.1)',
                                    tension: 0.1
                                }
                            ]
                        },
                        options: {
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
                                        text: 'Sample Number'
                                    }
                                }
                            },
                            plugins: {
                                title: {
                                    display: true,
                                    text: 'Performance Comparison'
                                },
                                tooltip: {
                                    callbacks: {
                                        label: function(context) {
                                            return `${context.dataset.label}: ${context.parsed.y.toFixed(2)} seconds`;
                                        }
                                    }
                                }
                            }
                        }
                    });

                    // Add distribution chart
                    const baselineStats = calculateStats(%s);
                    const currentStats = calculateStats(%s);
                    
                    new Chart(document.getElementById('distributionChart'), {
                        type: 'bar',
                        data: {
                            labels: ['Min', 'P25', 'Median', 'P75', 'P95', 'Max'],
                            datasets: [
                                {
                                    label: 'Baseline Distribution',
                                    data: [
                                        baselineStats.min,
                                        baselineStats.p25,
                                        baselineStats.median,
                                        baselineStats.p75,
                                        baselineStats.p95,
                                        baselineStats.max
                                    ],
                                    backgroundColor: 'rgba(54, 162, 235, 0.5)'
                                },
                                {
                                    label: 'Current Distribution',
                                    data: [
                                        currentStats.min,
                                        currentStats.p25,
                                        currentStats.median,
                                        currentStats.p75,
                                        currentStats.p95,
                                        currentStats.max
                                    ],
                                    backgroundColor: 'rgba(255, 99, 132, 0.5)'
                                }
                            ]
                        },
                        options: {
                            responsive: true,
                            maintainAspectRatio: false,
                            scales: {
                                y: {
                                    beginAtZero: true,
                                    title: {
                                        display: true,
                                        text: 'Execution Time (seconds)'
                                    }
                                }
                            },
                            plugins: {
                                title: {
                                    display: true,
                                    text: 'Performance Distribution Comparison'
                                }
                            }
                        }
                    });

                    function calculateStats(data) {
                        const sorted = [...data].sort((a, b) => a - b);
                        const n = sorted.length;
                        return {
                            min: sorted[0],
                            p25: sorted[Math.floor(n * 0.25)],
                            median: sorted[Math.floor(n * 0.5)],
                            p75: sorted[Math.floor(n * 0.75)],
                            p95: sorted[Math.floor(n * 0.95)],
                            max: sorted[n - 1]
                        };
                    }
                </script>

                <div class="chart-container">
                    <canvas id="distributionChart"></canvas>
                </div>
            </body>
            </html>
            """,
            comparison.testName(),
            comparison.baselineTimestamp().format(DATE_FORMAT),
            comparison.currentTimestamp().format(DATE_FORMAT),
            comparison.baselineMean(),
            comparison.currentMean(),
            comparison.baselineP95(),
            comparison.currentP95(),
            getDeviationClass(comparison.deviation()),
            comparison.deviation(),
            comparison.analysis(),
            baselineHistory.size(),
            currentHistory.size(),
            baselineHistory,
            currentHistory,
            baselineHistory,
            currentHistory
        );
    }

    private static String getDeviationClass(double deviation) {
        if (Math.abs(deviation) < 5) return "stable";
        return deviation > 0 ? "regression" : "improvement";
    }
}
