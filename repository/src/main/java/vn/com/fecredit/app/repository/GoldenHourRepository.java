package vn.com.fecredit.app.repository;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.com.fecredit.app.entity.EventLocation;
import vn.com.fecredit.app.entity.GoldenHour;
import vn.com.fecredit.app.repository.base.BaseRepository;

@Repository
public interface GoldenHourRepository extends BaseRepository<GoldenHour, Long> {
    
    @Query("SELECT gh FROM GoldenHour gh " +
           "WHERE gh.eventLocation = :location " +
           "AND gh.status = :status " +
           "AND gh.startTime <= :now " +
           "AND gh.endTime >= :now")
    List<GoldenHour> findActive(
        @Param("location") EventLocation location,
        @Param("status") int status,
        @Param("now") LocalDateTime now
    );

    @Query("SELECT gh FROM GoldenHour gh " +
           "WHERE gh.eventLocation = :location " +
           "AND gh.startTime <= :now " +
           "AND gh.endTime >= :now " +
           "ORDER BY gh.createdAt DESC")
    Page<GoldenHour> findCurrentByLocation(
        @Param("location") EventLocation location,
        @Param("now") LocalDateTime now,
        Pageable pageable
    );

    @Query("SELECT gh FROM GoldenHour gh " +
           "WHERE gh.eventLocation = :location " +
           "AND gh.endTime < :now " +
           "ORDER BY gh.endTime DESC")
    Page<GoldenHour> findPastByLocation(
        @Param("location") EventLocation location,
        @Param("now") LocalDateTime now,
        Pageable pageable
    );

    @Query("SELECT gh FROM GoldenHour gh " +
           "WHERE gh.eventLocation = :location " +
           "AND gh.startTime > :now " +
           "ORDER BY gh.startTime ASC")
    Page<GoldenHour> findUpcomingByLocation(
        @Param("location") EventLocation location,
        @Param("now") LocalDateTime now,
        Pageable pageable
    );

    @Query("SELECT COUNT(gh) > 0 FROM GoldenHour gh " +
           "WHERE gh.eventLocation = :location " +
           "AND gh.status = :status " +
           "AND ((gh.startTime BETWEEN :start AND :end) " +
           "OR (gh.endTime BETWEEN :start AND :end) " +
           "OR (:start BETWEEN gh.startTime AND gh.endTime))")
    boolean hasOverlappingHours(
        @Param("location") EventLocation location,
        @Param("status") int status,
        @Param("start") LocalDateTime start,
        @Param("end") LocalDateTime end
    );

    @Query("SELECT gh FROM GoldenHour gh " +
           "WHERE gh.endTime < :expireBefore " +
           "AND gh.status = :status")
    List<GoldenHour> findExpired(
        @Param("expireBefore") LocalDateTime expireBefore,
        @Param("status") int status
    );
}
