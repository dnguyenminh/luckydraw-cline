package vn.com.fecredit.app.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import vn.com.fecredit.app.dto.ProvinceDTO;
import vn.com.fecredit.app.entity.Province;
import vn.com.fecredit.app.entity.Region;
import vn.com.fecredit.app.exception.EntityNotFoundException;
import vn.com.fecredit.app.exception.InvalidOperationException;
import vn.com.fecredit.app.repository.ProvinceRepository;
import vn.com.fecredit.app.repository.RegionRepository;
import vn.com.fecredit.app.service.ProvinceService;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
@Sql(scripts = {"/schema-test.sql", "/data-test.sql"})
class ProvinceServiceIntegrationTest {

    @Autowired
    private ProvinceService provinceService;

    @Autowired
    private ProvinceRepository provinceRepository;

    @Autowired
    private RegionRepository regionRepository;

    private Region region;
    private ProvinceDTO.CreateRequest createRequest;
    private ProvinceDTO.UpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
        region = Region.builder()
            .name("North Region")
            .code("NORTH")
            .defaultWinProbability(0.3)
            .status(1)
            .build();
        region = regionRepository.save(region);

        createRequest = ProvinceDTO.CreateRequest.builder()
            .name("Hanoi")
            .code("HN")
            .regionId(region.getId())
            .defaultWinProbability(0.25)
            .build();

        updateRequest = ProvinceDTO.UpdateRequest.builder()
            .name("Updated Hanoi")
            .code("HN")
            .regionId(region.getId())
            .defaultWinProbability(0.3)
            .active(true)
            .build();
    }

    @Test
    void shouldCreateAndRetrieveProvinceSuccessfully() {
        ProvinceDTO.Response response = provinceService.createProvince(createRequest);
        assertNotNull(response);
        assertEquals("Hanoi", response.getName());
        
        ProvinceDTO.Response retrieved = provinceService.getProvince(response.getId()).orElseThrow();
        assertEquals("Hanoi", retrieved.getName());
        assertEquals(region.getId(), retrieved.getRegionId());
    }

    @Test
    void shouldUpdateProvinceSuccessfully() {
        ProvinceDTO.Response created = provinceService.createProvince(createRequest);
        ProvinceDTO.Response updated = provinceService.updateProvince(created.getId(), updateRequest);
        
        assertEquals("Updated Hanoi", updated.getName());
        assertEquals(0.3, updated.getDefaultWinProbability());
    }

    @Test
    void shouldThrowExceptionWhenUpdatingNonExistentProvince() {
        assertThrows(EntityNotFoundException.class, () ->
            provinceService.updateProvince(999L, updateRequest)
        );
    }

    @Test
    void shouldListProvincesWithPagination() {
        provinceService.createProvince(createRequest);
        createRequest.setCode("HN2");
        provinceService.createProvince(createRequest);

        Page<ProvinceDTO.Response> page = provinceService.listProvinces(PageRequest.of(0, 1));
        assertEquals(1, page.getContent().size());
        assertEquals(2, page.getTotalElements());
    }

    @Test
    void shouldDeactivateProvinceSuccessfully() {
        ProvinceDTO.Response created = provinceService.createProvince(createRequest);
        provinceService.deactivateProvince(created.getId());

        Province province = provinceRepository.findById(created.getId()).orElseThrow();
        assertEquals(0, province.getStatus());
    }

    @Test
    void shouldActivateProvinceSuccessfully() {
        ProvinceDTO.Response created = provinceService.createProvince(createRequest);
        provinceService.deactivateProvince(created.getId());
        provinceService.activateProvince(created.getId());

        Province province = provinceRepository.findById(created.getId()).orElseThrow();
        assertEquals(1, province.getStatus());
    }

    @Test
    void shouldFindByCodeSuccessfully() {
        provinceService.createProvince(createRequest);
        Optional<ProvinceDTO.Response> found = provinceService.findByCode("HN");
        
        assertTrue(found.isPresent());
        assertEquals("Hanoi", found.get().getName());
    }

    @Test
    void shouldReturnCorrectExistsByCode() {
        provinceService.createProvince(createRequest);
        assertTrue(provinceService.existsByCode("HN"));
        assertFalse(provinceService.existsByCode("NONEXISTENT"));
    }

    @Test
    void shouldPreventDuplicateCode() {
        provinceService.createProvince(createRequest);
        ProvinceDTO.CreateRequest duplicate = ProvinceDTO.CreateRequest.builder()
            .name("Another Hanoi")
            .code("HN")
            .regionId(region.getId())
            .build();

        assertThrows(InvalidOperationException.class, () ->
            provinceService.createProvince(duplicate)
        );
    }

    @Test
    void shouldInheritRegionWinProbability() {
        createRequest.setDefaultWinProbability(null);
        ProvinceDTO.Response response = provinceService.createProvince(createRequest);
        
        assertEquals(region.getDefaultWinProbability(), response.getEffectiveDefaultWinProbability());
    }
}
