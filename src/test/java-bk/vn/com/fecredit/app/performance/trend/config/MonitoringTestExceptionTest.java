package vn.com.fecredit.app.performance.trend.config;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MonitoringTestExceptionTest {

    @Test
    void shouldCreateExceptionWithMessage() {
        String message = "Test error message";
        MonitoringTestException exception = new MonitoringTestException(message);
        
        assertEquals(message, exception.getMessage());
        assertNull(exception.getCause());
    }

    @Test
    void shouldCreateExceptionWithCause() {
        RuntimeException cause = new RuntimeException("Original error");
        MonitoringTestException exception = new MonitoringTestException(cause);
        
        assertNotNull(exception.getMessage());
        assertSame(cause, exception.getCause());
    }

    @Test
    void shouldCreateExceptionWithMessageAndCause() {
        String message = "Test error message";
        RuntimeException cause = new RuntimeException("Original error");
        MonitoringTestException exception = new MonitoringTestException(message, cause);
        
        assertTrue(exception.getMessage().contains(message));
        assertTrue(exception.getMessage().contains("Original error"));
        assertSame(cause, exception.getCause());
    }

    @Test
    void shouldWrapExceptionPreservingType() {
        MonitoringTestException original = new MonitoringTestException("Original");
        MonitoringTestException wrapped = MonitoringTestException.wrap(original);
        
        assertSame(original, wrapped, "Should return same instance if already correct type");
    }

    @Test
    void shouldWrapOtherExceptionTypes() {
        RuntimeException original = new RuntimeException("Original");
        MonitoringTestException wrapped = MonitoringTestException.wrap(original);
        
        assertNotSame(original, wrapped, "Should create new instance for different type");
        assertSame(original, wrapped.getCause(), "Should preserve original as cause");
    }

    @Test
    void shouldWrapWithCustomMessage() {
        RuntimeException original = new RuntimeException("Original");
        String message = "Custom message";
        MonitoringTestException wrapped = MonitoringTestException.wrap(message, original);
        
        assertTrue(wrapped.getMessage().contains(message));
        assertTrue(wrapped.getMessage().contains("Original"));
        assertSame(original, wrapped.getCause());
    }

    @Test
    void shouldWrapIfNeededForNonRuntimeExceptions() {
        Exception original = new Exception("Original");
        RuntimeException wrapped = MonitoringTestException.wrapIfNeeded(original);
        
        assertTrue(wrapped instanceof MonitoringTestException);
        assertSame(original, wrapped.getCause());
    }

    @Test
    void shouldNotWrapExistingRuntimeExceptions() {
        RuntimeException original = new RuntimeException("Original");
        RuntimeException result = MonitoringTestException.wrapIfNeeded(original);
        
        assertSame(original, result, "Should not wrap existing RuntimeExceptions");
    }

    @Test
    void shouldOptimizeStackTraceHandling() {
        MonitoringTestException exception = new MonitoringTestException("Test");
        assertSame(exception, exception.fillInStackTrace(), 
            "fillInStackTrace should return this for optimization");
    }

    @Test
    void shouldHandleNullCauseGracefully() {
        MonitoringTestException exception = new MonitoringTestException("Test");
        assertEquals("Test", exception.getMessage(), 
            "Should handle null cause without including 'Caused by' text");
    }

    @Test
    void shouldCombineMessagesWithCause() {
        Exception cause = new Exception("Cause message");
        MonitoringTestException exception = new MonitoringTestException("Test message", cause);
        
        String message = exception.getMessage();
        assertTrue(message.contains("Test message"));
        assertTrue(message.contains("Cause message"));
        assertTrue(message.contains("Caused by:"));
    }
}
