package vn.com.fecredit.app.performance.model;

public record PerformanceTestResult(
    String testName,
    boolean passed,
    String executionTime,
    String metrics,
    String errorMessage
) {}
