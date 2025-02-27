package vn.com.fecredit.app.dto.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpinStats {
    private int totalSpins;
    private int totalWins;
    private double winRate;
    private int uniqueParticipants;
    private int newParticipants;
    private double averageSpinsPerParticipant;
}
