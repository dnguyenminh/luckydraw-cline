package vn.com.fecredit.app.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.com.fecredit.app.entity.Event;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    @Query("SELECT e FROM Event e WHERE e.status = :status")
    Page<Event> findAllByStatus(@Param("status") EntityStatus status, Pageable pageable);
    
    @Query("SELECT COUNT(e) FROM Event e WHERE e.status = :status")
    long countByStatus(@Param("status") EntityStatus status);

    @Query("SELECT e FROM Event e WHERE e.status = :status " +
           "AND (:searchTerm IS NULL OR LOWER(e.name) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) " +
           "AND (:startDate IS NULL OR e.startDate >= :startDate) " +
           "AND (:endDate IS NULL OR e.endDate <= :endDate)")
    Page<Event> searchEvents(
            @Param("searchTerm") String searchTerm,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("status") EntityStatus status,
            Pageable pageable);

    @Query("SELECT e FROM Event e WHERE e.status = 'ACTIVE' AND " +
           "e.startDate <= CURRENT_TIMESTAMP AND " +
           "(e.endDate IS NULL OR e.endDate >= CURRENT_TIMESTAMP)")
    List<Event> findActiveEvents();

    @Query("SELECT CASE WHEN COUNT(e) > 0 THEN true ELSE false END FROM Event e WHERE e.code = :code")
    boolean existsByCode(@Param("code") String code);

    @Query("SELECT e FROM Event e LEFT JOIN FETCH e.locations l " +
           "LEFT JOIN FETCH e.rewards r WHERE e.id = :id")
    Optional<Event> findByIdWithDetails(@Param("id") Long id);

    @Query("SELECT e FROM Event e WHERE e.status = 'ACTIVE' " +
           "AND EXISTS (SELECT r FROM e.rewards r WHERE r.remainingQuantity > 0)")
    List<Event> findActiveEventsWithRemainingSpins();

    @Query("SELECT COUNT(e) FROM Event e WHERE e.status = 'ACTIVE'")
    long countActive();

    @Query("SELECT e FROM Event e WHERE e.code = :code")
    Optional<Event> findByCode(@Param("code") String code);
}
