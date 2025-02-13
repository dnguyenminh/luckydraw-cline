package vn.com.fecredit.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import vn.com.fecredit.app.model.SpinHistory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@Transactional(readOnly = true)
public interface SpinHistoryRepository extends JpaRepository<SpinHistory, Long> {

    List<SpinHistory> findByParticipantId(Long participantId);

    @Query("SELECT s FROM SpinHistory s " +
           "WHERE s.participant.id = :participantId " +
           "AND s.spinTime >= :startTime " +
           "AND s.spinTime < :endTime")
    List<SpinHistory> findByParticipantIdAndTimeRange(
            @Param("participantId") Long participantId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    @Query("SELECT COUNT(s) FROM SpinHistory s " +
           "WHERE s.participant.id = :participantId " +
           "AND s.spinTime >= :startTime " +
           "AND s.spinTime < :endTime")
    long countSpinsByParticipantAndTimeRange(
            @Param("participantId") Long participantId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    @Query("SELECT s FROM SpinHistory s " +
           "WHERE s.event.id = :eventId " +
           "AND s.spinTime >= :startTime " +
           "AND s.spinTime < :endTime " +
           "ORDER BY s.spinTime DESC")
    List<SpinHistory> findByEventIdAndTimeRange(
            @Param("eventId") Long eventId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime);

    @Query("SELECT s FROM SpinHistory s " +
           "WHERE s.participant.id = :participantId " +
           "AND s.won = true " +
           "ORDER BY s.spinTime DESC")
    List<SpinHistory> findWinningSpins(@Param("participantId") Long participantId);

    @Query("SELECT s FROM SpinHistory s " +
           "LEFT JOIN FETCH s.participant p " +
           "LEFT JOIN FETCH s.reward r " +
           "WHERE s.id = :id")
    Optional<SpinHistory> findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT COUNT(s) FROM SpinHistory s " +
           "WHERE s.event.id = :eventId " +
           "AND s.won = true")
    long countWinningSpinsByEventId(@Param("eventId") Long eventId);

    @Query("SELECT COUNT(s) FROM SpinHistory s " +
           "WHERE s.event.id = :eventId " +
           "AND s.reward.id = :rewardId " +
           "AND s.won = true")
    long countWinningSpinsByEventIdAndRewardId(
            @Param("eventId") Long eventId,
            @Param("rewardId") Long rewardId);

    @Query("SELECT s FROM SpinHistory s " +
           "WHERE s.participant.id = :participantId " +
           "ORDER BY s.spinTime DESC")
    List<SpinHistory> findByParticipantIdOrderBySpinTimeDesc(@Param("participantId") Long participantId);

    Optional<SpinHistory> findFirstByParticipantIdOrderBySpinTimeDesc(Long participantId);

    @Query("SELECT COUNT(s) > 0 FROM SpinHistory s " +
           "WHERE s.participant.id = :participantId " +
           "AND s.spinTime >= :startTime")
    boolean hasSpinAfterTime(
            @Param("participantId") Long participantId,
            @Param("startTime") LocalDateTime startTime);
}