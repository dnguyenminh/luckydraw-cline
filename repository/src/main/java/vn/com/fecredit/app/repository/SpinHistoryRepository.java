package vn.com.fecredit.app.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.com.fecredit.app.entity.SpinHistory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SpinHistoryRepository extends JpaRepository<SpinHistory, Long> {

    // Basic queries
    Page<SpinHistory> findAllByParticipantEventId(Long participantEventId, Pageable pageable);
    
    List<SpinHistory> findAllByParticipantEventIdAndSpinTimeBetween(
        Long participantEventId, 
        LocalDateTime startTime, 
        LocalDateTime endTime
    );

    @Query("""
        SELECT sh FROM SpinHistory sh 
        WHERE sh.participantEvent.id = :participantEventId 
        AND CAST(sh.spinTime AS date) = CURRENT_DATE
        """)
    List<SpinHistory> findTodaySpins(@Param("participantEventId") Long participantEventId);

    @Query("""
        SELECT COUNT(sh) FROM SpinHistory sh 
        WHERE sh.participantEvent.id = :participantEventId 
        AND CAST(sh.spinTime AS date) = CURRENT_DATE
        """)
    long countTodaySpins(@Param("participantEventId") Long participantEventId);

    @Query("""
        SELECT COUNT(sh) FROM SpinHistory sh 
        WHERE sh.participantEvent.id = :participantEventId 
        AND sh.win = true
        """)
    long countWinningSpins(@Param("participantEventId") Long participantEventId);

    @Query("""
        SELECT COALESCE(SUM(sh.pointsEarned), 0) FROM SpinHistory sh 
        WHERE sh.participantEvent.id = :participantEventId
        """)
    Integer sumPointsEarned(@Param("participantEventId") Long participantEventId);

    // Latest spin query
    Optional<SpinHistory> findFirstByParticipantEventIdOrderBySpinTimeDesc(Long participantEventId);

    // Recent spins with limit
    List<SpinHistory> findByParticipantEventIdOrderBySpinTimeDesc(Long participantEventId, Pageable pageable);

    // State validation queries
    boolean existsByParticipantEventIdAndFinalizedFalse(Long participantEventId);

    // Event and location based queries
    @Query("""
        SELECT sh FROM SpinHistory sh 
        WHERE sh.participantEvent.event.id = :eventId
        """)
    List<SpinHistory> findAllByEventId(@Param("eventId") Long eventId);

    @Query("""
        SELECT sh FROM SpinHistory sh 
        WHERE sh.participantEvent.eventLocation.id = :locationId
        """)
    List<SpinHistory> findAllByEventLocationId(@Param("locationId") Long locationId);

    // Time-based aggregate queries
    @Query("""
        SELECT COUNT(sh) FROM SpinHistory sh 
        WHERE sh.participantEvent.id = :participantEventId 
        AND sh.spinTime BETWEEN :startTime AND :endTime
        """)
    long countSpinsInTimeRange(
        @Param("participantEventId") Long participantEventId,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );

    @Query("""
        SELECT COUNT(sh) as totalSpins,
               COUNT(CASE WHEN sh.win = true THEN 1 END) as winningSpins,
               COALESCE(SUM(sh.pointsEarned), 0) as totalPoints,
               MIN(sh.spinTime) as firstSpinTime,
               MAX(sh.spinTime) as lastSpinTime
        FROM SpinHistory sh 
        WHERE sh.participantEvent.id = :participantEventId
        """)
    Optional<Object[]> getSpinStats(@Param("participantEventId") Long participantEventId);
}
