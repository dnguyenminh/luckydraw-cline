package vn.com.fecredit.app.monitoring;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import java.nio.file.Path;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PerformanceProfilerVisualizer
 */
class PerformanceProfilerVisualizerTest {

    private PerformanceProfilerVisualizer visualizer;
    private PerformanceProfiler.ProfilerSummary mockSummary;
    private List<PerformanceProfiler.ProfilerSnapshot> mockSnapshots;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        visualizer = new PerformanceProfilerVisualizer();
        mockSnapshots = new ArrayList<>();
        mockSummary = mock(PerformanceProfiler.ProfilerSummary.class);
        
        // Create mock snapshots
        addMockSnapshot("Operation1", 100.0, 50.0, 256.0);
        addMockSnapshot("Operation2", 150.0, 75.0, 512.0);
        addMockSnapshot("Operation3", 200.0, 100.0, 1024.0);

        when(mockSummary.getSnapshots()).thenReturn(mockSnapshots);
        when(mockSummary.getTotalCpuTimeMs()).thenReturn(225.0);
        when(mockSummary.getTotalElapsedTimeMs()).thenReturn(450.0);
        when(mockSummary.getMaxMemoryMB()).thenReturn(1024.0);
    }

    @Test
    @DisplayName("Should generate valid HTML report")
    void shouldGenerateValidHtmlReport() throws Exception {
        Path outputPath = tempDir.resolve("test-report.html");
        visualizer.generateVisualization(mockSummary, outputPath.toString());

        assertTrue(Files.exists(outputPath), "Report file should be created");
        String content = Files.readString(outputPath);
        
        // Verify basic HTML structure
        assertTrue(content.contains("<!DOCTYPE html>"));
        assertTrue(content.contains("<title>Performance Profile Visualization</title>"));
        
        // Verify data inclusion
        assertTrue(content.contains("Operation1"));
        assertTrue(content.contains("Operation2"));
        assertTrue(content.contains("Operation3"));
        
        // Verify charts initialization
        assertTrue(content.contains("timelineChart"));
        assertTrue(content.contains("cpuChart"));
        assertTrue(content.contains("memoryChart"));
    }

    @Test
    @DisplayName("Should handle empty snapshot list")
    void shouldHandleEmptySnapshots() throws Exception {
        when(mockSummary.getSnapshots()).thenReturn(new ArrayList<>());
        Path outputPath = tempDir.resolve("empty-report.html");
        
        assertDoesNotThrow(() -> {
            visualizer.generateVisualization(mockSummary, outputPath.toString());
        });
        
        String content = Files.readString(outputPath);
        assertTrue(content.contains("[]"), "Should contain empty arrays for data");
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "invalid.txt", "report.json", "report"})
    @DisplayName("Should validate file extensions")
    void shouldValidateFileExtensions(String invalidPath) {
        assertThrows(IllegalArgumentException.class, () -> 
            visualizer.generateVisualization(mockSummary, invalidPath));
    }

    @Test
    @DisplayName("Should handle concurrent report generation")
    void shouldHandleConcurrentReports() throws Exception {
        int threadCount = 5;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        List<Exception> exceptions = new ArrayList<>();

        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            executor.submit(() -> {
                try {
                    Path outputPath = tempDir.resolve("concurrent-report-" + index + ".html");
                    visualizer.generateVisualization(mockSummary, outputPath.toString());
                } catch (Exception e) {
                    synchronized (exceptions) {
                        exceptions.add(e);
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        assertTrue(latch.await(10, TimeUnit.SECONDS), "Concurrent operations timed out");
        executor.shutdown();
        assertTrue(exceptions.isEmpty(), "No exceptions should occur during concurrent execution");
    }

    @Test
    @DisplayName("Should handle special characters and HTML injection attempts")
    void shouldHandleSpecialCharactersAndHtmlInjection() throws Exception {
        String maliciousLabel = "<script>alert('xss');</script> & 'quotes' & newlines\n";
        addMockSnapshot(maliciousLabel, 100.0, 50.0, 256.0);
        
        Path outputPath = tempDir.resolve("security-test.html");
        visualizer.generateVisualization(mockSummary, outputPath.toString());
        
        String content = Files.readString(outputPath);
        assertFalse(content.contains("<script>"), "Should escape script tags");
        assertTrue(content.contains("&lt;script&gt;"), "Should convert script tags to HTML entities");
        assertTrue(content.contains("\\'quotes\\'"), "Should escape quotes for JavaScript");
    }

    @Test
    @DisplayName("Should validate metric ranges")
    void shouldValidateMetricRanges() {
        // Test negative values
        assertThrows(IllegalArgumentException.class, () -> 
            addMockSnapshotAndValidate("Negative", -1.0, 50.0, 256.0));

        // Test infinity and NaN
        assertThrows(IllegalArgumentException.class, () -> 
            addMockSnapshotAndValidate("Invalid", Double.POSITIVE_INFINITY, 50.0, 256.0));
        assertThrows(IllegalArgumentException.class, () -> 
            addMockSnapshotAndValidate("Invalid", Double.NaN, 50.0, 256.0));

        // Test extremely large values
        addMockSnapshot("Large", Double.MAX_VALUE / 2, 50.0, 256.0);
        assertFalse(visualizer.getErrors().isEmpty(), "Should warn about large values");
    }

    @Test
    @DisplayName("Should maintain error state between operations")
    void shouldMaintainErrorState() throws Exception {
        // Generate first report with warning
        addMockSnapshot("Warning", Double.MAX_VALUE / 2, 50.0, 256.0);
        Path path1 = tempDir.resolve("warning1.html");
        visualizer.generateVisualization(mockSummary, path1.toString());
        assertFalse(visualizer.getErrors().isEmpty());

        // Clear errors and generate new report
        visualizer.clearErrors();
        assertTrue(visualizer.getErrors().isEmpty());
        
        Path path2 = tempDir.resolve("warning2.html");
        visualizer.generateVisualization(mockSummary, path2.toString());
        assertFalse(visualizer.getErrors().isEmpty());
    }

    @Test
    @DisplayName("Should format statistics correctly")
    void shouldFormatStatisticsCorrectly() throws Exception {
        // Override summary mock with specific values
        when(mockSummary.getTotalCpuTimeMs()).thenReturn(1234.5678);
        when(mockSummary.getTotalElapsedTimeMs()).thenReturn(65432.1);
        when(mockSummary.getMaxMemoryMB()).thenReturn(2048.0);

        Path outputPath = tempDir.resolve("statistics.html");
        visualizer.generateVisualization(mockSummary, outputPath.toString());
        
        String content = Files.readString(outputPath);
        assertTrue(content.contains("1234.57")); // Check number formatting
        assertTrue(content.contains("65.43 seconds")); // Check time unit conversion
        assertTrue(content.contains("2.00 GB")); // Check memory unit conversion
    }

    private void addMockSnapshot(String label, double elapsed, double cpu, double memory) {
        PerformanceProfiler.ProfilerSnapshot snapshot = mock(PerformanceProfiler.ProfilerSnapshot.class);
        when(snapshot.getLabel()).thenReturn(label);
        when(snapshot.getElapsedTimeMs()).thenReturn(elapsed);
        when(snapshot.getCpuTimeMs()).thenReturn(cpu);
        when(snapshot.getMemoryMB()).thenReturn(memory);
        mockSnapshots.add(snapshot);
    }

    private void addMockSnapshotAndValidate(String label, double elapsed, double cpu, double memory) 
            throws Exception {
        addMockSnapshot(label, elapsed, cpu, memory);
        Path outputPath = tempDir.resolve("validation-test.html");
        visualizer.generateVisualization(mockSummary, outputPath.toString());
    }
}
