package vn.com.fecredit.app.service;

import org.springframework.data.domain.Page;
import vn.com.fecredit.app.dto.common.PageRequest;
import vn.com.fecredit.app.dto.common.SearchRequest;
import vn.com.fecredit.app.entity.SpinHistory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface SpinHistoryService {

    /**
     * Record spin history
     */
    SpinResponse recordSpin(Long eventId, Long participantId, Long rewardId);

    /**
     * Get spin history by ID
     */
    SpinResponse getSpinHistoryById(Long id);

    /**
     * Get spin history entity by ID
     */
    Optional<SpinHistory> findById(Long id);

    /**
     * Get all spin history
     */
    List<SpinResponse> getAllSpinHistory();

    /**
     * Get paginated spin history
     */
    Page<SpinResponse> getSpinHistory(PageRequest pageRequest);

    /**
     * Search spin history
     */
    Page<SpinResponse> searchSpinHistory(SearchRequest searchRequest);

    /**
     * Get spin history by event
     */
    List<SpinResponse> getSpinHistoryByEvent(Long eventId);

    /**
     * Get spin history by participant
     */
    List<SpinResponse> getSpinHistoryByParticipant(Long participantId);

    /**
     * Get spin history by reward
     */
    List<SpinResponse> getSpinHistoryByReward(Long rewardId);

    /**
     * Get spin history by date range
     */
    List<SpinResponse> getSpinHistoryByDateRange(LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Get participant's last spin
     */
    SpinResponse getLastSpin(Long participantId);

    /**
     * Get participant's last winning spin
     */
    SpinResponse getLastWinningSpin(Long participantId);

    /**
     * Get participant's spin count
     */
    int getParticipantSpinCount(Long participantId);

    /**
     * Get participant's daily spin count
     */
    int getParticipantDailySpinCount(Long participantId);

    /**
     * Get participant's winning spins count
     */
    int getParticipantWinningSpinsCount(Long participantId);

    /**
     * Get event spin count
     */
    int getEventSpinCount(Long eventId);

    /**
     * Get event winning spins count
     */
    int getEventWinningSpinsCount(Long eventId);

    /**
     * Get reward spin count
     */
    int getRewardSpinCount(Long rewardId);

    /**
     * Get spin statistics by time period
     */
    Map<String, Integer> getSpinStatisticsByTimePeriod(Long eventId, String period);

    /**
     * Get win rate statistics by time period
     */
    Map<String, Double> getWinRateStatisticsByTimePeriod(Long eventId, String period);

    /**
     * Get reward distribution statistics
     */
    Map<String, Integer> getRewardDistributionStatistics(Long eventId);

    /**
     * Delete spin history
     */
    void deleteSpinHistory(Long id);

    /**
     * Get total spins
     */
    long countTotalSpins();

    /**
     * Get total winning spins
     */
    long countTotalWinningSpins();

    /**
     * Calculate overall win rate
     */
    double calculateOverallWinRate();

    /**
     * Calculate event win rate
     */
    double calculateEventWinRate(Long eventId);

    /**
     * Calculate participant win rate
     */
    double calculateParticipantWinRate(Long participantId);

    /**
     * Calculate reward win rate
     */
    double calculateRewardWinRate(Long rewardId);

    /**
     * Check if can spin
     */
    boolean canSpin(Long participantId, Long eventId);

    /**
     * Get time until next spin
     */
    long getTimeUntilNextSpin(Long participantId);
}
