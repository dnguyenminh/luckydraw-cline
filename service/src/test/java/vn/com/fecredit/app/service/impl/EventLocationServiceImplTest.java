package vn.com.fecredit.app.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import vn.com.fecredit.app.dto.EventLocationDTO;
import vn.com.fecredit.app.entity.EventLocation;
import vn.com.fecredit.app.common.EntityStatus;
import vn.com.fecredit.app.repository.EventLocationRepository;
import vn.com.fecredit.app.repository.EventRepository;
import vn.com.fecredit.app.repository.SpinHistoryRepository;
import vn.com.fecredit.app.mapper.EventLocationMapper;
import vn.com.fecredit.app.exception.EntityNotFoundException;
import vn.com.fecredit.app.projection.HourlyStatsProjection;
import vn.com.fecredit.app.exception.InvalidOperationException;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class EventLocationServiceImplTest {

    @Mock
    private EventLocationRepository eventLocationRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private SpinHistoryRepository spinHistoryRepository;

    @Mock
    private EventLocationMapper eventLocationMapper;

    @Mock 
    private HourlyStatsProjection hourlyStats;

    @InjectMocks
    private EventLocationServiceImpl eventLocationService;

    private EventLocation location;
    private EventLocationDTO.Response locationResponse;
    private EventLocationDTO.CreateRequest createRequest;
    private EventLocationDTO.UpdateRequest updateRequest;
    private EventLocationDTO.Summary locationSummary;
    private LocalDateTime startOfDay;
    private LocalDateTime endOfDay;

    @BeforeEach
    void setUp() {
        location = EventLocation.builder()
            .id(1L)
            .name("Test Location")
            .status(EntityStatus.ACTIVE.getValue())
            .dailySpinLimit(100)
            .build();

        locationResponse = EventLocationDTO.Response.builder()
            .id(1L)
            .locationName("Test Location")
            .dailySpinLimit(100)
            .status(EntityStatus.ACTIVE.getValue())
            .build();

        createRequest = EventLocationDTO.CreateRequest.builder()
            .name("New Location")
            .dailySpinLimit(50)
            .winProbabilityMultiplier(0.5)
            .build();

        updateRequest = EventLocationDTO.UpdateRequest.builder()
            .name("Updated Location")
            .dailySpinLimit(75)
            .active(true)
            .build();

        locationSummary = EventLocationDTO.Summary.builder()
            .id(1L)
            .locationName("Test Location")
            .dailySpinLimit(100)
            .status(EntityStatus.ACTIVE.getValue())
            .build();

        startOfDay = LocalDate.now().atStartOfDay();
        endOfDay = startOfDay.plusDays(1);
    }

    @Test
    void createLocation_ShouldCreateAndReturnLocation() {
        EventLocation newLocation = EventLocation.builder().build();
        when(eventLocationMapper.toEntity(createRequest)).thenReturn(newLocation);
        when(eventLocationRepository.save(any())).thenReturn(location);
        when(eventLocationMapper.toResponse(location)).thenReturn(locationResponse);

        EventLocationDTO.Response response = eventLocationService.createLocation(createRequest);

        assertNotNull(response);
        assertEquals(locationResponse, response);
        verify(eventLocationRepository).save(newLocation);
        assertEquals(EntityStatus.ACTIVE.getValue(), newLocation.getStatus());
    }

    @Test
    void updateLocation_ShouldUpdateAndReturnLocation() {
        when(eventLocationRepository.findById(1L)).thenReturn(Optional.of(location));
        when(eventLocationRepository.save(location)).thenReturn(location);
        when(eventLocationMapper.toResponse(location)).thenReturn(locationResponse);

        EventLocationDTO.Response response = eventLocationService.updateLocation(1L, updateRequest);

        assertNotNull(response);
        verify(eventLocationMapper).updateEntity(location, updateRequest);
        verify(eventLocationRepository).save(location);
    }

    @Test
    void updateLocation_ShouldThrowException_WhenLocationNotFound() {
        when(eventLocationRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> 
            eventLocationService.updateLocation(1L, updateRequest));
        verify(eventLocationRepository, never()).save(any());
    }

    @Test
    void getLocation_ShouldReturnLocation() {
        when(eventLocationRepository.findById(1L)).thenReturn(Optional.of(location));
        when(eventLocationMapper.toResponse(location)).thenReturn(locationResponse);

        Optional<EventLocationDTO.Response> response = eventLocationService.getLocation(1L);

        assertTrue(response.isPresent());
        assertEquals(locationResponse, response.get());
    }

    @Test
    void getLocation_ShouldReturnEmpty_WhenLocationNotFound() {
        when(eventLocationRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<EventLocationDTO.Response> response = eventLocationService.getLocation(1L);

        assertFalse(response.isPresent());
    }

    @Test
    void deleteLocation_ShouldMarkAsDeleted() {
        when(eventLocationRepository.findById(1L)).thenReturn(Optional.of(location));
        when(eventLocationRepository.hasActiveParticipants(location)).thenReturn(false);

        eventLocationService.deleteLocation(1L);

        assertEquals(EntityStatus.DELETED.getValue(), location.getStatus());
        verify(eventLocationRepository).save(location);
    }

    @Test
    void deleteLocation_ShouldThrowException_WhenLocationNotFound() {
        when(eventLocationRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () ->
            eventLocationService.deleteLocation(1L));
        verify(eventLocationRepository, never()).save(any());
    }

    @Test
    void deleteLocation_ShouldThrowException_WhenHasActiveParticipants() {
        when(eventLocationRepository.findById(1L)).thenReturn(Optional.of(location));
        when(eventLocationRepository.hasActiveParticipants(location)).thenReturn(true);

        assertThrows(InvalidOperationException.class, () ->
            eventLocationService.deleteLocation(1L));
        verify(eventLocationRepository, never()).save(any());
    }

    @Test
    void listLocations_ShouldReturnPageOfLocations() {
        Page<EventLocation> locationPage = new PageImpl<>(List.of(location));
        when(eventLocationRepository.findAll(any(Pageable.class))).thenReturn(locationPage);
        when(eventLocationMapper.toResponse(location)).thenReturn(locationResponse);

        Page<EventLocationDTO.Response> response = eventLocationService.listLocations(Pageable.unpaged());

        assertNotNull(response);
        assertFalse(response.isEmpty());
        assertEquals(locationResponse, response.getContent().get(0));
    }

    @Test
    void listActiveLocations_ShouldReturnActiveLocations() {
        Page<EventLocation> locationPage = new PageImpl<>(List.of(location));
        when(eventLocationRepository.findAllByStatus(eq(EntityStatus.ACTIVE.getValue()), any(Pageable.class)))
            .thenReturn(locationPage);
        when(eventLocationMapper.toResponse(location)).thenReturn(locationResponse);

        Page<EventLocationDTO.Response> response = eventLocationService.listActiveLocations(Pageable.unpaged());

        assertNotNull(response);
        assertFalse(response.isEmpty());
        assertEquals(locationResponse, response.getContent().get(0));
    }

    @Test
    void getLocationSummaries_ShouldReturnAllLocationSummaries() {
        when(eventLocationRepository.findAll()).thenReturn(List.of(location));
        when(eventLocationMapper.toSummary(location)).thenReturn(locationSummary);

        List<EventLocationDTO.Summary> summaries = eventLocationService.getLocationSummaries();

        assertNotNull(summaries);
        assertFalse(summaries.isEmpty());
        assertEquals(locationSummary, summaries.get(0));
    }

    @Test
    void getLocationSummaries_ShouldReturnEmptyList_WhenNoLocations() {
        when(eventLocationRepository.findAll()).thenReturn(Collections.emptyList());

        List<EventLocationDTO.Summary> summaries = eventLocationService.getLocationSummaries();

        assertNotNull(summaries);
        assertTrue(summaries.isEmpty());
    }

    @Test
    void getActiveLocationSummaries_ShouldReturnOnlyActiveLocations() {
        when(eventLocationRepository.findAllByStatus(EntityStatus.ACTIVE.getValue()))
            .thenReturn(List.of(location));
        when(eventLocationMapper.toSummary(location)).thenReturn(locationSummary);

        List<EventLocationDTO.Summary> summaries = eventLocationService.getActiveLocationSummaries();

        assertNotNull(summaries);
        assertFalse(summaries.isEmpty());
        assertEquals(locationSummary, summaries.get(0));
    }

    @Test
    void getEventLocationSummaries_ShouldReturnLocationSummariesForEvent() {
        when(eventLocationRepository.findAllByEventId(1L)).thenReturn(List.of(location));
        when(eventLocationMapper.toSummary(location)).thenReturn(locationSummary);

        List<EventLocationDTO.Summary> summaries = eventLocationService.getEventLocationSummaries(1L);

        assertNotNull(summaries);
        assertFalse(summaries.isEmpty());
        assertEquals(locationSummary, summaries.get(0));
    }

    @Test
    void getEventLocationStatistics_ShouldHandleNoHourlyStats() {
        when(eventLocationRepository.findById(1L)).thenReturn(Optional.of(location));
        when(spinHistoryRepository.countByLocationAndSpinTimeBetween(eq(location), any(), any())).thenReturn(100L);
        when(spinHistoryRepository.countByLocationAndWonTrueAndSpinTimeBetween(eq(location), any(), any())).thenReturn(25L);
        when(spinHistoryRepository.findHourlyStatsByLocation(eq(location), any(), any()))
            .thenReturn(Collections.emptyList());

        EventLocationDTO.LocationStatistics stats = eventLocationService.getEventLocationStatistics(1L);

        assertNotNull(stats);
        assertEquals(100L, stats.getTotalSpins());
        assertEquals(25L, stats.getWinningSpins());
        assertEquals(0.25, stats.getWinRate());
        assertTrue(stats.getHourlyStats().isEmpty());
    }

    @Test
    void deactivateLocation_ShouldDeactivateLocation() {
        when(eventLocationRepository.findById(1L)).thenReturn(Optional.of(location));

        eventLocationService.deactivateLocation(1L);

        assertEquals(EntityStatus.INACTIVE.getValue(), location.getStatus());
        verify(eventLocationRepository).save(location);
    }

    @Test
    void deactivateLocation_ShouldThrowException_WhenLocationNotFound() {
        when(eventLocationRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> 
            eventLocationService.deactivateLocation(1L));
        verify(eventLocationRepository, never()).save(any());
    }

    @Test
    void updateSpinLimits_ShouldUpdate_WithValidLimit() {
        when(eventLocationRepository.findById(1L)).thenReturn(Optional.of(location));
        
        eventLocationService.updateSpinLimits(1L, 200);
        
        assertEquals(200, location.getDailySpinLimit());
        verify(eventLocationRepository).save(location);
    }

    @Test
    void getRemainingSpinsToday_ShouldCalculateCorrectly() {
        when(eventLocationRepository.findById(1L)).thenReturn(Optional.of(location));
        when(spinHistoryRepository.countByLocationAndSpinTimeBetween(eq(location), any(), any())).thenReturn(30L);

        Long remaining = eventLocationService.getRemainingSpinsToday(1L);

        assertEquals(70L, remaining);
        verify(spinHistoryRepository).countByLocationAndSpinTimeBetween(eq(location), eq(startOfDay), eq(endOfDay));
    }

    @Test
    void getRemainingSpinsToday_ShouldThrowException_WhenLocationNotFound() {
        when(eventLocationRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> 
            eventLocationService.getRemainingSpinsToday(1L));
    }

    @Test
    void decrementRemainingSpins_ShouldDecrement_WhenSpinsAvailable() {
        when(eventLocationRepository.findById(1L)).thenReturn(Optional.of(location));
        when(spinHistoryRepository.countByLocationAndSpinTimeBetween(eq(location), any(), any())).thenReturn(50L);

        Long remaining = eventLocationService.decrementRemainingSpins(1L);

        assertEquals(49L, remaining);
    }

    @Test
    void getEventLocationStatistics_ShouldCalculateCorrectWinRate() {
        when(eventLocationRepository.findById(1L)).thenReturn(Optional.of(location));
        when(spinHistoryRepository.countByLocationAndSpinTimeBetween(eq(location), any(), any())).thenReturn(100L);
        when(spinHistoryRepository.countByLocationAndWonTrueAndSpinTimeBetween(eq(location), any(), any())).thenReturn(25L);
        
        when(hourlyStats.getHour()).thenReturn(10);
        when(hourlyStats.getCount()).thenReturn(40L);
        when(hourlyStats.getWinCount()).thenReturn(10L);
        when(spinHistoryRepository.findHourlyStatsByLocation(eq(location), any(), any()))
            .thenReturn(List.of(hourlyStats));

        EventLocationDTO.LocationStatistics stats = eventLocationService.getEventLocationStatistics(1L);

        assertNotNull(stats);
        assertEquals(100L, stats.getTotalSpins());
        assertEquals(25L, stats.getWinningSpins());
        assertEquals(0.25, stats.getWinRate());
        
        Set<EventLocationDTO.HourlyStats> hourlyStats = stats.getHourlyStats();
        assertNotNull(hourlyStats);
        assertEquals(1, hourlyStats.size());
        
        EventLocationDTO.HourlyStats hourly = hourlyStats.iterator().next();
        assertEquals(10, hourly.getHour());
        assertEquals(40L, hourly.getTotalSpins());
        assertEquals(10L, hourly.getWinningSpins());
        assertEquals(0.25, hourly.getWinRate());
    }
}
