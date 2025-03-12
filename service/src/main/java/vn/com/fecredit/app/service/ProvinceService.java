package vn.com.fecredit.app.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.com.fecredit.app.dto.ProvinceDTO;

import java.util.List;
import java.util.Optional;

public interface ProvinceService {
    // Basic CRUD operations
    ProvinceDTO.Response createProvince(ProvinceDTO.CreateRequest request);
    ProvinceDTO.Response updateProvince(Long id, ProvinceDTO.UpdateRequest request);
    ProvinceDTO.Response getProvince(Long id);
    void deleteProvince(Long id);

    // Status management
    void activateProvince(Long id);
    void deactivateProvince(Long id);

    // Listing operations
    Page<ProvinceDTO.Response> listProvinces(Pageable pageable);
    Page<ProvinceDTO.Response> listActiveProvinces(Pageable pageable);
    List<ProvinceDTO.Response> findAllByRegionId(Long regionId);
    List<ProvinceDTO.Response> findAllActiveByRegionId(Long regionId);

    // Summary operations
    List<ProvinceDTO.Summary> getProvinceSummaries();
    List<ProvinceDTO.Summary> getActiveProvinceSummaries();

    // Code validation operations
    boolean existsByCode(String code);
    Optional<ProvinceDTO.Response> findByCode(String code);

    // Statistics
    boolean hasActiveParticipants(Long id);
    ProvinceDTO.Statistics getProvinceStatistics(Long provinceId);
}
