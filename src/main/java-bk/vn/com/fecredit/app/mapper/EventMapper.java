package vn.com.fecredit.app.mapper;

import org.mapstruct.*;
import java.util.*;
import vn.com.fecredit.app.dto.EventDTO;
import vn.com.fecredit.app.entity.Event;
import vn.com.fecredit.app.entity.EventLocation;
import vn.com.fecredit.app.entity.Reward;

@Mapper(componentModel = "spring", 
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface EventMapper {

    @Mapping(target = "locations", ignore = true)
    @Mapping(target = "rewards", ignore = true)
    @Mapping(target = "participants", ignore = true)
    @Mapping(target = "spinHistories", ignore = true)
    @Mapping(target = "goldenHours", ignore = true)
    Event toEntity(EventDTO.CreateRequest dto);

    @Mapping(target = "locations", ignore = true)
    @Mapping(target = "rewards", ignore = true)
    @Mapping(target = "participants", ignore = true)
    @Mapping(target = "spinHistories", ignore = true)
    @Mapping(target = "goldenHours", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(@MappingTarget Event entity, EventDTO.UpdateRequest dto);

    @Mapping(target = "locationCount", expression = "java(event.getLocations() != null ? event.getLocations().size() : 0)")
    @Mapping(target = "rewardCount", expression = "java(event.getRewards() != null ? event.getRewards().size() : 0)")
    @Mapping(target = "participantCount", expression = "java(event.getParticipants() != null ? event.getParticipants().size() : 0)")
    @Mapping(target = "spinCount", expression = "java(event.getSpinHistories() != null ? event.getSpinHistories().size() : 0)")
    EventDTO.Response toResponse(Event event);

    default List<EventDTO.Response> toResponseList(List<Event> events) {
        if (events == null) return new ArrayList<>();
        return events.stream()
                .map(this::toResponse)
                .toList();
    }

    default Set<EventDTO.Response> toResponseSet(Set<Event> events) {
        if (events == null) return new HashSet<>();
        return events.stream()
                .map(this::toResponse)
                .collect(HashSet::new, Set::add, Set::addAll);
    }

    @Mapping(target = "totalEvents", expression = "java(events.size())")
    @Mapping(target = "activeEvents", expression = "java(countActiveEvents(events))")
    @Mapping(target = "totalLocations", expression = "java(countTotalLocations(events))")
    @Mapping(target = "totalRewards", expression = "java(countTotalRewards(events))")
    @Mapping(target = "totalParticipants", expression = "java(countTotalParticipants(events))")
    @Mapping(target = "totalSpins", expression = "java(countTotalSpins(events))")
    @Mapping(target = "totalWins", expression = "java(countTotalWins(events))")
    @Mapping(target = "winRate", expression = "java(calculateWinRate(events))")
    @Mapping(target = "averageRewardsPerEvent", expression = "java(calculateAverageRewards(events))")
    @Mapping(target = "averageLocationsPerEvent", expression = "java(calculateAverageLocations(events))")
    @Mapping(target = "averageParticipantsPerEvent", expression = "java(calculateAverageParticipants(events))")
    @Mapping(target = "topLocations", expression = "java(getTopLocations(events))")
    @Mapping(target = "topRewards", expression = "java(getTopRewards(events))")
    EventDTO.Statistics toStatistics(List<Event> events);

    default long countActiveEvents(List<Event> events) {
        if (events == null) return 0;
        return events.stream()
            .filter(e -> EventDTO.STATUS_ACTIVE == e.getStatus())
            .count();
    }

    default long countTotalLocations(List<Event> events) {
        if (events == null) return 0;
        return events.stream()
            .map(Event::getLocations)
            .filter(Objects::nonNull)
            .mapToLong(Set::size)
            .sum();
    }

    default long countTotalRewards(List<Event> events) {
        if (events == null) return 0;
        return events.stream()
            .map(Event::getRewards)
            .filter(Objects::nonNull)
            .mapToLong(Set::size)
            .sum();
    }

    default long countTotalParticipants(List<Event> events) {
        if (events == null) return 0;
        return events.stream()
            .map(Event::getParticipants)
            .filter(Objects::nonNull)
            .mapToLong(Set::size)
            .sum();
    }

    default long countTotalSpins(List<Event> events) {
        if (events == null) return 0;
        return events.stream()
            .map(Event::getSpinHistories)
            .filter(Objects::nonNull)
            .mapToLong(Set::size)
            .sum();
    }

    default long countTotalWins(List<Event> events) {
        if (events == null) return 0;
        return events.stream()
            .flatMap(e -> e.getSpinHistories().stream())
            .filter(s -> s.getReward() != null)
            .count();
    }

    default double calculateWinRate(List<Event> events) {
        long totalSpins = countTotalSpins(events);
        return totalSpins > 0 ? (double) countTotalWins(events) / totalSpins : 0;
    }

    default double calculateAverageRewards(List<Event> events) {
        if (events == null || events.isEmpty()) return 0;
        return (double) countTotalRewards(events) / events.size();
    }

    default double calculateAverageLocations(List<Event> events) {
        if (events == null || events.isEmpty()) return 0;
        return (double) countTotalLocations(events) / events.size();
    }

    default double calculateAverageParticipants(List<Event> events) {
        if (events == null || events.isEmpty()) return 0;
        return (double) countTotalParticipants(events) / events.size();
    }

    default Set<String> getTopLocations(List<Event> events) {
        if (events == null) return new HashSet<>();
        return events.stream()
            .flatMap(e -> e.getLocations().stream())
            .map(EventLocation::getName)
            .limit(10)
            .collect(HashSet::new, HashSet::add, HashSet::addAll);
    }

    default Set<String> getTopRewards(List<Event> events) {
        if (events == null) return new HashSet<>();
        return events.stream()
            .flatMap(e -> e.getRewards().stream())
            .map(Reward::getName)
            .limit(10)
            .collect(HashSet::new, HashSet::add, HashSet::addAll);
    }

    @AfterMapping
    default void linkEntities(@MappingTarget Event event) {
        if (event.getLocations() != null) {
            event.getLocations().forEach(l -> l.setEvent(event));
        }
        if (event.getRewards() != null) {
            event.getRewards().forEach(r -> r.setEvent(event));
        }
    }
}
