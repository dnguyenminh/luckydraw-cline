package vn.com.fecredit.app.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.com.fecredit.app.entity.EventLocation;
import vn.com.fecredit.app.repository.base.BaseRepository;

import java.util.List;
import java.util.Optional;

public interface EventLocationRepository extends BaseRepository<EventLocation, Long> {

    Optional<EventLocation> findByCode(String code);

    @Query("SELECT el FROM EventLocation el WHERE el.event.id = :eventId AND el.status = :status")
    List<EventLocation> findAllByEventIdAndStatus(@Param("eventId") Long eventId, @Param("status") int status);

    @Query("SELECT el FROM EventLocation el WHERE el.region.id = :regionId AND el.status = :status")
    List<EventLocation> findAllByRegionIdAndStatus(@Param("regionId") Long regionId, @Param("status") int status);

    @Query("SELECT el FROM EventLocation el WHERE el.province.id = :provinceId AND el.status = :status")
    List<EventLocation> findAllByProvinceIdAndStatus(@Param("provinceId") Long provinceId, @Param("status") int status);

    @Query("SELECT el FROM EventLocation el WHERE el.status = :status")
    Page<EventLocation> findAllByStatus(@Param("status") int status, Pageable pageable);

    @Query("SELECT el FROM EventLocation el WHERE el.status = :status")
    List<EventLocation> findAllByStatus(@Param("status") int status);

    @Query("SELECT el FROM EventLocation el WHERE el.event.id = :eventId")
    List<EventLocation> findAllByEventId(@Param("eventId") Long eventId);

    @Query("SELECT CASE WHEN COUNT(el) > 0 THEN true ELSE false END FROM EventLocation el " +
           "WHERE el.region.id = :regionId AND el.province IN " +
           "(SELECT p FROM Province p WHERE p.id != :excludedProvinceId)")
    boolean existsOverlappingProvinces(@Param("regionId") Long regionId, @Param("excludedProvinceId") Long excludedProvinceId);

    @Query("SELECT COUNT(el) FROM EventLocation el " +
           "WHERE el.event.id = :eventId AND el.status = :status")
    long countByEventIdAndStatus(@Param("eventId") Long eventId, @Param("status") int status);

    @Query("SELECT el FROM EventLocation el " +
           "LEFT JOIN FETCH el.event " +
           "LEFT JOIN FETCH el.region " +
           "LEFT JOIN FETCH el.province " +
           "WHERE el.id = :id")
    Optional<EventLocation> findByIdWithRelationships(@Param("id") Long id);

    @Query("SELECT el FROM EventLocation el " +
           "WHERE el.event.id = :eventId " +
           "AND el.region.id = :regionId " +
           "AND el.province.id = :provinceId")
    Optional<EventLocation> findByEventAndRegionAndProvince(
            @Param("eventId") Long eventId,
            @Param("regionId") Long regionId,
            @Param("provinceId") Long provinceId);

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
