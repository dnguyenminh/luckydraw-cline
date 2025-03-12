package vn.com.fecredit.app.mapper;

import org.mapstruct.*;
import vn.com.fecredit.app.dto.RegionDTO;
import vn.com.fecredit.app.entity.Region;

import java.util.List;

@Mapper(componentModel = "spring", uses = {ProvinceMapper.class, EventLocationMapper.class})
public interface RegionMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "provinces", ignore = true)
    @Mapping(target = "eventLocations", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    Region toEntity(RegionDTO.CreateRequest request);

    @Mapping(target = "locationCount", expression = "java(region.getEventLocations().size())")
    @Mapping(target = "provinceCount", expression = "java(region.getProvinces().size())")
    @Mapping(target = "activeLocationCount", expression = "java(region.getActiveLocationCount())")
    @Mapping(target = "activeProvinceCount", expression = "java(region.getActiveProvinceCount())")
    RegionDTO.Response toResponse(Region region);

    @Mapping(target = "locationCount", expression = "java(region.getEventLocations().size())")
    @Mapping(target = "provinceCount", expression = "java(region.getProvinces().size())")
    RegionDTO.Summary toSummary(Region region);

    @Mapping(target = "totalLocations", expression = "java(region.getEventLocations().size())")
    @Mapping(target = "activeLocations", expression = "java(region.getActiveLocationCount())")
    @Mapping(target = "totalProvinces", expression = "java(region.getProvinces().size())")
    @Mapping(target = "activeProvinces", expression = "java(region.getActiveProvinceCount())")
    @Mapping(target = "locationActivationRate", expression = "java(region.getLocationActivationRate())")
    @Mapping(target = "provinceActivationRate", expression = "java(region.getProvinceActivationRate())")
    RegionDTO.Statistics toStatistics(Region region);

    List<RegionDTO.Summary> toSummaryList(List<Region> regions);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "code", ignore = true)
    @Mapping(target = "provinces", ignore = true)
    @Mapping(target = "eventLocations", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "status", expression = "java(request.getActive() != null ? (request.getActive() ? 1 : 0) : null)")
    void updateEntity(@MappingTarget Region region, RegionDTO.UpdateRequest request);
}
