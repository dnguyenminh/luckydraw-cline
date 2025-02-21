package vn.com.fecredit.app.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import vn.com.fecredit.app.model.EventLocation;

@Repository
public interface EventLocationRepository extends JpaRepository<EventLocation, Long> {
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT el FROM EventLocation el WHERE el.id = :id")
    Optional<EventLocation> lockForUpdate(@Param("id") Long id);
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<EventLocation> findByEventIdAndIsActiveTrue(Long eventId);

    @Query("SELECT el FROM EventLocation el WHERE el.event.id = :eventId AND el.isActive = true")
    List<EventLocation> findActiveByEventId(@Param("eventId") Long eventId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<EventLocation> findByEventId(Long eventId);
    
    @Query("SELECT COALESCE(SUM(el.remainingSpins), 0) FROM EventLocation el WHERE el.event.id = :eventId AND el.isActive = true")
    int sumRemainingSpinsByEvent(@Param("eventId") Long eventId);
    
    @Query("SELECT el FROM EventLocation el JOIN FETCH el.event WHERE el.id = :id")
    Optional<EventLocation> findByIdWithEvent(@Param("id") Long id);
}