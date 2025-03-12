package vn.com.fecredit.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Generic API response wrapper for standardized response format.
 * @param <T> The type of data contained in the response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private String errorCode;
    
    /**
     * Creates a successful response with data.
     * 
     * @param <T> The type of data
     * @param data The response data
     * @param message The success message
     * @return A successful ApiResponse containing the data
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .build();
    }
    
    /**
     * Creates a successful response with data and default message.
     * 
     * @param <T> The type of data
     * @param data The response data
     * @return A successful ApiResponse containing the data
     */
    public static <T> ApiResponse<T> success(T data) {
        return success(data, "Operation successful");
    }
    
    /**
     * Creates an error response.
     * 
     * @param <T> The type of data (usually Void)
     * @param message The error message
     * @param errorCode The error code
     * @return An error ApiResponse
     */
    public static <T> ApiResponse<T> error(String message, String errorCode) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .errorCode(errorCode)
                .build();
    }
    
    /**
     * Creates an error response with only a message.
     * 
     * @param <T> The type of data (usually Void)
     * @param message The error message
     * @return An error ApiResponse
     */
    public static <T> ApiResponse<T> error(String message) {
        return error(message, null);
    }
}