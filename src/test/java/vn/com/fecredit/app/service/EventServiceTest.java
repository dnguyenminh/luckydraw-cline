package vn.com.fecredit.app.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import vn.com.fecredit.app.entity.Event;
import vn.com.fecredit.app.entity.Participant;
import vn.com.fecredit.app.mapper.EventMapper;
import vn.com.fecredit.app.repository.EventRepository;
import vn.com.fecredit.app.repository.ParticipantRepository;
import vn.com.fecredit.app.service.impl.EventServiceImpl;
import vn.com.fecredit.app.dto.event.CreateEventRequest;
import vn.com.fecredit.app.dto.event.UpdateEventRequest;
import vn.com.fecredit.app.dto.event.EventResponse;
import vn.com.fecredit.app.dto.event.EventSummary;

public class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private ParticipantRepository participantRepository;

    @Mock
    private EventMapper eventMapper;

    @InjectMocks
    private EventServiceImpl eventService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createEvent_Success() {
        // Given
        var request = createValidEventRequest();
        var mockEvent = createValidEvent();
        var mockResponse = createValidEventResponse();

        when(eventMapper.createEntityFromRequest(any())).thenReturn(mockEvent);
        when(eventRepository.save(any(Event.class))).thenReturn(mockEvent);
        when(eventMapper.toResponse(mockEvent)).thenReturn(mockResponse);

        // When
        var result = eventService.createEvent(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStartDate()).isEqualTo(mockResponse.getStartDate());
        assertThat(result.getEndDate()).isEqualTo(mockResponse.getEndDate());
        verify(eventRepository).save(any(Event.class));
    }

    @Test
    void getEventById_Success() {
        // Given
        var mockEvent = createValidEvent();
        var mockResponse = createValidEventResponse();

        when(eventRepository.findById(1L)).thenReturn(Optional.of(mockEvent));
        when(eventMapper.toResponse(mockEvent)).thenReturn(mockResponse);

        // When
        var result = eventService.getEventById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(mockResponse.getId());
    }

    @Test
    void updateEvent_Success() {
        // Given
        var mockEvent = createValidEvent();
        var request = createValidUpdateRequest();
        var mockResponse = createValidEventResponse();

        when(eventRepository.findById(1L)).thenReturn(Optional.of(mockEvent));
        when(eventRepository.save(any(Event.class))).thenReturn(mockEvent);
        when(eventMapper.toResponse(mockEvent)).thenReturn(mockResponse);

        // When
        var result = eventService.updateEvent(1L, request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStartDate()).isEqualTo(mockResponse.getStartDate());
        assertThat(result.getEndDate()).isEqualTo(mockResponse.getEndDate());
        verify(eventRepository).save(any(Event.class));
    }

    @Test
    void getActiveEvents_Success() {
        // Given
        var mockEvents = Arrays.asList(createValidEvent());
        var mockSummaries = Arrays.asList(createValidEventSummary());

        when(eventRepository.findByActiveTrue()).thenReturn(mockEvents);
        when(eventMapper.toSummaryList(mockEvents)).thenReturn(mockSummaries);

        // When
        var results = eventService.getActiveEvents();

        // Then
        assertThat(results).isNotEmpty();
        assertThat(results).hasSize(1);
    }

    @Test
    void isParticipantEligible_Success() {
        // Given
        var mockEvent = createValidEvent();
        var mockParticipant = Participant.builder()
                .id(1L)
                .event(mockEvent)
                .active(true)
                .build();

        when(eventRepository.findById(1L)).thenReturn(Optional.of(mockEvent));
        when(participantRepository.findById(1L)).thenReturn(Optional.of(mockParticipant));

        // When
        var result = eventService.checkParticipantEligibility(1L, 1L);

        // Then
        assertThat(result).isTrue();
    }

    private CreateEventRequest createValidEventRequest() {
        return CreateEventRequest.builder()
                .code("TEST-EVENT")
                .name("Test Event")
                .description("Test Description")
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusDays(7))
                .dailySpinLimit(10)
                .totalSpins(100)
                .active(true)
                .build();
    }

    private UpdateEventRequest createValidUpdateRequest() {
        return UpdateEventRequest.builder()
                .name("Updated Event")
                .description("Updated Description")
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusDays(14))
                .dailySpinLimit(20)
                .totalSpins(200)
                .active(true)
                .build();
    }

    private Event createValidEvent() {
        return Event.builder()
                .id(1L)
                .code("TEST-EVENT")
                .name("Test Event")
                .description("Test Description")
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusDays(7))
                .dailySpinLimit(10)
                .totalSpins(100)
                .remainingSpins(100)
                .active(true)
                .deleted(false)
                .build();
    }

    private EventResponse createValidEventResponse() {
        return EventResponse.builder()
                .id(1L)
                .code("TEST-EVENT")
                .name("Test Event")
                .description("Test Description")
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusDays(7))
                .dailySpinLimit(10)
                .totalSpins(100)
                .remainingSpins(100)
                .active(true)
                .deleted(false)
                .build();
    }

    private EventSummary createValidEventSummary() {
        return EventSummary.builder()
                .id(1L)
                .code("TEST-EVENT")
                .name("Test Event")
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusDays(7))
                .remainingSpins(100)
                .active(true)
                .build();
    }
}
