package vn.com.fecredit.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateRewardRequest {
    private String name;
    private String description;
    private Integer totalQuantity;
    private Double baseProbability;
    private Integer maxQuantityInPeriod;
    private String applicableProvinces;
    private Boolean isActive;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime limitFromDate;
    private LocalDateTime limitToDate;
    private List<GoldenHourDTO> goldenHours;
    
    public LocalDateTime getStartDateTime() {
        return startDate;
    }
    
    public LocalDateTime getEndDateTime() {
        return endDate;
    }
    
    public LocalDateTime getLimitFromDateTime() {
        return limitFromDate;
    }
    
    public LocalDateTime getLimitToDateTime() {
        return limitToDate;
    }
}
