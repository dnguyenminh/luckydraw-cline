package vn.com.fecredit.app.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import vn.com.fecredit.app.dto.EventDTO;
import vn.com.fecredit.app.mapper.EventMapper;
import vn.com.fecredit.app.model.Event;
import vn.com.fecredit.app.repository.EventRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("Event Date Validation Tests")
class EventDateValidationTest {

    @Mock
    private EventRepository eventRepository;

    private EventMapper eventMapper;
    private EventService eventService;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        eventMapper = new EventMapper();
        eventService = new EventService(eventRepository, eventMapper);
        now = LocalDateTime.now();
    }

    @Test
    @DisplayName("Should throw exception when end date is before start date")
    void shouldThrowExceptionWhenEndDateBeforeStartDate() {
        // Given
        EventDTO.CreateEventRequest request = EventDTO.CreateEventRequest.builder()
                .name("Invalid Event")
                .code("INVALID_EVENT")
                .startDate(now.plusDays(2))
                .endDate(now.plusDays(1))  // End date before start date
                .isActive(true)
                .build();

        // When/Then
        assertThatThrownBy(() -> eventService.createEvent(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Event start date must be before end date");

        verify(eventRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should validate event dates on update")
    void shouldValidateEventDatesOnUpdate() {
        // Given
        Long eventId = 1L;
        Event existingEvent = createValidEvent();
        EventDTO.UpdateEventRequest request = EventDTO.UpdateEventRequest.builder()
                .name("Updated Event")
                .startDate(now.plusDays(5))
                .endDate(now.plusDays(3))  // Invalid: end before start
                .isActive(true)
                .build();

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(existingEvent));

        // When/Then
        assertThatThrownBy(() -> eventService.updateEvent(eventId, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Event start date must be before end date");

        verify(eventRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should not activate event with past end date")
    void shouldNotActivateEventWithPastEndDate() {
        // Given
        Long eventId = 1L;
        Event pastEvent = Event.builder()
                .id(eventId)
                .name("Past Event")
                .code("PAST_EVENT")
                .startDate(now.minusDays(10))
                .endDate(now.minusDays(5))
                .isActive(false)
                .participants(new HashSet<>())
                .rewards(new HashSet<>())
                .build();

        when(eventRepository.findById(eventId)).thenReturn(Optional.of(pastEvent));

        // When/Then
        assertThatThrownBy(() -> eventService.activateEvent(eventId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Cannot activate event that has ended");

        verify(eventRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should validate event start date not in past")
    void shouldValidateEventStartDateNotInPast() {
        // Given
        EventDTO.CreateEventRequest request = EventDTO.CreateEventRequest.builder()
                .name("Past Start Event")
                .code("PAST_START")
                .startDate(now.minusDays(1))  // Start date in past
                .endDate(now.plusDays(5))
                .isActive(true)
                .build();

        // When/Then
        assertThatThrownBy(() -> eventService.createEvent(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Event start date cannot be in the past");

        verify(eventRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should automatically deactivate event after end date")
    void shouldAutomaticallyDeactivateEventAfterEndDate() {
        // Given
        Long eventId = 1L;
        LocalDateTime pastEndDate = now.minusDays(1);
        Event expiredEvent = Event.builder()
                .id(eventId)
                .name("Expired Event")
                .code("EXPIRED")
                .startDate(now.minusDays(10))
                .endDate(pastEndDate)
                .isActive(true)
                .participants(new HashSet<>())
                .rewards(new HashSet<>())
                .build();

        when(eventRepository.findByIdWithDetails(eventId)).thenReturn(Optional.of(expiredEvent));
        when(eventRepository.save(any())).thenAnswer(i -> {
            Event savedEvent = (Event) i.getArgument(0);
            // Event should be deactivated when saved
            assertThat(savedEvent.getIsActive()).isFalse();
            return savedEvent;
        });

        // When
        EventDTO result = eventService.getEvent(eventId);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getIsActive()).isFalse();
        assertThat(result.getEndDate()).isEqualTo(pastEndDate);
        verify(eventRepository).findByIdWithDetails(eventId);
        verify(eventRepository).save(any());
    }

    @Test
    @DisplayName("Should handle events spanning midnight")
    void shouldHandleEventsSpanningMidnight() {
        // Given
        LocalDateTime startDate = now.withHour(22).withMinute(0); // 10 PM
        LocalDateTime endDate = now.plusDays(1).withHour(2).withMinute(0); // 2 AM next day
        
        EventDTO.CreateEventRequest request = EventDTO.CreateEventRequest.builder()
                .name("Midnight Event")
                .code("MIDNIGHT")
                .startDate(startDate)
                .endDate(endDate)
                .isActive(true)
                .build();

        Event event = Event.builder()
                .id(1L)
                .name(request.getName())
                .code(request.getCode())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .isActive(true)
                .participants(new HashSet<>())
                .rewards(new HashSet<>())
                .build();

        when(eventRepository.save(any(Event.class))).thenReturn(event);

        // When
        EventDTO result = eventService.createEvent(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getStartDate()).isEqualTo(startDate);
        assertThat(result.getEndDate()).isEqualTo(endDate);
        verify(eventRepository).save(any());
    }

    private Event createValidEvent() {
        return Event.builder()
                .id(1L)
                .name("Test Event")
                .code("TEST_EVENT")
                .startDate(now.plusDays(1))
                .endDate(now.plusDays(7))
                .isActive(true)
                .participants(new HashSet<>())
                .rewards(new HashSet<>())
                .build();
    }
}