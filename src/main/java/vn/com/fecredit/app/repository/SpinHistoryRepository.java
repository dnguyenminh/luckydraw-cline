package vn.com.fecredit.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.com.fecredit.app.entity.SpinHistory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SpinHistoryRepository extends JpaRepository<SpinHistory, Long> {

    List<SpinHistory> findByParticipantId(Long participantId);
    
    List<SpinHistory> findByEventId(Long eventId);
    
    List<SpinHistory> findByEventLocationId(Long locationId);
    
    List<SpinHistory> findByRewardId(Long rewardId);
    
    List<SpinHistory> findByGoldenHourId(Long goldenHourId);

    @Query("SELECT sh FROM SpinHistory sh WHERE sh.participant.id = :participantId " +
           "AND sh.spinTime BETWEEN :startTime AND :endTime")
    List<SpinHistory> findByParticipantIdAndTimeRange(
            Long participantId, 
            LocalDateTime startTime, 
            LocalDateTime endTime
    );

    @Query("SELECT COUNT(sh) FROM SpinHistory sh WHERE sh.participant.id = :participantId " +
           "AND sh.spinTime BETWEEN :startTime AND :endTime")
    long countSpinsByParticipantIdAndTimeRange(
            Long participantId,
            LocalDateTime startTime,
            LocalDateTime endTime
    );

    @Query("SELECT sh FROM SpinHistory sh WHERE sh.eventLocation.id = :locationId " +
           "AND sh.spinTime BETWEEN :startTime AND :endTime")
    List<SpinHistory> findByLocationIdAndTimeRange(
            Long locationId,
            LocalDateTime startTime,
            LocalDateTime endTime
    );

    @Query("SELECT sh FROM SpinHistory sh WHERE sh.event.id = :eventId " +
           "AND sh.isWin = true ORDER BY sh.spinTime DESC")
    List<SpinHistory> findWinningSpinsByEventId(Long eventId);

    @Query("SELECT sh FROM SpinHistory sh WHERE sh.participant.id = :participantId " +
           "ORDER BY sh.spinTime DESC")
    List<SpinHistory> findRecentSpinsByParticipantId(Long participantId);

    Optional<SpinHistory> findFirstByParticipantIdOrderBySpinTimeDesc(Long participantId);

    @Query("SELECT COALESCE(MAX(sh.spinTime), :defaultTime) FROM SpinHistory sh " +
           "WHERE sh.participant.id = :participantId")
    LocalDateTime findLastSpinTime(Long participantId, LocalDateTime defaultTime);
}
