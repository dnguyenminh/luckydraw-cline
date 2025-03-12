package vn.com.fecredit.app.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for logging operations.
 * Provides standardized logging methods for consistent log formatting across the application.
 */
public class LoggingUtils {

    private static final Logger log = LoggerFactory.getLogger(LoggingUtils.class);

    /**
     * Creates a logger for the specified class.
     * 
     * @param clazz The class to create a logger for
     * @return A configured SLF4J Logger instance
     */
    public static Logger getLogger(Class<?> clazz) {
        return LoggerFactory.getLogger(clazz);
    }
    
    /**
     * Formats an entity operation log message.
     * 
     * @param operation The operation being performed (e.g., "create", "update")
     * @param entityType The type of entity (e.g., "User", "Event")
     * @param entityId The identifier of the entity
     * @return A formatted log message
     */
    public static String formatEntityOperation(String operation, String entityType, Object entityId) {
        return String.format("%s %s with id: %s", operation, entityType, entityId);
    }
    
    /**
     * Formats an entity operation log message with additional details.
     * 
     * @param operation The operation being performed (e.g., "create", "update")
     * @param entityType The type of entity (e.g., "User", "Event")
     * @param entityId The identifier of the entity
     * @param details Additional operation details
     * @return A formatted log message
     */
    public static String formatEntityOperation(String operation, String entityType, Object entityId, String details) {
        return String.format("%s %s with id: %s - %s", operation, entityType, entityId, details);
    }
    
    /**
     * Logs method entry with a single parameter.
     * 
     * @param methodName The name of the method being entered
     * @param param The parameter value
     */
    public static <T> void logMethodEntry(String methodName, T param) {
        log.debug("Entering method {} with parameter: {}", methodName, param);
    }
    
    /**
     * Logs method entry with multiple parameters.
     * 
     * @param methodName The name of the method being entered
     * @param params The parameter values
     */
    public static void logMethodEntry(String methodName, Object... params) {
        log.debug("Entering method {} with parameters: {}", methodName, params);
    }
    
    /**
     * Logs method exit with a return value.
     * 
     * @param methodName The name of the method being exited
     * @param result The return value
     */
    public static <T> void logMethodExit(String methodName, T result) {
        log.debug("Exiting method {} with result: {}", methodName, result);
    }
    
    /**
     * Logs method exit without a return value.
     * 
     * @param methodName The name of the method being exited
     */
    public static void logMethodExit(String methodName) {
        log.debug("Exiting method {}", methodName);
    }
}