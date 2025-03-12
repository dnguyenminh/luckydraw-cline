package vn.com.fecredit.app.benchmark;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BenchmarkResultVisualizer {

    private static final String RESULTS_PATH = "build/reports/jmh/results.json";
    private static final String CHARTS_DIR = "build/reports/jmh/charts";
    private static final int CHART_WIDTH = 1200;
    private static final int CHART_HEIGHT = 800;

    @SuppressWarnings("unchecked")
    public static void main(String[] args) throws Exception {
        // Read JMH results
        ObjectMapper mapper = new ObjectMapper();
        String content = new String(Files.readAllBytes(Paths.get(RESULTS_PATH)));
        List<Map<String, Object>> results = mapper.readValue(content, List.class);

        // Create charts directory
        Files.createDirectories(Paths.get(CHARTS_DIR));

        // Create datasets
        DefaultCategoryDataset avgTimeDataset = new DefaultCategoryDataset();
        DefaultCategoryDataset throughputDataset = new DefaultCategoryDataset();
        DefaultCategoryDataset gcTimeDataset = new DefaultCategoryDataset();
        DefaultCategoryDataset memoryDataset = new DefaultCategoryDataset();

        // Process results
        for (Map<String, Object> result : results) {
            String benchmark = (String) result.get("benchmark");
            String methodName = benchmark.substring(benchmark.lastIndexOf('.') + 1);
            String group = getGroupName(methodName);

            Map<String, Object> primaryMetric = (Map<String, Object>) result.get("primaryMetric");
            double score = (double) primaryMetric.get("score");
            String scoreUnit = (String) primaryMetric.get("scoreUnit");
            
            if (scoreUnit.contains("s/op")) {
                avgTimeDataset.addValue(score * 1_000_000, group, methodName); // Convert to microseconds
            } else if (scoreUnit.contains("ops/s")) {
                throughputDataset.addValue(score, group, methodName);
            }

            // Process secondary metrics if available
            Map<String, Object> secondaryMetrics = (Map<String, Object>) result.get("secondaryMetrics");
            if (secondaryMetrics != null) {
                if (secondaryMetrics.containsKey("·gc.time")) {
                    Map<String, Object> gcMetric = (Map<String, Object>) secondaryMetrics.get("·gc.time");
                    gcTimeDataset.addValue((double) gcMetric.get("score"), group, methodName);
                }
                if (secondaryMetrics.containsKey("·mem.alloc.norm")) {
                    Map<String, Object> memMetric = (Map<String, Object>) secondaryMetrics.get("·mem.alloc.norm");
                    memoryDataset.addValue((double) memMetric.get("score"), group, methodName);
                }
            }
        }

        // Create and save charts
        createChart(avgTimeDataset, "Average Execution Time", "Method", "Time (microseconds)", "execution_time.png");
        createChart(throughputDataset, "Throughput", "Method", "Operations/second", "throughput.png");
        createChart(gcTimeDataset, "GC Time", "Method", "Time (ms)", "gc_time.png");
        createChart(memoryDataset, "Memory Allocation", "Method", "Bytes", "memory.png");

        // Generate HTML report
        generateHtmlReport();
    }

    private static String getGroupName(String methodName) {
        if (methodName.startsWith("to")) {
            return "Mapping Operations";
        } else if (methodName.contains("parallel")) {
            return "Parallel Operations";
        } else if (methodName.contains("Statistics")) {
            return "Statistics Operations";
        }
        return "Other Operations";
    }

    private static void createChart(DefaultCategoryDataset dataset, String title, 
                                  String categoryLabel, String valueLabel, String fileName) throws Exception {
        JFreeChart chart = ChartFactory.createBarChart(
            title,
            categoryLabel,
            valueLabel,
            dataset
        );

        ChartUtils.saveChartAsPNG(
            new File(CHARTS_DIR + "/" + fileName),
            chart,
            CHART_WIDTH,
            CHART_HEIGHT
        );
    }

    private static void generateHtmlReport() throws Exception {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n")
            .append("<html><head><title>JMH Benchmark Results</title>")
            .append("<style>")
            .append("body { font-family: Arial, sans-serif; margin: 20px; }")
            .append("h1 { color: #333; }")
            .append("img { max-width: 100%; margin: 20px 0; }")
            .append("</style></head><body>")
            .append("<h1>SpinHistoryMapper Benchmark Results</h1>");

        // Add charts
        for (String chart : List.of("execution_time.png", "throughput.png", "gc_time.png", "memory.png")) {
            html.append(String.format("<h2>%s</h2>", chart.replace(".png", "").replace("_", " ")))
                .append(String.format("<img src='%s' alt='%s'>", chart, chart));
        }

        html.append("</body></html>");

        Files.write(Paths.get(CHARTS_DIR + "/report.html"), html.toString().getBytes());
    }
}
