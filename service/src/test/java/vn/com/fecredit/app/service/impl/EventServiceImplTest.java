package vn.com.fecredit.app.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import vn.com.fecredit.app.common.EntityStatus;
import vn.com.fecredit.app.dto.EventDTO;
import vn.com.fecredit.app.entity.Event;
import vn.com.fecredit.app.exception.ResourceNotFoundException;
import vn.com.fecredit.app.mapper.EventMapper;
import vn.com.fecredit.app.repository.EventRepository;

@ExtendWith(MockitoExtension.class)
class EventServiceImplTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private EventMapper eventMapper;

    @InjectMocks
    private EventServiceImpl eventService;

    @Captor
    private ArgumentCaptor<Event> eventCaptor;

    private Event event;
    private EventDTO.Response eventResponse;
    private EventDTO.CreateRequest createRequest;
    private EventDTO.UpdateRequest updateRequest;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();
        initializeEventData();
        initializeDTOs();
    }

    private void initializeEventData() {
        event = Event.builder()
                .id(1L)
                .code("EVENT001")
                .name("Test Event")
                .description("Test Description")
                .startTime(now.plusDays(1))
                .endTime(now.plusDays(7))
                .initialSpins(10)
                .dailySpinLimit(5)
                .defaultWinProbability(0.1)
                .status(EntityStatus.ACTIVE.getValue())
                .build();
    }

    private void initializeDTOs() {
        eventResponse = EventDTO.Response.builder()
                .id(1L)
                .code("EVENT001")
                .name("Test Event")
                .description("Test Description")
                .startTime(now.plusDays(1))
                .endTime(now.plusDays(7))
                .totalSpinsAllowed(50)
                .initialSpins(10)
                .status(EntityStatus.ACTIVE.getValue())
                .build();

        createRequest = EventDTO.CreateRequest.builder()
                .code("EVENT001")
                .name("Test Event")
                .description("Test Description")
                .startTime(now.plusDays(1))
                .endTime(now.plusDays(7))
                .totalSpinsAllowed(50)
                .initialSpins(10)
                .build();

        updateRequest = EventDTO.UpdateRequest.builder()
                .name("Updated Event")
                .description("Updated Description")
                .startTime(now.plusDays(2))
                .endTime(now.plusDays(8))
                .totalSpinsAllowed(100)
                .initialSpins(20)
                .build();
    }

    @Nested
    class CreateOperations {
        @Test
        void createEvent_ShouldCreateAndReturnEvent() {
            when(eventRepository.existsByStatus(EntityStatus.ACTIVE.getValue())).thenReturn(false);
            when(eventRepository.existsByCode("EVENT001")).thenReturn(false);
            when(eventMapper.toEntity(createRequest)).thenReturn(event);
            when(eventRepository.save(any(Event.class))).thenReturn(event);
            when(eventMapper.toResponse(event)).thenReturn(eventResponse);

            EventDTO.Response result = eventService.createEvent(createRequest);

            verify(eventRepository).save(eventCaptor.capture());
            Event savedEvent = eventCaptor.getValue();
            assertThat(savedEvent.getStatus()).isEqualTo(EntityStatus.ACTIVE.getValue());
            assertThat(result).isEqualTo(eventResponse);
            verify(eventRepository).existsByCode("EVENT001");
            verify(eventRepository).existsByStatus(EntityStatus.ACTIVE.getValue());
        }

        @Test
        void createEvent_WithNullCode_ShouldThrowException() {
            createRequest.setCode(null);

            assertThatThrownBy(() -> eventService.createEvent(createRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Event code cannot be null or empty");

            verify(eventRepository, never()).save(any());
        }

        @Test
        void createEvent_WithEmptyCode_ShouldThrowException() {
            createRequest.setCode("");

            assertThatThrownBy(() -> eventService.createEvent(createRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Event code cannot be null or empty");

            verify(eventRepository, never()).save(any());
        }

        @Test
        void createEvent_WithExistingCode_ShouldThrowException() {
            when(eventRepository.existsByCode("EVENT001")).thenReturn(true);

            assertThatThrownBy(() -> eventService.createEvent(createRequest))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Event with code EVENT001 already exists");

            verify(eventRepository, never()).save(any());
        }

        @Test
        void createEvent_WithInvalidDateRange_ShouldThrowException() {
            createRequest.setEndTime(createRequest.getStartTime().minusDays(1));

            assertThatThrownBy(() -> eventService.createEvent(createRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("End time must be after start time");

            verify(eventRepository, never()).save(any());
        }

        @Test
        void createEvent_WithNegativeSpins_ShouldThrowException() {
            createRequest.setInitialSpins(-1);

            assertThatThrownBy(() -> eventService.createEvent(createRequest))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Initial spins must be non-negative");

            verify(eventRepository, never()).save(any());
        }
    }

    @Nested
    class UpdateOperations {
        @BeforeEach
        void setUp() {
            when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        }

        @Test
        void updateEvent_WithValidData_ShouldUpdateAllFields() {
            when(eventRepository.save(any(Event.class))).thenReturn(event);
            when(eventMapper.toResponse(event)).thenReturn(eventResponse);

            eventService.updateEvent(1L, updateRequest);

            verify(eventRepository).save(eventCaptor.capture());
            Event savedEvent = eventCaptor.getValue();
            assertThat(savedEvent.getName()).isEqualTo("Updated Event");
            assertThat(savedEvent.getDescription()).isEqualTo("Updated Description");
            assertThat(savedEvent.getInitialSpins()).isEqualTo(20);
        }

        @Test
        void updateEvent_WithNullFields_ShouldNotUpdateExistingValues() {
            updateRequest.setName(null);
            updateRequest.setDescription(null);
            when(eventRepository.save(any(Event.class))).thenReturn(event);
            when(eventMapper.toResponse(event)).thenReturn(eventResponse);

            eventService.updateEvent(1L, updateRequest);

            verify(eventRepository).save(eventCaptor.capture());
            Event savedEvent = eventCaptor.getValue();
            assertThat(savedEvent.getName()).isEqualTo("Test Event");
            assertThat(savedEvent.getDescription()).isEqualTo("Test Description");
        }

        @ParameterizedTest
        @EnumSource(value = EntityStatus.class, names = {"DELETED"})
        void updateEvent_WhenDeletedStatus_ShouldThrowException(EntityStatus status) {
            event.setStatus(status.getValue());

            assertThatThrownBy(() -> eventService.updateEvent(1L, updateRequest))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Cannot update event in " + status + " status");

            verify(eventRepository, never()).save(any());
        }
    }

    @Nested
    class StatusOperations {
        @Test
        void updateEventStatus_WithValidTransition_ShouldUpdateStatus() {
            when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
            when(eventRepository.save(any(Event.class))).thenReturn(event);
            when(eventMapper.toResponse(event)).thenReturn(eventResponse);

            EventDTO.Response result = eventService.updateEventStatus(1L, EntityStatus.INACTIVE);

            verify(eventRepository).save(eventCaptor.capture());
            assertThat(eventCaptor.getValue().getStatus())
                .isEqualTo(EntityStatus.INACTIVE.getValue());
            assertThat(result).isEqualTo(eventResponse);
        }

        @Test
        void deleteEvent_ShouldMarkAsDeleted() {
            when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
            when(eventRepository.save(any(Event.class))).thenReturn(event);

            eventService.deleteEvent(1L);

            verify(eventRepository).save(eventCaptor.capture());
            assertThat(eventCaptor.getValue().getStatus())
                .isEqualTo(EntityStatus.DELETED.getValue());
        }

        @Test
        void deleteEvent_WhenAlreadyDeleted_ShouldThrowException() {
            event.setStatus(EntityStatus.DELETED.getValue());
            when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

            assertThatThrownBy(() -> eventService.deleteEvent(1L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Event is already deleted");

            verify(eventRepository, never()).save(any());
        }
    }

    @Nested
    class ValidationOperations {
        @Test
        void isActive_WithCurrentEvent_ShouldReturnTrue() {
            event.setStartTime(now.minusDays(1));
            event.setEndTime(now.plusDays(1));
            when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

            boolean result = eventService.isActive(1L);

            assertThat(result).isTrue();
        }

        @Test
        void isActive_WithExpiredEvent_ShouldReturnFalse() {
            event.setEndTime(now.minusDays(1));
            when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

            boolean result = eventService.isActive(1L);

            assertThat(result).isFalse();
        }

        @Test
        void isActive_WithFutureEvent_ShouldReturnFalse() {
            event.setStartTime(now.plusDays(1));
            when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

            boolean result = eventService.isActive(1L);

            assertThat(result).isFalse();
        }

        @Test
        void isActive_WithInactiveStatus_ShouldReturnFalse() {
            event.setStatus(EntityStatus.INACTIVE.getValue());
            when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

            boolean result = eventService.isActive(1L);

            assertThat(result).isFalse();
        }
    }

    @Nested
    class SearchOperations {
        @Test
        void searchEvents_WithAllCriteria_ShouldReturnFilteredResults() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Event> eventPage = new PageImpl<>(List.of(event), pageable, 1);
            when(eventRepository.findBySearchCriteria("test", now, now.plusDays(7), 
                    EntityStatus.ACTIVE.getValue(), pageable)).thenReturn(eventPage);
            when(eventMapper.toResponse(event)).thenReturn(eventResponse);

            Page<EventDTO.Response> result = eventService.searchEvents(
                "test", now, now.plusDays(7), EntityStatus.ACTIVE, pageable);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0)).isEqualTo(eventResponse);
            verify(eventRepository).findBySearchCriteria(
                "test", now, now.plusDays(7), EntityStatus.ACTIVE.getValue(), pageable);
        }

        @Test
        void getEventStatistics_WithMultipleEvents_ShouldAggregateCorrectly() {
            Event event2 = event.toBuilder().id(2L).build();
            when(eventRepository.findAll()).thenReturn(List.of(event, event2));
            
            EventDTO.Statistics stats = EventDTO.Statistics.builder()
                    .totalEvents(2L)
                    .activeEvents(2L)
                    .build();
            when(eventMapper.toStatistics(List.of(event, event2))).thenReturn(stats);

            EventDTO.Statistics result = eventService.getEventStatistics();

            assertThat(result.getTotalEvents()).isEqualTo(2L);
            assertThat(result.getActiveEvents()).isEqualTo(2L);
        }
    }
}
