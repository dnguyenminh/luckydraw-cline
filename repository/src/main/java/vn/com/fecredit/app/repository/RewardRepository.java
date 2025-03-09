package vn.com.fecredit.app.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.com.fecredit.app.entity.EventLocation;
import vn.com.fecredit.app.entity.Reward;
import vn.com.fecredit.app.repository.base.BaseRepository;

@Repository
public interface RewardRepository extends BaseRepository<Reward, Long> {
    
    Optional<Reward> findByCode(String code);
    
    @Query("SELECT r FROM Reward r WHERE r.eventLocation = :location AND r.status = :status")
    List<Reward> findByLocationAndStatus(
        @Param("location") EventLocation location,
        @Param("status") int status
    );

    @Query("SELECT r FROM Reward r " +
           "WHERE r.eventLocation = :location " +
           "AND r.status = :status " +
           "AND r.validFrom <= :now " +
           "AND r.validUntil >= :now " +
           "AND r.remainingQuantity > 0 " +
           "AND (r.dailyLimit IS NULL OR r.dailyCount < r.dailyLimit)")
    List<Reward> findAvailable(
        @Param("location") EventLocation location,
        @Param("status") int status,
        @Param("now") LocalDateTime now
    );

    @Query("SELECT r FROM Reward r " +
           "WHERE r.eventLocation = :location " +
           "AND r.validFrom <= :now " +
           "AND r.validUntil >= :now " +
           "ORDER BY r.createdAt DESC")
    Page<Reward> findActiveByLocation(
        @Param("location") EventLocation location,
        @Param("now") LocalDateTime now,
        Pageable pageable
    );

    @Query("SELECT r FROM Reward r " +
           "WHERE r.eventLocation = :location " +
           "AND r.validUntil < :now " +
           "ORDER BY r.validUntil DESC")
    Page<Reward> findExpiredByLocation(
        @Param("location") EventLocation location,
        @Param("now") LocalDateTime now,
        Pageable pageable
    );

    @Query("SELECT COUNT(sh) FROM SpinHistory sh " +
           "WHERE sh.reward = :reward " +
           "AND sh.timestamp >= :since " +
           "AND sh.win = true")
    long countWinsSince(
        @Param("reward") Reward reward,
        @Param("since") LocalDateTime since
    );

    @Query("SELECT r FROM Reward r " +
           "WHERE r.validUntil < :expireBefore " +
           "AND r.remainingQuantity > 0")
    List<Reward> findUnusedExpiredBefore(@Param("expireBefore") LocalDateTime expireBefore);

    @Query("SELECT r FROM Reward r " +
           "WHERE r.eventLocation = :location " +
           "AND r.validFrom BETWEEN :startTime AND :endTime")
    List<Reward> findByLocationAndStartTimeRange(
        @Param("location") EventLocation location,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );
}
