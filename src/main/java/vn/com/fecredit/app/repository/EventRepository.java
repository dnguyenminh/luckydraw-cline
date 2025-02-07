package vn.com.fecredit.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import vn.com.fecredit.app.model.Event;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    List<Event> findByIsActiveTrue();

    @Query("SELECT e FROM Event e WHERE e.isActive = true AND e.startDate <= ?1 AND (e.endDate IS NULL OR e.endDate >= ?1)")
    List<Event> findActiveEventsAtTime(LocalDateTime time);

    boolean existsByCode(String code);

    @Query("SELECT DISTINCT e FROM Event e LEFT JOIN FETCH e.participants LEFT JOIN FETCH e.rewards WHERE e.id = ?1")
    Optional<Event> findByIdWithDetails(Long id);
}