package vn.com.fecredit.app.service;

import org.springframework.data.domain.Page;
import vn.com.fecredit.app.dto.common.PageRequest;
import vn.com.fecredit.app.dto.common.SearchRequest;
import vn.com.fecredit.app.dto.GoldenHourDTO;
import vn.com.fecredit.app.entity.GoldenHour;
import vn.com.fecredit.app.model.CreateGoldenHourRequest;
import vn.com.fecredit.app.model.UpdateGoldenHourRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface GoldenHourService {

    /**
     * Create new golden hour
     */
    GoldenHourDTO createGoldenHour(CreateGoldenHourRequest request);

    /**
     * Update golden hour
     */
    GoldenHourDTO updateGoldenHour(Long id, UpdateGoldenHourRequest request);

    /**
     * Get golden hour by ID
     */
    GoldenHourDTO getGoldenHourById(Long id);

    /**
     * Get golden hour entity by ID
     */
    Optional<GoldenHour> findById(Long id);

    /**
     * Get all golden hours
     */
    List<GoldenHourDTO> getAllGoldenHours();

    /**
     * Get all active golden hours
     */
    List<GoldenHourDTO> getActiveGoldenHours();

    /**
     * Get paginated golden hours
     */
    Page<GoldenHourDTO> getGoldenHours(PageRequest pageRequest);

    /**
     * Search golden hours
     */
    Page<GoldenHourDTO> searchGoldenHours(SearchRequest searchRequest);

    /**
     * Get golden hours by event
     */
    List<GoldenHourDTO> getGoldenHoursByEvent(Long eventId);

    /**
     * Get current golden hours
     */
    List<GoldenHourDTO> getCurrentGoldenHours();

    /**
     * Get upcoming golden hours
     */
    List<GoldenHourDTO> getUpcomingGoldenHours();

    /**
     * Delete golden hour
     */
    void deleteGoldenHour(Long id);

    /**
     * Start golden hour
     */
    void startGoldenHour(Long id);

    /**
     * End golden hour
     */
    void endGoldenHour(Long id);

    /**
     * Update golden hour status
     */
    void updateGoldenHourStatus(Long id, String status);

    /**
     * Update golden hour time range
     */
    void updateGoldenHourTimeRange(Long id, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * Update golden hour multipliers
     */
    void updateGoldenHourMultipliers(Long id, Map<String, Double> multipliers);

    /**
     * Check if golden hour is active
     */
    boolean isGoldenHourActive(Long id);

    /**
     * Check if current time is in golden hour
     */
    boolean isInGoldenHour(Long eventId);

    /**
     * Get active golden hour for event
     */
    Optional<GoldenHour> getActiveGoldenHour(Long eventId);

    /**
     * Get golden hour multiplier for reward type
     */
    double getMultiplierForRewardType(Long goldenHourId, String rewardType);

    /**
     * Get golden hour win rates
     */
    Map<String, Double> getGoldenHourWinRates(Long id);

    /**
     * Get golden hour statistics
     */
    GoldenHourStatistics getGoldenHourStatistics(Long id);

    /**
     * Schedule golden hour
     */
    void scheduleGoldenHour(CreateGoldenHourRequest request);

    /**
     * Cancel scheduled golden hour
     */
    void cancelGoldenHour(Long id);

    /**
     * Get scheduled golden hours
     */
    List<GoldenHourDTO> getScheduledGoldenHours();

    /**
     * Count golden hours
     */
    long countGoldenHours();

    /**
     * Count active golden hours
     */
    long countActiveGoldenHours();

    /**
     * Golden hour statistics data class
     */
    @lombok.Data
    class GoldenHourStatistics {
        private final long totalSpins;
        private final long totalWins;
        private final Map<String, Integer> rewardTypeDistribution;
        private final double averageWinRate;
        private final Map<String, Double> rewardTypeWinRates;
    }
}
