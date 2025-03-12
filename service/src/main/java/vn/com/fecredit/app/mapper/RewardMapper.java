package vn.com.fecredit.app.mapper;

import org.mapstruct.*;
import vn.com.fecredit.app.dto.RewardDTO;
import vn.com.fecredit.app.entity.Reward;
import vn.com.fecredit.app.entity.base.AbstractStatusAwareEntity;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Mapper(componentModel = "spring", 
       unmappedTargetPolicy = ReportingPolicy.IGNORE,
       imports = {LocalDateTime.class})
public interface RewardMapper {

    // Using constants from AbstractStatusAwareEntity
    // int STATUS_ACTIVE = 1;
    // int STATUS_INACTIVE = 0;

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "eventLocation.id", source = "eventLocationId")
    @Mapping(target = "totalQuantity", source = "initialQuantity")
    @Mapping(target = "remainingQuantity", source = "initialQuantity")
    @Mapping(target = "status", constant = "1")
    @Mapping(target = "createdAt", expression = "java(LocalDateTime.now())")
    @Mapping(target = "updatedAt", expression = "java(LocalDateTime.now())")
    Reward toEntity(RewardDTO.CreateRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "status", expression = "java(mapActiveToStatus(request.getActive()))")
    @Mapping(target = "updatedAt", expression = "java(LocalDateTime.now())")
    void updateEntity(@MappingTarget Reward entity, RewardDTO.UpdateRequest request);

    @Mapping(target = "initialQuantity", source = "totalQuantity")
    @Mapping(target = "active", expression = "java(entity.isActive())")
    @Mapping(target = "eventLocationId", source = "eventLocation.id")
    RewardDTO.Response toResponse(Reward entity);

    @Mapping(target = "active", expression = "java(entity.isActive())")
    RewardDTO.Summary toSummary(Reward entity);

    default List<RewardDTO.Response> toResponseList(List<Reward> entities) {
        if (entities == null) return List.of();
        return entities.stream()
                .filter(Objects::nonNull)
                .map(this::toResponse)
                .toList();
    }

    default List<RewardDTO.Summary> toSummaryList(List<Reward> entities) {
        if (entities == null) return List.of();
        return entities.stream()
                .filter(Objects::nonNull)
                .map(this::toSummary)
                .toList();
    }

    default Integer mapActiveToStatus(Boolean active) {
        if (active == null) return null;
        return active ? AbstractStatusAwareEntity.STATUS_ACTIVE : AbstractStatusAwareEntity.STATUS_INACTIVE;
    }

    @AfterMapping
    default void afterToEntity(@MappingTarget Reward entity) {
        if (entity.getStatus() == 0) {
            entity.setStatus(AbstractStatusAwareEntity.STATUS_ACTIVE);
        }

        // Initialize timestamps if not set
        if (entity.getValidFrom() == null) {
            entity.setValidFrom(LocalDateTime.now());
        }
        if (entity.getValidUntil() == null) {
            entity.setValidUntil(LocalDateTime.now().plusYears(1));
        }

        // Initialize remaining quantity if not set
        if (entity.getRemainingQuantity() == null && entity.getTotalQuantity() != null) {
            entity.setRemainingQuantity(entity.getTotalQuantity());
        }
    }
}
