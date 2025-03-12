package vn.com.fecredit.app.performance.trend.config;

public class MonitoringTestException extends RuntimeException {
    
    public MonitoringTestException(String message) {
        super(message);
    }

    public MonitoringTestException(String message, Throwable cause) {
        super(message, cause);
    }

    public MonitoringTestException(Throwable cause) {
        super(cause);
    }

    public static MonitoringTestException wrap(Throwable e) {
        return e instanceof MonitoringTestException ? 
            (MonitoringTestException) e : 
            new MonitoringTestException(e);
    }

    public static MonitoringTestException wrap(String message, Throwable e) {
        return e instanceof MonitoringTestException ? 
            (MonitoringTestException) e : 
            new MonitoringTestException(message, e);
    }

    public static RuntimeException wrapIfNeeded(Throwable e) {
        return e instanceof RuntimeException ? 
            (RuntimeException) e : 
            new MonitoringTestException(e);
    }

    @Override
    public String getMessage() {
        if (getCause() != null) {
            return super.getMessage() + " Caused by: " + getCause().getMessage();
        }
        return super.getMessage();
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;  // Optimize performance by not filling stack trace for test exceptions
    }
}
