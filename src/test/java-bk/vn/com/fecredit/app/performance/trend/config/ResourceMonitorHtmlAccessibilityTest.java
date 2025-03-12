package vn.com.fecredit.app.performance.trend.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

class ResourceMonitorHtmlAccessibilityTest {

    private ResourceMonitor monitor;
    private static final Pattern ARIA_PATTERN = Pattern.compile("aria-[a-z]+=\"[^\"]+\"");
    private static final Pattern ROLE_PATTERN = Pattern.compile("role=\"[^\"]+\"");

    @BeforeEach
    void setUp() {
        monitor = new ResourceMonitor();
    }

    @Test
    void shouldIncludeAccessibilityAttributes(@TempDir Path tempDir) throws Exception {
        monitor.updateMetrics();
        Path reportPath = tempDir.resolve("accessibility-test.html");
        ResourceMonitorHtmlReport.generateReport(monitor, reportPath);

        String content = Files.readString(reportPath);

        // Check basic accessibility requirements
        assertBasicAccessibility(content);
        
        // Check ARIA attributes
        assertAriaAttributes(content);
        
        // Check semantic structure
        assertSemanticStructure(content);
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "section", "header", "main", "footer",
        "nav", "article", "aside", "table"
    })
    void shouldUseSemanticElements(String element, @TempDir Path tempDir) throws Exception {
        monitor.updateMetrics();
        Path reportPath = tempDir.resolve("semantic-test.html");
        ResourceMonitorHtmlReport.generateReport(monitor, reportPath);

        String content = Files.readString(reportPath);
        assertTrue(content.contains("<" + element),
            "Should use semantic element: " + element);
    }

    @Test
    void shouldProvideAdequateColorContrast(@TempDir Path tempDir) throws Exception {
        monitor.updateMetrics();
        Path reportPath = tempDir.resolve("contrast-test.html");
        ResourceMonitorHtmlReport.generateReport(monitor, reportPath);

        String content = Files.readString(reportPath);
        
        // Verify CSS contains high-contrast color combinations
        assertTrue(content.contains("background-color: #fff") || 
                  content.contains("background-color: #ffffff"),
            "Should use white background");
        assertTrue(content.contains("color: #000") || 
                  content.contains("color: #333"),
            "Should use dark text color");
    }

    @Test
    void shouldBeKeyboardNavigable(@TempDir Path tempDir) throws Exception {
        monitor.updateMetrics();
        Path reportPath = tempDir.resolve("keyboard-test.html");
        ResourceMonitorHtmlReport.generateReport(monitor, reportPath);

        String content = Files.readString(reportPath);
        
        // Check for tabindex attributes
        assertTrue(content.contains("tabindex=\"0\""),
            "Should have focusable elements");
        
        // Check for keyboard event handlers
        assertTrue(content.contains("keydown") || content.contains("keypress"),
            "Should handle keyboard events");
    }

    @Test
    void shouldProvideTextAlternatives(@TempDir Path tempDir) throws Exception {
        monitor.updateMetrics();
        Path reportPath = tempDir.resolve("alt-text-test.html");
        ResourceMonitorHtmlReport.generateReport(monitor, reportPath);

        String content = Files.readString(reportPath);
        
        // Check for alt attributes on any images or icons
        assertFalse(content.contains("<img") && !content.contains("alt="),
            "Images should have alt text");
        
        // Check for aria-label on interactive elements
        assertTrue(content.contains("aria-label="),
            "Interactive elements should have labels");
    }

    @Test
    void shouldBeResponsive(@TempDir Path tempDir) throws Exception {
        monitor.updateMetrics();
        Path reportPath = tempDir.resolve("responsive-test.html");
        ResourceMonitorHtmlReport.generateReport(monitor, reportPath);

        String content = Files.readString(reportPath);
        
        // Check for viewport meta tag
        assertTrue(content.contains("<meta name=\"viewport\""),
            "Should include viewport meta tag");
        
        // Check for responsive CSS
        assertTrue(content.contains("@media"),
            "Should include media queries");
    }

    private void assertBasicAccessibility(String content) {
        assertTrue(content.contains("lang=\"en\""),
            "Should specify document language");
        assertTrue(content.contains("<title>"),
            "Should include page title");
        assertTrue(content.contains("role=\"main\""),
            "Should mark main content area");
        assertFalse(content.contains("javascript:void(0)"),
            "Should not use javascript: links");
    }

    private void assertAriaAttributes(String content) {
        // Check for ARIA landmarks
        assertTrue(ARIA_PATTERN.matcher(content).find(),
            "Should use ARIA attributes");
        assertTrue(ROLE_PATTERN.matcher(content).find(),
            "Should use ARIA roles");
        
        // Check specific ARIA attributes
        assertTrue(content.contains("aria-label") || 
                  content.contains("aria-labelledby"),
            "Should label sections appropriately");
        assertTrue(content.contains("aria-live"),
            "Should mark dynamic content regions");
    }

    private void assertSemanticStructure(String content) {
        // Check heading hierarchy
        assertTrue(content.contains("<h1"),
            "Should have main heading");
        assertTrue(Pattern.compile("<h[1-6][^>]*>").matcher(content).find(),
            "Should use proper heading structure");
        
        // Check list structure
        assertTrue(content.contains("<ul") || content.contains("<ol"),
            "Should use semantic lists");
        
        // Check table structure
        assertTrue(content.contains("<th") && content.contains("scope="),
            "Tables should have proper headers");
    }
}
