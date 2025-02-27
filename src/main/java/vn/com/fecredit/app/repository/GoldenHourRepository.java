package vn.com.fecredit.app.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.com.fecredit.app.entity.GoldenHour;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface GoldenHourRepository extends JpaRepository<GoldenHour, Long> {

    List<GoldenHour> findByEventIdAndIsActiveTrue(Long eventId);

    @Query("SELECT gh FROM GoldenHour gh WHERE gh.event.id = :eventId " +
           "AND gh.isActive = true " +
           "AND gh.startTime <= :currentTime " +
           "AND gh.endTime >= :currentTime")
    Optional<GoldenHour> findCurrentGoldenHour(Long eventId, LocalDateTime currentTime);

    @Query("SELECT gh FROM GoldenHour gh WHERE gh.event.id = :eventId " +
           "AND gh.reward.id = :rewardId " +
           "AND gh.isActive = true " +
           "AND gh.startTime <= :currentTime " +
           "AND gh.endTime >= :currentTime")
    Optional<GoldenHour> findActiveGoldenHour(Long eventId, Long rewardId, LocalDateTime currentTime);

    @Query("SELECT gh FROM GoldenHour gh WHERE gh.event.id = :eventId " +
           "AND gh.startTime >= :startTime " +
           "AND gh.endTime <= :endTime")
    List<GoldenHour> findByEventIdAndTimeRange(Long eventId, LocalDateTime startTime, LocalDateTime endTime);

    @Query("SELECT gh FROM GoldenHour gh WHERE gh.event.id = :eventId " +
           "AND gh.isRecurring = true " +
           "AND gh.isActive = true")
    List<GoldenHour> findRecurringGoldenHours(Long eventId);

    @Query("SELECT gh FROM GoldenHour gh WHERE gh.event.id = :eventId " +
           "AND gh.isActive = true " +
           "ORDER BY gh.startTime")
    Page<GoldenHour> findActiveGoldenHoursByEventId(Long eventId, Pageable pageable);

    @Query("SELECT gh FROM GoldenHour gh " +
           "LEFT JOIN FETCH gh.event " +
           "LEFT JOIN FETCH gh.reward " +
           "WHERE gh.id = :id")
    Optional<GoldenHour> findByIdWithDetails(Long id);

    @Query("SELECT gh FROM GoldenHour gh WHERE gh.event.id = :eventId " +
           "AND gh.reward.id = :rewardId " +
           "AND gh.isActive = true")
    List<GoldenHour> findByEventIdAndRewardId(Long eventId, Long rewardId);

    @Query("SELECT COUNT(gh) FROM GoldenHour gh WHERE gh.event.id = :eventId " +
           "AND gh.isActive = true")
    long countActiveGoldenHours(Long eventId);

    @Query("SELECT DISTINCT gh FROM GoldenHour gh " +
           "WHERE gh.isActive = true " +
           "AND gh.startTime <= :now " +
           "AND gh.endTime >= :now")
    List<GoldenHour> findAllCurrentlyActive(LocalDateTime now);
}
