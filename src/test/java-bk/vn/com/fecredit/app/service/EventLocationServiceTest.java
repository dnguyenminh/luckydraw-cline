package vn.com.fecredit.app.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import vn.com.fecredit.app.dto.EventLocationDTO;
import vn.com.fecredit.app.entity.EventLocation;
import vn.com.fecredit.app.mapper.EventLocationMapper;
import vn.com.fecredit.app.repository.EventLocationRepository;
import vn.com.fecredit.app.repository.SpinHistoryRepository;
import vn.com.fecredit.app.service.impl.EventLocationServiceImpl;

import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventLocationServiceTest {

    @Mock
    private EventLocationRepository locationRepository;

    @Mock
    private SpinHistoryRepository spinHistoryRepository;

    @Mock
    private EventLocationMapper locationMapper;

    private EventLocationService locationService;

    @BeforeEach
    void setUp() {
        locationService = new EventLocationServiceImpl(locationRepository, spinHistoryRepository, locationMapper);
    }

    @Test
    void shouldCreateLocation() {
        // Given
        EventLocationDTO.CreateRequest request = EventLocationDTO.CreateRequest.builder()
                .name("Test Location")
                .dailySpinLimit(100)
                .winProbabilityMultiplier(0.5)
                .build();

        EventLocation entity = EventLocation.builder()
                .name("Test Location")
                .dailySpinLimit(100)
                .winProbabilityMultiplier(0.5)
                .status(EntityStatus.ACTIVE)
                .build();

        EventLocation savedEntity = EventLocation.builder()
                .id(1L)
                .name("Test Location")
                .dailySpinLimit(100)
                .winProbabilityMultiplier(0.5)
                .status(EntityStatus.ACTIVE)
                .build();

        EventLocationDTO.Response response = EventLocationDTO.Response.builder()
                .id(1L)
                .locationName("Test Location")
                .dailySpinLimit(100)
                .winProbabilityMultiplier(0.5)
                .status(EntityStatus.ACTIVE)
                .build();

        when(locationMapper.toEntity(request)).thenReturn(entity);
        when(locationRepository.save(entity)).thenReturn(savedEntity);
        when(locationMapper.toResponse(savedEntity)).thenReturn(response);

        // When
        EventLocationDTO.Response result = locationService.createLocation(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getLocationName()).isEqualTo("Test Location");
        assertThat(result.getDailySpinLimit()).isEqualTo(100);
        assertThat(result.getWinProbabilityMultiplier()).isEqualTo(0.5);
        assertThat(result.getStatus()).isEqualTo(EntityStatus.ACTIVE);

        verify(locationMapper).toEntity(request);
        verify(locationRepository).save(entity);
        verify(locationMapper).toResponse(savedEntity);
    }

    @Test
    void shouldListActiveLocations() {
        // Given
        Pageable pageable = PageRequest.of(0, 10);
        EventLocation location = EventLocation.builder()
                .id(1L)
                .name("Test Location")
                .dailySpinLimit(100)
                .winProbabilityMultiplier(0.5)
                .status(EntityStatus.ACTIVE)
                .build();
        Page<EventLocation> locationPage = new PageImpl<>(Arrays.asList(location));

        EventLocationDTO.Response response = EventLocationDTO.Response.builder()
                .id(1L)
                .locationName("Test Location")
                .dailySpinLimit(100)
                .winProbabilityMultiplier(0.5)
                .status(EntityStatus.ACTIVE)
                .build();

        when(locationRepository.findAllByStatus(EntityStatus.ACTIVE, pageable)).thenReturn(locationPage);
        when(locationMapper.toResponse(location)).thenReturn(response);

        // When
        Page<EventLocationDTO.Response> result = locationService.listActiveLocations(pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getLocationName()).isEqualTo("Test Location");

        verify(locationRepository).findAllByStatus(EntityStatus.ACTIVE, pageable);
        verify(locationMapper).toResponse(location);
    }

    @Test
    void shouldUpdateLocation() {
        // Given
        Long id = 1L;
        EventLocationDTO.UpdateRequest request = EventLocationDTO.UpdateRequest.builder()
                .name("Updated Location")
                .dailySpinLimit(200)
                .winProbabilityMultiplier(0.7)
                .active(true)
                .build();

        EventLocation existingLocation = EventLocation.builder()
                .id(id)
                .name("Test Location")
                .dailySpinLimit(100)
                .winProbabilityMultiplier(0.5)
                .status(EntityStatus.ACTIVE)
                .build();

        EventLocation updatedLocation = EventLocation.builder()
                .id(id)
                .name("Updated Location")
                .dailySpinLimit(200)
                .winProbabilityMultiplier(0.7)
                .status(EntityStatus.ACTIVE)
                .build();

        EventLocationDTO.Response response = EventLocationDTO.Response.builder()
                .id(id)
                .locationName("Updated Location")
                .dailySpinLimit(200)
                .winProbabilityMultiplier(0.7)
                .status(EntityStatus.ACTIVE)
                .build();

        when(locationRepository.findById(id)).thenReturn(Optional.of(existingLocation));
        doNothing().when(locationMapper).updateEntity(existingLocation, request);
        when(locationRepository.save(existingLocation)).thenReturn(updatedLocation);
        when(locationMapper.toResponse(updatedLocation)).thenReturn(response);

        // When
        EventLocationDTO.Response result = locationService.updateLocation(id, request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getLocationName()).isEqualTo("Updated Location");
        assertThat(result.getDailySpinLimit()).isEqualTo(200);
        assertThat(result.getWinProbabilityMultiplier()).isEqualTo(0.7);
        assertThat(result.getStatus()).isEqualTo(EntityStatus.ACTIVE);

        verify(locationRepository).findById(id);
        verify(locationMapper).updateEntity(existingLocation, request);
        verify(locationRepository).save(existingLocation);
        verify(locationMapper).toResponse(updatedLocation);
    }
}
