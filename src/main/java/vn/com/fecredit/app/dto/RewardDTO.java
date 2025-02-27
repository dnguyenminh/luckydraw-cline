package vn.com.fecredit.app.dto;

import java.time.LocalDateTime;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

public class RewardDTO {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRewardRequest {
        @NotNull
        private Long eventId;

        @NotBlank
        @Size(max = 100)
        private String name;

        @Size(max = 500)
        private String description;

        @NotNull
        private Integer quantity;
        
        private Integer remainingQuantity;
        private Double probability;
        private Boolean active;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateRewardRequest {
        @Size(max = 100)
        private String name;

        @Size(max = 500)
        private String description;

        private Integer quantity;
        private Integer remainingQuantity;
        private Double probability;
        private Boolean active;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RewardResponse {
        private Long id;
        private Long eventId;
        private String eventName;
        private String name;
        private String description;
        private Integer quantity;
        private Integer remainingQuantity;
        private Double probability;
        private Double effectiveProbability;
        private boolean active;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
        private RewardStatistics statistics;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RewardSummary {
        private Long id;
        private String name;
        private Integer remainingQuantity;
        private Double probability;
        private boolean active;
        private LocalDateTime endDate;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RewardStatistics {
        private Integer totalWins;
        private Integer totalSpins;
        private Double actualWinRate;
        private Double theoreticalWinRate;
        private Double averageProbabilityMultiplier;
        private Integer winsToday;
        private Integer winsThisWeek;
        private Integer winsThisMonth;
        private LocalDateTime lastWinDate;
        private Long estimatedRemainingDays;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RewardAvailability {
        private boolean available;
        private boolean active;
        private boolean hasQuantityRemaining;
        private boolean withinValidPeriod;
        private Integer remainingQuantity;
        private LocalDateTime endDate;
        private Double currentProbability;
        private Double baselineProbability;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class WinningProbability {
        private Double baseProbability;
        private Double goldenHourMultiplier;
        private Double locationMultiplier;
        private Double finalProbability;
        private Integer remainingQuantity;
        private Boolean withinGoldenHour;
        private LocalDateTime calculatedAt;
    }
}
