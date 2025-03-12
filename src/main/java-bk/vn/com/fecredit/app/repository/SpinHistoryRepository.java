package vn.com.fecredit.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.com.fecredit.app.dto.projection.HourlyStatsProjection;
import vn.com.fecredit.app.entity.EventLocation;
import vn.com.fecredit.app.entity.SpinHistory;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SpinHistoryRepository extends JpaRepository<SpinHistory, Long> {

    long countByLocation(EventLocation location);

    long countByLocationAndWonTrue(EventLocation location);

    long countByLocationAndSpinTimeBetween(EventLocation location, LocalDateTime start, LocalDateTime end);

    long countByLocationAndWonTrueAndSpinTimeBetween(EventLocation location, LocalDateTime start, LocalDateTime end);

    @Query("SELECT HOUR(sh.spinTime) as hour, " +
           "COUNT(sh) as totalSpins, " +
           "COUNT(CASE WHEN sh.won = true THEN 1 END) as winningSpins, " +
           "CAST(COUNT(CASE WHEN sh.won = true THEN 1 END) AS double) / CAST(COUNT(sh) AS double) as winRate " +
           "FROM SpinHistory sh " +
           "WHERE sh.location = :location " +
           "GROUP BY HOUR(sh.spinTime)")
    List<HourlyStatsProjection> findHourlyStatsByLocation(@Param("location") EventLocation location);

    @Query("SELECT sh FROM SpinHistory sh " +
           "WHERE sh.location = :location " +
           "AND sh.spinTime BETWEEN :startTime AND :endTime " +
           "ORDER BY sh.spinTime DESC")
    List<SpinHistory> findByLocationAndTimeRange(
        @Param("location") EventLocation location,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime);

    @Query("SELECT COUNT(sh) > 0 FROM SpinHistory sh " +
           "WHERE sh.location = :location " +
           "AND sh.spinTime BETWEEN :startTime AND :endTime")
    boolean existsByLocationAndTimeRange(
        @Param("location") EventLocation location,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime);
}
