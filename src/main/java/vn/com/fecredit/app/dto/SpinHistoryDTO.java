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
    private Long participantId;
    private String participantName;
    private Long rewardId;
    private String rewardName;
    private LocalDateTime spinTime;
    private Integer remainingSpins;
    private Double currentMultiplier;
}