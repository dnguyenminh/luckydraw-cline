package vn.com.fecredit.app.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import vn.com.fecredit.app.dto.Summary;
import vn.com.fecredit.app.entity.Event;
import vn.com.fecredit.app.entity.EventLocation;
import vn.com.fecredit.app.entity.Reward;
import vn.com.fecredit.app.enums.EventStatus;
import vn.com.fecredit.app.mapper.EventMapper;
import vn.com.fecredit.app.repository.EventLocationRepository;
import vn.com.fecredit.app.repository.EventRepository;
import vn.com.fecredit.app.repository.RewardRepository;
import vn.com.fecredit.app.service.impl.EventServiceImpl;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private EventLocationRepository locationRepository;

    @Mock
    private RewardRepository rewardRepository;

    @Mock
    private EventMapper eventMapper;

    @InjectMocks
    private EventServiceImpl eventService;

    private Event event;
    private Summary.CreateRequest createRequest;
    private Summary.UpdateRequest updateRequest;
    private Summary.Response response;
    private EventLocation location;
    private Reward reward;

    @BeforeEach
    void setUp() {
        createRequest = Summary.CreateRequest.builder()
                .name("Test Event")
                .description("Test Description")
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now().plusDays(7))
                .dailySpinLimit(10)
                .totalSpinLimit(100)
                .locationIds(Set.of(1L))
                .rewardIds(Set.of(1L))
                .build();

        event = new Event();
        event.setId(1L);
        event.setName("Test Event");
        event.setDescription("Test Description");
        event.setStartTime(LocalDateTime.now());
        event.setEndTime(LocalDateTime.now().plusDays(7));
        event.setEventStatus(EventStatus.DRAFT);
        event.setEntityStatus(EntityStatus.ACTIVE);
        event.setDailySpinLimit(10);
        event.setTotalSpinLimit(100);

        response = Summary.Response.builder()
                .id(1L)
                .name("Test Event")
                .description("Test Description")
                .startTime(event.getStartTime())
                .endTime(event.getEndTime())
                .eventStatus(EventStatus.DRAFT)
                .entityStatus(EntityStatus.ACTIVE)
                .dailySpinLimit(10)
                .totalSpinLimit(100)
                .build();

        location = new EventLocation();
        location.setId(1L);
        location.setName("Test Location");

        reward = new Reward();
        reward.setId(1L);
        reward.setName("Test Reward");

        updateRequest = Summary.UpdateRequest.builder()
                .name("Updated Event")
                .description("Updated Description")
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now().plusDays(14))
                .dailySpinLimit(20)
                .totalSpinLimit(200)
                .build();
    }

    @Test
    void createEvent_ShouldReturnCreatedEvent() {
        when(eventMapper.toEntity(any(Summary.CreateRequest.class))).thenReturn(event);
        when(locationRepository.findById(1L)).thenReturn(Optional.of(location));
        when(rewardRepository.findById(1L)).thenReturn(Optional.of(reward));
        when(eventRepository.save(any(Event.class))).thenReturn(event);
        when(eventMapper.toResponse(any(Event.class))).thenReturn(response);

        Summary.Response result = eventService.createEvent(createRequest);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Test Event");
    }

    @Test
    void listEvents_ShouldReturnPageOfEvents() {
        Page<Event> eventPage = new PageImpl<>(Collections.singletonList(event));
        when(eventRepository.findAll(PageRequest.of(0, 10))).thenReturn(eventPage);
        when(eventMapper.toResponse(any(Event.class))).thenReturn(response);

        Page<Summary.Response> result = eventService.listEvents(PageRequest.of(0, 10));

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void getEvent_ShouldReturnEvent() {
        when(eventRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(event));
        when(eventMapper.toResponse(event)).thenReturn(response);

        Summary.Response result = eventService.getEvent(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    void getEventStatistics_ShouldReturnStatistics() {
        event.setParticipants(new HashSet<>());
        event.setSpinHistories(new HashSet<>());
        event.setRewards(new HashSet<>());
        when(eventRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(event));

        Summary.Statistics result = eventService.getEventStatistics(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTotalParticipants()).isZero();
    }

    @Test
    void searchEvents_ShouldReturnFilteredEvents() {
        Page<Event> eventPage = new PageImpl<>(Collections.singletonList(event));
        when(eventRepository.findAll(any(Specification.class), any(PageRequest.class)))
                .thenReturn(eventPage);
        when(eventMapper.toResponse(any(Event.class))).thenReturn(response);

        Page<Summary.Response> result = eventService.searchEvents(
                "test", "location", "ACTIVE", PageRequest.of(0, 10));

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
    }
}
