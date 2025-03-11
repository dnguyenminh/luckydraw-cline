package vn.com.fecredit.app.service;

import vn.com.fecredit.app.dto.SpinDTO.SpinResultResponse;
import java.util.List;
import java.util.Map;

public interface RewardSelectionService {

    SpinResultResponse selectRandomReward(Long eventId, Long participantId, Long locationId);
    
    Map<String, Double> calculateRewardProbabilities(Long eventId);
    
    Map<String, Integer> getRewardDistribution(Long eventId);
    
    List<SpinResultResponse> getWinningHistory(Long eventId, Long locationId);
    
    Map<String, Double> getWinRatesByLocation(Long eventId);
    
    Map<String, Double> getWinRatesByReward(Long eventId);
    
    Map<String, Object> getLocationStatistics(Long eventId, Long locationId);
    
    Map<String, Double> recalculateProbabilities(Long eventId);
    
    void validateSpinEligibility(Long eventId, Long participantId, Long locationId);
}
