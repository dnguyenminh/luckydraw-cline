package vn.com.fecredit.app.performance.trend.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ResourceMonitoringIntegrationTest {

    private ResourceMonitor monitor;
    private ExecutorService executorService;
    private Path reportPath;
    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        monitor = new ResourceMonitor();
        executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        reportPath = tempDir.resolve("monitoring-report.html");
    }

    @AfterEach
    void tearDown() throws Exception {
        executorService.shutdownNow();
        assertTrue(executorService.awaitTermination(5, TimeUnit.SECONDS));
    }

    @Test
    void shouldGenerateValidReportUnderLoad() throws Exception {
        // Generate system load
        generateSystemLoad();

        // Collect metrics
        IntStream.range(0, 10).forEach(i -> {
            try {
                monitor.updateMetrics();
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new MonitoringTestException("Test interrupted", e);
            }
        });

        // Generate report
        ResourceMonitorHtmlReport.generateReport(monitor, reportPath);

        // Validate report exists and is readable
        assertTrue(Files.exists(reportPath));
        assertTrue(Files.isReadable(reportPath));
        assertTrue(Files.size(reportPath) > 0);

        // Parse and validate HTML content
        Document doc = Jsoup.parse(Files.readString(reportPath));
        validateHtmlStructure(doc);
        validateMetricsContent(doc);
        validateAccessibility(doc);
    }

    @Test
    void shouldHandleConcurrentMetricsCollection() throws Exception {
        int threadCount = Runtime.getRuntime().availableProcessors();
        ExecutorService metricsExecutor = Executors.newFixedThreadPool(threadCount);
        
        try {
            // Collect metrics concurrently
            IntStream.range(0, threadCount).forEach(i -> 
                metricsExecutor.submit(() -> {
                    for (int j = 0; j < 10; j++) {
                        try {
                            monitor.updateMetrics();
                            Thread.sleep(50);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                        }
                    }
                })
            );

            metricsExecutor.shutdown();
            assertTrue(metricsExecutor.awaitTermination(10, TimeUnit.SECONDS));

            // Generate and validate report
            ResourceMonitorHtmlReport.generateReport(monitor, reportPath);
            Document doc = Jsoup.parse(Files.readString(reportPath));
            Elements rows = doc.select("table.metrics tbody tr");
            assertFalse(rows.isEmpty());
            assertTrue(rows.size() <= 1000, "Should respect snapshot limit");

            // Validate data consistency
            for (Element row : rows) {
                Elements cells = row.select("td");
                assertEquals(4, cells.size(), "Each row should have 4 cells");
                assertNotNull(cells.get(0).text(), "Timestamp should not be null");
                assertTrue(cells.get(1).text().contains("%"), "CPU usage should be in percentage");
                assertTrue(cells.get(2).text().matches(".*[KMGT]?B"), "Memory usage should have unit");
                assertTrue(Integer.parseInt(cells.get(3).text()) > 0, "Thread count should be positive");
            }
        } finally {
            metricsExecutor.shutdownNow();
        }
    }

    @Test
    void shouldHandleMemoryPressure() throws Exception {
        // Create memory pressure
        byte[][] memoryHogs = new byte[10][];
        try {
            for (int i = 0; i < 10; i++) {
                memoryHogs[i] = new byte[1024 * 1024]; // 1MB chunks
                monitor.updateMetrics();
            }

            ResourceMonitorHtmlReport.generateReport(monitor, reportPath);
            Document doc = Jsoup.parse(Files.readString(reportPath));
            
            String memoryText = doc.select(".summary dd").get(1).text();
            assertTrue(memoryText.contains("MB"), "Should show increased memory usage");

            // Verify memory metrics are properly formatted
            Elements memoryMetrics = doc.select("table.metrics td:nth-child(3)");
            assertFalse(memoryMetrics.isEmpty());
            memoryMetrics.forEach(metric -> 
                assertTrue(metric.text().matches("\\d+(\\.\\d+)?\\s*[KMGT]?B"),
                    "Memory metric should be properly formatted")
            );
        } finally {
            for (int i = 0; i < memoryHogs.length; i++) {
                memoryHogs[i] = null;
            }
            System.gc();
        }
    }

    @Test
    void shouldHandleResourceCleanup() throws Exception {
        Path tempReport = tempDir.resolve("temp-report.html");
        
        // Generate multiple reports
        for (int i = 0; i < 5; i++) {
            monitor.updateMetrics();
            ResourceMonitorHtmlReport.generateReport(monitor, tempReport);
            
            assertTrue(Files.exists(tempReport));
            assertTrue(Files.size(tempReport) > 0);
            
            Document doc = Jsoup.parse(Files.readString(tempReport));
            assertNotNull(doc.select("html"));
            assertNotNull(doc.select("body"));
            
            // Verify no temporary files are left
            long tempFiles = Files.list(tempDir)
                .filter(p -> p.getFileName().toString().startsWith("report_"))
                .count();
            assertEquals(0, tempFiles, "No temporary files should remain");
        }
    }

    private void generateSystemLoad() {
        // Generate CPU load
        IntStream.range(0, Runtime.getRuntime().availableProcessors()).forEach(i ->
            executorService.submit(() -> {
                long endTime = System.currentTimeMillis() + 1000;
                while (System.currentTimeMillis() < endTime) {
                    Math.pow(Math.random() * 1000, 3);
                }
            })
        );

        // Generate memory load
        byte[][] memoryLoad = new byte[5][];
        try {
            for (int i = 0; i < 5; i++) {
                memoryLoad[i] = new byte[1024 * 1024];
            }
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            for (int i = 0; i < memoryLoad.length; i++) {
                memoryLoad[i] = null;
            }
            System.gc();
        }
    }

    private void validateHtmlStructure(Document doc) {
        Element html = doc.selectFirst("html");
        assertNotNull(html, "Should have html element");
        assertEquals("en", html.attr("lang"), "Should specify language");
        
        assertNotNull(doc.selectFirst("meta[charset]"), "Should specify charset");
        assertNotNull(doc.selectFirst("meta[name=viewport]"), "Should have viewport meta");
        assertNotNull(doc.selectFirst("title"), "Should have title");
        assertNotNull(doc.selectFirst("main#main-content"), "Should have main content");
        
        Elements headings = doc.select("h1, h2, h3, h4, h5, h6");
        assertFalse(headings.isEmpty(), "Should have headings");
        assertEquals("Resource Usage Summary", headings.first().text(),
            "Should have correct main heading");
    }

    private void validateMetricsContent(Document doc) {
        Elements summary = doc.select(".summary");
        assertFalse(summary.isEmpty(), "Should have summary section");
        
        Element table = doc.selectFirst("table.metrics");
        assertNotNull(table, "Should have metrics table");
        
        Elements headers = table.select("th");
        assertEquals(4, headers.size(), "Should have all column headers");
        
        Elements rows = table.select("tbody tr");
        assertFalse(rows.isEmpty(), "Should have metric rows");
        
        // Validate table structure
        assertTrue(table.hasAttr("aria-describedby"), "Table should have description");
        assertTrue(headers.stream().allMatch(h -> h.hasAttr("scope")),
            "Headers should have scope attribute");
    }

    private void validateAccessibility(Document doc) {
        assertNotNull(doc.selectFirst("[role=banner]"), "Should have banner role");
        assertNotNull(doc.selectFirst("[role=main]"), "Should have main role");
        assertNotNull(doc.selectFirst("[role=contentinfo]"), "Should have contentinfo role");
        assertNotNull(doc.selectFirst(".skip-link"), "Should have skip link");
        assertNotNull(doc.selectFirst("[aria-live]"), "Should have live regions");
        
        // Check form controls
        Elements buttons = doc.select("button");
        assertTrue(buttons.stream().allMatch(b -> b.hasAttr("type")),
            "Buttons should have type attribute");
        assertTrue(buttons.stream().allMatch(b -> b.hasAttr("aria-controls")),
            "Buttons should have aria-controls");

        // Check table accessibility
        Elements tables = doc.select("table");
        tables.forEach(table -> {
            assertNotNull(table.attr("aria-describedby"),
                "Tables should have descriptions");
            assertTrue(table.select("th").stream().allMatch(th -> 
                th.hasAttr("scope") && th.hasAttr("role")),
                "Table headers should have scope and role");
        });

        // Check color contrast
        Elements styles = doc.select("style");
        String css = styles.stream()
            .map(Element::html)
            .findFirst()
            .orElse("");
        assertTrue(css.contains("--main-text: #333333"),
            "Should use accessible text color");
    }
}
