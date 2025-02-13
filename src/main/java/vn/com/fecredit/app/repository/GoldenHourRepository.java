package vn.com.fecredit.app.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import vn.com.fecredit.app.model.GoldenHour;

@Repository
@Transactional(readOnly = true)
public interface GoldenHourRepository extends JpaRepository<GoldenHour, Long> {

       List<GoldenHour> findByEventIdAndIsActiveTrue(Long eventId);

       List<GoldenHour> findByRewardIdAndIsActiveTrue(Long rewardId);

       @Query("SELECT gh FROM GoldenHour gh " +
                     "WHERE gh.event.id = :eventId " +
                     "AND gh.isActive = true " +
                     "AND ((gh.startTime <= :testTime AND gh.endTime >= :testTime) " +
                     "     OR (gh.startTime > gh.endTime AND (:testTime >= gh.startTime OR :testTime <= gh.endTime)))")
       Optional<GoldenHour> findActiveGoldenHour(
                     @Param("eventId") Long eventId,
                     @Param("testTime") LocalDateTime testTime);

       @Modifying(clearAutomatically = true)
       @Transactional
       @Query("UPDATE GoldenHour gh SET gh.isActive = :status WHERE gh.id = :id")
       int updateStatus(@Param("id") Long id, @Param("status") boolean status);

       @Query("SELECT gh FROM GoldenHour gh " +
                     "JOIN FETCH gh.event " +
                     "LEFT JOIN FETCH gh.reward " +
                     "WHERE gh.id = :id")
       Optional<GoldenHour> findByIdWithDetails(@Param("id") Long id);

       @Query("SELECT CASE WHEN COUNT(gh) > 0 THEN true ELSE false END FROM GoldenHour gh " +
                     "WHERE gh.event.id = :eventId " +
                     "AND gh.isActive = true " +
                     "AND ((gh.startTime <= :testTime AND gh.endTime >= :testTime) " +
                     "     OR (gh.startTime > gh.endTime AND (:testTime >= gh.startTime OR :testTime <= gh.endTime)))")
       boolean isGoldenHourActive(@Param("eventId") Long eventId, @Param("testTime") LocalDateTime testTime);

       @Query("SELECT gh FROM GoldenHour gh " +
                     "WHERE gh.event.id = :eventId " +
                     "AND gh.isActive = true " +
                     "ORDER BY gh.startTime")
       List<GoldenHour> findActiveGoldenHoursOrdered(@Param("eventId") Long eventId);

       boolean existsByEventIdAndName(Long eventId, String name);
}