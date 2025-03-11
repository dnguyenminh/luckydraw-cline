package vn.com.fecredit.app.mapper;

import org.mapstruct.*;
import vn.com.fecredit.app.dto.ParticipantDTO;
import vn.com.fecredit.app.entity.Participant;
import vn.com.fecredit.app.entity.Event;
import vn.com.fecredit.app.entity.ParticipantEvent;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.Optional;

@Mapper(componentModel = "spring", 
       unmappedTargetPolicy = ReportingPolicy.IGNORE,
       uses = {EventMapper.class})
public interface ParticipantMapper {

    @Mapping(target = "account", source = "customerId")
    @Mapping(target = "name", source = "cardNumber")
    @Mapping(target = "phone", source = "phoneNumber")
    @Mapping(target = "status", constant = "1")
    @Mapping(target = "participantEvents", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    Participant toEntity(ParticipantDTO.CreateRequest dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "account", source = "customerId")
    @Mapping(target = "name", source = "cardNumber")
    @Mapping(target = "phone", source = "phoneNumber")
    @Mapping(target = "updatedAt", expression = "java(LocalDateTime.now())")
    void updateEntity(@MappingTarget Participant entity, ParticipantDTO.UpdateRequest dto);

    @Mapping(target = "customerId", source = "account")
    @Mapping(target = "cardNumber", source = "name")
    @Mapping(target = "phoneNumber", source = "phone")
    @Mapping(target = "eventName", expression = "java(getActiveEventName(participant))")
    @Mapping(target = "eventId", expression = "java(getActiveEventId(participant))")
    @Mapping(target = "spinsRemaining", expression = "java(getActiveEventSpinsRemaining(participant))")
    @Mapping(target = "dailySpinLimit", expression = "java(getActiveEventDailySpinLimit(participant))")
    @Mapping(target = "isEligibleForSpin", expression = "java(canSpinInActiveEvent(participant))")
    @Mapping(target = "lastSpinTime", expression = "java(getLastSpinTime(participant))")
    @Mapping(target = "lastSyncTime", expression = "java(getLastSyncTime(participant))")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "updatedAt", source = "updatedAt")
    @Mapping(target = "createdBy", source = "createdBy")
    @Mapping(target = "updatedBy", source = "updatedBy")
    ParticipantDTO.Response toResponse(Participant participant);

    @Mapping(target = "customerId", source = "account")
    @Mapping(target = "cardNumber", source = "name")
    @Mapping(target = "phoneNumber", source = "phone")
    @Mapping(target = "eventName", expression = "java(getActiveEventName(participant))")
    @Mapping(target = "spinsRemaining", expression = "java(getActiveEventSpinsRemaining(participant))")
    @Mapping(target = "dailySpinLimit", expression = "java(getActiveEventDailySpinLimit(participant))")
    @Mapping(target = "isEligibleForSpin", expression = "java(canSpinInActiveEvent(participant))")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "createdAt", source = "createdAt")
    @Mapping(target = "updatedAt", source = "updatedAt")
    ParticipantDTO.Summary toSummary(Participant participant);

    default String getActiveEventName(Participant participant) {
        return getActiveEvent(participant)
            .map(Event::getName)
            .orElse(null);
    }

    default Long getActiveEventId(Participant participant) {
        return getActiveEvent(participant)
            .map(Event::getId)
            .orElse(null);
    }

    default Integer getActiveEventSpinsRemaining(Participant participant) {
        return getActiveParticipantEvent(participant)
            .map(ParticipantEvent::getAvailableSpins)
            .orElse(null);
    }

    default Integer getActiveEventDailySpinLimit(Participant participant) {
        return getActiveParticipantEvent(participant)
            .map(ParticipantEvent::getEventLocation)
            .map(location -> location.getEffectiveDailySpinLimit())
            .orElse(null);
    }

    default Boolean canSpinInActiveEvent(Participant participant) {
        return getActiveParticipantEvent(participant)
            .map(ParticipantEvent::canSpin)
            .orElse(false);
    }

    default LocalDateTime getLastSpinTime(Participant participant) {
        return getActiveParticipantEvent(participant)
            .map(ParticipantEvent::getLastSpinTime)
            .orElse(null);
    }

    default LocalDateTime getLastSyncTime(Participant participant) {
        return getActiveParticipantEvent(participant)
            .map(ParticipantEvent::getLastSyncTime)
            .orElse(null);
    }

    default Optional<ParticipantEvent> getActiveParticipantEvent(Participant participant) {
        if (participant == null || participant.getParticipantEvents() == null) {
            return Optional.empty();
        }
        return participant.getParticipantEvents().stream()
            .filter(pe -> pe.isActive())
            .findFirst();
    }

    default Optional<Event> getActiveEvent(Participant participant) {
        return getActiveParticipantEvent(participant)
            .map(ParticipantEvent::getEvent);
    }
}
