package vn.com.fecredit.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for spin history statistics.
 * Tracks various metrics about a participant's spin activity.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpinHistoryStats {
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