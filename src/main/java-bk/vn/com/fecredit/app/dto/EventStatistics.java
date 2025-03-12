package vn.com.fecredit.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventStatistics {
    private Long eventId;
    private Integer totalParticipants;
    private Integer totalSpins;
    private Integer totalRewards;
    private Double participationRate;
    private Double spinCompletionRate;
    private Double rewardDistributionRate;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Summary {
        private int totalEvents;
        private int activeEvents;
        private int totalParticipants;
        private int totalSpins;
        private int totalRewards;
        private double averageParticipationRate;
        private double averageSpinCompletionRate;
        private double averageRewardDistributionRate;
    }
}
