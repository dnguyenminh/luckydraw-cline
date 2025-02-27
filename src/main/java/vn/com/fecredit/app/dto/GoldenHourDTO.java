package vn.com.fecredit.app.dto;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Set;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.com.fecredit.app.enums.RecurringDay;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoldenHourDTO {
    private Long id;
    private Long eventId;
    private String eventName;
    private Long rewardId;
    private String rewardName;
    private LocalTime startTime;
    private LocalTime endTime;
    private Set<RecurringDay> activeDays;
    private Double probabilityMultiplier;
    private Boolean isActive;
    private Long version;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRequest {
        @NotNull(message = "Event ID is required")
        private Long eventId;

        @NotNull(message = "Reward ID is required")
        private Long rewardId;

        @NotNull(message = "Start time is required")
        private LocalTime startTime;

        @NotNull(message = "End time is required")
        private LocalTime endTime;

        @NotNull(message = "Active days are required")
        private Set<RecurringDay> activeDays;

        @NotNull(message = "Probability multiplier is required")
        @DecimalMin(value = "1.0", message = "Probability multiplier must be at least 1.0")
        @DecimalMax(value = "10.0", message = "Probability multiplier cannot exceed 10.0")
        private Double probabilityMultiplier;

        private Boolean isActive;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateRequest {
        private LocalTime startTime;
        private LocalTime endTime;
        private Set<RecurringDay> activeDays;

        @DecimalMin(value = "1.0", message = "Probability multiplier must be at least 1.0")
        @DecimalMax(value = "10.0", message = "Probability multiplier cannot exceed 10.0")
        private Double probabilityMultiplier;

        private Boolean isActive;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GoldenHourSchedule {
        private Long id;
        private LocalTime startTime;
        private LocalTime endTime;
        private Set<RecurringDay> activeDays;
        private String rewardName;
        private Double baseWinProbability;
        private Double effectiveProbability;
        private Boolean isActiveNow;
        private Long timeUntilNext;
        private LocalDateTime nextOccurrence;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GoldenHourStatistics {
        private Long id;
        private String eventName;
        private String rewardName;
        private Integer totalSpins;
        private Integer winningSpins;
        private Double winRate;
        private Double averageWinProbability;
        private LocalDateTime lastSpinTime;
        private Double effectivenessRatio;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ActiveGoldenHour {
        private Long id;
        private String eventName;
        private String rewardName;
        private LocalTime startTime;
        private LocalTime endTime;
        private Double probabilityMultiplier;
        private Double currentWinProbability;
        private Long remainingTime;
        private Integer participantCount;
    }
}
