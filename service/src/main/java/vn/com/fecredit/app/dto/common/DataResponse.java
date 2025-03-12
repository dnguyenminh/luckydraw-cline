package vn.com.fecredit.app.dto.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DataResponse<T> extends BaseResponse {
    // Explicitly add getter methods to ensure they're available
    @Override
    public boolean isSuccess() {
        return super.isSuccess();
    }
    
    @Override
    public String getMessage() {
        return super.getMessage();
    }
    
    @Override
    public String getErrorCode() {
        return super.getErrorCode();
    }
    private final T data;

    private DataResponse(T data) {
        super(true, null, null);
        this.data = data;
    }

    private DataResponse(T data, String message) {
        super(true, message, null);
        this.data = data;
    }

    private DataResponse(boolean success, String message, String errorCode) {
        super(success, message, errorCode);
        this.data = null;
    }

    public static <T> DataResponse<T> of(T data) {
        return new DataResponse<>(data);
    }

    public static <T> DataResponse<T> of(T data, String message) {
        return new DataResponse<>(data, message);
    }

    public static <T> DataResponse<T> errorResult(String message) {
        return new DataResponse<>(false, message, null);
    }

    public static <T> DataResponse<T> errorResult(String message, String errorCode) {
        return new DataResponse<>(false, message, errorCode);
    }

    public boolean hasData() {
        return data != null;
    }

    @Override
    public String toString() {
        return String.format("DataResponse(success=%s, message=%s, errorCode=%s, data=%s)", 
            isSuccess(), getMessage(), getErrorCode(), data);
    }
}
