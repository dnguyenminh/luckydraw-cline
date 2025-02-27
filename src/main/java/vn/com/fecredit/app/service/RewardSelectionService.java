package vn.com.fecredit.app.service;

import vn.com.fecredit.app.dto.RewardDTO;
import vn.com.fecredit.app.entity.Event;
import vn.com.fecredit.app.entity.Participant;
import vn.com.fecredit.app.entity.Reward;
import vn.com.fecredit.app.model.SpinResultResponse;

import java.util.List;
import java.util.Map;

public interface RewardSelectionService {

    /**
     * Select a reward for participant in event
     */
    SpinResultResponse selectReward(Long eventId, Long participantId);

    /**
     * Select reward based on probabilities
     */
    Reward selectRewardByProbability(List<Reward> rewards);

    /**
     * Get available rewards for event and participant
     */
    List<Reward> getAvailableRewards(Long eventId, Long participantId);

    /**
     * Calculate reward probabilities
     */
    Map<Reward, Double> calculateProbabilities(List<Reward> rewards);

    /**
     * Adjust probabilities based on factors
     */
    Map<Reward, Double> adjustProbabilities(Map<Reward, Double> probabilities, 
                                          Event event, 
                                          Participant participant);

    /**
     * Check if participant can receive reward
     */
    boolean canReceiveReward(Long eventId, Long participantId, Long rewardId);

    /**
     * Validate spin attempt
     */
    void validateSpinAttempt(Long eventId, Long participantId);

    /**
     * Process reward selection
     */
    void processRewardSelection(Long eventId, Long participantId, Long rewardId);

    /**
     * Record spin history
     */
    void recordSpinHistory(Long eventId, Long participantId, Long rewardId);

    /**
     * Update participant statistics
     */
    void updateParticipantStats(Long participantId, Reward reward);

    /**
     * Update event statistics
     */
    void updateEventStats(Long eventId, Reward reward);

    /**
     * Update reward statistics
     */
    void updateRewardStats(Long rewardId);

    /**
     * Get spin result details
     */
    SpinResultResponse getSpinResult(Long spinHistoryId);

    /**
     * Get participant's winning probability
     */
    double getWinningProbability(Long eventId, Long participantId);

    /**
     * Get reward distribution statistics
     */
    Map<String, Object> getRewardDistributionStats(Long eventId);

    /**
     * Get win rate by reward type
     */
    Map<String, Double> getWinRateByRewardType(Long eventId);

    /**
     * Get win rate by time period
     */
    Map<String, Double> getWinRateByTimePeriod(Long eventId);

    /**
     * Check if maximum rewards limit reached
     */
    boolean isMaxRewardsReached(Long eventId);

    /**
     * Check if maximum rewards per type reached
     */
    boolean isMaxRewardsPerTypeReached(Long eventId, String rewardType);

    /**
     * Check if maximum rewards per participant reached
     */
    boolean isMaxRewardsPerParticipantReached(Long eventId, Long participantId);

    /**
     * Apply golden hour rules
     */
    void applyGoldenHourRules(Map<Reward, Double> probabilities, Long eventId);

    /**
     * Check if spin cooldown period passed
     */
    boolean isSpinCooldownPassed(Long participantId);

    /**
     * Reset daily spin counts
     */
    void resetDailySpinCounts();
}
