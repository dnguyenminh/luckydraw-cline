package vn.com.fecredit.app.mapper;

import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;
import vn.com.fecredit.app.dto.RewardDTO;
import vn.com.fecredit.app.entity.Event;
import vn.com.fecredit.app.entity.Reward;
import vn.com.fecredit.app.entity.SpinHistory;
import vn.com.fecredit.app.repository.EventRepository;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Mapper(
    componentModel = "spring",
    imports = {HashSet.class},
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE,
    nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS
)
public abstract class RewardMapper {

    @Autowired
    protected EventRepository eventRepository;

    @Named("toEntity")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "event", source = "eventId")
    @Mapping(target = "remainingQuantity", source = "initialQuantity")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "spinHistories", expression = "java(new HashSet<>())")
    @Mapping(target = "goldenHours", expression = "java(new HashSet<>())")
    public abstract Reward toEntity(RewardDTO.CreateRequest request);

    @Named("updateEntity")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "event", ignore = true)
    @Mapping(target = "initialQuantity", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "spinHistories", ignore = true)
    @Mapping(target = "goldenHours", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    public abstract void updateEntity(@MappingTarget Reward entity, RewardDTO.UpdateRequest request);

    @Named("toResponse")
    @Mapping(target = "eventId", source = "event.id")
    @Mapping(target = "active", expression = "java(entity.isActive())")
    public abstract RewardDTO.Response toResponse(Reward entity);

    @Named("toSummary")
    @Mapping(target = "active", expression = "java(entity.isActive())")
    public abstract RewardDTO.Summary toSummary(Reward entity);

    @Named("toStatistics")
    @Mapping(target = "active", expression = "java(entity.isActive())")
    @Mapping(target = "claimedQuantity", expression = "java(entity.getInitialQuantity() - entity.getRemainingQuantity())")
    @Mapping(target = "effectiveProbability", expression = "java(calculateEffectiveProbability(entity))")
    @Mapping(target = "uniqueWinners", expression = "java(calculateUniqueWinners(entity))")
    public abstract RewardDTO.Statistics toStatistics(Reward entity);

    @Named("toResponseList")
    @IterableMapping(qualifiedByName = "toResponse")
    public abstract List<RewardDTO.Response> toResponseList(List<Reward> entities);

    @Named("toSummaryList")
    @IterableMapping(qualifiedByName = "toSummary")
    public abstract List<RewardDTO.Summary> toSummaryList(List<Reward> entities);

    protected Event mapEvent(Long eventId) {
        if (eventId == null) return null;
        return eventRepository.findById(eventId)
            .orElseThrow(() -> new IllegalArgumentException("Event not found: " + eventId));
    }

    protected Double calculateEffectiveProbability(Reward reward) {
        if (reward == null) return 0.0;
        Set<SpinHistory> histories = reward.getSpinHistories();
        if (histories == null || histories.isEmpty()) {
            return reward.getWinProbability();
        }
        long totalSpins = histories.size();
        long winningSpins = histories.stream()
            .filter(spin -> spin.getReward() != null && spin.getReward().equals(reward))
            .count();
        return totalSpins > 0 ? (double) winningSpins / totalSpins : reward.getWinProbability();
    }

    protected Integer calculateUniqueWinners(Reward reward) {
        if (reward == null) return 0;
        Set<SpinHistory> histories = reward.getSpinHistories();
        if (histories == null || histories.isEmpty()) {
            return 0;
        }
        return (int) histories.stream()
            .filter(spin -> spin.getReward() != null && spin.getReward().equals(reward))
            .map(spin -> spin.getParticipant().getId())
            .distinct()
            .count();
    }

    @AfterMapping
    protected void validateProbability(@MappingTarget Reward entity, RewardDTO.CreateRequest request) {
        if (request.getWinProbability() != null 
            && (request.getWinProbability() < 0 || request.getWinProbability() > 1)) {
            throw new IllegalArgumentException("Win probability must be between 0 and 1");
        }
    }

    @AfterMapping
    protected void validateProbability(@MappingTarget Reward entity, RewardDTO.UpdateRequest request) {
        if (request.getWinProbability() != null 
            && (request.getWinProbability() < 0 || request.getWinProbability() > 1)) {
            throw new IllegalArgumentException("Win probability must be between 0 and 1");
        }
    }
}
