package vn.com.fecredit.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Set;

public class EventLocationDTO {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRequest {
        @NotBlank
        private String name;
        
        @NotNull
        @Min(1)
        private Integer dailySpinLimit;
        
        @NotNull
        @DecimalMin("0.0")
        @DecimalMax("1.0")
        private Double winProbabilityMultiplier;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateRequest {
        private String name;
        private Integer dailySpinLimit;
        private Double winProbabilityMultiplier;
        private Boolean active;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long id;
        private String locationName;
        private Integer dailySpinLimit;
        private Double winProbabilityMultiplier;
        private EntityStatus status;
        private LocalDateTime createdDate;
        private LocalDateTime lastModifiedDate;
        private String createdBy;
        private String lastModifiedBy;
        private Long version;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Summary {
        private Long id;
        private String locationName;
        private Integer dailySpinLimit;
        private EntityStatus status;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Statistics {
        private Long totalLocations;
        private Long activeLocations;
        private Map<String, Integer> spinsByLocation;
        private Map<String, Double> winRatesByLocation;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HourlyStats {
        private Integer hour;
        private Long totalSpins;
        private Long winningSpins;
        private Double winRate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LocationStatistics {
        private Long id;
        private String locationName;
        private Long totalSpins;
        private Long winningSpins;
        private Double winRate;
        private Set<HourlyStats> hourlyStats;
    }
}
