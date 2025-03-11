package vn.com.fecredit.app.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.com.fecredit.app.dto.SpinHistoryDTO;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SpinHistoryService {

    // Create operations
    SpinHistoryDTO.Response createSpin(SpinHistoryDTO.CreateRequest request);
    SpinHistoryDTO.Response recordWin(Long spinId, Long rewardId, Integer pointsEarned);
    SpinHistoryDTO.Response recordLoss(Long spinId);
    SpinHistoryDTO.Response finalizeSpin(Long spinId);

    // Read operations
    SpinHistoryDTO.Response getById(Long id);
    Page<SpinHistoryDTO.Response> findAll(Pageable pageable);
    Page<SpinHistoryDTO.Response> findAllByParticipantEvent(Long participantEventId, Pageable pageable);
    List<SpinHistoryDTO.Summary> findAllByEventId(Long eventId);
    List<SpinHistoryDTO.Summary> findAllByEventLocation(Long locationId);
    Optional<SpinHistoryDTO.Response> findLatestSpin(Long participantEventId);
    
    // Statistics operations
    SpinHistoryDTO.Statistics getParticipantEventStatistics(Long participantEventId);
    long countTotalSpins(Long participantEventId);
    long countWinningSpins(Long participantEventId);
    double calculateWinRate(Long participantEventId);
    Integer getTotalPointsEarned(Long participantEventId);
    
    // Time-based queries
    List<SpinHistoryDTO.Summary> findSpinsByTimeRange(
        Long participantEventId,
        LocalDateTime startTime,
        LocalDateTime endTime
    );
    
    List<SpinHistoryDTO.Summary> findTodaysSpins(Long participantEventId);
    List<SpinHistoryDTO.Summary> findRecentSpins(Long participantEventId, int limit);

    // Validation operations
    boolean hasReachedDailyLimit(Long participantEventId);
    boolean hasUnfinalizedSpins(Long participantEventId);
    boolean canSpin(Long participantEventId);
    
    // Administrative operations
    void validateSpinState(Long spinId);
    void resetDailySpins(Long participantEventId);
    void cancelSpin(Long spinId);
}
