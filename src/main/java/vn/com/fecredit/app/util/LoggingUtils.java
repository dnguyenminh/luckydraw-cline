package vn.com.fecredit.app.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

@Slf4j
@UtilityClass
public class LoggingUtils {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
        .findAndRegisterModules(); // This enables Java 8 date/time module

    /**
     * Log method entry with parameters
     */
    public static void logMethodEntry(String methodName, Object... params) {
        StringBuilder message = new StringBuilder()
            .append("Entering method: ")
            .append(methodName);
        
        if (params != null && params.length > 0) {
            message.append(" with parameters: ");
            for (int i = 0; i < params.length; i++) {
                message.append(maskSensitiveData(params[i]));
                if (i < params.length - 1) {
                    message.append(", ");
                }
            }
        }
        
        log.debug(message.toString());
    }

    /**
     * Log method exit with result
     */
    public static void logMethodExit(String methodName, Object result) {
        log.debug("Exiting method: {} with result: {}", methodName, maskSensitiveData(result));
    }

    /**
     * Log exception with context
     */
    public static void logException(String message, Throwable ex, Object... context) {
        StringBuilder logMessage = new StringBuilder()
            .append(message)
            .append(" - Exception: ")
            .append(ex.getClass().getSimpleName())
            .append(": ")
            .append(ex.getMessage());
            
        if (context != null && context.length > 0) {
            logMessage.append(" - Context: ");
            for (Object obj : context) {
                logMessage.append(maskSensitiveData(obj)).append(" ");
            }
        }
        
        log.error(logMessage.toString(), ex);
    }

    /**
     * Create success response
     */
    public static <T> ResponseEntity<ApiResponse<T>> successResponse(T data) {
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    /**
     * Create success response with message
     */
    public static <T> ResponseEntity<ApiResponse<T>> successResponse(T data, String message) {
        return ResponseEntity.ok(ApiResponse.success(data, message));
    }

    /**
     * Create error response
     */
    public static ResponseEntity<ApiResponse<Void>> errorResponse(
            HttpStatus status, String message, String errorCode) {
        return new ResponseEntity<>(ApiResponse.error(message, errorCode), status);
    }

    /**
     * Execute with logging
     */
    public static <T> T executeWithLogging(String operation, Supplier<T> supplier) {
        try {
            log.debug("Starting operation: {}", operation);
            long startTime = System.currentTimeMillis();
            T result = supplier.get();
            long duration = System.currentTimeMillis() - startTime;
            log.debug("Completed operation: {} in {}ms with result: {}", 
                operation, duration, maskSensitiveData(result));
            return result;
        } catch (Exception e) {
            log.error("Failed operation: {} with error: {}", operation, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Log request details
     */
    public static void logRequest(HttpServletRequest request, Object body) {
        StringBuilder message = new StringBuilder()
            .append("Request: ")
            .append(request.getMethod())
            .append(" ")
            .append(request.getRequestURI());

        String queryString = request.getQueryString();
        if (queryString != null) {
            message.append("?").append(maskSensitiveQueryParams(queryString));
        }

        if (body != null) {
            message.append(" - Body: ").append(maskSensitiveData(body));
        }

        message.append(" - Client IP: ").append(getClientIp(request))
               .append(" - User Agent: ").append(request.getHeader("User-Agent"));

        String authHeader = request.getHeader("Authorization");
        if (authHeader != null) {
            message.append(" - Auth: ").append(maskAuthHeader(authHeader));
        }

        log.debug(message.toString());
    }

    /**
     * Get client IP from request
     */
    private static String getClientIp(HttpServletRequest request) {
        String[] headers = {
            "X-Forwarded-For",
            "Proxy-Client-IP",
            "WL-Proxy-Client-IP",
            "HTTP_X_FORWARDED_FOR",
            "HTTP_X_FORWARDED",
            "HTTP_X_CLUSTER_CLIENT_IP",
            "HTTP_CLIENT_IP",
            "HTTP_FORWARDED_FOR",
            "HTTP_FORWARDED",
            "HTTP_VIA",
            "REMOTE_ADDR"
        };

        for (String header : headers) {
            String ip = request.getHeader(header);
            if (ip != null && ip.length() != 0 && !"unknown".equalsIgnoreCase(ip)) {
                return ip.split(",")[0].trim();
            }
        }
        return request.getRemoteAddr();
    }

    /**
     * Mask sensitive data in objects
     */
    private static String maskSensitiveData(Object obj) {
        if (obj == null) {
            return "null";
        }

        try {
            String json = OBJECT_MAPPER.writeValueAsString(obj);
            return maskSensitiveFields(json);
        } catch (Exception e) {
            return obj.toString();
        }
    }

    private static String maskSensitiveFields(String json) {
        return json.replaceAll("\"password\":\"[^\"]*\"", "\"password\":\"*****\"")
                  .replaceAll("\"token\":\"[^\"]*\"", "\"token\":\"*****\"")
                  .replaceAll("\"refreshToken\":\"[^\"]*\"", "\"refreshToken\":\"*****\"")
                  .replaceAll("\"cardNumber\":\"[^\"]*\"", "\"cardNumber\":\"*****\"")
                  .replaceAll("\"phoneNumber\":\"[^\"]*\"", "\"phoneNumber\":\"*****\"")
                  .replaceAll("\"email\":\"[^\"]*\"", "\"email\":\"*****\"")
                  .replaceAll("\"ssn\":\"[^\"]*\"", "\"ssn\":\"*****\"")
                  .replaceAll("\"pin\":\"[^\"]*\"", "\"pin\":\"*****\"");
    }

    private static String maskSensitiveQueryParams(String queryString) {
        return queryString.replaceAll("(password|token|cardNumber|phoneNumber|email|ssn|pin)=[^&]*", "$1=*****");
    }

    private static String maskAuthHeader(String authHeader) {
        if (authHeader.startsWith("Bearer ")) {
            return "Bearer *****";
        }
        return "*****";
    }

    @lombok.Data
    private static class ApiResponse<T> {
        private final boolean success;
        private final T data;
        private final String message;
        private final String errorCode;
        private final LocalDateTime timestamp;

        private ApiResponse(boolean success, T data, String message, String errorCode) {
            this.success = success;
            this.data = data;
            this.message = message;
            this.errorCode = errorCode;
            this.timestamp = DateTimeUtils.now();
        }

        public static <T> ApiResponse<T> success(T data) {
            return new ApiResponse<>(true, data, null, null);
        }

        public static <T> ApiResponse<T> success(T data, String message) {
            return new ApiResponse<>(true, data, message, null);
        }

        public static ApiResponse<Void> error(String message, String errorCode) {
            return new ApiResponse<>(false, null, message, errorCode);
        }
    }
}
