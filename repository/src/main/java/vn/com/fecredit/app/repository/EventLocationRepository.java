package vn.com.fecredit.app.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.com.fecredit.app.entity.EventLocation;
import vn.com.fecredit.app.repository.base.BaseRepository;

import java.util.Optional;
import java.util.Set;

public interface EventLocationRepository extends BaseRepository<EventLocation, Long> {

    Optional<EventLocation> findByCode(String code);

    @Query("SELECT el FROM EventLocation el WHERE el.event.id = :eventId AND el.status = :status")
    Set<EventLocation> findAllByEventIdAndStatus(@Param("eventId") Long eventId, @Param("status") int status);

    @Query("SELECT el FROM EventLocation el WHERE el.region.id = :regionId AND el.status = :status")
    Set<EventLocation> findAllByRegionIdAndStatus(@Param("regionId") Long regionId, @Param("status") int status);

    @Query("SELECT el FROM EventLocation el WHERE el.status = :status")
    Page<EventLocation> findAllByStatus(@Param("status") int status, Pageable pageable);

    @Query("SELECT el FROM EventLocation el WHERE el.status = :status")
    Set<EventLocation> findAllByStatus(@Param("status") int status);

    @Query("SELECT el FROM EventLocation el WHERE el.event.id = :eventId")
    Set<EventLocation> findAllByEventId(@Param("eventId") Long eventId);

    @Query("SELECT COUNT(el) FROM EventLocation el WHERE el.event.id = :eventId AND el.status = :status")
    long countByEventIdAndStatus(@Param("eventId") Long eventId, @Param("status") int status);

    @Query("SELECT el FROM EventLocation el " +
           "LEFT JOIN FETCH el.event " +
           "LEFT JOIN FETCH el.region " +
           "WHERE el.id = :id")
    Optional<EventLocation> findByIdWithRelationships(@Param("id") Long id);

    boolean existsByCodeAndIdNot(String code, Long id);

    @Query("SELECT COUNT(el) > 0 FROM EventLocation el " +
           "JOIN el.participantEvents pe " +
           "WHERE el.id = :locationId " +
           "AND pe.status = :status")
    boolean hasActiveParticipants(@Param("locationId") Long locationId, @Param("status") int status);

    @Query("SELECT DISTINCT el FROM EventLocation el " +
           "LEFT JOIN FETCH el.participantEvents " +
           "WHERE el.id = :id")
    Optional<EventLocation> findByIdWithParticipants(@Param("id") Long id);

    @Query("SELECT COUNT(r) > 0 FROM EventLocation el " +
           "JOIN el.event e " +
           "JOIN e.rewards r " +
           "WHERE el.id = :locationId " +
           "AND r.status = :status")
    boolean hasActiveRewards(@Param("locationId") Long locationId, @Param("status") int status);
}
