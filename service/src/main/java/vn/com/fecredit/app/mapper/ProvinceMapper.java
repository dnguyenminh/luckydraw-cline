package vn.com.fecredit.app.mapper;

import org.mapstruct.*;
import vn.com.fecredit.app.dto.ProvinceDTO;
import vn.com.fecredit.app.entity.Province;
import vn.com.fecredit.app.entity.Region;

import java.util.List;

@Mapper(componentModel = "spring", uses = {})
public interface ProvinceMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", constant = "1")
    @Mapping(target = "region", source = "regionId", qualifiedByName = "regionIdToEntity")
    Province toEntity(ProvinceDTO.CreateRequest dto);

    @Mapping(target = "regionId", source = "region.id")
    @Mapping(target = "regionName", source = "region.name")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    ProvinceDTO.Response toResponse(Province entity);

    @Mapping(target = "regionName", source = "region.name")
    @Mapping(target = "totalParticipants", expression = "java(countTotalParticipants(entity))")
    @Mapping(target = "activeParticipants", expression = "java(countActiveParticipants(entity))")
    ProvinceDTO.Summary toSummary(Province entity);

    @Mapping(target = "regionId", source = "region.id")
    @Mapping(target = "regionName", source = "region.name")
    @Mapping(target = "totalParticipants", expression = "java(countTotalParticipants(entity))")
    @Mapping(target = "activeParticipants", expression = "java(countActiveParticipants(entity))")
    @Mapping(target = "totalSpins", expression = "java(calculateTotalSpins(entity))")
    @Mapping(target = "totalWins", expression = "java(calculateTotalWins(entity))")
    @Mapping(target = "winRate", expression = "java(calculateWinRate(entity))")
    @Mapping(target = "startDate", ignore = true)
    @Mapping(target = "endDate", ignore = true)
    ProvinceDTO.Statistics toStatistics(Province entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntity(@MappingTarget Province entity, ProvinceDTO.UpdateRequest dto);

    List<ProvinceDTO.Response> toResponseList(List<Province> entities);
    List<ProvinceDTO.Summary> toSummaryList(List<Province> entities);

    @Named("regionIdToEntity")
    default Region regionIdToEntity(Long id) {
        if (id == null) {
            return null;
        }
        return Region.builder().id(id).build();
    }

    default int countTotalParticipants(Province entity) {
        return entity.getParticipantEvents() != null ? entity.getParticipantEvents().size() : 0;
    }

    default int countActiveParticipants(Province entity) {
        return entity.getParticipantEvents() != null ? 
            (int) entity.getParticipantEvents().stream()
                .filter(pe -> pe.getStatus() == 1)
                .count() : 0;
    }

    default int calculateTotalSpins(Province entity) {
        return entity.getParticipantEvents() != null ?
            entity.getParticipantEvents().stream()
                .mapToInt(pe -> pe.getSpinHistories() != null ? pe.getSpinHistories().size() : 0)
                .sum() : 0;
    }

    default int calculateTotalWins(Province entity) {
        return entity.getParticipantEvents() != null ?
            entity.getParticipantEvents().stream()
                .mapToInt(pe -> pe.getSpinHistories() != null ? 
                    (int) pe.getSpinHistories().stream().filter(sh -> sh.isWin()).count() : 0)
                .sum() : 0;
    }

    default Double calculateWinRate(Province entity) {
        int totalSpins = calculateTotalSpins(entity);
        return totalSpins > 0 ? 
            (double) calculateTotalWins(entity) / totalSpins : 0.0;
    }
}
