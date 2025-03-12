package vn.com.fecredit.app.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import java.time.Instant;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RestResponse<T> {
    private String status;
    private String message;
    private T data;
    private Instant timestamp;

    private RestResponse(String status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
        this.timestamp = Instant.now();
    }

    public static <T> RestResponse<T> success(String message) {
        return new RestResponse<>("SUCCESS", message, null);
    }

    public static <T> RestResponse<T> success(String message, T data) {
        return new RestResponse<>("SUCCESS", message, data);
    }

    public static <T> RestResponse<T> error(String message) {
        return new RestResponse<>("ERROR", message, null);
    }

    public static <T> RestResponse<T> error(String message, T data) {
        return new RestResponse<>("ERROR", message, data);
    }
}
