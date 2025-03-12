package vn.com.fecredit.app.dto.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PageResponse<T> extends BaseResponse {
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

    private final List<T> content;
    private final Pagination pagination;

    private PageResponse(List<T> content, Pagination pagination) {
        super(true, null, null);
        this.content = content;
        this.pagination = pagination;
    }

    private PageResponse(String message, String errorCode) {
        super(false, message, errorCode);
        this.content = null;
        this.pagination = null;
    }

    public static <T> PageResponse<T> of(Page<T> page) {
        return new PageResponse<>(
            page.getContent(),
            new Pagination(
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast(),
                page.hasNext(),
                page.hasPrevious()
            )
        );
    }

    public static <T> PageResponse<T> of(List<T> content, Pagination pagination) {
        return new PageResponse<>(content, pagination);
    }

    public static <T> PageResponse<T> errorResult(String message) {
        return new PageResponse<>(message, null);
    }

    public static <T> PageResponse<T> errorResult(String message, String errorCode) {
        return new PageResponse<>(message, errorCode);
    }

    @Data
    public static class Pagination {
        private final int pageNumber;
        private final int pageSize;
        private final long totalElements;
        private final int totalPages;
        private final boolean first;
        private final boolean last;
        private final boolean hasNext;
        private final boolean hasPrevious;

        public Pagination(
                int pageNumber,
                int pageSize,
                long totalElements,
                int totalPages,
                boolean first,
                boolean last,
                boolean hasNext,
                boolean hasPrevious) {
            this.pageNumber = pageNumber;
            this.pageSize = pageSize;
            this.totalElements = totalElements;
            this.totalPages = totalPages;
            this.first = first;
            this.last = last;
            this.hasNext = hasNext;
            this.hasPrevious = hasPrevious;
        }

        public static Pagination empty() {
            return new Pagination(0, 0, 0, 0, true, true, false, false);
        }
    }

    @Override
    public String toString() {
        return String.format("PageResponse(success=%s, message=%s, errorCode=%s, content.size=%d, pagination=%s)",
            isSuccess(), getMessage(), getErrorCode(),
            content != null ? content.size() : 0,
            pagination);
    }
}
