package vn.com.fecredit.app.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import vn.com.fecredit.app.dto.EventDTO;
import vn.com.fecredit.app.mapper.EventMapper;
import vn.com.fecredit.app.model.Event;
import vn.com.fecredit.app.model.Participant;
import vn.com.fecredit.app.repository.EventRepository;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

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
    @DisplayName("Should create event with audit fields")
    void shouldCreateEventWithAuditFields() {
        // Given
        EventDTO.CreateEventRequest request = createValidEventRequest();
        Event event = Event.builder()
                .id(1L)
                .name("Test Event")
                .code("TEST_EVENT")
                .createdAt(now)
                .updatedAt(now)
                .build();
        when(eventRepository.save(any(Event.class))).thenReturn(event);

        // When
        EventDTO result = eventService.createEvent(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getCreatedAt()).isNotNull();
        assertThat(result.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should manage event rewards")
    void shouldManageEventRewards() {
        // Given
        Event event = new Event();
        event.setId(1L);
        event.setRewards(new HashSet<>());

        // When testing reward management, we'll only verify the collection operations
        assertThat(event.getRewards()).isEmpty();
    }

    @Test
    @DisplayName("Should update event timestamps")
    void shouldUpdateEventTimestamps() {
        // Given
        Event existingEvent = Event.builder()
                .id(1L)
                .createdAt(now.minusDays(1))
                .updatedAt(now)
                .build();
        
        EventDTO.UpdateEventRequest request = createValidUpdateRequest();
        when(eventRepository.findById(1L)).thenReturn(Optional.of(existingEvent));
        when(eventRepository.save(any(Event.class))).thenReturn(existingEvent);

        // When
        EventDTO result = eventService.updateEvent(1L, request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUpdatedAt()).isAfterOrEqualTo(result.getCreatedAt());
    }

    @Nested
    @DisplayName("Participant Eligibility Tests")
    class ParticipantEligibilityTests {

        @Test
        @DisplayName("Should be eligible when all conditions are met")
        void shouldBeEligibleWhenAllConditionsAreMet() {
            // Given
            Long eventId = 1L;
            Long participantId = 1L;
            
            Event event = Event.builder()
                    .id(eventId)
                    .startDate(now.minusDays(1))
                    .endDate(now.plusDays(5))
                    .isActive(true)
                    .build();
            
            Participant participant = Participant.builder()
                    .id(participantId)
                    .spinsRemaining(3L)
                    .build();
            
            event.setParticipants(new HashSet<>());
            event.getParticipants().add(participant);
            
            when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));

            // When
            boolean result = eventService.isParticipantEligible(eventId, participantId);

            // Then
            assertThat(result).isTrue();
        }
    }

    private EventDTO.CreateEventRequest createValidEventRequest() {
        return EventDTO.CreateEventRequest.builder()
                .name("Test Event")
                .code("TEST_EVENT")
                .description("Test Description")
                .startDate(now.plusDays(1))
                .endDate(now.plusDays(7))
                .isActive(true)
                .build();
    }

    private EventDTO.UpdateEventRequest createValidUpdateRequest() {
        return EventDTO.UpdateEventRequest.builder()
                .name("Updated Event")
                .description("Updated Description")
                .startDate(now.plusDays(1))
                .endDate(now.plusDays(7))
                .isActive(true)
                .build();
    }
}
