package vn.com.fecredit.app.monitoring;

public class PerformanceReportingException extends RuntimeException {
    public PerformanceReportingException(String message) {
        super(message);
    }

    public PerformanceReportingException(String message, Throwable cause) {
        super(message, cause);
    }
}
