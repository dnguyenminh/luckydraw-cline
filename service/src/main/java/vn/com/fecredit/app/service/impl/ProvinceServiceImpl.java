package vn.com.fecredit.app.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.com.fecredit.app.dto.ProvinceDTO;
import vn.com.fecredit.app.entity.Province;
import vn.com.fecredit.app.exception.EntityNotFoundException;
import vn.com.fecredit.app.exception.InvalidOperationException;
import vn.com.fecredit.app.mapper.ProvinceMapper;
import vn.com.fecredit.app.repository.ProvinceRepository;
import vn.com.fecredit.app.service.ProvinceService;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ProvinceServiceImpl implements ProvinceService {

    private final ProvinceRepository provinceRepository;
    private final ProvinceMapper provinceMapper;

    @Override
    public ProvinceDTO.Response createProvince(ProvinceDTO.CreateRequest request) {
        if (provinceRepository.existsByCode(request.getCode())) {
            throw new InvalidOperationException("Province with code " + request.getCode() + " already exists");
        }
        Province province = provinceMapper.toEntity(request);
        province = provinceRepository.save(province);
        return provinceMapper.toResponse(province);
    }

    @Override
    public ProvinceDTO.Response updateProvince(Long id, ProvinceDTO.UpdateRequest request) {
        Province province = findProvinceById(id);
        if (!province.getCode().equals(request.getCode()) && 
            provinceRepository.existsByCode(request.getCode())) {
            throw new InvalidOperationException("Province with code " + request.getCode() + " already exists");
        }
        provinceMapper.updateEntity(province, request);
        province = provinceRepository.save(province);
        return provinceMapper.toResponse(province);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ProvinceDTO.Response> getProvince(Long id) {
        return provinceRepository.findById(id)
            .map(provinceMapper::toResponse);
    }

    @Override
    public void deleteProvince(Long id) {
        Province province = findProvinceById(id);
        if (hasActiveParticipants(id)) {
            throw new InvalidOperationException("Cannot delete province with active participants");
        }
        provinceRepository.delete(province);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProvinceDTO.Response> listProvinces(Pageable pageable) {
        return provinceRepository.findAll(pageable)
            .map(provinceMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProvinceDTO.Response> listActiveProvinces(Pageable pageable) {
        return provinceRepository.findAllByStatus(1, pageable)
            .map(provinceMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProvinceDTO.Summary> getProvinceSummaries() {
        return provinceRepository.findAll().stream()
            .map(provinceMapper::toSummary)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProvinceDTO.Summary> getActiveProvinceSummaries() {
        return provinceRepository.findAllByStatus(1).stream()
            .map(provinceMapper::toSummary)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProvinceDTO.Statistics getProvinceStatistics(Long id) {
        Province province = findProvinceById(id);
        return provinceMapper.toStatistics(province);
    }

    @Override
    public void activateProvince(Long id) {
        Province province = findProvinceById(id);
        province.setStatus(1);
        provinceRepository.save(province);
    }

    @Override
    public void deactivateProvince(Long id) {
        Province province = findProvinceById(id);
        if (hasActiveParticipants(id)) {
            throw new InvalidOperationException("Cannot deactivate province with active participants");
        }
        province.setStatus(0);
        provinceRepository.save(province);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByCode(String code) {
        return provinceRepository.existsByCode(code);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ProvinceDTO.Response> findByCode(String code) {
        return provinceRepository.findByCode(code)
            .map(provinceMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProvinceDTO.Summary> findAllByRegionId(Long regionId) {
        return provinceRepository.findAllByRegionId(regionId).stream()
            .map(provinceMapper::toSummary)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProvinceDTO.Summary> findAllActiveByRegionId(Long regionId) {
        return provinceRepository.findAllByRegionIdAndStatus(regionId, 1).stream()
            .map(provinceMapper::toSummary)
            .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasActiveParticipants(Long id) {
        Province province = findProvinceById(id);
        return province.getParticipants().stream()
            .anyMatch(p -> p.isActive());
    }

    private Province findProvinceById(Long id) {
        return provinceRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Province", id));
    }
}
