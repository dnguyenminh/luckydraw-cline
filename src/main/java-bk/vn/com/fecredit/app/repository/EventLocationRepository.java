package vn.com.fecredit.app.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.com.fecredit.app.entity.EventLocation;

import java.util.List;
import java.util.Optional;

@Repository
public interface EventLocationRepository extends JpaRepository<EventLocation, Long> {

    Page<EventLocation> findAllByStatus(EntityStatus status, Pageable pageable);

    List<EventLocation> findAllByStatus(EntityStatus status);

    @Query("SELECT el FROM EventLocation el JOIN el.events e WHERE e.id = :eventId")
    List<EventLocation> findAllByEventId(@Param("eventId") Long eventId);

    boolean existsByNameAndStatusNot(String name, EntityStatus status);

    @Query("SELECT el FROM EventLocation el " +
           "LEFT JOIN FETCH el.spinHistories sh " +
           "WHERE el.id = :id")
    Optional<EventLocation> findByIdWithSpinHistories(@Param("id") Long id);

    @Query("SELECT COUNT(sh) FROM SpinHistory sh " +
           "WHERE sh.location.id = :locationId " +
           "AND sh.won = true")
    long countWinningSpinsByLocation(@Param("locationId") Long locationId);

    @Query("SELECT COUNT(sh) FROM SpinHistory sh " +
           "WHERE sh.location.id = :locationId")
    long countSpinsByLocation(@Param("locationId") Long locationId);

    @Query("SELECT el FROM EventLocation el " +
           "WHERE el.status = :status AND " +
           "EXISTS (SELECT e FROM el.events e WHERE e.status = :status)")
    List<EventLocation> findAllActiveWithActiveEvents(@Param("status") EntityStatus status);
}
