package vn.com.fecredit.app.monitoring;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import java.io.IOException;
import java.nio.file.*;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for AlertVisualizer functionality including performance, load, error handling and boundary tests
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AlertVisualizerTest {
    private static final int PERFORMANCE_TEST_SIZE = 10_000;
    private static final int CONCURRENT_THREADS = 4;
    private static final Duration LOAD_TEST_DURATION = Duration.ofSeconds(30);

    @TempDir
    Path tempDir;

    private AlertVisualizer visualizer;
    private AlertHistory alertHistory;
    private SystemResourceMonitor monitor;

    @BeforeEach
    void setUp() {
        visualizer = new AlertVisualizer();
        alertHistory = new AlertHistory(tempDir);
        monitor = new SystemResourceMonitor();
        monitor.startMonitoring(Duration.ofSeconds(1));
    }

    @Test
    @Order(1)
    @DisplayName("Should generate valid HTML visualization")
    void shouldGenerateValidVisualization() throws Exception {
        // Setup test data
        generateTestAlerts();
        
        // Generate visualization
        Path outputPath = tempDir.resolve("test-visualization.html");
        visualizer.generateVisualization(alertHistory.getAlertSummary(), outputPath.toString());

        // Verify file exists and is not empty
        assertTrue(Files.exists(outputPath), "Visualization file should exist");
        assertTrue(Files.size(outputPath) > 0, "Visualization file should not be empty");
        
        // Verify content structure
        String content = Files.readString(outputPath);
        assertAll("HTML content validation",
            () -> assertTrue(content.contains("<!DOCTYPE html>"), "Should have HTML declaration"),
            () -> assertTrue(content.contains("<script src='https://cdn.jsdelivr.net/npm/chart.js'>"), 
                "Should include Chart.js"),
            () -> assertTrue(content.contains("<canvas id='severityChart'>"), 
                "Should have severity chart canvas"),
            () -> assertTrue(content.contains("<canvas id='resourceChart'>"), 
                "Should have resource chart canvas"),
            () -> assertTrue(content.contains("<table>"), "Should have data table")
        );
    }

    @Test
    @Order(2)
    @DisplayName("Should handle empty data")
    void shouldHandleEmptyData() throws Exception {
        Path outputPath = tempDir.resolve("empty-visualization.html");
        visualizer.generateVisualization(alertHistory.getAlertSummary(), outputPath.toString());

        String content = Files.readString(outputPath);
        assertAll("Empty data handling",
            () -> assertTrue(content.contains("Total Alerts: 0"), "Should show zero alerts"),
            () -> assertTrue(content.contains("[]"), "Should have empty datasets")
        );
    }

    @Test
    @Order(3)
    @DisplayName("Should render correct statistics")
    void shouldRenderCorrectStatistics() throws Exception {
        // Create test alerts
        ResourceAlert.AlertEvent cpuAlert = createAlertEvent(
            "CPU Alert",
            ResourceAlert.AlertSeverity.WARNING,
            "High CPU usage",
            monitor.getSummary()
        );

        ResourceAlert.AlertEvent memoryAlert = createAlertEvent(
            "Memory Alert",
            ResourceAlert.AlertSeverity.CRITICAL,
            "Critical memory usage",
            monitor.getSummary()
        );

        alertHistory.recordAlert(cpuAlert);
        alertHistory.recordAlert(memoryAlert);

        // Generate and verify visualization
        Path outputPath = tempDir.resolve("stats-visualization.html");
        visualizer.generateVisualization(alertHistory.getAlertSummary(), outputPath.toString());

        String content = Files.readString(outputPath);
        assertAll("Statistics validation",
            () -> assertTrue(content.contains("CPU Alert"), "Should contain CPU alert"),
            () -> assertTrue(content.contains("Memory Alert"), "Should contain memory alert"),
            () -> assertTrue(content.contains("WARNING"), "Should contain warning severity"),
            () -> assertTrue(content.contains("CRITICAL"), "Should contain critical severity")
        );
    }

    @Test
    @Order(4)
    @DisplayName("Should handle special characters")
    void shouldHandleSpecialCharacters() throws Exception {
        alertHistory.recordAlert(createAlertEvent(
            "Alert <script>with</script> special & chars",
            ResourceAlert.AlertSeverity.INFO,
            "Message with <, >, &, and \"quotes\"",
            monitor.getSummary()
        ));

        Path outputPath = tempDir.resolve("special-chars.html");
        visualizer.generateVisualization(alertHistory.getAlertSummary(), outputPath.toString());

        String content = Files.readString(outputPath);
        assertAll("HTML escaping",
            () -> assertFalse(content.contains("<script>"), "Should escape script tags"),
            () -> assertTrue(content.contains("&lt;script&gt;") || 
                           content.contains("&#60;script&#62;"), "Should have escaped characters"),
            () -> assertTrue(content.contains("&quot;") || 
                           content.contains("&#34;"), "Should escape quotes")
        );
    }

    @Test
    @Order(5)
    @DisplayName("Should handle extremely large alert messages")
    void shouldHandleLargeMessages() throws Exception {
        // Create alert with very large message
        StringBuilder largeMessage = new StringBuilder();
        for (int i = 0; i < 100_000; i++) {
            largeMessage.append("Large message content ");
        }

        alertHistory.recordAlert(createAlertEvent(
            "Large Message Alert",
            ResourceAlert.AlertSeverity.WARNING,
            largeMessage.toString(),
            monitor.getSummary()
        ));

        Path outputPath = tempDir.resolve("large-message.html");
        visualizer.generateVisualization(alertHistory.getAlertSummary(), outputPath.toString());

        assertTrue(Files.exists(outputPath), "Should create file despite large message");
        assertTrue(isValidHtml(outputPath), "Should generate valid HTML");
    }

    private void generateTestAlerts() throws Exception {
        // Add variety of alerts
        for (ResourceAlert.AlertSeverity severity : ResourceAlert.AlertSeverity.values()) {
            alertHistory.recordAlert(createAlertEvent(
                "Test Alert - " + severity,
                severity,
                "Test message for " + severity,
                monitor.getSummary()
            ));
            Thread.sleep(100); // Add small delay to get different timestamps
        }
    }

    private void generateLargeDataset(int size) throws Exception {
        ResourceAlert.AlertSeverity[] severities = ResourceAlert.AlertSeverity.values();
        String[] resources = {"CPU", "Memory", "Disk", "Network", "Thread"};
        
        for (int i = 0; i < size; i++) {
            ResourceAlert.AlertSeverity severity = severities[i % severities.length];
            String resource = resources[i % resources.length];
            
            alertHistory.recordAlert(createAlertEvent(
                resource + " Alert #" + i,
                severity,
                "Test message for " + resource,
                monitor.getSummary()
            ));
        }
    }

    private ResourceAlert.AlertEvent createAlertEvent(
            String name, 
            ResourceAlert.AlertSeverity severity,
            String message,
            SystemResourceMonitor.MonitoringSummary summary) {
        return new ResourceAlert.AlertEvent(name, severity, message, summary);
    }

    private boolean isValidHtml(Path path) throws IOException {
        String content = Files.readString(path);
        return content.startsWith("<!DOCTYPE html>") &&
               content.contains("<html") &&
               content.contains("<head>") &&
               content.contains("<body>") &&
               content.contains("</html>");
    }

    @AfterEach
    void cleanup() {
        if (monitor != null) {
            monitor.close();
        }
        visualizer = null;
        alertHistory = null;
        monitor = null;
        System.gc();
    }
}
