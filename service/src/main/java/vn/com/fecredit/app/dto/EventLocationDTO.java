package vn.com.fecredit.app.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

public class EventLocationDTO {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRequest {
        @NotNull
        private Long eventId;

        @NotNull
        private Long regionId;

        @NotNull
        private Long provinceId;

        @NotBlank
        @Size(max = 100)
        private String name;

        @NotBlank
        @Size(max = 20)
        private String code;

        private String description;
        
        private Integer dailySpinLimit;
        private Integer initialSpins;
        private Double winProbability;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateRequest {
        @Size(max = 100)
        private String name;
        
        @Size(max = 20)
        private String code;
        
        private String description;
        private Integer dailySpinLimit;
        private Integer initialSpins;
        private Double winProbability;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long id;
        private String name;
        private String code;
        private String description;
        private Integer dailySpinLimit;
        private Integer initialSpins;
        private Double winProbability;
        private Integer status;
        
        private Long eventId;
        private String eventName;
        private Long regionId;
        private String regionName;
        private Long provinceId;
        private String provinceName;
        
        private LocalDateTime createdDate;
        private LocalDateTime lastModifiedDate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Summary {
        private Long id;
        private String name;
        private String code;
        private Integer status;
        private String eventName;
        private String regionName;
        private String provinceName;
        private Integer totalParticipants;
        private Integer activeParticipants;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Statistics {
        private Long id;
        private String name;
        private Integer totalSpins;
        private Integer totalWins;
        private Double winRate;
        private Integer uniqueParticipants;
        private Integer averageSpinsPerDay;
        private Integer peakSpinsInDay;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
    }
}
