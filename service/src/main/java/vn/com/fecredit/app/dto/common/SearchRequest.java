package vn.com.fecredit.app.dto.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.format.annotation.DateTimeFormat;
import vn.com.fecredit.app.util.StringUtils;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import vn.com.fecredit.app.exception.BusinessException;


@Data
@EqualsAndHashCode(callSuper = true)
public class SearchRequest extends PageRequest {

    private String keyword;
    private Boolean isActive;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime fromDate;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime toDate;

    public SearchRequest() {
        super();
    }

    @JsonIgnore
    public String getNormalizedKeyword() {
        return keyword != null ? StringUtils.normalizeForSearch(keyword) : null;
    }

    @JsonIgnore
    public Map<String, Object> getSearchParams() {
        Map<String, Object> params = new LinkedHashMap<>();
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            params.put("keyword", getNormalizedKeyword());
        }
        
        if (isActive != null) {
            params.put("isActive", isActive);
        }
        
        if (fromDate != null) {
            params.put("fromDate", fromDate);
        }
        
        if (toDate != null) {
            params.put("toDate", toDate);
        }
        
        return params;
    }

    @Override
    protected void validateFields() {
        super.validateFields();

        if (fromDate != null && toDate != null && toDate.isBefore(fromDate)) {
            throw new BusinessException("To date must be after from date");
        }

        // If only one date is provided, throw an error
        if ((fromDate == null && toDate != null) || (fromDate != null && toDate == null)) {
            throw new BusinessException("Both from date and to date must be provided together");
        }

        // Optional: Limit the date range
        if (fromDate != null && toDate != null) {
            long days = java.time.temporal.ChronoUnit.DAYS.between(fromDate, toDate);
            if (days > 365) { // 1 year
                throw new BusinessException("Date range cannot exceed 1 year");
            }
        }
    }

    public static SearchRequest withKeyword(String keyword) {
        SearchRequest request = new SearchRequest();
        request.setKeyword(keyword);
        return request;
    }

    public static SearchRequest withDateRange(LocalDateTime fromDate, LocalDateTime toDate) {
        SearchRequest request = new SearchRequest();
        request.setFromDate(fromDate);
        request.setToDate(toDate);
        return request;
    }

    public static SearchRequest activeOnly() {
        SearchRequest request = new SearchRequest();
        request.setIsActive(true);
        return request;
    }

    public static SearchRequest inactiveOnly() {
        SearchRequest request = new SearchRequest();
        request.setIsActive(false);
        return request;
    }

    @Override
    public String toString() {
        return String.format("SearchRequest(keyword=%s, isActive=%s, fromDate=%s, toDate=%s, page=%d, size=%d, sort=%s %s)",
            keyword, isActive, fromDate, toDate, getPage(), getSize(),
            getSortBy(), getSortDirection());
    }
}
