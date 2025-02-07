package vn.com.fecredit.app.mapper;

import org.springframework.stereotype.Component;
import vn.com.fecredit.app.dto.SpinHistoryDTO;
import vn.com.fecredit.app.model.SpinHistory;

@Component
public class SpinHistoryMapper {

    public SpinHistoryDTO toDTO(SpinHistory spinHistory) {
        if (spinHistory == null) {
            return null;
        }

        return SpinHistoryDTO.builder()
                .id(spinHistory.getId())
                .participantId(spinHistory.getParticipant().getId())
                .participantName(spinHistory.getParticipant().getName())
                .rewardId(spinHistory.getReward() != null ? spinHistory.getReward().getId() : null)
                .rewardName(spinHistory.getReward() != null ? spinHistory.getReward().getName() : null)
                .spinTime(spinHistory.getSpinTime())
                .remainingSpins(spinHistory.getRemainingSpins())
                .currentMultiplier(spinHistory.getCurrentMultiplier())
                .build();
    }
}