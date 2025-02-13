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
public class SpinHistoryDTO {
    private Long id;
    private SimpleParticipantDTO participant;
    private SimpleRewardDTO reward;
    private LocalDateTime spinTime;
    private Long remainingSpins;
    private Double currentMultiplier;
    private Boolean isGoldenHour;
    private Boolean won;
}