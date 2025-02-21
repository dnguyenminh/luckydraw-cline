package vn.com.fecredit.app.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import vn.com.fecredit.app.dto.EventLocationDTO;
import vn.com.fecredit.app.mapper.EventLocationMapper;
import vn.com.fecredit.app.model.Event;
import vn.com.fecredit.app.model.EventLocation;
import vn.com.fecredit.app.repository.EventLocationRepository;
import vn.com.fecredit.app.repository.EventRepository;

@ExtendWith(MockitoExtension.class)
class EventLocationServiceTest {

    @Mock
    private EventLocationRepository locationRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private EventLocationMapper mapper;

    @InjectMocks
    private EventLocationService service;

    private Event event;
    private EventLocation location;
    private EventLocationDTO locationDTO;

    @BeforeEach
    void setUp() {
        event = Event.builder()
            .id(1L)
            .name("Test Event")
            .build();

        location = EventLocation.builder()
            .id(1L)
            .event(event)
            .name("Test Location")
            .totalSpins(100L)
            .remainingSpins(50L)
            .isActive(true)
            .build();

        locationDTO = EventLocationDTO.builder()
            .id(1L)
            .eventId(1L)
            .name("Test Location")
            .totalSpins(100L)
            .remainingSpins(50L)
            .active(true)
            .build();
    }

    @Test
    void findById_ShouldReturnLocation() {
        when(locationRepository.findById(1L)).thenReturn(Optional.of(location));
        when(mapper.toDTO(location)).thenReturn(locationDTO);

        EventLocationDTO result = service.findById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(location.getId());
        assertThat(result.getName()).isEqualTo(location.getName());
    }

    @Test
    void create_ShouldReturnNewLocation() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        when(mapper.toEntity(locationDTO, event)).thenReturn(location);
        when(locationRepository.save(any(EventLocation.class))).thenReturn(location);
        when(mapper.toDTO(location)).thenReturn(locationDTO);

        EventLocationDTO result = service.create(locationDTO);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(locationDTO.getId());
        assertThat(result.getName()).isEqualTo(locationDTO.getName());
    }

    @Test
    void update_ShouldReturnUpdatedLocation() {
        EventLocationDTO updateDTO = EventLocationDTO.builder()
            .id(1L)
            .eventId(1L)
            .name("Updated Location")
            .totalSpins(200L)
            .remainingSpins(150L)
            .active(false)
            .build();

        when(locationRepository.findById(1L)).thenReturn(Optional.of(location));
        when(locationRepository.save(any(EventLocation.class))).thenReturn(location);
        when(mapper.toDTO(location)).thenReturn(updateDTO);

        EventLocationDTO result = service.update(1L, updateDTO);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(updateDTO.getName());
        assertThat(result.getTotalSpins()).isEqualTo(updateDTO.getTotalSpins());
        assertThat(result.getRemainingSpins()).isEqualTo(updateDTO.getRemainingSpins());
        assertThat(result.isActive()).isEqualTo(updateDTO.isActive());
    }
}