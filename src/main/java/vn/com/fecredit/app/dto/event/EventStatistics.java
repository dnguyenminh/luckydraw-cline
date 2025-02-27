package vn.com.fecredit.app.dto.event;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventStatistics {
    private int totalParticipants;
    private int activeParticipants;
    private int totalSpins;
    private int remainingSpins;
    private double participationRate;
    private double completionRate;
    private int totalLocations;
    private int activeLocations;
    private int totalRewards;
    private int activeRewards;
    private LocalDateTime lastSpinDate;
    private LocalDateTime lastWinDate;
    private SpinStats todayStats;
    private SpinStats weeklyStats;
    private SpinStats monthlyStats;
}
