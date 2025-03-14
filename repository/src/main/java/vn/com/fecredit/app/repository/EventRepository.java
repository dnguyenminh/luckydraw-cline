package vn.com.fecredit.app.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.com.fecredit.app.entity.Event;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface EventRepository extends JpaRepository<Event, Long> {
    
    Optional<Event> findByCode(String code);
    
    boolean existsByCode(String code);
    
    boolean existsByStatus(int status);
    
    Page<Event> findByStatus(int status, Pageable pageable);

    @Query("SELECT DISTINCT e FROM Event e " +
           "WHERE (:searchText IS NULL OR LOWER(e.name) LIKE LOWER(CONCAT('%', :searchText, '%'))) AND " +
           "(:startDate IS NULL OR e.startTime >= :startDate) AND " +
           "(:endDate IS NULL OR e.endTime <= :endDate) AND " +
           "(:status IS NULL OR e.status = :status)")
    Page<Event> findBySearchCriteria(@Param("searchText") String searchText,
                                    @Param("startDate") LocalDateTime startDate,
                                    @Param("endDate") LocalDateTime endDate,
                                    @Param("status") Integer status,
                                    Pageable pageable);

    @Query("SELECT DISTINCT e FROM Event e " +
           "WHERE e.status = :status " +
           "AND e.startTime <= :currentTime AND e.endTime >= :currentTime")
    Set<Event> findActive(@Param("status") int status, 
                         @Param("currentTime") LocalDateTime currentTime);

    @Query("SELECT DISTINCT e FROM Event e " +
           "LEFT JOIN FETCH e.eventLocations el " + 
           "LEFT JOIN FETCH el.region r " +
           "LEFT JOIN FETCH r.provinces " +
           "LEFT JOIN FETCH e.provinces " +
           "WHERE e.id = :id")
    Optional<Event> findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT e FROM Event e " + 
           "WHERE e.startTime <= CURRENT_TIMESTAMP " +
           "AND e.endTime >= CURRENT_TIMESTAMP " + 
           "AND e.status = 1 " +
           "AND e.remainingSpins > 0")
    List<Event> findActiveEventsWithRemainingSpins();

    @Query("SELECT DISTINCT e FROM Event e " +
           "WHERE e.startTime <= :currentTime " +
           "AND e.endTime >= :currentTime " +
           "AND e.status = 1")
    Set<Event> findCurrent(@Param("currentTime") LocalDateTime currentTime);
}
