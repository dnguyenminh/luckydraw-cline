package vn.com.fecredit.app.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import vn.com.fecredit.app.model.Event;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    boolean existsByCode(String code);

    List<Event> findByIsActiveTrue();

    @Query("SELECT DISTINCT e FROM Event e " +
           "LEFT JOIN FETCH e.eventLocations " +
           "LEFT JOIN FETCH e.rewards " +
           "WHERE e.id = :eventId")
    Optional<Event> findByIdWithDetails(@Param("eventId") Long eventId);

    @Query("SELECT e FROM Event e " +
           "WHERE e.isActive = true " +
           "AND e.remainingSpins > 0")
    List<Event> findActiveEventsWithRemainingSpins();

    @Query("SELECT COUNT(e) > 0 FROM Event e WHERE e.code = :code")
    boolean isCodeExists(@Param("code") String code);

    @Query("SELECT e FROM Event e " +
           "WHERE e.isActive = true " +
           "AND (e.startDate IS NULL OR e.startDate <= CURRENT_TIMESTAMP) " +
           "AND (e.endDate IS NULL OR e.endDate >= CURRENT_TIMESTAMP)")
    List<Event> findCurrentlyActiveEvents();
}