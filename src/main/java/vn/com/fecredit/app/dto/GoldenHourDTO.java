package vn.com.fecredit.app.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.com.fecredit.app.validation.ValidTimeRange;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoldenHourDTO {
    private Long id;
    private Long eventId;
    private Long rewardId;
    private String name;
    private Integer startHour;
    private Integer endHour;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Double multiplier;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
        if (startTime != null) {
            this.startHour = startTime.getHour();
        }
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
        if (endTime != null) {
            this.endHour = endTime.getHour();
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @ValidTimeRange(message = "End time must be after start time")
    public static class CreateRequest {
        @NotBlank(message = "Name is required")
        private String name;
        
        @Min(value = 0, message = "Start hour must be between 0 and 23")
        @Max(value = 23, message = "Start hour must be between 0 and 23")
        private Integer startHour;
        
        @Min(value = 0, message = "End hour must be between 0 and 23")
        @Max(value = 23, message = "End hour must be between 0 and 23")
        private Integer endHour;
        
        @NotNull(message = "Start time is required")
        private LocalDateTime startTime;
        
        @NotNull(message = "End time is required")
        private LocalDateTime endTime;
        
        @NotNull(message = "Multiplier is required")
        @Positive(message = "Multiplier must be positive")
        private Double multiplier;

        @Builder.Default
        private Boolean isActive = true;

        public void setStartTime(LocalDateTime startTime) {
            this.startTime = startTime;
            if (startTime != null) {
                this.startHour = startTime.getHour();
            }
        }

        public void setEndTime(LocalDateTime endTime) {
            this.endTime = endTime;
            if (endTime != null) {
                this.endHour = endTime.getHour();
            }
        }
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @ValidTimeRange(message = "End time must be after start time")
    public static class UpdateRequest {
        private String name;
        
        @Min(value = 0, message = "Start hour must be between 0 and 23")
        @Max(value = 23, message = "Start hour must be between 0 and 23")
        private Integer startHour;
        
        @Min(value = 0, message = "End hour must be between 0 and 23")
        @Max(value = 23, message = "End hour must be between 0 and 23")
        private Integer endHour;
        
        private LocalDateTime startTime;
        
        private LocalDateTime endTime;
        
        @Positive(message = "Multiplier must be positive")
        private Double multiplier;
        
        private Boolean isActive;

        public void setStartTime(LocalDateTime startTime) {
            this.startTime = startTime;
            if (startTime != null) {
                this.startHour = startTime.getHour();
            }
        }

        public void setEndTime(LocalDateTime endTime) {
            this.endTime = endTime;
            if (endTime != null) {
                this.endHour = endTime.getHour();
            }
        }
    }
}