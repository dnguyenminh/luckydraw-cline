package vn.com.fecredit.app.performance.trend.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class ResourceMonitorHtmlReportTest {

    private ResourceMonitor monitor;
    private static final int SAMPLE_COUNT = 10;
    private static final Duration SAMPLE_INTERVAL = Duration.ofMillis(100);
    
    @BeforeEach
    void setUp() {
        monitor = new ResourceMonitor();
    }

    @Test
    void shouldGenerateValidHtmlReport(@TempDir Path tempDir) throws Exception {
        // Generate some sample metrics
        for (int i = 0; i < SAMPLE_COUNT; i++) {
            monitor.updateMetrics();
            Thread.sleep(SAMPLE_INTERVAL.toMillis());
        }

        Path reportPath = tempDir.resolve("test-report.html");
        ResourceMonitorHtmlReport.generateReport(monitor, reportPath);

        assertTrue(Files.exists(reportPath), "Report file should be created");
        String content = Files.readString(reportPath);

        // Verify HTML structure
        assertValidHtmlStructure(content);
        
        // Verify required sections
        assertContainsSection(content, "Resource Usage Summary");
        assertContainsSection(content, "CPU Usage");
        assertContainsSection(content, "Memory Usage");
        
        // Verify data presence
        assertTrue(content.contains("<table"), "Should contain data table");
        assertTrue(content.contains("<tr"), "Should contain table rows");
        assertTrue(content.contains("Last Updated:"), "Should show last update time");
    }

    @Test
    void shouldHandleEmptyMetrics(@TempDir Path tempDir) throws Exception {
        Path reportPath = tempDir.resolve("empty-report.html");
        ResourceMonitorHtmlReport.generateReport(monitor, reportPath);

        String content = Files.readString(reportPath);
        assertValidHtmlStructure(content);
        assertTrue(content.contains("0.0%"), "Should show zero CPU usage");
        assertTrue(content.contains("0 B"), "Should show zero memory usage");
    }

    @Test
    void shouldHandleLargeDatasets(@TempDir Path tempDir) throws Exception {
        // Generate large number of metrics
        for (int i = 0; i < 2000; i++) {
            monitor.updateMetrics();
        }

        Path reportPath = tempDir.resolve("large-report.html");
        ResourceMonitorHtmlReport.generateReport(monitor, reportPath);

        String content = Files.readString(reportPath);
        assertTrue(content.length() < 5_000_000, "Report should not be excessively large");
        assertTrue(Files.size(reportPath) < 5_000_000, "File should not be excessively large");
    }

    @Test
    void shouldHandleSpecialCharacters(@TempDir Path tempDir) throws Exception {
        Path reportPath = tempDir.resolve("special-chars-report.html");
        
        // Create some load with special characters in thread names
        Thread specialThread = new Thread(() -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, "Test Thread & <special> chars");
        specialThread.start();
        
        monitor.updateMetrics();
        ResourceMonitorHtmlReport.generateReport(monitor, reportPath);
        
        specialThread.interrupt();
        specialThread.join(1000);

        String content = Files.readString(reportPath);
        assertTrue(content.contains("&amp;"), "Should escape ampersands");
        assertTrue(content.contains("&lt;"), "Should escape less than");
        assertTrue(content.contains("&gt;"), "Should escape greater than");
    }

    @Test
    void shouldHandleInvalidPaths(@TempDir Path tempDir) {
        Path invalidPath = tempDir.resolve("nonexistent").resolve("report.html");
        assertThrows(IOException.class, () -> 
            ResourceMonitorHtmlReport.generateReport(monitor, invalidPath));
    }

    @Test
    void shouldGenerateAccessibleHtml(@TempDir Path tempDir) throws Exception {
        Path reportPath = tempDir.resolve("accessible-report.html");
        monitor.updateMetrics();
        ResourceMonitorHtmlReport.generateReport(monitor, reportPath);

        String content = Files.readString(reportPath);
        assertTrue(content.contains("lang=\"en\""), "Should specify language");
        assertTrue(content.contains("<title>"), "Should include title");
        assertTrue(content.contains("charset=\"UTF-8\""), "Should specify charset");
        assertTrue(content.contains("<th"), "Should use semantic table headers");
    }

    private void assertValidHtmlStructure(String content) {
        assertTrue(content.contains("<!DOCTYPE html>"), "Should include DOCTYPE");
        assertTrue(content.contains("<html"), "Should have html tag");
        assertTrue(content.contains("<head>"), "Should have head section");
        assertTrue(content.contains("<body>"), "Should have body section");
        assertTrue(content.contains("</html>"), "Should close html tag");
        
        // Check proper nesting
        int headOpenIndex = content.indexOf("<head>");
        int headCloseIndex = content.indexOf("</head>");
        int bodyOpenIndex = content.indexOf("<body>");
        int bodyCloseIndex = content.indexOf("</body>");
        
        assertTrue(headOpenIndex < headCloseIndex, "Head tags should be properly nested");
        assertTrue(headCloseIndex < bodyOpenIndex, "Head should come before body");
        assertTrue(bodyOpenIndex < bodyCloseIndex, "Body tags should be properly nested");
    }

    private void assertContainsSection(String content, String sectionTitle) {
        assertTrue(content.contains(sectionTitle), 
            "Should contain section: " + sectionTitle);
        assertTrue(content.contains("<h2"), 
            "Should use proper heading hierarchy");
    }
}
