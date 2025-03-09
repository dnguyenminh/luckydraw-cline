package vn.com.fecredit.app.repository;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.com.fecredit.app.entity.Event;
import vn.com.fecredit.app.entity.EventLocation;
import vn.com.fecredit.app.entity.Participant;
import vn.com.fecredit.app.entity.Reward;
import vn.com.fecredit.app.entity.SpinHistory;
import vn.com.fecredit.app.repository.base.BaseRepository;

@Repository
public interface SpinHistoryRepository extends BaseRepository<SpinHistory, Long> {

    @Query("SELECT sh FROM SpinHistory sh WHERE sh.participant = :participant " +
           "AND sh.timestamp >= :since ORDER BY sh.timestamp DESC")
    List<SpinHistory> findByParticipantSince(
        @Param("participant") Participant participant,
        @Param("since") LocalDateTime since
    );

    @Query("SELECT COUNT(sh) FROM SpinHistory sh WHERE sh.participant = :participant " +
           "AND sh.timestamp >= :since")
    long countByParticipantSince(
        @Param("participant") Participant participant,
        @Param("since") LocalDateTime since
    );

    @Query("SELECT COUNT(sh) FROM SpinHistory sh WHERE sh.eventLocation = :location " +
           "AND sh.win = true AND sh.timestamp >= :since")
    long countWinsByLocationSince(
        @Param("location") EventLocation location,
        @Param("since") LocalDateTime since
    );

    @Query("SELECT COUNT(sh) FROM SpinHistory sh WHERE sh.reward = :reward " +
           "AND sh.win = true AND sh.timestamp >= :since")
    long countWinsByRewardSince(
        @Param("reward") Reward reward,
        @Param("since") LocalDateTime since
    );

    @Query("SELECT sh FROM SpinHistory sh WHERE sh.eventLocation = :location " +
           "AND sh.timestamp BETWEEN :start AND :end " +
           "ORDER BY sh.timestamp DESC")
    Page<SpinHistory> findByLocationAndTimeRange(
        @Param("location") EventLocation location,
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end,
        Pageable pageable
    );

    @Query("SELECT sh FROM SpinHistory sh WHERE sh.participant = :participant " +
           "AND sh.timestamp BETWEEN :start AND :end " +
           "ORDER BY sh.timestamp DESC")
    Page<SpinHistory> findByParticipantAndTimeRange(
        @Param("participant") Participant participant,
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end,
        Pageable pageable
    );

    @Query("SELECT sh FROM SpinHistory sh WHERE sh.reward = :reward " +
           "AND sh.timestamp BETWEEN :start AND :end " +
           "ORDER BY sh.timestamp DESC")
    Page<SpinHistory> findByRewardAndTimeRange(
        @Param("reward") Reward reward,
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end,
        Pageable pageable
    );

    @Query("SELECT DISTINCT sh.participant FROM SpinHistory sh " +
           "WHERE sh.eventLocation.event = :event " +
           "AND sh.timestamp >= :since")
    List<Participant> findDistinctParticipantsByEventSince(
        @Param("event") Event event,
        @Param("since") LocalDateTime since
    );

    @Query("SELECT COUNT(DISTINCT sh.participant) FROM SpinHistory sh " +
           "WHERE sh.eventLocation.event = :event " +
           "AND sh.timestamp >= :since")
    long countDistinctParticipantsByEventSince(
        @Param("event") Event event,
        @Param("since") LocalDateTime since
    );
}
