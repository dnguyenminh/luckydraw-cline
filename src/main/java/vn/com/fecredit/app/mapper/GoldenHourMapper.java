package vn.com.fecredit.app.mapper;

import org.mapstruct.*;
import vn.com.fecredit.app.dto.GoldenHourDTO;
import vn.com.fecredit.app.entity.Event;
import vn.com.fecredit.app.entity.GoldenHour;
import vn.com.fecredit.app.entity.Reward;
import vn.com.fecredit.app.entity.SpinHistory;

import java.time.LocalDateTime;
import java.util.List;

@Mapper(
    componentModel = "spring",
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface GoldenHourMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "event", source = "eventId")
    @Mapping(target = "reward", source = "rewardId")
    @Mapping(target = "activeDays", source = "activeDays")
    @Mapping(target = "multiplier", source = "probabilityMultiplier")
    GoldenHour toEntity(GoldenHourDTO.CreateRequest dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "event", ignore = true)
    @Mapping(target = "reward", ignore = true)
    void updateEntityFromDto(GoldenHourDTO.UpdateRequest dto, @MappingTarget GoldenHour entity);

    @Mapping(target = "eventId", source = "event.id")
    @Mapping(target = "eventName", source = "event.name")
    @Mapping(target = "rewardId", source = "reward.id")
    @Mapping(target = "rewardName", source = "reward.name")
    @Mapping(target = "probabilityMultiplier", source = "multiplier")
    GoldenHourDTO toDto(GoldenHour entity);

    @Mapping(target = "isActiveNow", expression = "java(isActiveNow(entity))")
    @Mapping(target = "timeUntilNext", expression = "java(calculateTimeUntilNext(entity))")
    @Mapping(target = "nextOccurrence", expression = "java(calculateNextOccurrence(entity))")
    @Mapping(target = "baseWinProbability", source = "reward.probability")
    @Mapping(target = "effectiveProbability", expression = "java(calculateEffectiveProbability(entity))")
    GoldenHourDTO.GoldenHourSchedule toSchedule(GoldenHour entity);

    @Mapping(target = "totalSpins", expression = "java(calculateTotalSpins(entity))")
    @Mapping(target = "winningSpins", expression = "java(calculateWinningSpins(entity))")
    @Mapping(target = "winRate", expression = "java(calculateWinRate(entity))")
    @Mapping(target = "averageWinProbability", expression = "java(calculateAvgWinProbability(entity))")
    @Mapping(target = "effectivenessRatio", expression = "java(calculateEffectivenessRatio(entity))")
    @Mapping(target = "lastSpinTime", expression = "java(findLastSpinTime(entity))")
    GoldenHourDTO.GoldenHourStatistics toStatistics(GoldenHour entity);

    List<GoldenHourDTO> toDtoList(List<GoldenHour> entities);
    
    default Event eventFromId(Long eventId) {
        if (eventId == null) return null;
        Event event = new Event();
        event.setId(eventId);
        return event;
    }

    default Reward rewardFromId(Long rewardId) {
        if (rewardId == null) return null;
        Reward reward = new Reward();
        reward.setId(rewardId);
        return reward;
    }

    default boolean isActiveNow(GoldenHour goldenHour) {
        return goldenHour.isActive(LocalDateTime.now());
    }

    default Long calculateTimeUntilNext(GoldenHour goldenHour) {
        // Calculate time until next golden hour in minutes
        LocalDateTime now = LocalDateTime.now();
        if (goldenHour.getEndTime().isBefore(now.toLocalTime())) {
            return java.time.Duration.between(now, now.plusDays(1).with(goldenHour.getStartTime())).toMinutes();
        } else if (goldenHour.getStartTime().isAfter(now.toLocalTime())) {
            return java.time.Duration.between(now, now.with(goldenHour.getStartTime())).toMinutes();
        }
        return 0L;
    }

    default LocalDateTime calculateNextOccurrence(GoldenHour goldenHour) {
        LocalDateTime now = LocalDateTime.now();
        if (!goldenHour.isActive()) {
            return null;
        }
        if (goldenHour.getEndTime().isBefore(now.toLocalTime())) {
            return now.plusDays(1).with(goldenHour.getStartTime());
        } else if (goldenHour.getStartTime().isAfter(now.toLocalTime())) {
            return now.with(goldenHour.getStartTime());
        }
        return now;
    }

    default Double calculateEffectiveProbability(GoldenHour goldenHour) {
        return goldenHour.getReward() != null ? 
               goldenHour.getReward().getProbability() * goldenHour.getMultiplier() : 
               0.0;
    }

    default int calculateTotalSpins(GoldenHour goldenHour) {
        return goldenHour.getReward() != null ? 
               goldenHour.getReward().getSpinHistories().size() : 
               0;
    }

    default int calculateWinningSpins(GoldenHour goldenHour) {
        if (goldenHour.getReward() == null) {
            return 0;
        }
        return (int) goldenHour.getReward().getSpinHistories().stream()
                .filter(SpinHistory::isWin)
                .count();
    }

    default double calculateWinRate(GoldenHour goldenHour) {
        int total = calculateTotalSpins(goldenHour);
        return total > 0 ? (double) calculateWinningSpins(goldenHour) / total : 0.0;
    }

    default double calculateAvgWinProbability(GoldenHour goldenHour) {
        if (goldenHour.getReward() == null) {
            return 0.0;
        }
        return goldenHour.getReward().getSpinHistories().stream()
                .mapToDouble(SpinHistory::getWinProbability)
                .average()
                .orElse(0.0);
    }

    default double calculateEffectivenessRatio(GoldenHour goldenHour) {
        double actualWinRate = calculateWinRate(goldenHour);
        double expectedWinRate = calculateEffectiveProbability(goldenHour);
        return expectedWinRate > 0 ? actualWinRate / expectedWinRate : 0.0;
    }

    default LocalDateTime findLastSpinTime(GoldenHour goldenHour) {
        if (goldenHour.getReward() == null) {
            return null;
        }
        return goldenHour.getReward().getSpinHistories().stream()
                .map(SpinHistory::getSpinTime)
                .max(LocalDateTime::compareTo)
                .orElse(null);
    }
}
