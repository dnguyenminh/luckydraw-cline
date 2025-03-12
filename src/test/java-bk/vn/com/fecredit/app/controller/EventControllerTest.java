package vn.com.fecredit.app.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import vn.com.fecredit.app.dto.Summary;
import vn.com.fecredit.app.enums.EventStatus;
import vn.com.fecredit.app.service.EventService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EventControllerTest {

    @Mock
    private EventService eventService;

    @InjectMocks
    private EventController eventController;

    private Summary.CreateRequest createRequest;
    private Summary.Response response;
    private Summary.Statistics statistics;
    private Page<Summary.Response> eventPage;

    @BeforeEach
    void setUp() {
        createRequest = Summary.CreateRequest.builder()
                .name("Test Event")
                .description("Test Description")
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now().plusDays(7))
                .dailySpinLimit(10)
                .totalSpinLimit(100)
                .imageUrl("test.jpg")
                .locationIds(new HashSet<>())
                .rewardIds(new HashSet<>())
                .build();

        response = Summary.Response.builder()
                .id(1L)
                .name("Test Event")
                .description("Test Description")
                .startTime(LocalDateTime.now())
                .endTime(LocalDateTime.now().plusDays(7))
                .eventStatus(EventStatus.DRAFT)
                .entityStatus(EntityStatus.ACTIVE)
                .dailySpinLimit(10)
                .totalSpinLimit(100)
                .imageUrl("test.jpg")
                .build();

        statistics = Summary.Statistics.builder()
                .id(1L)
                .name("Test Event")
                .totalParticipants(10)
                .totalSpins(50)
                .remainingSpins(50)
                .totalRewardsGiven(20)
                .build();

        eventPage = new PageImpl<>(Collections.singletonList(response));
    }

    @Test
    void createEvent_ShouldReturnCreatedEvent() {
        when(eventService.createEvent(any(Summary.CreateRequest.class)))
                .thenReturn(response);

        ResponseEntity<Summary.Response> result = eventController.createEvent(createRequest);

        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getId()).isEqualTo(1L);
        assertThat(result.getBody().getName()).isEqualTo("Test Event");
    }

    @Test
    void listEvents_ShouldReturnEventPage() {
        when(eventService.listEvents(any(Pageable.class)))
                .thenReturn(eventPage);

        ResponseEntity<Page<Summary.Response>> result = eventController.listEvents(Pageable.unpaged());

        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getContent()).hasSize(1);
    }

    @Test
    void getEventStatistics_ShouldReturnStatistics() {
        when(eventService.getEventStatistics(1L))
                .thenReturn(statistics);

        ResponseEntity<Summary.Statistics> result = eventController.getEventStatistics(1L);

        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getId()).isEqualTo(1L);
        assertThat(result.getBody().getTotalParticipants()).isEqualTo(10);
    }

    @Test
    void searchEvents_ShouldReturnFilteredEvents() {
        when(eventService.searchEvents(eq("test"), eq("location"), eq("ACTIVE"), any(Pageable.class)))
                .thenReturn(eventPage);

        ResponseEntity<Page<Summary.Response>> result = eventController.searchEvents(
                "test", "location", "ACTIVE", Pageable.unpaged());

        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().getContent()).hasSize(1);
    }
}
