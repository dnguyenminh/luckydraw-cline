package vn.com.fecredit.app.mapper;

import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;
import vn.com.fecredit.app.dto.EventLocationDTO;
import vn.com.fecredit.app.entity.EventLocation;
import vn.com.fecredit.app.repository.EventRepository;

@Mapper(
    componentModel = "spring",
    unmappedTargetPolicy = ReportingPolicy.IGNORE,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    builder = @Builder(disableBuilder = true)
)
public abstract class EventLocationMapper {

    @Autowired
    protected EventRepository eventRepository;

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "name", source = "name")
    @Mapping(target = "dailySpinLimit", source = "dailySpinLimit")
    @Mapping(target = "winProbabilityMultiplier", source = "winProbabilityMultiplier")
    @Mapping(target = "status", expression = "java(EntityStatus.ACTIVE)")
    @Mapping(target = "events", ignore = true)
    @Mapping(target = "spinHistories", ignore = true)
    public abstract EventLocation toEntity(EventLocationDTO.CreateRequest request);

    @Mapping(target = "locationName", source = "name")
    public abstract EventLocationDTO.Response toResponse(EventLocation entity);

    @Mapping(target = "locationName", source = "name")
    public abstract EventLocationDTO.Summary toSummary(EventLocation entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "name", source = "name")
    @Mapping(target = "events", ignore = true)
    @Mapping(target = "spinHistories", ignore = true)
    public abstract void updateEntity(@MappingTarget EventLocation entity, EventLocationDTO.UpdateRequest request);

    @AfterMapping
    protected void mapStatus(@MappingTarget EventLocation entity, EventLocationDTO.UpdateRequest request) {
        if (request.getActive() != null) {
            entity.setStatus(request.getActive() ? EntityStatus.ACTIVE : EntityStatus.INACTIVE);
        }
    }
}
