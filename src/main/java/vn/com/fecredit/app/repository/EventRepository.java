package vn.com.fecredit.app.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import vn.com.fecredit.app.entity.Event;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    Optional<Event> findByCode(String code);
    boolean existsByCode(String code);
    
    List<Event> findByActiveTrue();
    long countByActiveTrue();
    
    @Query("SELECT e FROM Event e WHERE e.active = true AND e.startDate <= ?1 AND e.endDate >= ?2")
    List<Event> findByActiveTrueAndStartDateBeforeAndEndDateAfter(LocalDateTime currentTime1, LocalDateTime currentTime2);
    
    @Query("SELECT e FROM Event e WHERE e.active = true AND e.startDate > ?1")
    List<Event> findByActiveTrueAndStartDateAfter(LocalDateTime currentTime);
    
    List<Event> findByEndDateBefore(LocalDateTime date);
    List<Event> findByStartDateBetween(LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT COUNT(e) FROM Event e WHERE e.location.id = ?1")
    long countByLocationId(Long locationId);

    @Query("SELECT e FROM Event e WHERE e.location.id = ?1")
    List<Event> findByLocationId(Long locationId);
    
    @Query("SELECT COUNT(e) FROM Event e WHERE e.status = ?1")
    long countByStatus(String status);

    @Query("SELECT e FROM Event e WHERE LOWER(e.status) = LOWER(?1)")
    List<Event> findByStatusIgnoreCase(String status);
}
