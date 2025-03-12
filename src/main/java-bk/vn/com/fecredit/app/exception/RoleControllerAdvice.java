package vn.com.fecredit.app.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import lombok.extern.slf4j.Slf4j;
import vn.com.fecredit.app.dto.ApiResponse;

@Slf4j
@ControllerAdvice
public class RoleControllerAdvice {

    @ExceptionHandler(RoleNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleRoleNotFound(RoleNotFoundException ex) {
        log.error("Role not found: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.<Void>builder()
                        .success(false)
                        .message(ex.getMessage())
                        .build());
    }

    @ExceptionHandler(RoleOperationException.class)
    public ResponseEntity<ApiResponse<Object>> handleRoleOperation(RoleOperationException ex) {
        log.error("Role operation failed: {}", ex.getMessage());
        
        ApiResponse.ApiResponseBuilder<Object> response = ApiResponse.builder()
                .success(false)
                .message(ex.getMessage());

        if (ex.getRoleId() != null) {
            response.data("roleId", ex.getRoleId());
        }
        if (ex.getOperation() != null) {
            response.data("operation", ex.getOperation());
        }
        if (ex.getReason() != null) {
            response.data("reason", ex.getReason());
        }

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(response.build());
    }

    @ExceptionHandler(RoleValidationException.class) 
    public ResponseEntity<ApiResponse<Object>> handleRoleValidation(RoleValidationException ex) {
        log.error("Role validation failed: {}", ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.builder()
                        .success(false)
                        .message(ex.getMessage())
                        .data("violations", ex.getViolations())
                        .build());
    }

}
