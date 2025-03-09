package vn.com.fecredit.app.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.com.fecredit.app.entity.Event;
import vn.com.fecredit.app.repository.base.BaseRepository;

@Repository
public interface EventRepository extends BaseRepository<Event, Long> {
    
    Optional<Event> findByCode(String code);
    
    @Query("SELECT e FROM Event e WHERE e.status = :status " +
           "AND e.startTime <= :now AND e.endTime >= :now")
    List<Event> findActive(
        @Param("status") int status,
        @Param("now") LocalDateTime now
    );

    @Query("SELECT e FROM Event e WHERE e.startTime <= :now AND e.endTime >= :now")
    List<Event> findCurrent(@Param("now") LocalDateTime now);

    @Query("SELECT e FROM Event e WHERE e.endTime < :now ORDER BY e.endTime DESC")
    Page<Event> findPast(@Param("now") LocalDateTime now, Pageable pageable);

    @Query("SELECT e FROM Event e WHERE e.startTime > :now ORDER BY e.startTime ASC")
    Page<Event> findUpcoming(@Param("now") LocalDateTime now, Pageable pageable);

    @Query("SELECT COUNT(e) > 0 FROM Event e WHERE e.code = :code")
    boolean existsByCode(@Param("code") String code);

    @Query("SELECT COUNT(e) FROM Event e WHERE e.status = :status " +
           "AND e.startTime <= :now AND e.endTime >= :now")
    long countActive(
        @Param("status") int status,
        @Param("now") LocalDateTime now
    );

    @Query("SELECT DISTINCT e FROM Event e " +
           "LEFT JOIN FETCH e.eventLocations el " +
           "LEFT JOIN FETCH el.rewards r " +
           "WHERE e.id = :id")
    Optional<Event> findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT COUNT(DISTINCT pe.participant) FROM ParticipantEvent pe " +
           "WHERE pe.eventLocation.event = :event AND pe.status = :status")
    long countActiveParticipants(@Param("event") Event event);

    @Query("SELECT COUNT(DISTINCT r) FROM Reward r " +
           "WHERE r.eventLocation.event = :event AND r.status = :status")
    long countActiveRewards(
        @Param("event") Event event,
        @Param("status") int status
    );
}
