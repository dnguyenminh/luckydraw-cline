package vn.com.fecredit.app.mapper;

import org.mapstruct.*;
import vn.com.fecredit.app.dto.EventLocationDTO;
import vn.com.fecredit.app.entity.Event;
import vn.com.fecredit.app.entity.EventLocation;
import vn.com.fecredit.app.entity.Region;

@Mapper(componentModel = "spring", uses = {})
public interface EventLocationMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", constant = "1")
    @Mapping(target = "event", source = "eventId", qualifiedByName = "eventIdToEntity")
    @Mapping(target = "region", source = "regionId", qualifiedByName = "regionIdToEntity")
    @Mapping(target = "participantEvents", ignore = true)
    EventLocation toEntity(EventLocationDTO.CreateRequest dto);

    @Mapping(target = "eventId", source = "event.id")
    @Mapping(target = "eventName", source = "event.name")
    @Mapping(target = "regionId", source = "region.id")
    @Mapping(target = "regionName", source = "region.name")
    EventLocationDTO.Response toDto(EventLocation entity);

    @Mapping(target = "eventName", source = "event.name")
    @Mapping(target = "regionName", source = "region.name")
    @Mapping(target = "totalParticipants", expression = "java(entity.getParticipantEvents().size())")
    @Mapping(target = "activeParticipants", expression = "java(countActiveParticipants(entity))")
    EventLocationDTO.Summary toSummary(EventLocation entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "event", ignore = true)
    @Mapping(target = "region", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "participantEvents", ignore = true)
    void updateEntity(EventLocationDTO.UpdateRequest dto, @MappingTarget EventLocation entity);

    @Named("eventIdToEntity")
    default Event eventIdToEntity(Long id) {
        if (id == null) {
            return null;
        }
        return Event.builder().id(id).build();
    }

    @Named("regionIdToEntity")
    default Region regionIdToEntity(Long id) {
        if (id == null) {
            return null;
        }
        return Region.builder().id(id).build();
    }

    default int countActiveParticipants(EventLocation entity) {
        return (int) entity.getParticipantEvents().stream()
                .filter(pe -> pe.getStatus() == 1)
                .count();
    }
}
