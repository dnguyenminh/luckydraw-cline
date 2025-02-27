package vn.com.fecredit.app.dto.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BaseResponse {
    private final boolean success;
    private final String message;
    private final String errorCode;
    private final LocalDateTime timestamp;

    protected BaseResponse() {
        this(true, null, null);
    }

    protected BaseResponse(boolean success, String message, String errorCode) {
        this.success = success;
        this.message = message;
        this.errorCode = errorCode;
        this.timestamp = LocalDateTime.now();
    }

    public static BaseResponse success() {
        return new BaseResponse();
    }

    public static BaseResponse success(String message) {
        return new BaseResponse(true, message, null);
    }

    public static BaseResponse error(String message) {
        return new BaseResponse(false, message, null);
    }

    public static BaseResponse error(String message, String errorCode) {
        return new BaseResponse(false, message, errorCode);
    }
}
