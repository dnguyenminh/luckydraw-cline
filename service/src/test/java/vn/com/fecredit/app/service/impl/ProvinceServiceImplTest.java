package vn.com.fecredit.app.service.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import vn.com.fecredit.app.dto.ProvinceDTO;
import vn.com.fecredit.app.entity.Province;
import vn.com.fecredit.app.entity.Region;
import vn.com.fecredit.app.exception.EntityNotFoundException;
import vn.com.fecredit.app.mapper.ProvinceMapper;
import vn.com.fecredit.app.repository.ProvinceRepository;

@ExtendWith(MockitoExtension.class)
class ProvinceServiceImplTest {

    @Mock
    private ProvinceRepository provinceRepository;

    @Mock
    private ProvinceMapper provinceMapper;

    @InjectMocks
    private ProvinceServiceImpl provinceService;

    private Province province;
    private ProvinceDTO.Response responseDTO;
    private ProvinceDTO.CreateRequest createDTO;
    private ProvinceDTO.UpdateRequest updateDTO;
    private Region region;

    @BeforeEach
    void setUp() {
        region = Region.builder()
            .id(1L)
            .name("Test Region")
            .code("TEST_REG")
            .status(1)
            .build();

        province = Province.builder()
            .id(1L)
            .name("Test Province")
            .code("TEST_PROV")
            .region(region)
            .status(1)
            .build();

        responseDTO = ProvinceDTO.Response.builder()
            .id(1L)
            .name("Test Province")
            .code("TEST_PROV")
            .regionId(1L)
            .regionName("Test Region")
            .status(1)
            .build();

        createDTO = ProvinceDTO.CreateRequest.builder()
            .name("Test Province")
            .code("TEST_PROV")
            .regionId(1L)
            .build();

        updateDTO = ProvinceDTO.UpdateRequest.builder()
            .name("Updated Province")
            .code("UPD_PROV")
            .build();
    }

    @Test
    void whenCreateProvince_thenSuccess() {
        when(provinceMapper.toEntity(any())).thenReturn(province);
        when(provinceRepository.save(any())).thenReturn(province);
        when(provinceMapper.toResponse(any())).thenReturn(responseDTO);

        ProvinceDTO.Response result = provinceService.createProvince(createDTO);

        assertNotNull(result);
        assertEquals(responseDTO.getId(), result.getId());
        verify(provinceRepository).save(any());
    }

    @Test
    void whenUpdateProvince_thenSuccess() {
        when(provinceRepository.findById(anyLong())).thenReturn(Optional.of(province));
        when(provinceRepository.save(any())).thenReturn(province);
        when(provinceMapper.toResponse(any())).thenReturn(responseDTO);

        ProvinceDTO.Response result = provinceService.updateProvince(1L, updateDTO);

        assertNotNull(result);
        assertEquals(responseDTO.getId(), result.getId());
        verify(provinceMapper).updateEntity(any(), any());
    }

    @Test
    void whenGetProvince_thenSuccess() {
        when(provinceRepository.findById(anyLong())).thenReturn(Optional.of(province));
        when(provinceMapper.toResponse(any())).thenReturn(responseDTO);

        ProvinceDTO.Response result = provinceService.getProvince(1L);

        assertNotNull(result);
        assertEquals(responseDTO.getId(), result.getId());
    }

    @Test
    void whenGetProvince_thenThrowException() {
        when(provinceRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> provinceService.getProvince(1L));
    }

    @Test
    void whenListProvinces_thenSuccess() {
        Page<Province> page = new PageImpl<>(Arrays.asList(province));
        when(provinceRepository.findAll(any(Pageable.class))).thenReturn(page);
        when(provinceMapper.toResponse(any())).thenReturn(responseDTO);

        Page<ProvinceDTO.Response> result = provinceService.listProvinces(Pageable.unpaged());

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
    }

    @Test
    void whenGetProvinceSummaries_thenSuccess() {
        List<Province> provinces = Arrays.asList(province);
        when(provinceRepository.findAll()).thenReturn(provinces);
        when(provinceMapper.toSummaryList(any())).thenReturn(Arrays.asList(ProvinceDTO.Summary.builder().build()));

        List<ProvinceDTO.Summary> result = provinceService.getProvinceSummaries();

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void whenActivateProvince_thenSuccess() {
        when(provinceRepository.findById(anyLong())).thenReturn(Optional.of(province));
        when(provinceRepository.save(any())).thenReturn(province);

        assertDoesNotThrow(() -> provinceService.activateProvince(1L));
        verify(provinceRepository).save(any());
    }

    @Test
    void whenDeactivateProvince_thenSuccess() {
        when(provinceRepository.findById(anyLong())).thenReturn(Optional.of(province));
        when(provinceRepository.save(any())).thenReturn(province);

        assertDoesNotThrow(() -> provinceService.deactivateProvince(1L));
        verify(provinceRepository).save(any());
    }

    @Test
    void whenFindByCode_thenSuccess() {
        when(provinceRepository.findByCode(anyString())).thenReturn(Optional.of(province));
        when(provinceMapper.toResponse(any())).thenReturn(responseDTO);

        Optional<ProvinceDTO.Response> result = provinceService.findByCode("TEST_PROV");

        assertTrue(result.isPresent());
        assertEquals(responseDTO.getCode(), result.get().getCode());
    }

    @Test
    void whenGetProvinceStatistics_thenSuccess() {
        when(provinceRepository.findById(anyLong())).thenReturn(Optional.of(province));
        when(provinceMapper.toStatistics(any())).thenReturn(ProvinceDTO.Statistics.builder().build());

        ProvinceDTO.Statistics result = provinceService.getProvinceStatistics(1L);

        assertNotNull(result);
    }
}
