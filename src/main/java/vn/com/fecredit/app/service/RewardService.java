package vn.com.fecredit.app.service;

import org.springframework.data.domain.Page;
import vn.com.fecredit.app.dto.common.PageRequest;
import vn.com.fecredit.app.dto.common.SearchRequest;
import vn.com.fecredit.app.dto.RewardDTO;
import vn.com.fecredit.app.entity.Reward;
import vn.com.fecredit.app.model.CreateRewardRequest;
import vn.com.fecredit.app.model.UpdateRewardRequest;

import java.util.List;
import java.util.Optional;

public interface RewardService {

    /**
     * Create new reward
     */
    RewardDTO createReward(CreateRewardRequest request);

    /**
     * Update reward
     */
    RewardDTO updateReward(Long id, UpdateRewardRequest request);

    /**
     * Get reward by ID
     */
    RewardDTO getRewardById(Long id);

    /**
     * Get reward by code
     */
    RewardDTO getRewardByCode(String code);

    /**
     * Get reward entity by code
     */
    Optional<Reward> findByCode(String code);

    /**
     * Get all rewards
     */
    List<RewardDTO> getAllRewards();

    /**
     * Get all active rewards
     */
    List<RewardDTO> getActiveRewards();

    /**
     * Get paginated rewards
     */
    Page<RewardDTO> getRewards(PageRequest pageRequest);

    /**
     * Search rewards
     */
    Page<RewardDTO> searchRewards(SearchRequest searchRequest);

    /**
     * Get rewards by event ID
     */
    List<RewardDTO> getRewardsByEventId(Long eventId);

    /**
     * Get rewards by type
     */
    List<RewardDTO> getRewardsByType(String type);

    /**
     * Delete reward
     */
    void deleteReward(Long id);

    /**
     * Update reward status
     */
    void updateRewardStatus(Long id, boolean active);

    /**
     * Update reward quantity
     */
    void updateRewardQuantity(Long id, int quantity);

    /**
     * Add reward quantity
     */
    void addRewardQuantity(Long id, int additionalQuantity);

    /**
     * Deduct reward quantity
     */
    void deductRewardQuantity(Long id, int deductQuantity);

    /**
     * Check if reward is available
     */
    boolean isRewardAvailable(Long id);

    /**
     * Get remaining quantity
     */
    int getRemainingQuantity(Long id);

    /**
     * Get used quantity
     */
    int getUsedQuantity(Long id);

    /**
     * Update reward probability
     */
    void updateRewardProbability(Long id, Double probability);

    /**
     * Update reward value
     */
    void updateRewardValue(Long id, Double value);

    /**
     * Update reward points
     */
    void updateRewardPoints(Long id, Integer points);

    /**
     * Check if reward code exists
     */
    boolean existsByCode(String code);

    /**
     * Count rewards
     */
    long countRewards();

    /**
     * Count active rewards
     */
    long countActiveRewards();

    /**
     * Count rewards by event
     */
    long countRewardsByEvent(Long eventId);

    /**
     * Count rewards by type
     */
    long countRewardsByType(String type);

    /**
     * Get total value of rewards
     */
    double getTotalRewardsValue();

    /**
     * Get total points of rewards
     */
    int getTotalRewardsPoints();

    /**
     * Get reward win rate
     */
    double getRewardWinRate(Long id);

    /**
     * Get available reward types
     */
    List<String> getAvailableRewardTypes();
}
