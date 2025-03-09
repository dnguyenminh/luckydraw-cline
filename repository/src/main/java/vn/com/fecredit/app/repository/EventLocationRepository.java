package vn.com.fecredit.app.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.com.fecredit.app.entity.Event;
import vn.com.fecredit.app.entity.EventLocation;
import vn.com.fecredit.app.entity.Region;
import vn.com.fecredit.app.repository.base.BaseRepository;

@Repository
public interface EventLocationRepository extends BaseRepository<EventLocation, Long> {
    
    Optional<EventLocation> findByCode(String code);
    
    @Query("SELECT el FROM EventLocation el WHERE el.event = :event AND el.status = :status")
    List<EventLocation> findByEventAndStatus(
        @Param("event") Event event,
        @Param("status") int status
    );

    @Query("SELECT el FROM EventLocation el WHERE el.region = :region AND el.status = :status")
    List<EventLocation> findByRegionAndStatus(
        @Param("region") Region region,
        @Param("status") int status
    );

    @Query("SELECT COUNT(el) FROM EventLocation el WHERE el.event = :event AND el.status = :status")
    long countByEventAndStatus(
        @Param("event") Event event,
        @Param("status") int status
    );

    @Query("SELECT COUNT(el) FROM EventLocation el WHERE el.region = :region AND el.status = :status")
    long countByRegionAndStatus(
        @Param("region") Region region,
        @Param("status") int status
    );

    @Query("SELECT el FROM EventLocation el " +
           "LEFT JOIN FETCH el.rewards " +
           "LEFT JOIN FETCH el.goldenHours " +
           "WHERE el.id = :id AND el.status = :status")
    Optional<EventLocation> findByIdWithConfigurationsAndStatus(
        @Param("id") Long id,
        @Param("status") int status
    );

    @Query("""
        SELECT COUNT(pe) > 0 FROM ParticipantEvent pe 
        INNER JOIN pe.participant p 
        WHERE pe.eventLocation = :location 
        AND pe.status = 1
        AND p.status = 1
    """)
    boolean hasActiveParticipants(@Param("location") EventLocation location);

    @Query("SELECT COUNT(r) > 0 FROM Reward r " +
           "WHERE r.eventLocation = :location AND r.status = :status")
    boolean hasActiveRewards(
        @Param("location") EventLocation location,
        @Param("status") int status
    );
}
