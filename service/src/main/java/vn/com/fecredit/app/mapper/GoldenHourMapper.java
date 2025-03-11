package vn.com.fecredit.app.mapper;

import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import vn.com.fecredit.app.dto.GoldenHourDTO;
import vn.com.fecredit.app.entity.GoldenHour;
import vn.com.fecredit.app.entity.EventLocation;
import vn.com.fecredit.app.repository.EventLocationRepository;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;

@Mapper(
        componentModel = "spring",
        imports = {HashSet.class, DayOfWeek.class},
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS
)
@Component
public abstract class GoldenHourMapper {

    @Autowired
    protected EventLocationRepository eventLocationRepository;

    @Mapping(target = "eventLocation", source = "eventLocationId", qualifiedByName = "mapEventLocationFromId")
    @Mapping(target = "spinHistories", ignore = true)
    public abstract GoldenHour toEntity(GoldenHourDTO.CreateRequest request);

    @Mapping(target = "eventLocation", ignore = true)
    @Mapping(target = "spinHistories", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    public abstract void updateEntity(@MappingTarget GoldenHour entity, GoldenHourDTO.UpdateRequest request);

    @Mapping(target = "eventLocationId", source = "eventLocation.id")
    @Mapping(target = "active", expression = "java(entity.isActive(java.time.LocalDateTime.now()))")
    @Mapping(target = "activeDays", expression = "java(entity.getActiveDaysAsSet())")
    @Named("toResponse")
    public abstract GoldenHourDTO.Response toResponse(GoldenHour entity);

    @Mapping(target = "active", expression = "java(entity.isActive(java.time.LocalDateTime.now()))")
    @Mapping(target = "activeDays", expression = "java(entity.getActiveDaysAsSet())")
    @Named("toSummary")
    public abstract GoldenHourDTO.Summary toSummary(GoldenHour entity);

    @Mapping(target = "startTime", expression = "java(formatTime(entity.getStartTime()))")
    @Mapping(target = "endTime", expression = "java(formatTime(entity.getEndTime()))")
    @Mapping(target = "activeDays", expression = "java(entity.getActiveDaysAsSet())")
    public abstract GoldenHourDTO.Schedule toSchedule(GoldenHour entity);

    @IterableMapping(qualifiedByName = "toResponse")
    public abstract List<GoldenHourDTO.Response> toResponseList(List<GoldenHour> entities);

    @IterableMapping(qualifiedByName = "toSummary")
    public abstract List<GoldenHourDTO.Summary> toSummaryList(List<GoldenHour> entities);

    @Named("mapEventLocationFromId")
    protected EventLocation mapEventLocation(Long eventLocationId) {
        if (eventLocationId == null) return null;
        return eventLocationRepository.findById(eventLocationId)
                .orElseThrow(() -> new IllegalArgumentException("EventLocation not found: " + eventLocationId));
    }

    protected String formatTime(LocalDateTime dateTime) {
        if (dateTime == null) return null;
        return dateTime.toLocalTime().toString();
    }

//    @AfterMapping
//    protected void updateActiveDays(@MappingTarget GoldenHour entity, GoldenHourDTO.CreateRequest request) {
//        if (request.getActiveDays() != null) {
//            entity.setActiveDaysFromSet(request.getActiveDays());
//        }
//    }
//
//    @AfterMapping
//    protected void updateActiveDays(@MappingTarget GoldenHour entity, GoldenHourDTO.UpdateRequest request) {
//        if (request.getActiveDays() != null) {
//            entity.setActiveDaysFromSet(request.getActiveDays());
//        }
//    }
}