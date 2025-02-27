package vn.com.fecredit.app.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Container class for SpinHistory related DTOs
 */
public class SpinHistoryDTO {
    
    private SpinHistoryDTO() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRequest {
        @NotNull
        private Long eventId;

        @NotNull
        private Long participantId;
        
        private Long eventLocationId;
        private Long rewardId;
        private LocalDateTime spinTime;
        private Double winProbability;
        private Double probabilityMultiplier;
        private boolean goldenHourActive;
        private Double goldenHourMultiplier;
        private String notes;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SpinResponse {
        private Long id;
        private Long eventId;
        private String eventName;
        private Long participantId;
        private String participantName;
        private Long eventLocationId;
        private String eventLocationName;
        private String eventLocationProvince;
        private Long rewardId;
        private String rewardName;
        private LocalDateTime spinTime;
        private LocalDateTime spinDate;
        private boolean win;
        private Double winProbability;
        private Double finalProbability;
        private boolean goldenHourActive;
        private Double goldenHourMultiplier;
        private Double probabilityMultiplier;
        private String notes;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SpinSummary {
        private Long id;
        private String eventName;
        private String participantName;
        private String eventLocationName;
        private String rewardName;
        private LocalDateTime spinTime;
        private boolean win;
        private Double finalProbability;
        private boolean goldenHourActive;
    }

    @Data
    @Builder 
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SpinStats {
        private Long eventId;
        private Long participantId;
        private int totalSpins;
        private int totalWins;
        private double winRate;
        private Double averageProbability;
        private Double averageFinalProbability;
        private int goldenHourSpins;
        private Double averageGoldenHourMultiplier;
        private LocalDateTime firstSpinTime;
        private LocalDateTime lastSpinTime;
        private LocalDateTime lastWinTime;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SpinValidation {
        private boolean canSpin;
        private boolean withinEventPeriod;
        private boolean hasRemainingSpins;
        private boolean withinDailyLimit;
        private boolean participantActive;
        private boolean rewardsAvailable;
        private String validationMessage;
    }
}
