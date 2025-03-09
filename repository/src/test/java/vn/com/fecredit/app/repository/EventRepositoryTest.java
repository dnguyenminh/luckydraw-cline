package vn.com.fecredit.app.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import vn.com.fecredit.app.entity.Event;
import vn.com.fecredit.app.entity.base.AbstractStatusAwareEntity;
import vn.com.fecredit.app.BaseRepositoryTest;

class EventRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private EventRepository eventRepository;

    // Fixed test time for consistent test execution
    private static final LocalDateTime TEST_TIME = LocalDateTime.of(2024, 7, 1, 0, 0);

    @BeforeEach
    void setUp() {
        // Clean up any preset data that might affect our tests
        eventRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();
    }

    @Test
    void findByCode_ShouldReturnEvent_WhenExists() {
        // Given
        Event event = Event.builder()
            .code("EVENT_TEST001")
            .name("Test Event")
            .description("Test event description")
            .startTime(TEST_TIME.minusMonths(1))
            .endTime(TEST_TIME.plusMonths(1))
            .initialSpins(10)
            .dailySpinLimit(5)
            .defaultWinProbability(0.1)
            .status(AbstractStatusAwareEntity.STATUS_ACTIVE)
            .createdAt(TEST_TIME)
            .build();
        persistAndFlush(event);

        // When
        Optional<Event> found = eventRepository.findByCode("EVENT_TEST001");

        // Then
        assertTrue(found.isPresent());
        assertEquals("Test Event", found.get().getName());
    }

    @Test
    void findActive_ShouldReturnOnlyActiveEvents() {
        // Given
        Event activeEvent1 = Event.builder()
            .code("EVENT_ACTIVE001")
            .name("Active Event 1")
            .description("Test event description")
            .startTime(TEST_TIME.minusMonths(1))
            .endTime(TEST_TIME.plusMonths(1))
            .initialSpins(10)
            .dailySpinLimit(5)
            .defaultWinProbability(0.1)
            .status(AbstractStatusAwareEntity.STATUS_ACTIVE)
            .createdAt(TEST_TIME)
            .build();

        Event activeEvent2 = Event.builder()
            .code("EVENT_ACTIVE002")
            .name("Active Event 2")
            .description("Test event description")
            .startTime(TEST_TIME.minusMonths(1))
            .endTime(TEST_TIME.plusMonths(1))
            .initialSpins(10)
            .dailySpinLimit(5)
            .defaultWinProbability(0.1)
            .status(AbstractStatusAwareEntity.STATUS_ACTIVE)
            .createdAt(TEST_TIME)
            .build();

        Event inactiveEvent = Event.builder()
            .code("EVENT_INACTIVE001")
            .name("Inactive Event")
            .description("Test event description")
            .startTime(TEST_TIME.minusMonths(1))
            .endTime(TEST_TIME.plusMonths(1))
            .initialSpins(10)
            .dailySpinLimit(5)
            .defaultWinProbability(0.1)
            .status(AbstractStatusAwareEntity.STATUS_INACTIVE)
            .createdAt(TEST_TIME)
            .build();
        
        persistAndFlush(activeEvent1);
        persistAndFlush(activeEvent2);
        persistAndFlush(inactiveEvent);

        // When
        List<Event> activeEvents = eventRepository.findActive(
            AbstractStatusAwareEntity.STATUS_ACTIVE,
            TEST_TIME
        );

        // Then
        assertThat(activeEvents).hasSize(2);
        assertTrue(activeEvents.stream().anyMatch(e -> e.getCode().equals("EVENT_ACTIVE001")));
        assertTrue(activeEvents.stream().anyMatch(e -> e.getCode().equals("EVENT_ACTIVE002")));
        assertFalse(activeEvents.stream().anyMatch(e -> e.getCode().equals("EVENT_INACTIVE001")));
    }

    @Test
    void findCurrent_ShouldReturnEventsInDateRange() {
        // Given
        Event current = Event.builder()
            .code("EVENT_CURRENT001")
            .name("Current Event")
            .description("Test event description")
            .startTime(TEST_TIME.minusMonths(1))
            .endTime(TEST_TIME.plusMonths(1))
            .initialSpins(10)
            .dailySpinLimit(5)
            .defaultWinProbability(0.1)
            .status(AbstractStatusAwareEntity.STATUS_ACTIVE)
            .createdAt(TEST_TIME)
            .build();
        
        Event future = Event.builder()
            .code("EVENT_FUTURE001")
            .name("Future Event")
            .description("Test event description")
            .startTime(TEST_TIME.plusMonths(2))
            .endTime(TEST_TIME.plusMonths(3))
            .initialSpins(10)
            .dailySpinLimit(5)
            .defaultWinProbability(0.1)
            .status(AbstractStatusAwareEntity.STATUS_ACTIVE)
            .createdAt(TEST_TIME)
            .build();
        
        persistAndFlush(current);
        persistAndFlush(future);

        // When
        List<Event> currentEvents = eventRepository.findCurrent(TEST_TIME);

        // Then
        assertThat(currentEvents).hasSize(1);
        assertTrue(currentEvents.stream().anyMatch(e -> e.getCode().equals("EVENT_CURRENT001")));
        assertFalse(currentEvents.stream().anyMatch(e -> e.getCode().equals("EVENT_FUTURE001")));
    }

    @Test
    void findByIdWithDetails_ShouldFetchRelatedEntities() {
        // Given
        Event event = Event.builder()
            .code("EVENT_DETAILS001")
            .name("Test Event")
            .description("Test event description")
            .startTime(TEST_TIME.minusMonths(1))
            .endTime(TEST_TIME.plusMonths(1))
            .initialSpins(10)
            .dailySpinLimit(5)
            .defaultWinProbability(0.1)
            .status(AbstractStatusAwareEntity.STATUS_ACTIVE)
            .createdAt(TEST_TIME)
            .build();
        persistAndFlush(event);

        // When
        Optional<Event> found = eventRepository.findByIdWithDetails(event.getId());

        // Then
        assertTrue(found.isPresent());
        assertNotNull(found.get().getEventLocations());
    }
}
