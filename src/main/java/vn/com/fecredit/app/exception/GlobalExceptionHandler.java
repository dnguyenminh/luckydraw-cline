package vn.com.fecredit.app.exception;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Global exception handler for the application.
 * Provides centralized exception handling and consistent error responses.
 * Includes support for:
 * - Validation errors
 * - Authentication and authorization errors
 * - Database errors
 * - File operation errors
 * - General application errors
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    private static final String CACHE_CONTROL_HEADER = "no-cache, no-store, must-revalidate";
    private static final String PRAGMA_HEADER = "no-cache";
    private static final String EXPIRES_HEADER = "0";

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(ResourceNotFoundException ex, WebRequest request) {
        String path = getRequestPath(request);
        setMDCContext(path);
        log.error("Resource not found at path: {}, details: {}", path, ex.getMessage());
        return buildErrorResponse(HttpStatus.NOT_FOUND, "Resource Not Found", ex.getMessage(), path);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex, WebRequest request) {
        String path = getRequestPath(request);
        setMDCContext(path);
        Map<String, String> errors = ex.getBindingResult().getAllErrors().stream()
            .collect(Collectors.toMap(
                error -> ((FieldError) error).getField(),
                error -> error.getDefaultMessage() != null ? error.getDefaultMessage() : "Invalid value",
                (first, second) -> first
            ));

        log.error("Validation failed at path: {}, errors: {}", path, errors);
        return buildErrorResponseWithHeaders(HttpStatus.BAD_REQUEST, "Validation Error", "Invalid request parameters", path, errors);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException ex, WebRequest request) {
        String path = getRequestPath(request);
        setMDCContext(path);
        log.error("Illegal argument at path: {}, error: {}", path, ex.getMessage());
        return buildErrorResponseWithHeaders(HttpStatus.BAD_REQUEST, "Validation Error", ex.getMessage(), path);
    }

    @ExceptionHandler({
        HttpMessageNotReadableException.class,
        MethodArgumentTypeMismatchException.class,
        MissingServletRequestParameterException.class,
        MissingRequestHeaderException.class,
        HttpMediaTypeNotSupportedException.class,
        HttpMediaTypeNotAcceptableException.class
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> handleHttpMessageErrors(Exception ex, WebRequest request) {
        String path = getRequestPath(request);
        setMDCContext(path);
        log.error("HTTP message error at path: {}, error: {}", path, ex.getMessage());
        return buildErrorResponseWithHeaders(HttpStatus.BAD_REQUEST, "Invalid Request", getHttpMessageErrorDetail(ex), path);
    }

    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(AuthenticationException ex, WebRequest request) {
        String path = getRequestPath(request);
        setMDCContext(path);
        String message = getAuthenticationErrorMessage(ex);
        log.error("Authentication failed at path: {}, error: {}", path, ex.getMessage());
        return buildErrorResponseWithHeaders(HttpStatus.UNAUTHORIZED, "Authentication Failed", message, path);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException ex, WebRequest request) {
        String path = getRequestPath(request);
        setMDCContext(path);
        log.error("Access denied at path: {}, error: {}", path, ex.getMessage());
        HttpStatus status = request.getUserPrincipal() == null ? HttpStatus.UNAUTHORIZED : HttpStatus.FORBIDDEN;
        String errorMessage = request.getUserPrincipal() == null ? "Authentication Failed" : "You do not have permission to access this resource";
        return buildErrorResponseWithHeaders(status, "Access Denied", errorMessage, path);
    }

    @ExceptionHandler({
        DataIntegrityViolationException.class,
        OptimisticLockingFailureException.class,
        TransactionSystemException.class,
        BadSqlGrammarException.class,
        SQLException.class
    })
    @ResponseStatus(HttpStatus.CONFLICT)
    public ResponseEntity<ErrorResponse> handleDatabaseException(Exception ex, WebRequest request) {
        String path = getRequestPath(request);
        setMDCContext(path);
        String transactionId = generateTransactionId();
        log.error("Database error [{}] at path: {}, error: {}", transactionId, path, ex.getMessage(), ex);
        return buildErrorResponseWithHeaders(HttpStatus.CONFLICT, "Database Error", 
            "A database error occurred. Please quote reference: " + transactionId, path);
    }

    @ExceptionHandler({
        MaxUploadSizeExceededException.class,
        IOException.class
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> handleFileOperationException(Exception ex, WebRequest request) {
        String path = getRequestPath(request);
        setMDCContext(path);
        log.error("File operation error at path: {}, error: {}", path, ex.getMessage());
        return buildErrorResponseWithHeaders(HttpStatus.BAD_REQUEST, "File Operation Error", 
            getFileOperationErrorMessage(ex), path);
    }

    @ExceptionHandler({
            BusinessException.class
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ErrorResponse> handleBusinessExceptionException(Exception ex, WebRequest request) {
        String path = getRequestPath(request);
        setMDCContext(path);
        log.error("Business exception: {}", ex.getMessage());
        return buildErrorResponseWithHeaders(HttpStatus.BAD_REQUEST, "Business error",
                ex.getMessage(), path);
    }


    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseEntity<ErrorResponse> handleAllUncaughtException(Exception ex, WebRequest request) {
        String path = getRequestPath(request);
        String transactionId = generateTransactionId();
        setMDCContext(path, transactionId);
        log.error("Unexpected error [{}] at path: {}", transactionId, path, ex);
        return buildErrorResponseWithHeaders(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error",
            "An unexpected error occurred. Please quote reference: " + transactionId, path);
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(HttpStatus status, String error, String message, String path) {
        return buildErrorResponse(status, error, message, path, null);
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(HttpStatus status, String error, String message, String path, Map<String, String> errors) {
        ErrorResponse response = ErrorResponse.of(status.value(), error, message, path, errors);
        return ResponseEntity.status(status).body(response);
    }

    private ResponseEntity<ErrorResponse> buildErrorResponseWithHeaders(HttpStatus status, String error, String message, String path) {
        return buildErrorResponseWithHeaders(status, error, message, path, null);
    }

    private ResponseEntity<ErrorResponse> buildErrorResponseWithHeaders(HttpStatus status, String error, String message, String path, Map<String, String> errors) {
        ErrorResponse response = ErrorResponse.of(status.value(), error, message, path, errors);
        return ResponseEntity.status(status)
                .header(HttpHeaders.CACHE_CONTROL, CACHE_CONTROL_HEADER)
                .header(HttpHeaders.PRAGMA, PRAGMA_HEADER)
                .header(HttpHeaders.EXPIRES, EXPIRES_HEADER)
                .body(response);
    }

    private String getRequestPath(WebRequest request) {
        if (request instanceof ServletWebRequest) {
            HttpServletRequest httpRequest = ((ServletWebRequest) request).getRequest();
            return httpRequest.getRequestURI();
        }
        return "unknown";
    }

    private void setMDCContext(String path) {
        setMDCContext(path, null);
    }

    private void setMDCContext(String path, String transactionId) {
        MDC.put("path", path);
        if (transactionId != null) {
            MDC.put("transactionId", transactionId);
        }
    }

    private String generateTransactionId() {
        return "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private String getHttpMessageErrorDetail(Exception ex) {
        if (ex instanceof HttpMessageNotReadableException) {
            return "Invalid request body format";
        } else if (ex instanceof MethodArgumentTypeMismatchException) {
            return "Invalid parameter type";
        } else if (ex instanceof MissingServletRequestParameterException) {
            return "Missing required parameter";
        } else if (ex instanceof MissingRequestHeaderException) {
            return "Missing required header";
        } else if (ex instanceof HttpMediaTypeNotSupportedException) {
            return "Unsupported media type";
        } else if (ex instanceof HttpMediaTypeNotAcceptableException) {
            return "Not acceptable media type";
        }
        return ex.getMessage();
    }

    private String getAuthenticationErrorMessage(Exception ex) {
        if (ex instanceof BadCredentialsException) {
            return "Invalid username or password";
        } else if (ex instanceof DisabledException) {
            return "Account is disabled";
        } else if (ex instanceof LockedException) {
            return "Account is locked";
        }
        return "Authentication failed";
    }

    private String getFileOperationErrorMessage(Exception ex) {
        if (ex instanceof MaxUploadSizeExceededException) {
            return "Uploaded file size exceeds the maximum allowed size";
        }
        return "File operation failed";
    }
}