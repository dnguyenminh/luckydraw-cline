package vn.com.fecredit.app.dto.common;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;
import lombok.EqualsAndHashCode;

import org.springframework.data.domain.Sort;
import vn.com.fecredit.app.exception.BusinessException;

@Data
@EqualsAndHashCode(callSuper = true)
public class PageRequest extends BaseRequest {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 100;

    @Min(0)
    private Integer page;

    @Min(1)
    @Max(MAX_SIZE)
    private Integer size;

    private String sortBy;
    private String sortDirection;

    public PageRequest() {
        this.page = DEFAULT_PAGE;
        this.size = DEFAULT_SIZE;
    }

    @JsonIgnore
    public org.springframework.data.domain.PageRequest toSpringPageRequest() {
        int pageNumber = page != null ? page : DEFAULT_PAGE;
        int pageSize = size != null ? size : DEFAULT_SIZE;

        if (sortBy != null && !sortBy.trim().isEmpty()) {
            Sort.Direction direction = Sort.Direction.fromOptionalString(sortDirection)
                    .orElse(Sort.Direction.ASC);
            return org.springframework.data.domain.PageRequest.of(
                pageNumber, pageSize, direction, sortBy.trim()
            );
        }

        return org.springframework.data.domain.PageRequest.of(pageNumber, pageSize);
    }

    @JsonIgnore
    public org.springframework.data.domain.PageRequest toSpringPageRequest(String defaultSortBy) {
        if (sortBy == null && defaultSortBy != null) {
            sortBy = defaultSortBy;
        }
        return toSpringPageRequest();
    }

    @JsonIgnore
    public org.springframework.data.domain.PageRequest toSpringPageRequest(
            String defaultSortBy, Sort.Direction defaultDirection) {
        if (sortBy == null && defaultSortBy != null) {
            sortBy = defaultSortBy;
        }
        if (sortDirection == null && defaultDirection != null) {
            sortDirection = defaultDirection.name();
        }
        return toSpringPageRequest();
    }

    @Override
    protected void validateFields() {
        if (page != null && page < 0) {
            throw new BusinessException("Page number must be non-negative");
        }

        if (size != null) {
            if (size < 1) {
                throw new BusinessException("Page size must be greater than 0");
            }
            if (size > MAX_SIZE) {
                throw new BusinessException("Page size must not exceed " + MAX_SIZE);
            }
        }

        if (sortDirection != null) {
            try {
                Sort.Direction.valueOf(sortDirection.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new BusinessException("Invalid sort direction. Must be ASC or DESC");
            }
        }
    }

    public static PageRequest of(int page, int size) {
        PageRequest request = new PageRequest();
        request.setPage(page);
        request.setSize(size);
        return request;
    }

    public static PageRequest of(int page, int size, String sortBy) {
        PageRequest request = of(page, size);
        request.setSortBy(sortBy);
        return request;
    }

    public static PageRequest of(int page, int size, String sortBy, String sortDirection) {
        PageRequest request = of(page, size, sortBy);
        request.setSortDirection(sortDirection);
        return request;
    }

    public static PageRequest first() {
        return of(0, DEFAULT_SIZE);
    }

    public static PageRequest first(int size) {
        return of(0, size);
    }
}
