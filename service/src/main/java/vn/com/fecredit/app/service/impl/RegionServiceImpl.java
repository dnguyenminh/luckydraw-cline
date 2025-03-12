package vn.com.fecredit.app.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.com.fecredit.app.dto.RegionDTO;
import vn.com.fecredit.app.entity.Region;
import vn.com.fecredit.app.exception.EntityNotFoundException;
import vn.com.fecredit.app.exception.InvalidOperationException;
import vn.com.fecredit.app.mapper.RegionMapper;
import vn.com.fecredit.app.repository.RegionRepository;
import vn.com.fecredit.app.service.RegionService;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class RegionServiceImpl implements RegionService {

    private final RegionRepository regionRepository;
    private final RegionMapper regionMapper;

    @Override
    public RegionDTO.Response createRegion(RegionDTO.CreateRequest request) {
        if (regionRepository.existsByCode(request.getCode())) {
            throw new InvalidOperationException("Region with code " + request.getCode() + " already exists");
        }

        Region region = regionMapper.toEntity(request);
        region = regionRepository.save(region);
        return regionMapper.toResponse(region);
    }

    @Override
    public RegionDTO.Response updateRegion(Long id, RegionDTO.UpdateRequest request) {
        Region region = findRegionById(id);

        regionMapper.updateEntity(region, request);
        region = regionRepository.save(region);
        return regionMapper.toResponse(region);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<RegionDTO.Response> getRegion(Long id) {
        return regionRepository.findById(id)
                .map(regionMapper::toResponse);
    }

    @Override
    public void deleteRegion(Long id) {
        Region region = findRegionById(id);
        
        if (hasActiveProvinces(id)) {
            throw new InvalidOperationException("Cannot delete region with active provinces");
        }
        if (hasActiveEventLocations(id)) {
            throw new InvalidOperationException("Cannot delete region with active event locations");
        }

        regionRepository.delete(region);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RegionDTO.Response> listRegions(Pageable pageable) {
        return regionRepository.findAll(pageable)
                .map(regionMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RegionDTO.Response> listActiveRegions(Pageable pageable) {
        return regionRepository.findAllByStatus(1, pageable)
                .map(regionMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RegionDTO.Summary> getRegionSummaries() {
        return regionMapper.toSummaryList(regionRepository.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RegionDTO.Summary> getActiveRegionSummaries() {
        return regionMapper.toSummaryList(regionRepository.findAllByStatus(1));
    }

    @Override
    @Transactional(readOnly = true)
    public RegionDTO.Statistics getRegionStatistics(Long id) {
        Region region = findRegionById(id);
        return regionMapper.toStatistics(region);
    }

    @Override
    public void activateRegion(Long id) {
        Region region = findRegionById(id);
        region.setStatus(1);
        regionRepository.save(region);
    }

    @Override
    public void deactivateRegion(Long id) {
        Region region = findRegionById(id);
        
        if (hasActiveProvinces(id)) {
            throw new InvalidOperationException("Cannot deactivate region with active provinces");
        }
        if (hasActiveEventLocations(id)) {
            throw new InvalidOperationException("Cannot deactivate region with active event locations");
        }

        region.setStatus(0);
        regionRepository.save(region);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByCode(String code) {
        return regionRepository.existsByCode(code);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<RegionDTO.Response> findByCode(String code) {
        return regionRepository.findByCode(code)
                .map(regionMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasActiveProvinces(Long id) {
        return regionRepository.hasActiveProvinces(id);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasActiveEventLocations(Long id) {
        return regionRepository.hasActiveEventLocations(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RegionDTO.Summary> getRegionsWithParticipants() {
        return regionMapper.toSummaryList(regionRepository.findRegionsWithParticipants());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RegionDTO.Summary> getRegionsWithLocations() {
        return regionMapper.toSummaryList(regionRepository.findRegionsWithLocations());
    }

    private Region findRegionById(Long id) {
        return regionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Region", id));
    }
}
