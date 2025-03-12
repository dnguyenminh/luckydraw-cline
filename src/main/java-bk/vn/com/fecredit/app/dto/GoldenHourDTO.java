package vn.com.fecredit.app.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class GoldenHourDTO {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRequest {
        @NotBlank(message = "Name is required")
        @Size(min = 3, max = 100, message = "Name must be between 3 and 100 characters")
        private String name;

        @NotNull(message = "Start time is required")
        @Future(message = "Start time must be in the future")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime startTime;

        @NotNull(message = "End time is required")
        @Future(message = "End time must be in the future")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime endTime;

        @NotNull(message = "Probability multiplier is required")
        @DecimalMin(value = "1.0", message = "Probability multiplier must be at least 1.0")
        @DecimalMax(value = "10.0", message = "Probability multiplier cannot exceed 10.0")
        private Double probabilityMultiplier;

        @Size(max = 500, message = "Description cannot exceed 500 characters")
        private String description;
        
        private Set<DayOfWeek> activeDays;
        
        private Long eventId;
        private Long rewardId;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateRequest {
        @Size(min = 3, max = 100, message = "Name must be between 3 and 100 characters")
        private String name;

        @Future(message = "Start time must be in the future")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime startTime;

        @Future(message = "End time must be in the future")
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime endTime;

        @DecimalMin(value = "1.0", message = "Probability multiplier must be at least 1.0")
        @DecimalMax(value = "10.0", message = "Probability multiplier cannot exceed 10.0")
        private Double probabilityMultiplier;

        @Size(max = 500, message = "Description cannot exceed 500 characters")
        private String description;
        
        private Set<DayOfWeek> activeDays;
        private Long rewardId;
        private GoldenHourStatus status;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private Long id;
        private Long eventId;
        private String name;
        
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime startTime;
        
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime endTime;
        
        private Double probabilityMultiplier;
        private String description;
        private Set<DayOfWeek> activeDays;
        private GoldenHourStatus status;
        private boolean active;
        private RewardDTO.Summary reward;
        
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime createdAt;
        
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime updatedAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Summary {
        private Long id;
        private String name;
        private Double probabilityMultiplier;
        
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime startTime;
        
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime endTime;
        
        private Set<DayOfWeek> activeDays;
        private GoldenHourStatus status;
        private boolean active;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Statistics {
        private Long id;
        private String name;
        private int totalSpins;
        private int winningSpins;
        private double winRate;
        private double avgMultiplier;
        private double totalBonus;
        private int participantCount;
        
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime startTime;
        
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime endTime;
        
        private GoldenHourStatus status;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor 
    public static class Schedule {
        private Long id;
        private String name;
        private Set<DayOfWeek> activeDays;
        
        @JsonFormat(pattern = "HH:mm:ss")
        private String startTime;
        
        @JsonFormat(pattern = "HH:mm:ss")
        private String endTime;
        
        private Double probabilityMultiplier;
        private GoldenHourStatus status;
    }
}
