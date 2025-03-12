package vn.com.fecredit.app.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import vn.com.fecredit.app.config.ApiResponse;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
    }

    @Test
    void handleEntityNotFound_ShouldReturnNotFoundResponse() {
        // Given
        EntityNotFoundException ex = new EntityNotFoundException("Entity not found");

        // When
        ApiResponse<Void> response = handler.handleEntityNotFound(ex);

        // Then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(response.getMessage()).isEqualTo("Entity not found");
    }

    @Test
    void handleInvalidOperation_ShouldReturnBadRequestResponse() {
        // Given
        InvalidOperationException ex = new InvalidOperationException("Invalid operation");

        // When
        ApiResponse<Void> response = handler.handleInvalidOperation(ex);

        // Then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getMessage()).isEqualTo("Invalid operation");
    }

    @Test
    @SuppressWarnings("unchecked")
    void handleValidationErrors_ShouldReturnValidationErrors() {
        // Given
        MethodArgumentNotValidException ex = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        
        List<FieldError> fieldErrors = List.of(
            new FieldError("object", "field1", "error1"),
            new FieldError("object", "field2", "error2")
        );

        when(ex.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(fieldErrors);

        // When
        ApiResponse<Map<String, String>> response = handler.handleValidationErrors(ex);

        // Then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getMessage()).isEqualTo("Validation failed");
        assertThat(response.getData())
                .containsEntry("field1", "error1")
                .containsEntry("field2", "error2");
    }

    @Test
    void handleConstraintViolation_ShouldReturnValidationErrors() {
        // Given
        Set<ConstraintViolation<?>> violations = new HashSet<>();
        ConstraintViolation<?> violation1 = mock(ConstraintViolation.class);
        ConstraintViolation<?> violation2 = mock(ConstraintViolation.class);

        when(violation1.getPropertyPath()).thenReturn(new TestPropertyPath("field1"));
        when(violation1.getMessage()).thenReturn("error1");
        when(violation2.getPropertyPath()).thenReturn(new TestPropertyPath("field2"));
        when(violation2.getMessage()).thenReturn("error2");

        violations.add(violation1);
        violations.add(violation2);

        ConstraintViolationException ex = new ConstraintViolationException("Validation failed", violations);

        // When
        ApiResponse<Map<String, String>> response = handler.handleConstraintViolation(ex);

        // Then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(response.getMessage()).isEqualTo("Validation failed");
        assertThat(response.getData())
                .containsEntry("field1", "error1")
                .containsEntry("field2", "error2");
    }

    @Test
    void handleAccessDenied_ShouldReturnForbiddenResponse() {
        // Given
        AccessDeniedException ex = new AccessDeniedException("Access denied");

        // When
        ApiResponse<Void> response = handler.handleAccessDenied(ex);

        // Then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.FORBIDDEN.value());
        assertThat(response.getMessage()).isEqualTo("Access denied: insufficient privileges");
    }

    @Test
    void handleAuthentication_ShouldReturnUnauthorizedResponse() {
        // Given
        AuthenticationException ex = new AuthenticationException("Auth failed") {};

        // When
        ApiResponse<Void> response = handler.handleAuthentication(ex);

        // Then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
        assertThat(response.getMessage()).isEqualTo("Authentication failed");
    }

    @Test
    void handleBadCredentials_ShouldReturnUnauthorizedResponse() {
        // Given
        BadCredentialsException ex = new BadCredentialsException("Bad credentials");

        // When
        ApiResponse<Void> response = handler.handleBadCredentials(ex);

        // Then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.UNAUTHORIZED.value());
        assertThat(response.getMessage()).isEqualTo("Invalid username or password");
    }

    @Test
    void handleAllUncaughtException_ShouldReturnServerErrorResponse() {
        // Given
        Exception ex = new RuntimeException("Unexpected error");

        // When
        ApiResponse<Void> response = handler.handleAllUncaughtException(ex, null);

        // Then
        assertThat(response.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(response.getMessage()).isEqualTo("An unexpected error occurred");
    }

    private static class TestPropertyPath implements Path {
        private final String path;

        TestPropertyPath(String path) {
            this.path = path;
        }

        @Override
        public String toString() {
            return path;
        }

        @Override
        public Iterator<Node> iterator() {
            throw new UnsupportedOperationException("Not needed for testing");
        }
    }
}
