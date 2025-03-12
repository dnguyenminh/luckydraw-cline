package vn.com.fecredit.app.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.com.fecredit.app.dto.RegionDTO;

import java.util.List;
import java.util.Optional;

public interface RegionService {

    RegionDTO.Response createRegion(RegionDTO.CreateRequest request);

    RegionDTO.Response updateRegion(Long id, RegionDTO.UpdateRequest request);

    Optional<RegionDTO.Response> getRegion(Long id);

    void deleteRegion(Long id);

    Page<RegionDTO.Response> listRegions(Pageable pageable);

    Page<RegionDTO.Response> listActiveRegions(Pageable pageable);

    List<RegionDTO.Summary> getRegionSummaries();

    List<RegionDTO.Summary> getActiveRegionSummaries();

    RegionDTO.Statistics getRegionStatistics(Long id);

    void activateRegion(Long id);

    void deactivateRegion(Long id);

    boolean existsByCode(String code);

    Optional<RegionDTO.Response> findByCode(String code);

    boolean hasActiveProvinces(Long id);

    boolean hasActiveEventLocations(Long id);

    List<RegionDTO.Summary> getRegionsWithParticipants();

    List<RegionDTO.Summary> getRegionsWithLocations();
}
