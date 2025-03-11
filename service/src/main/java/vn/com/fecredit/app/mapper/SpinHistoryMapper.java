package vn.com.fecredit.app.mapper;

import org.mapstruct.*;
import vn.com.fecredit.app.dto.SpinHistoryDTO;
import vn.com.fecredit.app.entity.ParticipantEvent;
import vn.com.fecredit.app.entity.Reward;
import vn.com.fecredit.app.entity.SpinHistory;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Comparator;

@Mapper(
    componentModel = "spring",
    imports = {LocalDateTime.class, Comparator.class},
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS
)
public interface SpinHistoryMapper {

    @BeforeMapping
    default void setDefaultValues(@MappingTarget SpinHistory entity) {
        entity.setSpinTime(LocalDateTime.now());
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", constant = "1")
    @Mapping(target = "participantEvent", source = "participantEventId", qualifiedByName = "participantEventIdToEntity")
    @Mapping(target = "win", constant = "false")
    @Mapping(target = "finalized", constant = "false")
    @Mapping(target = "pointsEarned", constant = "0")
    SpinHistory toEntity(SpinHistoryDTO.CreateRequest dto);

    @Mapping(target = "participantEventId", source = "participantEvent.id")
    @Mapping(target = "participantName", source = "participantEvent.participant.name")
    @Mapping(target = "eventName", source = "participantEvent.event.name")
    @Mapping(target = "locationName", source = "participantEvent.eventLocation.name")
    @Mapping(target = "rewardId", source = "reward.id")
    @Mapping(target = "rewardName", source = "reward.name")
    SpinHistoryDTO.Response toResponse(SpinHistory entity);

    @Mapping(target = "eventName", source = "participantEvent.event.name")
    @Mapping(target = "locationName", source = "participantEvent.eventLocation.name")
    @Mapping(target = "participantName", source = "participantEvent.participant.name")
    SpinHistoryDTO.Summary toSummary(SpinHistory entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "reward", source = "rewardId", qualifiedByName = "rewardIdToEntity")
    void updateEntity(@MappingTarget SpinHistory entity, SpinHistoryDTO.UpdateRequest dto);

    @IterableMapping(elementTargetType = SpinHistoryDTO.Response.class)
    List<SpinHistoryDTO.Response> toResponseList(List<SpinHistory> entities);

    @IterableMapping(elementTargetType = SpinHistoryDTO.Summary.class)
    List<SpinHistoryDTO.Summary> toSummaryList(List<SpinHistory> entities);

    @Mapping(target = "participantEventId", source = "id")
    @Mapping(target = "participantName", source = "participant.name")
    @Mapping(target = "eventName", source = "event.name")
    @Mapping(target = "locationName", source = "eventLocation.name")
    @Mapping(target = "totalSpins", expression = "java(entity.getSpinHistories().size())")
    @Mapping(target = "winningSpins", expression = "java(countWinningSpins(entity))")
    @Mapping(target = "totalPoints", expression = "java(calculateTotalPoints(entity))")
    @Mapping(target = "winRate", expression = "java(calculateWinRate(entity))")
    @Mapping(target = "firstSpinTime", expression = "java(findFirstSpinTime(entity))")
    @Mapping(target = "lastSpinTime", expression = "java(findLastSpinTime(entity))")
    SpinHistoryDTO.Statistics toStatistics(ParticipantEvent entity);

    @Named("participantEventIdToEntity")
    default ParticipantEvent participantEventIdToEntity(Long id) {
        if (id == null) {
            return null;
        }
        return ParticipantEvent.builder().id(id).build();
    }

    @Named("rewardIdToEntity")
    default Reward rewardIdToEntity(Long id) {
        if (id == null) {
            return null;
        }
        return Reward.builder().id(id).build();
    }

    default int countWinningSpins(ParticipantEvent entity) {
        if (entity == null || entity.getSpinHistories() == null) {
            return 0;
        }
        return (int) entity.getSpinHistories().stream()
                .filter(SpinHistory::isWin)
                .count();
    }

    default int calculateTotalPoints(ParticipantEvent entity) {
        if (entity == null || entity.getSpinHistories() == null) {
            return 0;
        }
        return entity.getSpinHistories().stream()
                .mapToInt(sh -> sh.getPointsEarned() != null ? sh.getPointsEarned() : 0)
                .sum();
    }

    default double calculateWinRate(ParticipantEvent entity) {
        if (entity == null || entity.getSpinHistories() == null) {
            return 0.0;
        }
        int total = entity.getSpinHistories().size();
        return total > 0 ? (double) countWinningSpins(entity) / total : 0.0;
    }

    default LocalDateTime findFirstSpinTime(ParticipantEvent entity) {
        if (entity == null || entity.getSpinHistories() == null) {
            return null;
        }
        return entity.getSpinHistories().stream()
                .map(SpinHistory::getSpinTime)
                .min(Comparator.naturalOrder())
                .orElse(null);
    }

    default LocalDateTime findLastSpinTime(ParticipantEvent entity) {
        if (entity == null || entity.getSpinHistories() == null) {
            return null;
        }
        return entity.getSpinHistories().stream()
                .map(SpinHistory::getSpinTime)
                .max(Comparator.naturalOrder())
                .orElse(null);
    }
}
