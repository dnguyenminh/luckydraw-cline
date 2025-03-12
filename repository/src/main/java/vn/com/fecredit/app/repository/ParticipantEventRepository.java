package vn.com.fecredit.app.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import vn.com.fecredit.app.entity.Event;
import vn.com.fecredit.app.entity.EventLocation;
import vn.com.fecredit.app.entity.Participant;
import vn.com.fecredit.app.entity.ParticipantEvent;
import vn.com.fecredit.app.repository.base.BaseRepository;

@Repository
public interface ParticipantEventRepository extends BaseRepository<ParticipantEvent, Long> {
    
    @Query("SELECT pe FROM ParticipantEvent pe WHERE pe.participant = :participant AND pe.eventLocation = :eventLocation")
    Optional<ParticipantEvent> findByParticipantAndLocation(
        @Param("participant") Participant participant, 
        @Param("eventLocation") EventLocation eventLocation
    );

    @Query("SELECT pe FROM ParticipantEvent pe WHERE pe.eventLocation = :eventLocation AND pe.status = 1")
    Set<ParticipantEvent> findActiveByLocation(@Param("eventLocation") EventLocation eventLocation);

    @Query("SELECT COUNT(pe) FROM ParticipantEvent pe WHERE pe.eventLocation = :eventLocation AND pe.status = 1")
    long countActiveByLocation(@Param("eventLocation") EventLocation eventLocation);

    @Query("SELECT pe FROM ParticipantEvent pe WHERE pe.eventLocation.event = :event AND pe.status = 1")
    Set<ParticipantEvent> findActiveByEvent(@Param("event") Event event);

    @Query("SELECT COUNT(pe) FROM ParticipantEvent pe WHERE pe.eventLocation.event = :event AND pe.status = 1")
    long countActiveByEvent(@Param("event") Event event);

    @Query("SELECT COUNT(pe) FROM ParticipantEvent pe " +
           "WHERE pe.participant = :participant AND pe.status = 1 " +
           "AND pe.eventLocation.event = :event")
    long countActiveEventRegistrations(
        @Param("participant") Participant participant,
        @Param("event") Event event
    );

    @Query("SELECT pe FROM ParticipantEvent pe " +
           "WHERE pe.participant = :participant " +
           "AND pe.eventLocation.event = :event " +
           "AND pe.status = 1")
    Set<ParticipantEvent> findActiveByParticipantAndEvent(
        @Param("participant") Participant participant,
        @Param("event") Event event
    );

//    @Query("SELECT pe FROM ParticipantEvent pe WHERE pe.lastResetTime < :resetBefore AND pe.status = 1")
//    List<ParticipantEvent> findStatusWithoutResetSince(@Param("resetBefore") LocalDateTime resetBefore);
}
