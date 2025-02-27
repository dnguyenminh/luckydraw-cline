package vn.com.fecredit.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpinResultDTO {

    private Long id;
    private Long eventId;
    private Long participantId;
    private Long rewardId;
    
    @Builder.Default
    private Boolean won = false;
    
    @Builder.Default
    private Boolean isGoldenHour = false;
    
    @Builder.Default
    private Double multiplier = 1.0;
    
    private Long remainingSpins;
    private String location;
    private SpinStatus status;
    private String message;
    private LocalDateTime timestamp;

    // Reward details if won
    private String rewardName;
    private String rewardCode;
    private Double rewardValue;
    private String rewardImageUrl;

    // Spin statistics
    private SpinHistoryStats stats;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SpinHistoryStats {
        private Long totalSpins;
        private Long totalWins;
        private Long dailySpins;
        private Long dailyWins;
        private Long weeklySpins;
        private Long weeklyWins;
        private Long monthlySpins;
        private Long monthlyWins;
        private LocalDateTime lastSpinTime;
        private LocalDateTime nextEligibleTime;
    }

    public enum SpinStatus {
        SUCCESS,
        INELIGIBLE,
        NO_REMAINING_SPINS,
        REWARD_UNAVAILABLE,
        SYSTEM_ERROR,
        QUOTA_EXCEEDED,
        TIME_CONSTRAINT,
        LOCATION_INVALID
    }

    // Helper methods
    public boolean isSuccessful() {
        return SpinStatus.SUCCESS.equals(status);
    }

    public boolean isWin() {
        return won && isSuccessful();
    }

    public boolean isGoldenHourWin() {
        return isGoldenHour && isWin();
    }

    public Double getEffectiveRewardValue() {
        if (!isWin()) return 0.0;
        return rewardValue * multiplier;
    }

    public boolean canSpinAgain() {
        return remainingSpins > 0 && isSuccessful();
    }

    public boolean needsCooldown() {
        return stats != null && stats.getNextEligibleTime() != null 
            && LocalDateTime.now().isBefore(stats.getNextEligibleTime());
    }

    public Long getCooldownSeconds() {
        if (!needsCooldown() || stats == null || stats.getNextEligibleTime() == null) {
            return 0L;
        }
        return java.time.Duration.between(
            LocalDateTime.now(), 
            stats.getNextEligibleTime()
        ).getSeconds();
    }
}
