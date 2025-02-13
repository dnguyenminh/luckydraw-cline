package vn.com.fecredit.app.mapper;

import org.springframework.stereotype.Component;
import vn.com.fecredit.app.dto.SpinHistoryDTO;
import vn.com.fecredit.app.dto.SimpleParticipantDTO;
import vn.com.fecredit.app.dto.SimpleRewardDTO;
import vn.com.fecredit.app.model.SpinHistory;

@Component
public class SpinHistoryMapper {

    public SpinHistoryDTO toDTO(SpinHistory spinHistory) {
        if (spinHistory == null) {
            return null;
        }

        return SpinHistoryDTO.builder()
                .id(spinHistory.getId())
                .participant(SimpleParticipantDTO.builder()
                        .id(spinHistory.getParticipant().getId())
                        .fullName(spinHistory.getParticipant().getFullName())
                        .build())
                .reward(spinHistory.getReward() != null ? 
                        SimpleRewardDTO.builder()
                        .id(spinHistory.getReward().getId())
                        .name(spinHistory.getReward().getName())
                        .build() : null)
                .spinTime(spinHistory.getSpinTime())
                .remainingSpins(spinHistory.getRemainingSpins())
                .currentMultiplier(spinHistory.getCurrentMultiplier())
                .isGoldenHour(spinHistory.getIsGoldenHour())
                .won(spinHistory.getWon())
                .build();
    }
}