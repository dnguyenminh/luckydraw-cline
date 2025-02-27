package vn.com.fecredit.app.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import vn.com.fecredit.app.dto.EventLocationDTO;
import vn.com.fecredit.app.entity.Event;
import vn.com.fecredit.app.entity.EventLocation;
import vn.com.fecredit.app.exception.ResourceNotFoundException;
import vn.com.fecredit.app.mapper.EventLocationMapper;
import vn.com.fecredit.app.repository.EventLocationRepository;
import vn.com.fecredit.app.repository.EventRepository;

class EventLocationServiceTest {

    @Mock
    private EventLocationRepository eventLocationRepository;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private EventLocationMapper eventLocationMapper;

    @InjectMocks
    private EventLocationService eventLocationService;

    private Event testEvent;
    private EventLocation testLocation;
    private EventLocationDTO.CreateRequest createRequest;
    private EventLocationDTO.UpdateRequest updateRequest;
    private EventLocationDTO.EventLocationResponse locationResponse;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testEvent = Event.builder()
                .id(1L)
                .name("Test Event")
                .build();

        testLocation = EventLocation.builder()
                .id(1L)
                .eventId(testEvent.getId())
                .name("Test Location")
                .addressLine1("123 Test St")
                .addressLine2("Suite 456")
                .province("Test Province")
                .district("Test District")
                .postalCode("12345")
                .totalSpins(100)
                .remainingSpins(100)
                .dailySpinLimit(10)
                .winProbabilityMultiplier(1.0)
                .sortOrder(1)
                .active(true)
                .event(testEvent)
                .build();

        createRequest = EventLocationDTO.CreateRequest.builder()
                .eventId(testEvent.getId())
                .name("Test Location")
                .addressLine1("123 Test St")
                .addressLine2("Suite 456")
                .province("Test Province")
                .district("Test District")
                .postalCode("12345")
                .totalSpins(100)
                .dailySpinLimit(10)
                .winProbabilityMultiplier(1.0)
                .sortOrder(1)
                .active(true)
                .build();

        updateRequest = EventLocationDTO.UpdateRequest.builder()
                .name("Updated Location")
                .addressLine1("Updated Address")
                .province("Updated Province")
                .district("Updated District")
                .totalSpins(200)
                .dailySpinLimit(20)
                .active(true)
                .build();

        locationResponse = EventLocationDTO.EventLocationResponse.builder()
                .id(1L)
                .eventId(testEvent.getId())
                .eventName(testEvent.getName())
                .name(testLocation.getName())
                .addressLine1(testLocation.getAddressLine1())
                .province(testLocation.getProvince())
                .district(testLocation.getDistrict())
                .totalSpins(testLocation.getTotalSpins())
                .remainingSpins(testLocation.getRemainingSpins())
                .dailySpinLimit(testLocation.getDailySpinLimit())
                .active(testLocation.isActive())
                .build();
    }

    @Test
    void createEventLocation_Success() {
        when(eventRepository.findById(testEvent.getId())).thenReturn(Optional.of(testEvent));
        when(eventLocationMapper.createEntityFromRequest(createRequest)).thenReturn(testLocation);
        when(eventLocationRepository.save(any(EventLocation.class))).thenReturn(testLocation);
        when(eventLocationMapper.toResponse(testLocation)).thenReturn(locationResponse);

        EventLocationDTO.EventLocationResponse result = eventLocationService.createEventLocation(createRequest);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(locationResponse.getId());
        assertThat(result.getName()).isEqualTo(locationResponse.getName());
        verify(eventLocationRepository, times(1)).save(any(EventLocation.class));
    }

    @Test
    void updateEventLocation_Success() {
        when(eventLocationRepository.findById(testLocation.getId())).thenReturn(Optional.of(testLocation));
        when(eventLocationRepository.save(any(EventLocation.class))).thenReturn(testLocation);
        when(eventLocationMapper.toResponse(testLocation)).thenReturn(locationResponse);

        EventLocationDTO.EventLocationResponse result = eventLocationService.updateEventLocation(1L, updateRequest);

        assertThat(result).isNotNull();
        verify(eventLocationRepository, times(1)).save(any(EventLocation.class));
        verify(eventLocationMapper, times(1)).updateEntityFromRequest(any(EventLocation.class), any(EventLocationDTO.UpdateRequest.class));
    }

    @Test
    void deleteEventLocation_Success() {
        when(eventLocationRepository.existsById(1L)).thenReturn(true);

        eventLocationService.deleteEventLocation(1L);

        verify(eventLocationRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteEventLocation_NotFound() {
        when(eventLocationRepository.existsById(1L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> {
            eventLocationService.deleteEventLocation(1L);
        });
    }

    @Test
    void decrementRemainingSpins_Success() {
        when(eventLocationRepository.findById(1L)).thenReturn(Optional.of(testLocation));
        when(eventLocationRepository.save(any(EventLocation.class))).thenReturn(testLocation);

        eventLocationService.decrementRemainingSpins(1L);

        verify(eventLocationRepository, times(1)).save(any(EventLocation.class));
        assertThat(testLocation.getRemainingSpins()).isEqualTo(99);
    }
}
