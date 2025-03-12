package vn.com.fecredit.app.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.springframework.http.ResponseEntity;

import java.util.Map;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;

    public ApiResponse() {
    }

    public ApiResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    public ApiResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public static <T> ApiResponse<T> success(String message) {
        return new ApiResponse<>(true, message);
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message);
    }

    public static <T> ApiResponse<T> error(String message, T data) {
        return new ApiResponse<>(false, message, data);
    }

    public static ApiResponse<Map<String, Object>> withData(Map<String, Object> data) {
        return new ApiResponse<>(true, null, data);
    }

    public static <T> ResponseEntity<ApiResponse<T>> okResponse(String message) {
        return ResponseEntity.ok(success(message));
    }

    public static <T> ResponseEntity<ApiResponse<T>> okResponse(String message, T data) {
        return ResponseEntity.ok(success(message, data));
    }

    public static <T> ResponseEntity<ApiResponse<T>> errorResponse(String message) {
        return ResponseEntity.badRequest().body(error(message));
    }

    public static <T> ResponseEntity<ApiResponse<T>> errorResponse(String message, T data) {
        return ResponseEntity.badRequest().body(error(message, data));
    }

    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

    public static class Builder<T> {
        private boolean success;
        private String message;
        private T data;

        public Builder<T> success(boolean success) {
            this.success = success;
            return this;
        }

        public Builder<T> message(String message) {
            this.message = message;
            return this;
        }

        public Builder<T> data(T data) {
            this.data = data;
            return this;
        }

        public ApiResponse<T> build() {
            return new ApiResponse<>(success, message, data);
        }

        public ResponseEntity<ApiResponse<T>> buildResponseEntity() {
            return ResponseEntity.ok(build());
        }
    }
}
