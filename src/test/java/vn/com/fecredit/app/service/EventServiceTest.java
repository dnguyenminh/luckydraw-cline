package vn.com.fecredit.app.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Arrays;
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
import vn.com.fecredit.app.model.Reward;
import vn.com.fecredit.app.repository.EventRepository;

@ExtendWith(MockitoExtension.class)
public class EventServiceTest {

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
        Event event = createValidEvent();
        when(eventRepository.save(any(Event.class))).thenReturn(event);

        // When
        EventDTO result = eventService.createEvent(request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getCreatedAt()).isNotNull();
        assertThat(result.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should update event timestamps")
    void shouldUpdateEventTimestamps() {
        // Given
        Event existingEvent = createValidEvent();
        existingEvent.setCreatedAt(now.minusDays(1));
        existingEvent.setUpdatedAt(now.minusDays(1));
        
        EventDTO.UpdateEventRequest request = createValidUpdateRequest();
        Event updatedEvent = existingEvent;
        updatedEvent.setName(request.getName());
        updatedEvent.setDescription(request.getDescription());
        updatedEvent.setStartDate(request.getStartDate());
        updatedEvent.setEndDate(request.getEndDate());
        updatedEvent.setUpdatedAt(now);
        
        when(eventRepository.findById(1L)).thenReturn(Optional.of(existingEvent));
        when(eventRepository.save(any(Event.class))).thenReturn(updatedEvent);

        // When
        EventDTO result = eventService.updateEvent(1L, request);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getUpdatedAt()).isAfterOrEqualTo(result.getCreatedAt());
    }

    @Test
    @DisplayName("Should manage event rewards")
    void shouldManageEventRewards() {
        // Given
        Event event = createValidEvent();
        Reward reward = Reward.builder()
                .id(1L)
                .name("Test Reward")
                .quantity(10)
                .remainingQuantity(10)
                .isActive(true)
                .build();

        // When
        event.addReward(reward);

        // Then
        assertThat(event.getRewards()).contains(reward);
        assertThat(reward.getEvent()).isEqualTo(event);

        // When removing
        event.removeReward(reward);

        // Then
        assertThat(event.getRewards()).isEmpty();
        assertThat(reward.getEvent()).isNull();
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
                    .name("Test Event")
                    .code("TEST_EVENT")
                    .startDate(now.minusDays(1))  // Started yesterday
                    .endDate(now.plusDays(5))     // Ends in 5 days
                    .isActive(true)
                    .participants(new HashSet<>())
                    .rewards(new HashSet<>())
                    .spinHistories(new HashSet<>())
                    .createdAt(now.minusDays(1))
                    .updatedAt(now.minusDays(1))
                    .build();
            
            Participant participant = new Participant();
            participant.setId(participantId);
            participant.setSpinsRemaining(3L);
            event.setParticipants(new HashSet<>(Arrays.asList(participant)));

            when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));

            // When
            boolean result = eventService.isParticipantEligible(eventId, participantId);

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Should be ineligible when event hasn't started")
        void shouldBeIneligibleWhenEventHasNotStarted() {
            // Given
            Long eventId = 1L;
            Long participantId = 1L;
            Event event = Event.builder()
                    .id(eventId)
                    .name("Future Event")
                    .code("FUTURE_EVENT")
                    .startDate(now.plusDays(1))   // Starts tomorrow
                    .endDate(now.plusDays(7))
                    .isActive(true)
                    .participants(new HashSet<>())
                    .rewards(new HashSet<>())
                    .spinHistories(new HashSet<>())
                    .createdAt(now)
                    .updatedAt(now)
                    .build();
            
            Participant participant = new Participant();
            participant.setId(participantId);
            participant.setSpinsRemaining(3L);
            event.setParticipants(new HashSet<>(Arrays.asList(participant)));

            when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));

            // When
            boolean result = eventService.isParticipantEligible(eventId, participantId);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should be ineligible when event has ended")
        void shouldBeIneligibleWhenEventHasEnded() {
            // Given
            Long eventId = 1L;
            Long participantId = 1L;
            Event event = Event.builder()
                    .id(eventId)
                    .name("Past Event")
                    .code("PAST_EVENT")
                    .startDate(now.minusDays(10)) // Started 10 days ago
                    .endDate(now.minusDays(1))    // Ended yesterday
                    .isActive(true)
                    .participants(new HashSet<>())
                    .rewards(new HashSet<>())
                    .spinHistories(new HashSet<>())
                    .createdAt(now.minusDays(10))
                    .updatedAt(now.minusDays(1))
                    .build();
            
            Participant participant = new Participant();
            participant.setId(participantId);
            participant.setSpinsRemaining(3L);
            event.setParticipants(new HashSet<>(Arrays.asList(participant)));

            when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));

            // When
            boolean result = eventService.isParticipantEligible(eventId, participantId);

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Should be ineligible when no spins remaining")
        void shouldBeIneligibleWhenNoSpinsRemaining() {
            // Given
            Long eventId = 1L;
            Long participantId = 1L;
            Event event = Event.builder()
                    .id(eventId)
                    .name("Active Event")
                    .code("ACTIVE_EVENT")
                    .startDate(now.minusDays(1))
                    .endDate(now.plusDays(5))
                    .isActive(true)
                    .participants(new HashSet<>())
                    .rewards(new HashSet<>())
                    .spinHistories(new HashSet<>())
                    .createdAt(now.minusDays(1))
                    .updatedAt(now.minusDays(1))
                    .build();
            
            Participant participant = new Participant();
            participant.setId(participantId);
            participant.setSpinsRemaining(0L); // No spins remaining
            event.setParticipants(new HashSet<>(Arrays.asList(participant)));

            when(eventRepository.findById(eventId)).thenReturn(Optional.of(event));

            // When
            boolean result = eventService.isParticipantEligible(eventId, participantId);

            // Then
            assertThat(result).isFalse();
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

    private Event createValidEvent() {
        return Event.builder()
                .id(1L)
                .name("Test Event")
                .code("TEST_EVENT")
                .description("Test Description")
                .startDate(now.plusDays(1))
                .endDate(now.plusDays(7))
                .isActive(true)
                .participants(new HashSet<>())
                .rewards(new HashSet<>())
                .spinHistories(new HashSet<>())
                .createdAt(now)
                .updatedAt(now)
                .build();
    }
}
