package vn.com.fecredit.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import vn.com.fecredit.app.model.Event;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@Transactional(readOnly = true)
public interface EventRepository extends JpaRepository<Event, Long> {

    Optional<Event> findByCode(String code);

    Optional<Event> findByCodeAndIsActive(String code, boolean isActive);

    boolean existsByCode(String code);

    List<Event> findByIsActiveTrue();

    @Query("SELECT e FROM Event e " +
           "WHERE e.isActive = true " +
           "AND e.startDate <= :currentTime " +
           "AND e.endDate > :currentTime")
    List<Event> findActiveEvents(@Param("currentTime") LocalDateTime currentTime);

    @Query("SELECT e FROM Event e " +
           "WHERE e.startDate >= :startDate " +
           "AND e.startDate < :endDate")
    List<Event> findCurrentEvents(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);

    @Query("SELECT e FROM Event e " +
           "LEFT JOIN FETCH e.rewards " +
           "LEFT JOIN FETCH e.participants " +
           "WHERE e.id = :id")
    Optional<Event> findByIdWithDetails(@Param("id") Long id);

    @Modifying
    @Transactional
    @Query("UPDATE Event e " +
           "SET e.isActive = :status, " +
           "    e.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE e.id = :id")
    int updateEventStatus(@Param("id") Long id, @Param("status") boolean status);

    @Modifying
    @Transactional
    @Query("UPDATE Event e " +
           "SET e.remainingSpins = :remainingSpins, " +
           "    e.updatedAt = CURRENT_TIMESTAMP " +
           "WHERE e.id = :id")
    int updateRemainingSpins(@Param("id") Long id, @Param("remainingSpins") Long remainingSpins);
}