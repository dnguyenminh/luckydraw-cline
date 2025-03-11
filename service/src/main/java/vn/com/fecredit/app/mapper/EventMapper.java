package vn.com.fecredit.app.mapper;

import org.mapstruct.*;
import java.util.*;
import vn.com.fecredit.app.dto.EventDTO;
import vn.com.fecredit.app.entity.Event;
import vn.com.fecredit.app.entity.EventLocation;

@Mapper(componentModel = "spring", 
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface EventMapper {

    @Mapping(target = "eventLocations", ignore = true) // Bidirectional relationship handled by service
    @Mapping(target = "status", ignore = true)
    Event toEntity(EventDTO.CreateRequest dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(@MappingTarget Event entity, EventDTO.UpdateRequest dto);

    @Mapping(target = "locationCount", expression = "java(event != null && event.getEventLocations() != null ? event.getEventLocations().size() : 0)")
    @Mapping(target = "rewardCount", constant = "0")
    @Mapping(target = "participantCount", expression = "java((int) countParticipants(event))")
    @Mapping(target = "spinCount", constant = "0")
    EventDTO.Response toResponse(Event event);

    default EventDTO.Statistics toStatistics(List<Event> events) {
        if (events == null) events = Collections.emptyList();
        
        return EventDTO.Statistics.builder()
            .totalEvents(events.size())
            .activeEvents(events.stream().filter(e -> e != null && e.isActive()).count())
            .totalLocations(countLocations(events))
            .totalRewards(0L)
            .totalParticipants(countTotalParticipants(events))
            .totalSpins(0L)
            .totalWins(0L)
            .winRate(0.0)
            .averageRewardsPerEvent(0.0)
            .averageLocationsPerEvent(calculateAverage(countLocations(events), events.size()))
            .averageParticipantsPerEvent(calculateAverage(countTotalParticipants(events), events.size()))
            .topLocations(getTopLocations(events))
            .topRewards(Collections.emptySet())
            .build();
    }

    default long countParticipants(Event event) {
        if (event == null || event.getEventLocations() == null) return 0;
        
        return event.getEventLocations().stream()
            .filter(Objects::nonNull)
            .map(location -> location.getParticipantEvents())
            .filter(Objects::nonNull)
            .mapToLong(Set::size)
            .sum();
    }

    default long countLocations(List<Event> events) {
        if (events == null) return 0;
        
        return events.stream()
            .filter(Objects::nonNull)
            .map(Event::getEventLocations)
            .filter(Objects::nonNull)
            .mapToLong(Set::size)
            .sum();
    }

    default long countTotalParticipants(List<Event> events) {
        if (events == null) return 0;
        
        return events.stream()
            .filter(Objects::nonNull)
            .mapToLong(this::countParticipants)
            .sum();
    }

    default double calculateAverage(long total, long count) {
        return count == 0 ? 0.0 : (double) total / count;
    }

    default Set<String> getTopLocations(List<Event> events) {
        if (events == null) return Collections.emptySet();
        
        return events.stream()
            .filter(Objects::nonNull)
            .map(Event::getEventLocations)
            .filter(Objects::nonNull)
            .flatMap(Set::stream)
            .filter(Objects::nonNull)
            .map(EventLocation::getName)
            .filter(Objects::nonNull)
            .limit(10)
            .collect(HashSet::new, HashSet::add, HashSet::addAll);
    }
}
