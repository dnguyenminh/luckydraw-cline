package vn.com.fecredit.app.mapper;

import org.mapstruct.*;
import vn.com.fecredit.app.dto.SpinHistoryDTO;
import vn.com.fecredit.app.entity.SpinHistory;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", 
       unmappedTargetPolicy = ReportingPolicy.IGNORE,
       uses = {EventMapper.class, ParticipantMapper.class, RewardMapper.class})
public interface SpinHistoryMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "event.id", source = "eventId")
    @Mapping(target = "participant.id", source = "participantId")
    @Mapping(target = "status", constant = "1")
    @Mapping(target = "spinTime", ignore = true)
    SpinHistory toEntity(SpinHistoryDTO.CreateRequest dto);

    @Mapping(target = "eventId", source = "event.id")
    @Mapping(target = "participantId", source = "participant.id")
    @Mapping(target = "rewardId", source = "reward.id")
    @Mapping(target = "rewardName", source = "reward.name")
    @Mapping(target = "status", expression = "java(EntityStatus.fromCode(entity.getStatus()))")
    SpinHistoryDTO.SpinResponse toSpinResponse(SpinHistory entity);

    default List<SpinHistoryDTO.SpinResponse> toSpinResponses(Set<SpinHistory> entities) {
        if (entities == null) return List.of();
        return entities.stream()
                      .map(this::toSpinResponse)
                      .collect(Collectors.toList());
    }

    @Mapping(target = "eventId", source = "event.id")
    @Mapping(target = "participantId", source = "participant.id")
    @Mapping(target = "rewardId", source = "reward.id")
    @Mapping(target = "status", expression = "java(EntityStatus.fromCode(entity.getStatus()))")
    SpinHistoryDTO.Summary toSummary(SpinHistory entity);

    default List<SpinHistoryDTO.Summary> toSummaries(Set<SpinHistory> entities) {
        if (entities == null) return List.of();
        return entities.stream()
                      .map(this::toSummary)
                      .collect(Collectors.toList());
    }

    @Mapping(target = "eventId", source = "event.id") 
    @Mapping(target = "eventName", source = "event.name")
    @Mapping(target = "participantId", source = "participant.id")
    @Mapping(target = "participantName", source = "participant.fullName")
    @Mapping(target = "rewardId", source = "reward.id")
    @Mapping(target = "rewardName", source = "reward.name")
    @Mapping(target = "status", expression = "java(EntityStatus.fromCode(entity.getStatus()))")
    SpinHistoryDTO.ListResponse toListResponse(SpinHistory entity);

    default List<SpinHistoryDTO.ListResponse> toListResponses(Set<SpinHistory> entities) {
        if (entities == null) return List.of();
        return entities.stream()
                      .map(this::toListResponse)
                      .collect(Collectors.toList());
    }

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromDto(SpinHistoryDTO.UpdateRequest dto, @MappingTarget SpinHistory entity);

    default void updateStatus(SpinHistory entity, EntityStatus status) {
        if (status != null) {
            entity.setStatus(status.getCode());
        }
    }

    @AfterMapping
    default void afterToEntity(@MappingTarget SpinHistory entity) {
        if (entity.getStatus() == 0) {
            entity.setStatus(SpinHistory.STATUS_ACTIVE);
        }
    }
}
