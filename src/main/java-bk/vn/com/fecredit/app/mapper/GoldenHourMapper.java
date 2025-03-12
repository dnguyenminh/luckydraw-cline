package vn.com.fecredit.app.mapper;

import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;
import vn.com.fecredit.app.dto.GoldenHourDTO;
import vn.com.fecredit.app.dto.RewardDTO;
import vn.com.fecredit.app.entity.Event;
import vn.com.fecredit.app.entity.GoldenHour;
import vn.com.fecredit.app.entity.Reward;
import vn.com.fecredit.app.repository.EventRepository;
import vn.com.fecredit.app.repository.RewardRepository;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Mapper(
    componentModel = "spring",
    imports = {HashSet.class, DayOfWeek.class},
    uses = {RewardMapper.class},
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS
)
public abstract class GoldenHourMapper {

    @Autowired
    protected EventRepository eventRepository;
    
    @Autowired
    protected RewardRepository rewardRepository;
    
    @Autowired
    protected RewardMapper rewardMapper;

    @Named("toEntity")
    @Mapping(target = "event", source = "eventId", qualifiedByName = "mapEventFromId")
    @Mapping(target = "reward", source = "rewardId", qualifiedByName = "mapRewardFromId")
    @Mapping(target = "spinHistories", expression = "java(new HashSet<>())")
    @Mapping(target = "activeDays", ignore = true)
    public abstract GoldenHour toEntity(GoldenHourDTO.CreateRequest request);

    @Named("updateEntity")
    @Mapping(target = "event", ignore = true)
    @Mapping(target = "reward", source = "rewardId", qualifiedByName = "mapRewardFromId")
    @Mapping(target = "spinHistories", ignore = true)
    @Mapping(target = "activeDays", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    public abstract void updateEntity(@MappingTarget GoldenHour entity, GoldenHourDTO.UpdateRequest request);

    @Named("toResponse")
    @Mapping(target = "eventId", source = "event.id")
    @Mapping(target = "reward", source = "reward", qualifiedByName = "toRewardSummary")
    @Mapping(target = "active", expression = "java(entity.isActive(java.time.LocalDateTime.now()))")
    @Mapping(target = "activeDays", expression = "java(entity.getActiveDaysAsSet())")
    public abstract GoldenHourDTO.Response toResponse(GoldenHour entity);

    @Named("toSummary")
    @Mapping(target = "active", expression = "java(entity.isActive(java.time.LocalDateTime.now()))")
    @Mapping(target = "activeDays", expression = "java(entity.getActiveDaysAsSet())")
    public abstract GoldenHourDTO.Summary toSummary(GoldenHour entity);

    @Named("toSchedule")
    @Mapping(target = "startTime", expression = "java(formatTime(entity.getStartTime()))")
    @Mapping(target = "endTime", expression = "java(formatTime(entity.getEndTime()))")
    @Mapping(target = "activeDays", expression = "java(entity.getActiveDaysAsSet())")
    public abstract GoldenHourDTO.Schedule toSchedule(GoldenHour entity);

    @Named("toResponseList")
    @IterableMapping(qualifiedByName = "toResponse")
    public abstract List<GoldenHourDTO.Response> toResponseList(List<GoldenHour> entities);

    @Named("toSummaryList")
    @IterableMapping(qualifiedByName = "toSummary")
    public abstract List<GoldenHourDTO.Summary> toSummaryList(List<GoldenHour> entities);

    @Named("mapEventFromId")
    protected Event mapEvent(Long eventId) {
        if (eventId == null) return null;
        return eventRepository.findById(eventId)
            .orElseThrow(() -> new IllegalArgumentException("Event not found: " + eventId));
    }

    @Named("mapRewardFromId")
    protected Reward mapReward(Long rewardId) {
        if (rewardId == null) return null;
        return rewardRepository.findById(rewardId)
            .orElseThrow(() -> new IllegalArgumentException("Reward not found: " + rewardId));
    }

    protected String formatTime(LocalDateTime dateTime) {
        if (dateTime == null) return null;
        return dateTime.toLocalTime().toString();
    }

    @AfterMapping
    protected void updateActiveDays(@MappingTarget GoldenHour entity, GoldenHourDTO.CreateRequest request) {
        if (request.getActiveDays() != null) {
            entity.setActiveDaysFromSet(request.getActiveDays());
        }
    }

    @AfterMapping
    protected void updateActiveDays(@MappingTarget GoldenHour entity, GoldenHourDTO.UpdateRequest request) {
        if (request.getActiveDays() != null) {
            entity.setActiveDaysFromSet(request.getActiveDays());
        }
    }
}
